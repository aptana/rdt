package com.aptana.rdt.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.aptana.rdt.AptanaRDTPlugin;

/**
 * Cleanup old versions of gems
 * 
 * @author cwilliams
 */
public class CleanupGemsActionDelegate implements IObjectActionDelegate, IViewActionDelegate
{

	private IWorkbenchPart targetPart;

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		this.targetPart = targetPart;
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		// Present an "Are you really sure!??!?!?!" dialog since apparently users aren't.
		Shell shell = null;
		if (targetPart != null && targetPart.getSite() != null)
			shell = targetPart.getSite().getShell();
		if (shell == null)
			shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		boolean doIt = MessageDialog
				.openConfirm(
						shell,
						"Really clean up outdated gems?",
						"This will uninstall older versions of gems that you have installed when you have more than one version oa a gem installed on your system. This cannot be undone, and the older versions would need to be installed individually to restore them.");
		if (!doIt)
			return;
		Job job = new Job("Cleaning up gems...")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				return AptanaRDTPlugin.getDefault().getGemManager().cleanup(monitor);
			}
		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view)
	{
	}

}
