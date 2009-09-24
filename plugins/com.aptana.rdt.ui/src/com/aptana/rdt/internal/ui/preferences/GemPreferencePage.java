package com.aptana.rdt.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rubypeople.rdt.ui.EclipsePreferencesAdapter;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.ui.AptanaRDTUIPlugin;
import com.aptana.rdt.ui.preferences.IPreferenceConstants;

public class GemPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	private FileFieldEditor gemScriptEditor;
	private EclipsePreferencesAdapter store;

	public GemPreferencePage()
	{
		super(GRID);
		setPreferenceStore(AptanaRDTUIPlugin.getDefault().getPreferenceStore());
		setDescription("");
	}

	public void createFieldEditors()
	{
		gemScriptEditor = new FileFieldEditor(com.aptana.rdt.core.preferences.IPreferenceConstants.GEM_SCRIPT_PATH,
				"Path to gem bin script", true, StringButtonFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent());
		addField(gemScriptEditor);
		addField(new BooleanFieldEditor(IPreferenceConstants.PROMPT_TO_AUTO_INSTALL_GEMS,
				"Prompt me to install or update recommended gems when necessary", getFieldEditorParent()));
	}

	@Override
	protected void initialize()
	{
		super.initialize();

		store = new EclipsePreferencesAdapter(new InstanceScope(), AptanaRDTPlugin.PLUGIN_ID);
		gemScriptEditor.setPreferenceStore(store);
		gemScriptEditor.load();
	}

	@Override
	public boolean performOk()
	{
		// TODO Auto-generated method stub
		boolean ret = super.performOk();
		store.flush();
		store = null;
		return ret;
	}

	public void init(IWorkbench workbench)
	{
		// do nothing
	}

}
