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
package org.rubypeople.rdt.internal.debug.ui.display;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.texteditor.IUpdate;


public class DisplayViewAction extends Action implements IUpdate {
	
	/** The text operation code */
	private int fOperationCode= -1;
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	/** The text operation target provider */
	private IAdaptable fTargetProvider;
	
	
	public DisplayViewAction(ITextOperationTarget target, int operationCode) {
		super();
		fOperationTarget= target;
		fOperationCode= operationCode;
		update();
	}
		
	public DisplayViewAction(IAdaptable targetProvider, int operationCode) {
		super();
		fTargetProvider= targetProvider;
		fOperationCode= operationCode;
		update();
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IAction</code> method runs the operation with the current
	 * operation code.
	 */
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null)
			fOperationTarget.doOperation(fOperationCode);
	}
	
	/**
	 * The <code>TextOperationAction</code> implementation of this 
	 * <code>IUpdate</code> method discovers the operation through the current
	 * editor's <code>ITextOperationTarget</code> adapter, and sets the
	 * enabled state accordingly.
	 */
	public void update() {
		if (fOperationTarget == null && fTargetProvider != null && fOperationCode != -1){
			fOperationTarget= (ITextOperationTarget) fTargetProvider.getAdapter(ITextOperationTarget.class);
		}
	
		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);
	}
}
