package com.aptana.rdt.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.ui.gems.GemsView;

/**
 * Install a gem
 * 
 * @author cwilliams
 */
public class RefreshGemsActionDelegate implements IObjectActionDelegate, IViewActionDelegate
{

	private GemsView gemsView;

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{

	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		UIJob job = new UIJob("Refreshing gem listing")
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				return getGemManager().refresh(monitor);
			}

		};
		job.setUser(true);
		job.schedule();
	}

	protected IGemManager getGemManager()
	{
		return gemsView.getGemManager();
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
		this.gemsView = (GemsView) view;
	}

}
