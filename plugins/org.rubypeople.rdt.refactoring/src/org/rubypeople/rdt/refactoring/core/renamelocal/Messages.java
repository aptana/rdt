package org.rubypeople.rdt.refactoring.core.renamelocal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.renamelocal.messages"; //$NON-NLS-1$

	public static String LocalVariableRenamer_Modified;

	public static String RenameLocalConditionChecker_NameAlreadyExists;

	public static String RenameLocalConditionChecker_NameInvalid;

	public static String RenameLocalConditionChecker_NoLocalVariable;

	public static String RenameLocalConditionChecker_NoSelection;

	public static String RenameLocalConditionChecker_SameName;

	public static String RenameLocalRefactoring_Name;

	public static String VariableNameProvider_NoValidName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
