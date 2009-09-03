package org.rubypeople.rdt.refactoring.ui.pages.encapsulatefield;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.ui.pages.encapsulatefield.messages"; //$NON-NLS-1$

	public static String EncapsulateFieldAccessorComposite_AccessModifier;

	public static String EncapsulateFieldAccessorComposite_Generate;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
