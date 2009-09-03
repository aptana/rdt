package org.rubypeople.rdt.refactoring.ui.pages.extractmethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.ui.pages.extractmethod.messages"; //$NON-NLS-1$

	public static String ExtractMethodComposite_AccessModifier;

	public static String ExtractMethodComposite_ButtonDown;

	public static String ExtractMethodComposite_ButtonEdit;

	public static String ExtractMethodComposite_ButtonUp;

	public static String ExtractMethodComposite_ExpansionHint;

	public static String ExtractMethodComposite_MethodName;

	public static String ExtractMethodComposite_Name;

	public static String ExtractMethodComposite_Parameters;

	public static String ExtractMethodComposite_ReplaceAll;

	public static String ExtractMethodComposite_SameAsSource;

	public static String ExtractMethodComposite_SelectedCode;

	public static String ExtractMethodComposite_SignaturePreview;

	public static String MethodNameListener_IsNotValidName;

	public static String ParametersTableCellEditorListener_CannotHaveParametersWithEqualNames;

	public static String ParametersTableCellEditorListener_IsAlreadyUsed;

	public static String ParametersTableCellEditorListener_IsNotValidParameterName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
