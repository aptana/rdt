/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.internal.refactoring;

import org.eclipse.osgi.util.NLS;

public class RefactoringMessages extends NLS {

	private static final String BUNDLE_NAME = RefactoringMessages.class.getName();
	
	public static String Refactor;
	public static String ExtractConstantAction_extract_constant;
	public static String ExtractConstantAction_label;
	public static String ExtractConstantInputPage_constant_name;
	public static String ExtractConstantInputPage_enter_name;
	public static String ExtractConstantInputPage_invalid_name;
	public static String ExtractConstantInputPage_replace_all_occurrences;
	public static String ExtractConstantWizard_defaultPageTitle;
	public static String PullUpMethod_Wizard_title;

	private RefactoringMessages() {}

	static {
		NLS.initializeMessages(BUNDLE_NAME, RefactoringMessages.class);
	}
}
