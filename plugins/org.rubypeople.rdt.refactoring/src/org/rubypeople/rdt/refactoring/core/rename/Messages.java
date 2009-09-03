package org.rubypeople.rdt.refactoring.core.rename;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.rename.messages"; //$NON-NLS-1$

	public static String RenameConditionChecker_NothingSelected;

	public static String RenameRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
