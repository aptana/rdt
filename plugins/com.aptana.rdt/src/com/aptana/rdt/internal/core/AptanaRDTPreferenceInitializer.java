package com.aptana.rdt.internal.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.parser.warnings.LintOptions;

public class AptanaRDTPreferenceInitializer extends AbstractPreferenceInitializer
{

	public void initializeDefaultPreferences()
	{
		Set<String> optionNames = AptanaRDTPlugin.getDefault().optionNames;
		// Lint visitor settings
		Map<String, String> defaultOptionsMap = new LintOptions().getMap(); // compiler defaults

		// Store default values to default preferences
		IEclipsePreferences defaultPreferences = new DefaultScope().getNode(AptanaRDTPlugin.PLUGIN_ID);
		for (Iterator<Map.Entry<String, String>> iter = defaultOptionsMap.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			String optionName = (String) entry.getKey();
			defaultPreferences.put(optionName, (String) entry.getValue());
			optionNames.add(optionName);
		}
		AptanaRDTPlugin.getDefault().optionsCache = null;
		
		// do duplicate code check by default
		defaultPreferences.putBoolean(AptanaRDTPlugin.DUPLICATE_CODE_CHECK_ENABLED, true);
		defaultPreferences.putInt(AptanaRDTPlugin.DUPLICATE_CODE_MASS_THRESHOLD, 20);
	}

}
