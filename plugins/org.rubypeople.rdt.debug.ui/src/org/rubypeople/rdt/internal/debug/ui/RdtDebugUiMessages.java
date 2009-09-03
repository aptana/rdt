package org.rubypeople.rdt.internal.debug.ui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class RdtDebugUiMessages {

	private static final String BUNDLE_NAME = RdtDebugUiMessages.class.getName();

	private RdtDebugUiMessages() {}

	public static String LaunchConfigurationTab_RubyArguments_working_dir_error_message;
	public static String LaunchConfigurationTab_RubyEntryPoint_invalidProjectSelectionMessage;
	public static String LaunchConfigurationTab_RubyEntryPoint_invalidFileSelectionMessage;
	public static String LaunchConfigurationTab_RubyEnvironment_interpreter_not_selected_error_message;
	public static String RdtDebugUiPlugin_internalErrorOccurred;
	public static String LaunchConfigurationTab_RubyArguments_interpreter_args_box_title;
	public static String LaunchConfigurationTab_RubyArguments_program_args_box_title;
	public static String ModifyCatchpointDialog_title;
	public static String ModifyCatchpointDialog_message;
	public static String Dialog_launchErrorTitle;
	public static String Dialog_launchErrorMessage;
	public static String LaunchConfigurationShortcut_Ruby_multipleConfigurationsError;
	public static String Dialog_launchWithoutSelectedInterpreter_title;
	public static String Dialog_launchWithoutSelectedInterpreter;
	public static String LaunchConfigurationTab_RubyArguments_working_dir;
	public static String LaunchConfigurationTab_RubyArguments_working_dir_browser_message;
	public static String LaunchConfigurationTab_RubyArguments_working_dir_use_default_message;
	public static String LaunchConfigurationTab_RubyArguments_name;
	public static String LaunchConfigurationTab_RubyEntryPoint_projectLabel;
	public static String LaunchConfigurationTab_RubyEntryPoint_projectSelectorMessage;
	public static String LaunchConfigurationTab_RubyEntryPoint_fileLabel;
	public static String LaunchConfigurationTab_RubyEntryPoint_fileSelectorMessage;
	public static String LaunchConfigurationTab_RubyEntryPoint_name;
	public static String LaunchConfigurationTab_RubyEnvironment_loadPathTab_label;
	public static String LaunchConfigurationTab_RubyEnvironment_loadPathDefaultButton_label;
	public static String LaunchConfigurationTab_RubyEnvironment_interpreterAddButton_label;
	public static String LaunchConfigurationTab_RubyEnvironment_interpreterTab_label;
	public static String LaunchConfigurationTab_RubyEnvironment_name;
	public static String EditEvaluationExpression_name_label;
	public static String EditEvaluationExpression_description_label;
	public static String EditEvaluationExpression_expression_label;
	public static String EvaluationExpressionsPreferencePage_description;
	public static String EvaluationExpressionsPreferencePage_column_name;
	public static String EvaluationExpressionsPreferencePage_column_description;
	public static String EvaluationExpressionsPreferencePage_new;
	public static String EvaluationExpressionsPreferencePage_edit;
	public static String EvaluationExpressionsPreferencePage_remove;
	public static String EvaluationExpressionsPreferencePage_import;
	public static String EvaluationExpressionsPreferencePage_export;
	public static String EditEvaluationExpressionDialog_add;
	public static String EditEvaluationExpressionDialog_edit;
	public static String EvaluationExpressionsPreferencePage_import_title;
	public static String EvaluationExpressionsPreferencePage_importexport_extension;
	public static String EvaluationExpressionsPreferencePage_export_title;
	public static String EvaluationExpressionsPreferencePage_export_filename;
	public static String EvaluationExpressionsPreferencePage_export_error_title;
	public static String EvaluationExpressionsPreferencePage_export_error_hidden;
	public static String EvaluationExpressionsPreferencePage_export_error_canNotWrite;
	public static String EvaluationExpressionsPreferencePage_export_exists_title;
	public static String EvaluationExpressionsPreferencePage_export_exists_message;
	public static String EvaluationExpressionsPreferencePage_title;
	public static String RubyInterpreterPreferencePage_addButton_label;
	public static String RubyInterpreterPreferencePage_editButton_label;
	public static String RubyInterpreterPreferencePage_removeButton_label;
	public static String RubyInterpreterPreferencePage_rubyInterpreterTable_interpreterName;
	public static String RubyInterpreterPreferencePage_rubyInterpreterTable_interpreterPath;
	public static String RdtDebugUiPlugin_couldNotOpenFile;
	public static String RubyInterpreterPreferencePage_rubyInterpreterTable_interpreterType;
	
	public static String ToolChainNotFound_title;
	public static String ToolChainNotFound_msg;
	public static String ToolChainNotFound_msg_osx;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, RdtDebugUiMessages.class);
	}

	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(key, new Object[] { arg });
	}

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}