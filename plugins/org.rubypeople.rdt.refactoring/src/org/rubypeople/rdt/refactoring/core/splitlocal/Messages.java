package org.rubypeople.rdt.refactoring.core.splitlocal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.splitlocal.messages"; //$NON-NLS-1$

	public static String SplitTempConditionChecker_NoLocal;

	public static String SplitTempRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
