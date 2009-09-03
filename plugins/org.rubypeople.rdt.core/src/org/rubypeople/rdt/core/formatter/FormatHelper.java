package org.rubypeople.rdt.core.formatter;

import org.rubypeople.rdt.internal.formatter.Indentor;

public interface FormatHelper
{

	public static final String SPACE_AFTER_COMMA_IN_LISTS = "space_after_comma_in_lists";
	public static final String SPACE_BEFORE_AND_AFTER_ASSIGNMENTS = "space_before_and_after_assignment";
	public static final String ALWAYS_SURROUND_METHOD_CALLS_IN_PARENS = "always_surround_method_calls_in_parens";
	public static final String SPACES_AROUND_HASH_ASSIGNMENT = "space_around_hash_assignment";
	public static final String SPACES_BEFORE_AND_AFTER_HASH_CONTENT = "space_before_and_after_hash_content";
	public static final String SPACES_BEFORE_ITER_VARS = "space_before_block_vars";
	public static final String SPACE_AFTER_ITER_VARS = "space_after_block_vars";
	public static final String NEWLINE_BETWEEN_CLASS_BODY_ELEMENTS = "newline_between_class_body_elements";
	public static final String ALWAYS_SURROUND_METHOD_ARGUMENTS_IN_PARENS = "always_surround_method_def_args_in_parens";
	public static final String SPACE_BEFORE_BLOCK_BRACKETS = "space_before_opening_block_bracket";
	public static final String SPACE_BEFORE_CLOSING_BLOCK_BRACKET = "space_before_closing_block_bracket";
	public static final String INSERT_DO_AFTER_WHILE_EXPRESSION = "insert_do_after_while_expression";
	/**
	 * Pref key for collapsing operator self assignments (i.e. "x = x + 1" vs. "x += 1").
	 */
	public static final String COLLAPSE_OPERATOR_SELF_ASSIGNMENTS = "collapse_operator_self_assignments";

	public abstract Indentor getIndentor();

	public abstract String getListSeparator();

	public abstract String beforeCallArguments();

	public abstract String afterCallArguments();

	public abstract String beforeMethodArguments();

	public abstract String afterMethodArguments();

	public abstract String hashAssignment();

	public abstract String beforeHashContent();

	public abstract String afterHashContent();

	public abstract String matchOperator();

	public abstract String beforeAssignment();

	public abstract String beforeIterBrackets();

	public abstract String afterAssignment();

	public abstract String beforeIterVars();

	public abstract String afterIterVars();

	public abstract String beforeClosingIterBrackets();

	public abstract String classBodyElementsSeparator();

	public abstract String getLineDelimiter();

	public abstract boolean insertDoAfterWhileExpression();

	public abstract boolean collapseOperatorSelfAssignments();
}
