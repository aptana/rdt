package org.rubypeople.rdt.refactoring.core;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.messages"; //$NON-NLS-1$

	public static String RefactoringConditionChecker_EmptyDocument;

	public static String RefactoringConditionChecker_SyntaxErrorInCurrent;

	public static String RefactoringConditionChecker_SyntaxErrorInProject;

	public static String RenameResourceChange_name;

	public static String RenameResourceChange_does_not_exist;

	public static String RenameResourceChange_rename_resource;

	public static String DynamicValidationStateChange_workspace_changed;

	public static String Change_is_unsaved;

	public static String Change_is_read_only;

	public static String Change_same_read_only;

	public static String Change_has_modifications;

	public static String Change_does_not_exist;

	public static String deleteFile_deleting_resource;
	public static String createFile_Create_file;
	public static String CreateFileChange_error_unknownLocation;
	public static String CreateFileChange_error_exists;
	public static String createFile_creating_resource;
	public static String deleteFile_Delete_File;
	public static String RubyScriptChange_label;
	public static String MultiStateRubyScriptChange_name_pattern;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String format(String message, Object object) {
		return MessageFormat.format(message, new Object[] { object});
	}

	public static String format(String message, Object[] objects) {
		return MessageFormat.format(message, objects);
	}
	
	private Messages() {
	}
}
