/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;


import org.eclipse.jface.action.Action;

/**
 * Action to enable/disable stack trace filtering.
 */
public class EnableStackFilterAction extends Action {

	private FailureTrace fView;	
	
	public EnableStackFilterAction(FailureTrace view) {
		super(TestUnitMessages.EnableStackFilterAction_action_label);  
		setDescription(TestUnitMessages.EnableStackFilterAction_action_description);  
		setToolTipText(TestUnitMessages.EnableStackFilterAction_action_tooltip); 
		
		setDisabledImageDescriptor(TestunitPlugin.getImageDescriptor("dlcl16/cfilter.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
		setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/cfilter.gif")); //$NON-NLS-1$
		// TODO Uncomment when we have help context ids set up
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ITestUnitHelpContextIds.ENABLEFILTER_ACTION);

		fView= view;
		setChecked(TestUnitPreferencePage.getFilterStack());
	}

	/*
	 * @see Action#actionPerformed
	 */		
	public void run() {
		TestUnitPreferencePage.setFilterStack(isChecked());
		fView.refresh();
	}
}
