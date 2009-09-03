package org.rubypeople.rdt.refactoring.core.inlineclass;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.inlineclass.messages"; //$NON-NLS-1$

	public static String InlineClassConditionChecker_CannorDerivedClasses;

	public static String InlineClassConditionChecker_CannotInlineToItself;

	public static String InlineClassConditionChecker_CannotMultipleClassParts;

	public static String InlineClassConditionChecker_CannotWithSubclasses;

	public static String InlineClassConditionChecker_NoClassSelected;

	public static String InlineClassConditionChecker_NoFieldToReference;

	public static String InlineClassRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
