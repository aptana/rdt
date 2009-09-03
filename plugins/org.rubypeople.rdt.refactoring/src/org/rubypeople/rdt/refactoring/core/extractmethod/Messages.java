package org.rubypeople.rdt.refactoring.core.extractmethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.extractmethod.messages"; //$NON-NLS-1$

	public static String ExtractMethodConditionChecker_MethodAlreadyExists;

	public static String ExtractMethodConditionChecker_MustNotContainAClass;

	public static String ExtractMethodConditionChecker_MustNotContainAMethod;

	public static String ExtractMethodConditionChecker_MustNotContainSubmethods;

	public static String ExtractMethodConditionChecker_NothingToDo;

	public static String ExtractMethodConditionChecker_NotInsideAMethod;

	public static String ExtractMethodConditionChecker_NotPossibleContainsSuper;

	public static String ExtractMethodConditionChecker_NotPossibleContainsYield;

	public static String ExtractMethodConditionChecker_NotPossibleModule;

	public static String ExtractMethodRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
