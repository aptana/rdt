package org.rubypeople.rdt.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
import org.rubypeople.rdt.internal.ui.wizards.InstallStandardRubyWizard;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.RubyRuntime;

public class RubyInstalledDetector extends UIJob
{

	private static boolean fgFinished;

	public RubyInstalledDetector()
	{
		super("Detecting Ruby installation");
	}

	private boolean usingIncludedJRuby()
	{
		Preferences store = LaunchingPlugin.getDefault().getPluginPreferences();
		if (store == null)
			return false;
		return store.getBoolean(LaunchingPlugin.USING_INCLUDED_JRUBY);
	}

	private boolean rubyInstalled()
	{
		return !rubyNotInstalled();
	}

	private boolean rubyNotInstalled()
	{
		IVMInstall[] cRubyInstalls = RubyRuntime
				.getVMInstallType(IRubyLaunchConfigurationConstants.ID_STANDARD_VM_TYPE).getVMInstalls();
		return cRubyInstalls == null || cRubyInstalls.length == 0;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor)
	{
		if (rubyInstalled() || usingIncludedJRuby())
		{
			markFinished();
			return Status.CANCEL_STATUS;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		InstallStandardRubyWizard wizard = new InstallStandardRubyWizard();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getDisplay().getActiveShell(), wizard);
		dialog.open();
		monitor.done();
		return Status.OK_STATUS;
	}

	/**
	 * Used by the InstallRubyWizard to alert that the ruby setup has finished, so we can do gem autoinstall stuff
	 */
	public static void markFinished()
	{
		fgFinished = true;
	}

	/**
	 * Returns whether or not the check has run. If check pops up wizard, should only be marked finished after wizard is
	 * disposed.
	 * 
	 * @return
	 */
	public static boolean isFinished()
	{
		return fgFinished;
	}

}
