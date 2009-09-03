package org.rubypeople.rdt.refactoring.editprovider;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.editprovider.messages"; //$NON-NLS-1$

	public static String MultiEditProvider_Offset;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
