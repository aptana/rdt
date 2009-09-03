package com.aptana.rdt.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;

import com.aptana.rdt.ui.AptanaRDTUIPlugin;
import com.aptana.rdt.ui.preferences.IPreferenceConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		new DefaultScope().getNode(AptanaRDTUIPlugin.PLUGIN_ID).putBoolean(
				IPreferenceConstants.PROMPT_TO_AUTO_INSTALL_GEMS, true);
	}

}
