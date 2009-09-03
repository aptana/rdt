/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import org.eclipse.jface.action.Action;

/**
 * Action to enable/disable stack trace filtering.
 */
public class CompareResultsAction extends Action {


	private final FailureTrace fView;

	public CompareResultsAction(FailureTrace view) {
		super(TestUnitMessages.CompareResultsAction_label);
		this.fView = view;
		setDescription(TestUnitMessages.CompareResultsAction_description); 
		setToolTipText(TestUnitMessages.CompareResultsAction_tooltip);

		setDisabledImageDescriptor(TestunitPlugin.getImageDescriptor("dlcl16/compare.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/compare.gif")); //$NON-NLS-1$
		setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/compare.gif")); //$NON-NLS-1$
	}

	/*
	 * @see Action#actionPerformed
	 */
	public void run() {
		// TODO Allow comparison of results
		CompareResultDialog dialog = new CompareResultDialog(fView.getShell(), fView.getFailedTest());
		dialog.create();
		dialog.open();
	}
}