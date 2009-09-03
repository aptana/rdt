package org.rubypeople.rdt.refactoring.core.movefield;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.movefield.messages"; //$NON-NLS-1$

	public static String MoveFieldConditionChecker_NoDestination;

	public static String MoveFieldConditionChecker_NoFieldSelected;

	public static String MoveFieldConditionChecker_NoInstanceInsideClass;

	public static String MoveFieldConditionChecker_NoReference;

	public static String MoveFieldRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
