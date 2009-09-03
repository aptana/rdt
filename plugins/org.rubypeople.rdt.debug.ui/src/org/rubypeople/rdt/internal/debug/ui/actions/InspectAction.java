/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 */

package org.rubypeople.rdt.internal.debug.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.rubypeople.rdt.debug.core.RdtDebugCorePlugin;
import org.rubypeople.rdt.debug.core.model.IRubyStackFrame;
import org.rubypeople.rdt.debug.core.model.IRubyVariable;
import org.rubypeople.rdt.internal.debug.core.model.RubyExpression;
import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;

public class InspectAction extends AbstractInspectAction implements IViewActionDelegate, IEditorActionDelegate {
	private IRubyVariable inspectResult;

	protected IRubyStackFrame getRubyStackFrame() {
		IViewPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part == null) {
			return null;
		}
		IDebugView launchView = (IDebugView) part;
		StructuredSelection selected = (StructuredSelection) launchView.getViewer().getSelection();
		if (selected.isEmpty()) {
			return null;
		}
		if (!(selected.getFirstElement() instanceof IRubyStackFrame)) {
			return null;
		}
		return (IRubyStackFrame) selected.getFirstElement();

	}

	public void run(IAction action) {
		final IRubyStackFrame stackFrame = this.getRubyStackFrame();
		if (stackFrame == null) {
			MessageDialog.openInformation(page.getActivePart().getSite().getShell(), "No suitable stack frame", "Could not inspect because there is no context (a ruby stack frame) for inspection selected.");
			return;
		}
		if (!(selection instanceof ITextSelection)) {
			return;
		}
		final String selectedText = ((ITextSelection) selection).getText().replace('\n', ';').replace('\r', ' ');
		String jobName = "Inspect " + (selectedText.length() < 20 ? selectedText : selectedText.substring(0, 19) + "...");
		Job job = new Job(jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("inspecting", IProgressMonitor.UNKNOWN);
				IStatus result = null;
				try {
					inspectResult = stackFrame.getRubyDebuggerProxy().readInspectExpression(stackFrame, selectedText);
					result = Status.OK_STATUS;
				} catch (RubyProcessingException e) {
					String message = e.getRubyExceptionType() + " inspecting '" + selectedText + "':\n" + e.getMessage();
					result = new Status(IStatus.ERROR, RdtDebugCorePlugin.PLUGIN_ID, IStatus.ERROR, message, e);
				}
				return result;
			}

		};
		job.addJobChangeListener(new JobChangeAdapter() {
			public void done(final IJobChangeEvent event) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (event.getResult().isOK()) {
							showExpressionView();
							DebugPlugin.getDefault().getExpressionManager().addExpression(new RubyExpression(selectedText, inspectResult));
						}
						// if (event.getResult().getSeverity() == IStatus.ERROR)
						// {
						// String message = event.getResult().getMessage();
						// MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
						// "Error", "Could not inspect '" + selectedText + "': "
						// + message);
						// }
					}
				});
			}
		});
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor == null || targetEditor.getEditorSite() == null) {
			this.page = null;
		} else {
			this.page = targetEditor.getEditorSite().getPage();

		}
	}

}
