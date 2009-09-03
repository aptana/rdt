/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.rubypeople.rdt.internal.debug.ui.launcher;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.rubypeople.rdt.core.IRubyModel;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.debug.ui.launchConfigurations.RubyLaunchTab;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.launching.RubyMigrationDelegate;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.ui.RubyElementLabelProvider;

/**
 * Provides general widgets and methods for a Ruby type launch configuration 
 * 'Main' tab. 
 * Currently there are only three Ruby type launch configurations: Local Ruby Application, Applet, and Remote Debug
 * which this class is used by
 * 
 * @since 3.2
 */
public abstract class AbstractRubyMainTab extends RubyLaunchTab {

/**
 * A listener which handles widget change events for the controls
 * in this tab.
 */
private class WidgetListener implements ModifyListener, SelectionListener {
	
	public void modifyText(ModifyEvent e) {
		updateLaunchConfigurationDialog();
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {/*do nothing*/}
	
	public void widgetSelected(SelectionEvent e) {
		Object source = e.getSource();
		if (source == fProjButton) {
			handleProjectButtonSelected();
		}
		else {
			updateLaunchConfigurationDialog();
		}
	}
}
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	//Project UI widgets
	protected Text fProjText;

	private Button fProjButton;
	
	private WidgetListener fListener = new WidgetListener();
	
	/**
	 * chooses a project for the type of java launch config that it is
	 * @return
	 */
	private IRubyProject chooseRubyProject() {
		ILabelProvider labelProvider= new RubyElementLabelProvider(RubyElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LauncherMessages.AbstractRubyMainTab_4); 
		dialog.setMessage(LauncherMessages.AbstractRubyMainTab_3); 
		try {
			dialog.setElements(RubyCore.create(getWorkspaceRoot()).getRubyProjects());
		}
		catch (RubyModelException jme) {RdtDebugUiPlugin.log(jme);}
		IRubyProject javaProject= getRubyProject();
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == Window.OK) {			
			return (IRubyProject) dialog.getFirstResult();
		}		
		return null;		
	}
	
	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent the parent composite
	 */
	protected void createProjectEditor(Composite parent) {
		Font font= parent.getFont();
		Group group= new Group(parent, SWT.NONE);
		group.setText(LauncherMessages.AbstractRubyMainTab_0); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		fProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.setFont(font);
		fProjText.addModifyListener(fListener);
		fProjButton = createPushButton(group, LauncherMessages.AbstractRubyMainTab_1, null); 
		fProjButton.addSelectionListener(fListener);
	}	
	
	/**
	 * returns the default listener from this class. For all subclasses
	 * this listener will only provide the functionality of updating the current tab
	 * 
	 * @return a widget listener
	 */
	protected WidgetListener getDefaultListener() {
		return fListener;
	}
	
	/**
	 * Convenience method to get access to the java model.
	 */
	private IRubyModel getRubyModel() {
		return RubyCore.create(getWorkspaceRoot());
	}
	
	/**
	 * Return the IRubyProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	protected IRubyProject getRubyProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getRubyModel().getRubyProject(projectName);		
	}

	/**
	 * Convenience method to get the workspace root.
	 */
	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		IRubyProject project = chooseRubyProject();
		if (project == null) {
			return;
		}
		String projectName = project.getElementName();
		fProjText.setText(projectName);		
	}
	
	/* (non-Rubydoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		updateProjectFromConfig(config);
		super.initializeFrom(config);
	}
	
	/**
	 * updates the project text field form the configuration
	 * @param config the configuration we are editing
	 */
	private void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);	
		}
		catch (CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
		fProjText.setText(projectName);
	}
	
	/**
	 * Maps the config to associated ruby resource
	 * 
	 * @param config
	 */
	protected void mapResources(ILaunchConfigurationWorkingCopy config)  {
		try {
		//CONTEXTLAUNCHING
			IRubyProject rubyProject = getRubyProject();
			if (rubyProject != null && rubyProject.exists() && rubyProject.isOpen()) {
				RubyMigrationDelegate.updateResourceMapping(config);
			}
		} catch(CoreException ce) {
			setErrorMessage(ce.getStatus().getMessage());
		}
	}	
	
}
