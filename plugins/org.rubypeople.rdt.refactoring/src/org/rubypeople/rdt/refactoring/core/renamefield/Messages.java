package org.rubypeople.rdt.refactoring.core.renamefield;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.renamefield.messages"; //$NON-NLS-1$

	public static String FieldProvider_RetrievedAsAttribute;

	public static String FieldProvider_RetrievedAsField;

	public static String FieldProvider_UnexpectedNodeOfType;

	public static String RenameFieldConditionChecker_AlreadyExists;

	public static String RenameFieldConditionChecker_CannotNoSurroundingClass;

	public static String RenameFieldConditionChecker_NoFieldAtCaretPosition;

	public static String RenameFieldConditionChecker_NoNewName;

	public static String RenameFieldRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
