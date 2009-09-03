package com.aptana.rdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.ui.util.CoreUtility;
import org.rubypeople.rdt.ui.EclipsePreferencesAdapter;

import com.aptana.rdt.AptanaRDTPlugin;

public class DuplicateCodePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public DuplicateCodePreferencePage()
	{
		super(GRID);
		setPreferenceStore(new EclipsePreferencesAdapter(new InstanceScope(), AptanaRDTPlugin.PLUGIN_ID));
		setDescription("Duplicate code check compares the AST structures of various sections of your ruby code to determine if the structures are similar without comparing specific variable, object or method names. This check identifies code that may have been copy-pasted or may be a good candidate for refactoring to a higher level.");
	}

	public void createFieldEditors()
	{

		addField(new BooleanFieldEditor(AptanaRDTPlugin.DUPLICATE_CODE_CHECK_ENABLED,
				"Enable checking my code for duplicate AST structures", getFieldEditorParent()));

		IntegerFieldEditor intFieldEditor = new IntegerFieldEditor(AptanaRDTPlugin.DUPLICATE_CODE_MASS_THRESHOLD,
				"Minimum size of AST structures to check", getFieldEditorParent());
		intFieldEditor.setValidRange(10, 1000);
		addField(intFieldEditor);
	}

	public void init(IWorkbench workbench)
	{
		// do nothing
	}

	@Override
	public boolean performOk()
	{
		IProject[] rubyProjects = RubyCore.getRubyProjects();
		if (rubyProjects != null)
		{
			for (IProject project : rubyProjects)
			{
				CoreUtility.startBuildInBackground(project);
			}
		}
		return super.performOk();
	}
}
