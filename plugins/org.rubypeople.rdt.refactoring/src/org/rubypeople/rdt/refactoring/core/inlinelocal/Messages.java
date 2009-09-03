package org.rubypeople.rdt.refactoring.core.inlinelocal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.inlinelocal.messages"; //$NON-NLS-1$

	public static String InlineLocalConditionChecker_CannotBlockArgument;

	public static String InlineLocalConditionChecker_CannotMethodParameters;

	public static String InlineLocalConditionChecker_CannotMultiAssigned;

	public static String InlineLocalConditionChecker_CannotMultipleAssignments;

	public static String InlineLocalConditionChecker_CannotSelfReferencing;

	public static String InlineLocalConditionChecker_NameNotUnique;

	public static String InlineLocalConditionChecker_NoLocalVariable;

	public static String InlineLocalConditionChecker_NoTarget;

	public static String InlineLocalRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
