package org.rubypeople.rdt.internal.debug.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

/**
 * @see PreferencePage
 */
public class RubyKeywordsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public RubyKeywordsPreferencePage() {
	}

	/**
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench)  {
	}

	/**
	 * @see PreferencePage#createContents
	 */
	protected Control createContents(Composite parent)  {
		Composite composite = new Composite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		Text keywordText = new Text(composite, SWT.NONE);
		keywordText.setText(getOriginalText());
		keywordText.setLayoutData(new GridData(GridData.FILL_BOTH));
		keywordText.setEditable(false);
		return composite;
	}
	
	protected String getOriginalText() {
		IPreferenceStore prefs = RdtDebugUiPlugin.getDefault().getPreferenceStore();
		return prefs.getDefaultString(RdtDebugUiConstants.PREFERENCE_KEYWORDS);
	}
}
