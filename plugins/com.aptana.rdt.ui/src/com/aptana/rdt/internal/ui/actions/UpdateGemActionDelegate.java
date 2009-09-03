package com.aptana.rdt.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.ui.gems.GemsView;

/**
 * Install a gem
 * 
 * @author cwilliams
 */
public class UpdateGemActionDelegate implements IObjectActionDelegate, IViewActionDelegate
{

	private IViewPart view;
	private Gem selectedGem;

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
		Job job = new Job("Updating gem...")
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				return AptanaRDTPlugin.getDefault().getGemManager().update(selectedGem, monitor);
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
		if (view instanceof GemsView)
		{
			if (selection != null && !selection.isEmpty())
			{
				if (selection instanceof IStructuredSelection)
				{
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object element = sel.getFirstElement();
					if (element instanceof Gem)
					{
						this.selectedGem = (Gem) element;
					}
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view)
	{
		this.view = view;
	}

}
