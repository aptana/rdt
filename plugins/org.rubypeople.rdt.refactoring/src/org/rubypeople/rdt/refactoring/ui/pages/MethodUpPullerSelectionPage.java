/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.ui.pages;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.rubypeople.rdt.internal.refactoring.RefactoringMessages;
import org.rubypeople.rdt.refactoring.core.pullup.MethodUpPuller;
import org.rubypeople.rdt.refactoring.ui.NotifiedContainerCheckedTree;

public class MethodUpPullerSelectionPage extends UserInputWizardPage {

	private NotifiedContainerCheckedTree tree;
	private MethodUpPuller methodUpPuller;

	public MethodUpPullerSelectionPage(MethodUpPuller methodUpPuller) {
		super(RefactoringMessages.PullUpMethod_Wizard_title);
		setTitle(RefactoringMessages.PullUpMethod_Wizard_title);
		this.methodUpPuller = methodUpPuller;
	}

	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FillLayout(SWT.HORIZONTAL));

		initTree(c);
		setControl(c);
	}

	private NotifiedContainerCheckedTree initTree(Composite c) {
		tree = new NotifiedContainerCheckedTree(c, methodUpPuller, methodUpPuller);
		return tree;
	}
}
