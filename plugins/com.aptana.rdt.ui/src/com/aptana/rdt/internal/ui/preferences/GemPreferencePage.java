package com.aptana.rdt.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aptana.rdt.ui.AptanaRDTUIPlugin;
import com.aptana.rdt.ui.preferences.IPreferenceConstants;

public class GemPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public GemPreferencePage()
	{
		super(GRID);
		setPreferenceStore(AptanaRDTUIPlugin.getDefault().getPreferenceStore());
		setDescription("");
	}

	public void createFieldEditors()
	{

		addField(new BooleanFieldEditor(IPreferenceConstants.PROMPT_TO_AUTO_INSTALL_GEMS,
				"Prompt me to install or update recommended gems when necessary", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench)
	{
		// do nothing
	}

}
