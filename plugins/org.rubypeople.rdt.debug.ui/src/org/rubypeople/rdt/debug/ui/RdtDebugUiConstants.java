package org.rubypeople.rdt.debug.ui;

import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

public interface RdtDebugUiConstants
{
	public static final String PREFERENCE_KEYWORDS = RdtDebugUiPlugin.PLUGIN_ID + ".preference_keywords";
	public static final String SHOW_STATIC_VARIABLES_PREFERENCE = RdtDebugUiPlugin.PLUGIN_ID + ".showStaticVariables";
	public static final String SHOW_CONSTANTS_PREFERENCE = RdtDebugUiPlugin.PLUGIN_ID + ".showConstants";
	public static final String EVALUATION_EXPRESSIONS_PREFERENCE = RdtDebugUiPlugin.PLUGIN_ID
			+ ".evaluationExpressions";
	public static final int INTERNAL_ERROR = 0;

	/**
	 * Identifier for a group of evaluation actions in a menu (value <code>"evaluationGroup"</code>).
	 */
	public static final String EVALUATION_GROUP = "evaluationGroup"; //$NON-NLS-1$

	/**
	 * Display view identifier (value <code>"org.rubypeople.rdt.debug.ui.DisplayView"</code>).
	 */
	public static final String ID_DISPLAY_VIEW = RdtDebugUiPlugin.PLUGIN_ID + ".DisplayView"; //$NON-NLS-1$

	public static final String RUBY_SOURCE_LOCATOR = "org.rubypeople.rdt.debug.ui.rubySourceLocator";
}
