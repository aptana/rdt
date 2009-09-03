package org.rubypeople.rdt.internal.testunit.ui;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class TestUnitMessages {

	private static final String BUNDLE_NAME= "org.rubypeople.rdt.internal.testunit.ui.TestUnitMessages"; //$NON-NLS-1$
	
	private TestUnitMessages() {}
	
	public static String LaunchConfigurationTab_RubyEntryPoint_allTestCases;
	public static String LaunchConfigurationTab_RubyEntryPoint_classSelectorMessage;
	public static String LaunchConfigurationTab_RubyEntryPoint_classLabel;
	public static String CompareResultsAction_label;
	public static String CompareResultsAction_description;
	public static String CompareResultsAction_tooltip;
	public static String CopyTrace_action_label;
	public static String CopyTraceAction_problem;
	public static String CopyTraceAction_clipboard_busy;
	public static String CounterPanel_label_runs;
	public static String CounterPanel_label_errors;
	public static String CounterPanel_label_failures;
	public static String FailureRunView_tab_tooltip;
	public static String FailureRunView_tab_title;
	public static String OpenEditor_action_label;
	public static String OpenEditorAction_action_label;
	public static String RerunAction_label_debug;
	public static String RerunAction_label_run;
	public static String TestRunnerViewPart_label_failure;
	public static String TestRunnerViewPart_error_cannotrerun;
	public static String TestRunnerViewPart_cannotrerun_title;
	public static String TestRunnerViewPart_cannotrerurn_message;
	public static String TestRunnerViewPart_message_launching;
	public static String TestRunnerViewPart_message_stopped;
	public static String TestRunnerViewPart_message_terminated;
	public static String TestRunnerViewPart_jobName;
	public static String TestRunnerViewPart_terminate_title;
	public static String TestRunnerViewPart_terminate_message;
	public static String TestRunnerViewPart_rerunaction_label;
	public static String TestRunnerViewPart_rerunaction_tooltip;
	public static String LaunchTestAction_message_selectConfiguration;
	public static String LaunchTestAction_message_selectDebugConfiguration;
	public static String LaunchTestAction_message_selectRunConfiguration;
	public static String Dialog_launchWithoutSelectedInterpreter_title;
	public static String Dialog_launchWithoutSelectedInterpreter;
	public static String LaunchConfigurationTab_RubyEntryPoint_allTestMethods;
	public static String LaunchConfigurationTab_RubyEntryPoint_methodLabel;
	public static String JUnitMainTab_tab_label;
	public static String ExpandAllAction_text;
	public static String ExpandAllAction_tooltip;
	public static String HierarchyRunView_tab_tooltip;
	public static String HierarchyRunView_tab_title;
	public static String ScrollLockAction_action_label;
	public static String ScrollLockAction_action_tooltip;
	public static String RubyClassSelector_Title;
	public static String CounterPanel_runcount;
	public static String FailureRunView_labelfmt;
	public static String TestRunnerViewPart_message_error;
	public static String TestRunnerViewPart_message_failure;
	public static String TestRunnerViewPart_message_success;
	public static String TestRunnerViewPart_message_finish;
	public static String TestRunnerViewPart_message_started;
	public static String TestRunnerViewPart_configName;
	public static String CompareResultDialog_expectedLabel;
	public static String CompareResultDialog_actualLabel;
	public static String CompareResultDialog_labelOK;
	public static String CompareResultDialog_title;
	public static String TestRunnerViewPart_toggle_horizontal_label;
	public static String TestRunnerViewPart_toggle_vertical_label;
	public static String TestRunnerViewPart_toggle_automatic_label;
	public static String TestRunnerViewPart_layout_menu;
	public static String OpenEditorAction_error_cannotopen_title;
	public static String OpenEditorAction_error_cannotopen_message;
	public static String OpenEditorAction_error_dialog_title;
	public static String OpenEditorAction_error_dialog_message;
	public static String OpenEditorAction_message_cannotopen;
	public static String OpenTestAction_error_title;
	public static String OpenTestAction_error_methodNoFound;
	public static String TestUnitBaseLaunchConfiguration_error_invalidproject;
	public static String JUnitBaseLaunchConfiguration_dialog_title;
	public static String EnableStackFilterAction_action_tooltip;
	public static String EnableStackFilterAction_action_description;
	public static String EnableStackFilterAction_action_label;
	public static String TestUnitMainTab_label_defaultpackage;
	public static String TestUnitPreferencePage_invalidstepfilterreturnescape;
	public static String TestUnitPreferencePage_disableallbutton_tooltip;
	public static String TestUnitPreferencePage_disableallbutton_label;
	public static String TestUnitPreferencePage_enableallbutton_tooltip;
	public static String TestUnitPreferencePage_enableallbutton_label;
	public static String TestUnitPreferencePage_removefilterbutton_tooltip;
	public static String TestUnitPreferencePage_removefilterbutton_label;
	public static String TestUnitPreferencePage_addfilterbutton_tooltip;
	public static String TestUnitPreferencePage_addfilterbutton_label;
	public static String TestUnitPreferencePage_filter_label;
	public static String TestUnitPreferencePage_description;
	public static String TestRunnerViewPart_activate_on_failure_only;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TestUnitMessages.class);
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(key, new Object[] { arg });
	}

	/**
	 * Gets a string from the resource bundle and formats it with arguments
	 */	
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(key, args);
	}
}
