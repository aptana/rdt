package com.aptana.rdt.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.debug.ui.InstallDeveloperToolsDialog;
import org.rubypeople.rdt.internal.debug.ui.launcher.InstallGemsJob;
import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
import org.rubypeople.rdt.internal.ui.RubyInstalledDetector;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallChangedListener;
import org.rubypeople.rdt.launching.PropertyChangeEvent;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.ContributedGemRegistry;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.GemListener;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.Version;
import com.aptana.rdt.ui.AptanaRDTUIPlugin;
import com.aptana.rdt.ui.preferences.IPreferenceConstants;

/**
 * Automatically installs any relevant (by platform and VM type) contributed gems from com.aptana.rdt.gems extension
 * point with auto-install flag set to true.
 * 
 * @author Chris Williams
 */
public class GemAutoInstallDialogJob extends UIJob implements GemListener, IVMInstallChangedListener
{

	private static class SerialRule implements ISchedulingRule
	{

		public SerialRule()
		{
		}

		public boolean contains(ISchedulingRule rule)
		{
			return rule == this;
		}

		public boolean isConflicting(ISchedulingRule rule)
		{
			return rule instanceof SerialRule;
		}
	}

	private boolean rescheduleOnRefresh;

	public GemAutoInstallDialogJob()
	{
		super("Installing gems");
		setRule(new SerialRule());
		AptanaRDTPlugin.getDefault().getGemManager().addGemListener(this);
		// FIX ROR-524 Auto install gem job doesn't run when user changes VMs
		RubyRuntime.addVMInstallChangedListener(this);
	}

	public boolean shouldRun()
	{
		return !PlatformUI.getWorkbench().isClosing() && AptanaRDTPlugin.getDefault().getGemManager().isInitialized();
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor)
	{
		// If there's no standard VM, and they're using JRuby, and they haven't explicitly stated they want to use
		// it - then don't pop up this dialog!
		if (!usingIncludedJRuby() && rubyNotInstalled())
		{
			if (RubyInstalledDetector.isFinished())
			{ // They've finished and are probably installing ruby
				return Status.CANCEL_STATUS;
			}
			// They haven't made the choice to use JRuby explicitly or to install/setup ruby yet. Reschedule
			schedule(30000);
			return Status.OK_STATUS;
		}

		if (!getGemManager().isRubyGemsInstalled())
		{
			String key = "dont_bug_about_gems_not_installed";
			String dontBug = AptanaRDTUIPlugin.getDefault().getPreferenceStore().getString(key);
			if (dontBug != null && dontBug.equals(MessageDialogWithToggle.ALWAYS))
			{
				return Status.OK_STATUS;
			}
			MessageDialogWithToggle
					.openWarning(
							RubyPlugin.getActiveWorkbenchShell(),
							"RubyGems Not Installed",
							"You do not appear to have RubyGems installed. It is highly recommended that you install this, as it is the standard way of installing and managing ruby libraries. Please see http://rubygems.org/read/chapter/3.",
							"Don't bug me anymore.", false, AptanaRDTUIPlugin.getDefault().getPreferenceStore(), key);
			return Status.OK_STATUS;
		}

		if (!Platform.getPreferencesService().getBoolean(AptanaRDTUIPlugin.PLUGIN_ID,
				IPreferenceConstants.PROMPT_TO_AUTO_INSTALL_GEMS, true, null))
		{
			// User has explicitly told us not to prompt them about this!
			return Status.OK_STATUS;
		}

		monitor.beginTask("Getting auto-install gems...", 35);
		monitor.subTask("Getting contributed gems");
		Collection<Gem> gems = getContributedGems();
		monitor.worked(10);

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		monitor.subTask("Filtering by platform");
		gems = filterByPlatform(gems);
		monitor.worked(5);

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		monitor.subTask("Filtering out already installed gems");
		gems = filterOutInstalled(gems);
		monitor.worked(10);

		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		monitor.subTask("Filtering out user ignored gems");
		gems = filterOutIgnored(gems);
		monitor.worked(10);

		// TODO This is a rails specific hack. We should be checking dependencies for gems we're asking as well as
		// what's installed here. If we have a gem like activeresource 2.0.2 alone and rails 1.2.6 installed, we
		// shouldn't even ask the user to install activeresource.
		gems = filterActiveResource(gems);

		if (gems.isEmpty())
		{
			monitor.done();
			return Status.OK_STATUS;
		}

		if (hasGemsWhichCompile(gems) && InstallDeveloperToolsDialog.shouldShow())
		{
			InstallDeveloperToolsDialog installToolsDialog = new InstallDeveloperToolsDialog(RubyPlugin
					.getActiveWorkbenchShell());
			installToolsDialog.open();
			// TODO Remove the compiling gems and any that depend on them
			return Status.CANCEL_STATUS;
		}

		GemAutoInstallDialog dialog = new GemAutoInstallDialog(RubyPlugin.getActiveWorkbenchShell(), gems);
		if (PlatformUI.getWorkbench().isClosing())
		{
			return Status.CANCEL_STATUS;
		}
		int code = dialog.open();
		if (code == Dialog.CANCEL)
		{
			monitor.setCanceled(true);
			return Status.CANCEL_STATUS;
		}

		Collection<Gem> finalGems = dialog.getSelectedGems();
		if (finalGems.isEmpty())
		{
			monitor.done();
			return Status.OK_STATUS;
		}

		Job job = new InstallGemsJob(finalGems);
		job.setSystem(true);
		job.schedule();
		monitor.done();
		return Status.OK_STATUS;
	}

	private boolean usingIncludedJRuby()
	{
		return Platform.getPreferencesService().getBoolean(LaunchingPlugin.PLUGIN_ID,
				LaunchingPlugin.USING_INCLUDED_JRUBY, false, null);
	}

	private boolean rubyNotInstalled()
	{
		IVMInstall[] cRubyInstalls = RubyRuntime
				.getVMInstallType(IRubyLaunchConfigurationConstants.ID_STANDARD_VM_TYPE).getVMInstalls();
		return cRubyInstalls == null || cRubyInstalls.length == 0;
	}

	private Collection<Gem> filterByPlatform(Collection<Gem> gems)
	{
		return ContributedGemRegistry.filterByPlatform(gems);
	}

	private Collection<Gem> filterActiveResource(Collection<Gem> gems)
	{
		if (!contains(gems, "activeresource"))
			return gems;
		if (!getGemManager().gemInstalled("rails"))
		{
			return gems;
		}
		boolean containsRails2 = false;
		List<Version> versions = getGemManager().getVersions("rails");
		for (Version version : versions)
		{
			if (version.isGreaterThanOrEqualTo("2.0.0"))
			{
				containsRails2 = true;
				break;
			}
		}

		if (!containsRails2)
		{
			return remove(gems, "activeresource");
		}
		return gems;
	}

	private Collection<Gem> remove(Collection<Gem> gems, String name)
	{
		Gem toRemove = get(gems, name);
		if (toRemove == null)
			return gems;
		Collection<Gem> copy = new ArrayList<Gem>(gems);
		copy.remove(toRemove);
		return copy;
	}

	private boolean contains(Collection<Gem> gems, String name)
	{
		return get(gems, name) != null;
	}

	private Gem get(Collection<Gem> gems, String name)
	{
		for (Gem gem : gems)
		{
			if (gem.getName().equals(name))
			{
				return gem;
			}
		}
		return null;
	}

	private boolean hasGemsWhichCompile(Collection<Gem> finalGems)
	{
		for (Gem gem : finalGems)
		{
			if (gem.compiles())
				return true;
		}
		return false;
	}

	private static Collection<Gem> filterOutIgnored(Collection<Gem> gems)
	{
		Collection<Gem> filtered = new ArrayList<Gem>();
		for (Gem gem : gems)
		{
			if (Platform.getPreferencesService().getBoolean(AptanaRDTPlugin.PLUGIN_ID,
					GemAutoInstallDialog.getIgnorePrefKey(gem), false, null))
				continue; // user asked us to ignore this gem and version
			filtered.add(gem);
		}
		return filtered;
	}

	private Collection<Gem> filterOutInstalled(Collection<Gem> gems)
	{
		Collection<Gem> filtered = new ArrayList<Gem>();
		for (Gem gem : gems)
		{
			if (getGemManager().gemInstalled(gem.getName()))
			{
				// Check if we're offering a newer version and we're asking to force updates
				if (!gem.forceUpdates())
					continue;
				if (newerVersionInstalled(gem))
					continue;
			}
			filtered.add(gem);
		}
		return filtered;
	}

	private boolean newerVersionInstalled(Gem gem)
	{
		List<Version> versions = getGemManager().getVersions(gem.getName());
		for (Version version : versions)
		{
			if (version.isGreaterThanOrEqualTo(gem.getVersionObject()))
			{
				return true;
			}
		}
		return false;
	}

	private IGemManager getGemManager()
	{
		return AptanaRDTPlugin.getDefault().getGemManager();
	}

	private Collection<Gem> getContributedGems()
	{
		return ContributedGemRegistry.getContributedGems();
	}

	public void managerInitialized()
	{
		schedule();
	}

	public void gemsRefreshed()
	{
		if (rescheduleOnRefresh)
		{
			schedule();
			rescheduleOnRefresh = false;
		}
	}

	public void gemRemoved(Gem gem)
	{
		// ignore
	}

	public void gemAdded(Gem gem)
	{
		// ignore
	}

	public void gemUpdated(Gem gem)
	{
		// ignore
	}

	public void vmRemoved(IVMInstall removedVm)
	{
	}

	public void vmChanged(PropertyChangeEvent event)
	{
	}

	public void vmAdded(IVMInstall newVm)
	{
	}

	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current)
	{
		if (current == null)
			return;
		// Wait until gem manager is refreshed again
		rescheduleOnRefresh = true;
	}
}
