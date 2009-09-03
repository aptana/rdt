package org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile.messages"; //$NON-NLS-1$

	public static String InFileClassPartsMerger_GroupInsertion;

	public static String MergeClassPartsInFileConditionChecker_NotEnoughPartsFound;

	public static String MergeClassPartsInFileConditionChecker_TooFewParts;

	public static String MergeClassPartsInFileRefactoring_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
