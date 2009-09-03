package com.aptana.rdt.internal.rake.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aptana.rdt.rake.PreferenceConstants;
import com.aptana.rdt.rake.RakePlugin;

/**
 * @author matt
 *
 */
public class ConfigurationPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public ConfigurationPreferencePage() {
		super(GRID);
		setPreferenceStore(RakePlugin.getDefault().getPreferenceStore());
		StringBuffer desc = new StringBuffer();
		desc.append("Set the location of the rake script.\n");
		desc.append("Please note that this should only be done to override the location we detect if you are having problems with Rake. If there is no value set here, we will automatically detect the script for you from your currently selected Installed Interpreter.\n");
		desc.append("Example (Win32): C:\\ruby\\bin\\rake\n");
		desc.append("Example (Linux): /usr/local/bin/rake\n");
		desc.append("Example (OSX): /usr/bin/rake\n");
		setDescription(desc.toString());
	}
	
	protected void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.PREF_RAKE_PATH, "rake path", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {

	}
}
