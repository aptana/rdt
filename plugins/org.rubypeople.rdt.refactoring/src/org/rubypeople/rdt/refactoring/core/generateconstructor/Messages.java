package org.rubypeople.rdt.refactoring.core.generateconstructor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.generateconstructor.messages"; //$NON-NLS-1$

	public static String GenerateConstructorRefactoring_AlreadyContainsConstructors;

	public static String GenerateConstructorRefactoring_ClassesPluralForm;

	public static String GenerateConstructorRefactoring_Name;

	public static String GenerateConstructorRefactoring_TheClass;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
