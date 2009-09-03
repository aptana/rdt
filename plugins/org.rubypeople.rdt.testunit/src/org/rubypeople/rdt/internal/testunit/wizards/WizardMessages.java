package org.rubypeople.rdt.internal.testunit.wizards;

import org.eclipse.osgi.util.NLS;

public class WizardMessages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.internal.testunit.wizards.WizardMessages";//$NON-NLS-1$
	public static String NewTestCaseWizardPage_title;
	public static String NewTestCaseWizardPage_description;
	public static String Wizard_title_new_testcase;
	
	public static String NewTestCaseWizardPage_method_Stub_label;
	public static String NewTestCaseWizardPage_methodStub_setUp;
	public static String NewTestCaseWizardPage_methodStub_tearDown;
	public static String NewTestCaseWizardPage_methodStub_constructor;
	public static String NewTestCaseWizardPage_class_to_test_label;
	public static String NewTestCaseWizardPage_class_to_test_browse;
	public static String NewTestCaseWizardPage_class_to_test_dialog_title;
	public static String NewTestCaseWizardPage_class_to_test_dialog_message;
	public static String NewTestCaseWizardPage_error_class_to_test_not_valid;
	public static String NewTestCaseWizardPage_error_class_to_test_not_exist;
	public static String NewTestCaseWizardPage_warning_class_to_test_is_interface;
	public static String NewTestCaseWizardPageTwo_title;
	public static String NewTestCaseWizardPageTwo_description;
	public static String NewTestCaseWizardPageTwo_create_final_method_stubs_text;
	public static String NewTestCaseWizardPageTwo_create_tasks_text;
	public static String NewTestCaseWizardPageTwo_methods_tree_label;
	public static String NewTestCaseWizardPageTwo_selectAll;
	public static String NewTestCaseWizardPageTwo_deselectAll;
	public static String NewTestCaseWizardPageTwo_selected_methods_label_one;
	public static String NewTestCaseWizardPageTwo_selected_methods_label_many;
	public static String NewTestCaseWizardPageOne_not_yet_implemented_string;

	private WizardMessages() {
		// Do not instantiate
	}
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, WizardMessages.class);
	}
}
