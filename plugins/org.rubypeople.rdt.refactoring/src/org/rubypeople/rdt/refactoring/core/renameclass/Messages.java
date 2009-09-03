package org.rubypeople.rdt.refactoring.core.renameclass;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.renameclass.messages"; //$NON-NLS-1$

	public static String RenameClassConditionChecker_PleaseSelectNameOfAClassDeclaration;

	public static String RenameClassRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
