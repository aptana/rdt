package org.rubypeople.rdt.refactoring.preview;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.preview.messages"; //$NON-NLS-1$

	public static String RubyTextEditChangePreviewViewer_OriginalSource;

	public static String RubyTextEditChangePreviewViewer_RefactoredSource;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
