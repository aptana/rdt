/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.debug.core.IRubyBreakpoint;
import org.rubypeople.rdt.debug.core.RdtDebugModel;
import org.rubypeople.rdt.debug.core.model.IRubyExceptionBreakpoint;
import org.rubypeople.rdt.internal.debug.ui.BreakpointUtils;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.ui.RubyUI;

/**
 * The workbench menu action for adding an exception breakpoint
 */
public class AddExceptionAction implements IViewActionDelegate, IWorkbenchWindowActionDelegate
{

	/**
	 * constants
	 * 
	 * @since 3.2
	 */
	public static final String DIALOG_SETTINGS = "AddExceptionDialog"; //$NON-NLS-1$

	/**
	 * the current workbench
	 * 
	 * @since 3.2
	 */
	private IWorkbenchWindow fWorkbenchWindow = null;

	/*
	 * (non-Rubydoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action)
	{
		Shell shell = RdtDebugUiPlugin.getActiveWorkbenchShell();
		AddExceptionDialogExtension ext = new AddExceptionDialogExtension();
		try
		{
			SelectionDialog dialog = RubyUI.createTypeDialog(shell, fWorkbenchWindow, SearchEngine
					.createWorkspaceScope(), IRubySearchConstants.CLASS, true, "*Error*", ext); //$NON-NLS-1$
			dialog.setTitle(BreakpointMessages.AddExceptionAction_0);
			dialog.setMessage(BreakpointMessages.AddExceptionAction_1);
			if (dialog.open() == IDialogConstants.OK_ID)
			{
				Object[] results = dialog.getResult();
				for (int i = 0; i < results.length; i++)
				{
					createBreakpoint((IType) results[i]);
				}
			}
		}
		catch (CoreException e)
		{
			RdtDebugUiPlugin.errorDialog(BreakpointMessages.AddExceptionAction_error, e.getStatus());
		}
	}

	/**
	 * creates a single breakpoint of the specified type
	 * 
	 * @param type
	 *            the type of the exception
	 * @since 3.2
	 */
	private void createBreakpoint(final IType type) throws CoreException
	{
		final IResource resource = BreakpointUtils.getBreakpointResource(type);
		final Map map = new HashMap(10);
		BreakpointUtils.addRubyBreakpointAttributes(map, type);
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(
				RdtDebugModel.getModelIdentifier());
		boolean exists = false;
		for (int j = 0; j < breakpoints.length; j++)
		{
			IRubyBreakpoint breakpoint = (IRubyBreakpoint) breakpoints[j];
			if (breakpoint instanceof IRubyExceptionBreakpoint)
			{
				if (breakpoint.getTypeName().equals(type.getFullyQualifiedName()))
				{
					exists = true;
					break;
				}
			}
		}
		if (!exists)
		{
			new Job(BreakpointMessages.AddExceptionAction_0)
			{
				protected IStatus run(IProgressMonitor monitor)
				{
					try
					{
						RdtDebugModel.createExceptionBreakpoint(resource, type.getFullyQualifiedName(), true, map);
						return Status.OK_STATUS;
					}
					catch (CoreException e)
					{
						return e.getStatus();
					}
				}

			}.schedule();
		}
	}

	/*
	 * (non-Rubydoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view)
	{
	}

	/*
	 * (non-Rubydoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
	}

	/*
	 * (non-Rubydoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
		fWorkbenchWindow = null;
	}

	/*
	 * (non-Rubydoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window)
	{
		fWorkbenchWindow = window;
	}
}
