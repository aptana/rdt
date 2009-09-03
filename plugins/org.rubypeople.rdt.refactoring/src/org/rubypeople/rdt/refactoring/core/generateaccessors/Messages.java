package org.rubypeople.rdt.refactoring.core.generateaccessors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.generateaccessors.messages"; //$NON-NLS-1$

	public static String AccessorsGenerator_Reader;

	public static String AccessorsGenerator_Writer;

	public static String GenerateAccessorsRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
