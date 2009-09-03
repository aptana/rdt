package org.rubypeople.rdt.internal.debug.ui.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.internal.debug.ui.actions.ActionMessages";//$NON-NLS-1$
	
	public static String Evaluate_error_message_direct_exception;
	public static String Evaluate_error_message_exception_pattern;
	public static String Evaluate_error_message_src_context;
	public static String Evaluate_error_message_stack_frame_context;
	public static String Evaluate_error_message_wrapped_exception;
	public static String Evaluate_error_problem_append_pattern;
	public static String Evaluate_error_title_eval_problems;
	public static String EvaluateAction_Cannot_open_Display_view;
	public static String EvaluateAction__evaluation_failed__1;
	public static String EvaluateAction__evaluation_failed__Reason;
	public static String EvaluateAction_Thread_not_suspended___unable_to_perform_evaluation__1;
	public static String EvaluateAction_Cannot_perform_nested_evaluations__1;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ActionMessages.class);
	}

}
