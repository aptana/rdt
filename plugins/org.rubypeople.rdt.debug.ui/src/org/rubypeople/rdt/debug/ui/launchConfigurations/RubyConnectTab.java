/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.debug.ui.launchConfigurations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.debug.ui.SWTFactory;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.launcher.AbstractRubyMainTab;
import org.rubypeople.rdt.internal.debug.ui.launcher.LauncherMessages;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMConnector;
import org.rubypeople.rdt.launching.RubyRuntime;

/**
 * A launch configuration tab that displays and edits the project associated
 * with a remote connection and the connector used to connect to a remote VM.
 * <p>
 * This class may be instantiated.
 * </p>
 * 
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RubyConnectTab extends AbstractRubyMainTab implements
		IPropertyChangeListener {

	// UI widgets
	private Map<String, Object> fArgumentMap;
	private Map<String, FieldEditor> fFieldEditorMap = new HashMap<String, FieldEditor>();
	private Composite fArgumentComposite;
	private Combo fConnectorCombo;

	// the selected connector
	private IVMConnector fConnector;
	private IVMConnector[] fConnectors = RubyRuntime.getVMConnectors();

	/*
	 * (non-Rubydoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite comp = SWTFactory.createComposite(parent, font, 1, 1,
				GridData.FILL_BOTH);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		comp.setLayout(layout);
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);

		// connection type
		Group group = SWTFactory.createGroup(comp,
				LauncherMessages.RubyConnectTab_Connect_ion_Type__7, 1, 1,
				GridData.FILL_HORIZONTAL);
		String[] names = new String[fConnectors.length];
		for (int i = 0; i < fConnectors.length; i++) {
			names[i] = fConnectors[i].getName();
		}
		fConnectorCombo = SWTFactory.createCombo(group, SWT.READ_ONLY, 1,
				GridData.FILL_HORIZONTAL, names);
		fConnectorCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleConnectorComboModified();
			}
		});
		createVerticalSpacer(comp, 1);

		// connection properties
		group = SWTFactory.createGroup(comp,
				LauncherMessages.RubyConnectTab_Connection_Properties_1, 2, 1,
				GridData.FILL_HORIZONTAL);
		Composite cgroup = SWTFactory.createComposite(group, font, 2, 1,
				GridData.FILL_HORIZONTAL);
		fArgumentComposite = cgroup;
		createVerticalSpacer(comp, 2);

		setControl(comp);
//		PlatformUI
//				.getWorkbench()
//				.getHelpSystem()
//				.setHelp(
//						getControl(),
//						IRubyDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CONNECT_TAB);
	}

	/**
	 * Update the argument area to show the selected connector's arguments
	 */
	private void handleConnectorComboModified() {
		int index = fConnectorCombo.getSelectionIndex();
		if ((index < 0) || (index >= fConnectors.length)) {
			return;
		}
		IVMConnector vm = fConnectors[index];
		if (vm.equals(fConnector)) {
			return; // selection did not change
		}
		fConnector = vm;
		try {
			fArgumentMap = vm.getDefaultArguments();
		} catch (CoreException e) {
			RdtDebugUiPlugin
					.statusDialog(
							LauncherMessages.RubyConnectTab_Unable_to_display_connection_arguments__2,
							e.getStatus());
			return;
		}

		// Dispose of any current child widgets in the tab holder area
		Control[] children = fArgumentComposite.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		fFieldEditorMap.clear();
		PreferenceStore store = new PreferenceStore();
		// create editors
		Iterator<String> keys = vm.getArgumentOrder().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object arg = fArgumentMap.get(key);
			FieldEditor field = null;
			if (arg instanceof Integer)
			{
				store.setDefault(key, ((Integer) arg).intValue());
				field = new IntegerFieldEditor(key, key,
						fArgumentComposite);
			}
			else if (arg instanceof String)
			{
				store.setDefault(key, (String) arg);
				field = new StringFieldEditor(key, key,
						fArgumentComposite);
			}
//			if (arg instanceof Connector.IntegerArgument) {
//				store.setDefault(arg.name(), ((Connector.IntegerArgument) arg)
//						.intValue());
//				field = new IntegerFieldEditor(arg.name(), arg.label(),
//						fArgumentComposite);
//			} else if (arg instanceof Connector.SelectedArgument) {
//				List choices = ((Connector.SelectedArgument) arg).choices();
//				String[][] namesAndValues = new String[choices.size()][2];
//				Iterator iter = choices.iterator();
//				int count = 0;
//				while (iter.hasNext()) {
//					String choice = (String) iter.next();
//					namesAndValues[count][0] = choice;
//					namesAndValues[count][1] = choice;
//					count++;
//				}
//				store.setDefault(arg.name(), arg.value());
//				field = new ComboFieldEditor(arg.name(), arg.label(),
//						namesAndValues, fArgumentComposite);
//			} else if (arg instanceof Connector.StringArgument) {
//				store.setDefault(arg.name(), arg.value());
//				field = new StringFieldEditor(arg.name(), arg.label(),
//						fArgumentComposite);
//			} else if (arg instanceof Connector.BooleanArgument) {
//				store.setDefault(arg.name(), ((Connector.BooleanArgument) arg)
//						.booleanValue());
//				field = new BooleanFieldEditor(arg.name(), arg.label(),
//						fArgumentComposite);
//			}
			if (field != null) {
				field.setPreferenceStore(store);
				field.loadDefault();
				field.setPropertyChangeListener(this);
				fFieldEditorMap.put(key, field);
			}
		}
		fArgumentComposite.getParent().getParent().layout();
		fArgumentComposite.layout(true);
		updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see
	 * org.eclipse.jdt.internal.debug.ui.launcher.AbstractRubyMainTab#initializeFrom
	 * (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		updateConnectionFromConfig(config);
	}

	/**
	 * Updates the connection argument field editors from the specified
	 * configuration
	 * 
	 * @param config
	 *            the config to load from
	 */
	private void updateConnectionFromConfig(ILaunchConfiguration config) {
		String id = null;
		try {
			id = config.getAttribute(
					IRubyLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
					RubyRuntime.getDefaultVMConnector().getIdentifier());
			fConnectorCombo.setText(RubyRuntime.getVMConnector(id).getName());
			handleConnectorComboModified();

			Map<String, Object> attrMap = config.getAttribute(
					IRubyLaunchConfigurationConstants.ATTR_CONNECT_MAP,
					(Map) null);
			if (attrMap == null) {
				return;
			}
			Iterator<String> keys = attrMap.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Object arg = fArgumentMap.get(key);
				FieldEditor editor = (FieldEditor) fFieldEditorMap.get(key);
				if (arg != null && editor != null) {
					String value = (String) attrMap.get(key);
					if (arg instanceof String
							) {
//							|| arg instanceof Connector.SelectedArgument) {
						editor.getPreferenceStore().setValue(key, value);
//					} else if (arg instanceof Connector.BooleanArgument) {
//						editor.getPreferenceStore().setValue(key,
//								Boolean.valueOf(value).booleanValue());
					} else if (arg instanceof Integer) {
						editor.getPreferenceStore().setValue(key,
								new Integer(value).intValue());
					}
					editor.load();
				}
			}
		} catch (CoreException ce) {
			RdtDebugUiPlugin.log(ce);
		}
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(
				IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText
						.getText().trim());
		config.setAttribute(
				IRubyLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
				getSelectedConnector().getIdentifier());
		mapResources(config);
		Map<String, Object> attrMap = new HashMap<String, Object>(fFieldEditorMap.size());
		Iterator<String> keys = fFieldEditorMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			FieldEditor editor = (FieldEditor) fFieldEditorMap.get(key);
			if (!editor.isValid()) {
				return;
			}
			Object arg = (Object) fArgumentMap.get(key);
			editor.store();
			if (arg instanceof String
					) {
//					|| arg instanceof Connector.SelectedArgument) {
				attrMap.put(key, editor.getPreferenceStore().getString(key));
//			} else if (arg instanceof Connector.BooleanArgument) {
//				attrMap
//						.put(key, Boolean.valueOf(
//								editor.getPreferenceStore().getBoolean(key))
//								.toString());
			} else if (arg instanceof Integer) {
				attrMap.put(key, new Integer(editor.getPreferenceStore()
						.getInt(key)).toString());
			}
		}
		config.setAttribute(IRubyLaunchConfigurationConstants.ATTR_CONNECT_MAP,
				attrMap);
	}

	/**
	 * Initialize default settings for the given Ruby element
	 */
	private void initializeDefaults(IRubyElement javaElement,
			ILaunchConfigurationWorkingCopy config) {
		initializeRubyProject(javaElement, config);
		initializeName(javaElement, config);
		initializeHardCodedDefaults(config);
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		IRubyElement javaElement = getContext();
		if (javaElement == null) {
			initializeHardCodedDefaults(config);
		} else {
			initializeDefaults(javaElement, config);
		}
	}

	/**
	 * Find the first instance of a type, compilation unit, class file or
	 * project in the specified element's parental hierarchy, and use this as
	 * the default name.
	 */
	private void initializeName(IRubyElement javaElement,
			ILaunchConfigurationWorkingCopy config) {
		String name = EMPTY_STRING;
		try {
			IResource resource = javaElement.getUnderlyingResource();
			if (resource != null) {
				name = resource.getName();
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(0, index);
				}
			} else {
				name = javaElement.getElementName();
			}
			name = getLaunchConfigurationDialog().generateName(name);
		} catch (RubyModelException jme) {
			RdtDebugUiPlugin.log(jme);
		}
		config.rename(name);
	}

	/**
	 * Initialize those attributes whose default values are independent of any
	 * context.
	 */
	private void initializeHardCodedDefaults(
			ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(
				IRubyLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
				RubyRuntime.getDefaultVMConnector().getIdentifier());
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);
		String name = fProjText.getText().trim();
		if (name.length() > 0) {
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name)
					.exists()) {
				setErrorMessage(LauncherMessages.RubyConnectTab_Project_does_not_exist_14);
				return false;
			}
		}
		Iterator<String> keys = fFieldEditorMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			Object arg = (Object) fArgumentMap.get(key);
			FieldEditor editor = (FieldEditor) fFieldEditorMap.get(key);
			if (editor instanceof StringFieldEditor) {
				String value = ((StringFieldEditor) editor).getStringValue();
//				if (!arg.isValid(value)) {
//					setErrorMessage(arg.label()
//							+ LauncherMessages.RubyConnectTab__is_invalid__5);
//					return false;
//				}
			}
		}
		return true;
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LauncherMessages.RubyConnectTab_Conn_ect_20;
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugUITools.getImage(IDebugUIConstants.IMG_LCL_DISCONNECT);
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	public String getId() {
		return "org.rubypeople.rdt.debug.ui.rubyConnectTab"; //$NON-NLS-1$
	}

	/**
	 * Returns the selected connector
	 */
	private IVMConnector getSelectedConnector() {
		return fConnector;
	}

	/*
	 * (non-Rubydoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		updateLaunchConfigurationDialog();
	}
}
