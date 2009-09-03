package com.aptana.rdt.internal.rake.view;

import org.eclipse.osgi.util.NLS;

public class RakeViewMessages extends NLS {

	private static final String BUNDLE_NAME = RakeViewMessages.class.getName();
	
	public static String SpecifyRakePath_message;
	public static String SelectRubyProject_message;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, RakeViewMessages.class);
	}
}
