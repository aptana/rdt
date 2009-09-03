package org.rubypeople.rdt.refactoring.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.ui.messages"; //$NON-NLS-1$

	public static String NewNameListener_AlreadyInUse;

	public static String NewNameListener_IsNotValid;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
