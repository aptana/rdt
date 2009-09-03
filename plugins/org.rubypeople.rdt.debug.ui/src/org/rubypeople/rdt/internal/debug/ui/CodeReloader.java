/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT
 * is subject to the "Common Public License (CPL) v 1.0". You may not use RDT
 * except in compliance with the License. For further information see
 * org.rubypeople.rdt/rdt.license.
 */
package org.rubypeople.rdt.internal.debug.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.rubypeople.rdt.internal.debug.core.model.IRubyDebugTarget;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

public class CodeReloader implements IPartListener, IPropertyListener, IWindowListener {

	public void windowActivated(IWorkbenchWindow window) {}

	public void windowClosed(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	public void windowDeactivated(IWorkbenchWindow window) {}

	public void windowOpened(IWorkbenchWindow window) {
		window.getPartService().addPartListener(this);
	}

	public void partActivated(IWorkbenchPart part) {}

	public void partBroughtToTop(IWorkbenchPart part) {}

	public void partClosed(IWorkbenchPart part) {
		if (part instanceof RubyEditor) {
			part.removePropertyListener(this);
		}
	}

	public void partOpened(IWorkbenchPart part) {
		this.addAsListener(part);
	}

	public void partDeactivated(IWorkbenchPart part) {}

	public void propertyChanged(Object source, int propId) {
		if (!(source instanceof IEditorPart)) { return; }
		if (propId != IEditorPart.PROP_DIRTY) { return; }

		RubyEditor editor = (RubyEditor) source;
		if (editor.isDirty()) {
			// editor has not been saved, but code of a previously clean editor
			// has been edited
			return;
		}
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (int i = 0; i < launches.length; i++) {
			try {
				ILaunch launch = launches[i];
				if (!launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
					continue;
				}
				if (!launch.getLaunchConfiguration().getType().getIdentifier().equals(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION)) {
					continue;
				}
				final IRubyDebugTarget target = (IRubyDebugTarget) launch.getDebugTarget();
				if (target.isTerminated() || target.isDisconnected()) {
					continue;
				}
				final IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
				if (resource == null) {
					continue;
				}
				CodeReloadJob job = new CodeReloadJob(target, resource.getLocation().toOSString());
				job.schedule();
			} catch (CoreException e) {
				RdtDebugUiPlugin.log(new Status(IStatus.ERROR, RdtDebugUiPlugin.PLUGIN_ID, 0, "Error while trying to reload file.", e));
			}
		}
	}

	public void addAsListener(IWorkbenchPart part) {
		if (part == null || !(part instanceof RubyEditor)) { return; }
		part.addPropertyListener(this);
	}

	public CodeReloader() {
		RdtDebugUiPlugin.getDefault().getWorkbench().addWindowListener(this);
		IWorkbenchWindow[] windows = RdtDebugUiPlugin.getDefault().getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchWindow window = windows[i];
			window.getPartService().addPartListener(this);
		}
		// the CodeReloader constructor is not always called from a UI thread,
		// e.g. if debug.ui is activated from
		// pressing F11. If activation is triggered from adding a breakpoint
		// this is part of a UI thread.
		DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {

			public void run() {
				IWorkbenchWindow activeWindow = RdtDebugUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
				if (activeWindow == null) { return; }
				IWorkbenchPage page = activeWindow.getActivePage();
				if (page == null) { return; }
				addAsListener(page.getActivePart());
			}
		});
	}
}

