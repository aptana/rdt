package com.aptana.rdt.internal.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.ui.gems.GemsMessages;
import com.aptana.rdt.ui.gems.GemsView;
import com.aptana.rdt.ui.gems.RemoveGemDialog;

/**
 * Uninstall a gem
 * 
 * @author cwilliams
 */
// FIXME Combine common code from this and InstallGemActionDelegate into a superclass!
public class RemoveGemActionDelegate implements IObjectActionDelegate, IViewActionDelegate
{

	private GemsView view;
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
		if (selectedGem == null)
			return;
		boolean okay = MessageDialog.openConfirm(view.getViewSite().getShell(), null, GemsMessages.bind(
				GemsMessages.RemoveGemDialog_msg, selectedGem.getName()));
		if (!okay)
			return;
		Job job = null;
		if (selectedGem.hasMultipleVersions())
		{
			final int[] result = new int[1];
			final String[] version = new String[1];
			Display.getDefault().syncExec(new Runnable()
			{

				public void run()
				{
					RemoveGemDialog dialog = new RemoveGemDialog(Display.getDefault().getActiveShell(), selectedGem
							.versions());
					result[0] = dialog.open();
					version[0] = dialog.getVersion();
				}
			});
			if (result[0] != RemoveGemDialog.OK)
				return;
			job = new Job("Removing gem...")
			{

				protected IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor)
				{
					return getGemManager().removeGem(
							new Gem(selectedGem.getName(), version[0], selectedGem.getDescription()), monitor);
				}
			};
		}
		else
		{
			job = new Job("Removing gem...")
			{
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					return getGemManager().removeGem(selectedGem, monitor);
				}
			};
		}
		if (job != null)
		{
			job.setUser(true);
			job.schedule();
		}
	}

	protected IGemManager getGemManager()
	{
		return view.getGemManager();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection == null || selection.isEmpty())
		{
			action.setEnabled(false);
			return;
		}

		if (!(selection instanceof IStructuredSelection))
		{
			action.setEnabled(false);
			return;
		}

		IStructuredSelection sel = (IStructuredSelection) selection;
		Object element = sel.getFirstElement();
		if (!(element instanceof Gem))
		{
			action.setEnabled(false);
			return;
		}

		this.selectedGem = (Gem) element;
		action.setEnabled(true);
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view)
	{
		this.view = (GemsView) view;
	}

}
