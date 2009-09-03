package org.rubypeople.rdt.refactoring.core.encapsulatefield;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.encapsulatefield.messages"; //$NON-NLS-1$

	public static String EncapsulateFieldConditionChecker_AlreadyExists;

	public static String EncapsulateFieldConditionChecker_MethodWithName;

	public static String EncapsulateFieldConditionChecker_NoInstanceVariableSelected;

	public static String EncapsulateFieldConditionChecker_NothingToRefactor;

	public static String EncapsulateFieldConditionChecker_NotInsideAClass;

	public static String EncapsulateFieldConditionChecker_NotInsideAMethod;

	public static String EncapsulateFieldRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
