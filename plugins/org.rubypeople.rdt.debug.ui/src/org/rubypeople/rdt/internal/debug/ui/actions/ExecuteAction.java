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
package org.rubypeople.rdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.swt.widgets.Display;
import org.rubypeople.rdt.debug.core.model.IEvaluationResult;
import org.rubypeople.rdt.debug.core.model.IRubyValue;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.display.IDataDisplay;

public class ExecuteAction extends EvaluateAction {

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#displayResult(org.rubypeople.rdt.debug.core.model.jdt.debug.eval.IEvaluationResult)
	 */
	protected void displayResult(final IEvaluationResult result) {
		if (result.hasErrors()) {
			final Display display = RdtDebugUiPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					reportErrors(result);
					evaluationCleanup();
				}
			});
		} else {		
			final Display display = RdtDebugUiPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					IValue value = result.getValue();
					IDataDisplay dataDisplay= getDirectDataDisplay();
					if (dataDisplay != null) {
						try {
							dataDisplay.displayExpressionValue(valueToCode(value));
						} catch (DebugException e) {
							RdtDebugUiPlugin.log(e);
						} 
					}
					evaluationCleanup();
				}				
			});			
		}
	}

	public static String valueToCode(IValue value) throws DebugException {
		String string = value.getValueString();
		if (value instanceof IRubyValue) {
			IRubyValue rubyValue = (IRubyValue) value;
			if (value.getReferenceTypeName().equals("Array")) {
				StringBuffer buffer = new StringBuffer("[");
				IVariable[] vars = rubyValue.getVariables();
				for (int i = 0; i < vars.length; i++) {
					buffer.append(vars[i].getValue().getValueString());
					if (i < vars.length - 1)
						buffer.append(", ");
				}
				buffer.append("]");
				string = buffer.toString();
			} else if (value.getReferenceTypeName().equals("Hash")) {
				StringBuffer buffer = new StringBuffer("{");
				IVariable[] vars = rubyValue.getVariables();
				for (int i = 0; i < vars.length; i++) {
					buffer.append(vars[i]);
					if (i < vars.length - 1)
						buffer.append(", ");
				}
				buffer.append("}");
				string = buffer.toString();
			}
		}
		return "=> " + string;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction#getDataDisplay()
	 */
	protected IDataDisplay getDataDisplay() {
		return super.getDirectDataDisplay();
	}

}
