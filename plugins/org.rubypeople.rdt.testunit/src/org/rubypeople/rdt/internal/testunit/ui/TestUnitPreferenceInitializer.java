/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Davids <sdavids@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Default preference value initialization for the <code>org.rubypeople.rdt.testunit</code> plug-in.
 */
public class TestUnitPreferenceInitializer extends AbstractPreferenceInitializer
{

	private static final String BAD_FILTER = ".metadata/.plugins/org.rubypeople.rdt.testunit/RemoteTestRunner.rb";
	private static final String GOOD_FILTER = ".metadata/.plugins/org.rubypeople.rdt.testunit/ruby/RemoteTestRunner.rb";

	/** {@inheritDoc} */
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new DefaultScope().getNode(TestunitPlugin.PLUGIN_ID);
		prefs.putBoolean(TestUnitPreferencesConstants.DO_FILTER_STACK, true);
		prefs.putBoolean(TestUnitPreferencesConstants.SHOW_ON_ERROR_ONLY, false);
		// prefs.putBoolean(TestUnitPreferencesConstants.ENABLE_ASSERTIONS, false);

		List<String> defaults = TestUnitPreferencesConstants.createDefaultStackFiltersList();
		String[] filters = (String[]) defaults.toArray(new String[defaults.size()]);
		String active = TestUnitPreferencesConstants.serializeList(filters);
		prefs.put(TestUnitPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, active);
		prefs.put(TestUnitPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, ""); //$NON-NLS-1$
		// prefs.putInt(TestUnitPreferencesConstants.MAX_TEST_RUNS, 10);

		IEclipsePreferences instance = new InstanceScope().getNode(TestunitPlugin.PLUGIN_ID);
		String activeFilters = instance.get(TestUnitPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, "");
		String[] activeFiltersAry = TestUnitPreferencesConstants.parseList(activeFilters);
		boolean found = false;
		for (int i = 0; i < activeFiltersAry.length; i++)
		{
			String activeFilter = activeFiltersAry[i];
			if (activeFilter.equals(BAD_FILTER))
			{
				activeFilter = GOOD_FILTER;
				found = true;
				break;
			}
		}
		if (found)
		{
			instance.put(TestUnitPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, TestUnitPreferencesConstants
					.serializeList(activeFiltersAry));
		}
	}
}
