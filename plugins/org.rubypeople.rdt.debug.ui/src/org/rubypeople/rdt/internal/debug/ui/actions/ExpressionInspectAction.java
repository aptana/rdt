/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 */

package org.rubypeople.rdt.internal.debug.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.rubypeople.rdt.internal.debug.ui.evaluation.EvaluationExpression;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

public class ExpressionInspectAction extends Action {

	private EvaluationExpression expression;
	private ISelection selection;

	public ExpressionInspectAction(EvaluationExpression expression, ISelection selection) {
		this.expression = expression;
		this.setText(expression.getName());
		this.selection = selection;
	}

	public void run() {
		if (!(selection instanceof TextSelection)) { return; }
		String replacementValue = ((TextSelection) selection).getText();
        if (replacementValue == null || replacementValue.length() == 0) {
        	replacementValue = "self" ;
        }
		final String evaluationText = expression.substitute(replacementValue);
		ITextSelection textSelection = new ITextSelection() {

			public int getOffset() {
				return 0;
			}

			public int getLength() {
				return 0;
			}

			public int getStartLine() {
				return 0;
			}

			public int getEndLine() {
				return 0;
			}

			public String getText() {
				return evaluationText;
			}

			public boolean isEmpty() {
				return false;
			}
		};
		
		InspectAction inspectAction = new InspectAction();
		inspectAction.selectionChanged(null, textSelection);
        inspectAction.setActiveEditor(this, (IEditorPart) RubyPlugin.getActivePage().getActivePart());
		inspectAction.run(this);

		super.run();
	}
}