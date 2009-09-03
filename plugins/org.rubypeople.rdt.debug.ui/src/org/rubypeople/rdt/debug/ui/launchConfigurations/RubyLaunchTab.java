/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.debug.ui.launchConfigurations;

 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

/**
 * Common function for Ruby launch configuration tabs.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class RubyLaunchTab extends AbstractLaunchConfigurationTab {
		
	/**
	 * Config being modified
	 */
	private ILaunchConfiguration fLaunchConfig;
	
	/**
	 * Returns the current Ruby element context in the active workbench page
	 * or <code>null</code> if none.
	 * 
	 * @return current Ruby element in the active page or <code>null</code>
	 */
	protected IRubyElement getContext() {
		IWorkbenchPage page = RdtDebugUiPlugin.getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IRubyElement) {
						return (IRubyElement)obj;
					}
					if (obj instanceof IResource) {
						IRubyElement je = RubyCore.create((IResource)obj);
						if (je == null) {
							IProject pro = ((IResource)obj).getProject();
							je = RubyCore.create(pro);
						}
						if (je != null) {
							return je;
						}
					}
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				return (IRubyElement) input.getAdapter(IRubyElement.class);
			}
		}
		return null;
	}
	
	/**
	 * Returns the launch configuration this tab was initialized from.
	 * 
	 * @return launch configuration this tab was initialized from
	 */
	protected ILaunchConfiguration getCurrentLaunchConfiguration() {
		return fLaunchConfig;
	}
	
	/**
	 * Sets the launch configuration this tab was initialized from
	 * 
	 * @param config launch configuration this tab was initialized from
	 */
	private void setCurrentLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfig = config;
	}
	
	/**
	 * Sets the Ruby project attribute on the given working copy to the Ruby project
	 * associated with the given Ruby element.
	 * 
	 * @param javaElement Ruby model element this tab is associated with
	 * @param config configuration on which to set the Ruby project attribute
	 */
	protected void initializeRubyProject(IRubyElement javaElement, ILaunchConfigurationWorkingCopy config) {
		IRubyProject javaProject = javaElement.getRubyProject();
		String name = null;
		if (javaProject != null && javaProject.exists()) {
			name = javaProject.getElementName();
		}
		config.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
	}	
	
	/* (non-Rubydoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 * 
	 * Subclasses may override this method and should call super.initializeFrom(...).
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		setCurrentLaunchConfiguration(config);
	}	
	
}

