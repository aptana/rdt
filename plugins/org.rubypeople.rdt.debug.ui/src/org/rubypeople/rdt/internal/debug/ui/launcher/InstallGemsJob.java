package org.rubypeople.rdt.internal.debug.ui.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.ContributedGemRegistry;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;

public class InstallGemsJob extends UIJob
{

	Collection<Gem> finalGems;

	public InstallGemsJob(Collection<Gem> finalGems)
	{
		super("Installing the selected gems");
		this.finalGems = finalGems;
	}

	public IStatus runInUIThread(IProgressMonitor monitor)
	{
		if (!getGemManager().isRubyGemsInstalled())
		{
			IPreferenceStore store = RdtDebugUiPlugin.getDefault().getPreferenceStore();
			String key = "rubygems_not_installed_dialog";
			if (!store.getString(key).equals(MessageDialogWithToggle.ALWAYS))
			{
				MessageDialogWithToggle
						.openWarning(
								Display.getDefault().getActiveShell(),
								"RubyGems Not Installed",
								"You do not appear to have RubyGems installed. It is highly recommended that you install this, as it is the standard way of installing and managing ruby libraries. Please see http://rubygems.org/read/chapter/3.",
								"Don't bug me again", false, store, key);
			}
			return Status.OK_STATUS;
		}
		try
		{
			ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
			progressDialog.run(true, true, new InstallGemsRunnableWithProgress(finalGems));
		}
		catch (InvocationTargetException e)
		{
			AptanaRDTPlugin.log(e);
		}
		catch (InterruptedException e)
		{
			AptanaRDTPlugin.log(e);
		}
		return Status.OK_STATUS;
	}

	private class InstallGemsRunnableWithProgress implements IRunnableWithProgress
	{

		private Collection<Gem> finalGems;

		public InstallGemsRunnableWithProgress(Collection<Gem> finalGems)
		{
			this.finalGems = finalGems;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
		{
			monitor.beginTask("Auto-installing gems...", 100);
			monitor.subTask("Sorting by dependencies");
			List<Gem> sorted = ContributedGemRegistry.sortByDependency(finalGems);
			monitor.worked(10);
			if (monitor.isCanceled())
				return;
			if (!sorted.isEmpty())
			{
				int step = 90 / sorted.size();
				for (Gem gem : sorted)
				{
					if (monitor.isCanceled())
						return;
					monitor.subTask("Installing " + gem.getName());
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, step - 1);
					getGemManager().installGem(gem, subMonitor);
					subMonitor.done();
					if (gem.isLocal())
					{
						gem.delete();
					}
					monitor.worked(1);
				}
			}
			monitor.done();
		}
	}

	protected IGemManager getGemManager()
	{
		return AptanaRDTPlugin.getDefault().getGemManager();
	}
}
