package org.rubypeople.rdt.internal.testunit.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;


public class TestUnitPreferencesConstants {
	
	/**
	 * Boolean preference controlling whether the failure stack should be
	 * filtered.
	 */	
	public final static String DO_FILTER_STACK= TestunitPlugin.PLUGIN_ID + ".do_filter_stack"; //$NON-NLS-1$

	/**
	 * Boolean preference controlling whether the JUnit view should be shown on
	 * errors only.
	 */	
	public final static String SHOW_ON_ERROR_ONLY= TestunitPlugin.PLUGIN_ID + ".show_on_error"; //$NON-NLS-1$
	
	/**
	 * List of active stack filters. A String containing a comma separated list
	 * of fully qualified type names/patterns.
	 */			
	public static final String PREF_ACTIVE_FILTERS_LIST = TestunitPlugin.PLUGIN_ID + ".active_filters"; //$NON-NLS-1$
	
	/**
	 * List of inactive stack filters. A String containing a comma separated
	 * list of fully qualified type names/patterns.
	 */				
	public static final String PREF_INACTIVE_FILTERS_LIST = TestunitPlugin.PLUGIN_ID + ".inactive_filters"; //$NON-NLS-1$	

	private static String[] fgDefaultFilterPatterns= new String[] {
		"lib/ruby/1.8/test/unit", //$NON-NLS-1$
		"lib/ruby/gems/1.8/gems",  //$NON-NLS-1$
		".metadata/.plugins/org.rubypeople.rdt.testunit/ruby/RemoteTestRunner.rb", //$NON-NLS-1$
	};
	
	private TestUnitPreferencesConstants() {
		// no instance
	}
	
	/**
	 * Returns the default list of active stack filters.
	 * 
	 * @return list
	 */
	public static List<String> createDefaultStackFiltersList() {
		return Arrays.asList(fgDefaultFilterPatterns);
	}

	/**
	 * Serializes the array of strings into one comma
	 * separated string.
	 * 
	 * @param list array of strings
	 * @return a single string composed of the given list
	 */
	public static String serializeList(String[] list) {
		if (list == null)
			return ""; //$NON-NLS-1$

		StringBuffer buffer= new StringBuffer();
		for (int i= 0; i < list.length; i++) {
			if (i > 0)
				buffer.append(',');

			buffer.append(list[i]);
		}
		return buffer.toString();
	}
	
	/*
	 * Parses the comma separated string into an array of strings
	 */
	public static String[] parseList(String listString) {
		List<String> list= new ArrayList<String>(10);
		StringTokenizer tokenizer= new StringTokenizer(listString, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens())
			list.add(tokenizer.nextToken());
		return list.toArray(new String[list.size()]);
	}
}
