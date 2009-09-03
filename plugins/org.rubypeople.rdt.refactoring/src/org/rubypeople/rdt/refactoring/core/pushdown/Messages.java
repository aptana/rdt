package org.rubypeople.rdt.refactoring.core.pushdown;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.pushdown.messages"; //$NON-NLS-1$

	public static String MethodDownPusher_Constructors;

	public static String MethodDownPusher_Methods;

	public static String MethodDownPusher_NewClasses;

	public static String MethodDownPusher_RemoveOldMethods;

	public static String PushDownRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
