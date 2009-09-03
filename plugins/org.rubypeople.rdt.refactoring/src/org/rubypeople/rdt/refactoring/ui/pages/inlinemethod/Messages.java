package org.rubypeople.rdt.refactoring.ui.pages.inlinemethod;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.ui.pages.inlinemethod.messages"; //$NON-NLS-1$

	public static String TargetClassFinderUI_ChooseType;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
