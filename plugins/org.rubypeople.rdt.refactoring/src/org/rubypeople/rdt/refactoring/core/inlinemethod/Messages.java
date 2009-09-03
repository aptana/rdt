package org.rubypeople.rdt.refactoring.core.inlinemethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.inlinemethod.messages"; //$NON-NLS-1$

	public static String InlineMethodConditionChecker_CannotFindDefinition;

	public static String InlineMethodConditionChecker_CannotGuessType;

	public static String InlineMethodConditionChecker_NoMethodCall;

	public static String InlineMethodConditionChecker_ToManyReturns;

	public static String InlineMethodRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
