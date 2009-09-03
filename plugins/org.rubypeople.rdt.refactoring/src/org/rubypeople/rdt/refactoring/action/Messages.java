package org.rubypeople.rdt.refactoring.action;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.action.messages"; //$NON-NLS-1$

	public static String RefactoringActionGroup;

	public static String SourceActionGroup;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
