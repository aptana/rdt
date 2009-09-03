package org.rubypeople.rdt.refactoring.core.renamemethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.renamemethod.messages"; //$NON-NLS-1$

	public static String RenameMethodConditionChecker_AlreadyExists;

	public static String RenameMethodConditionChecker_NoMethodSelected;

	public static String RenameMethodConditionChecker_NotChanged;

	public static String RenameMethodRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
