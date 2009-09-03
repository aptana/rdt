/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 * 
 */

package org.rubypeople.rdt.internal.debug.ui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.rubypeople.rdt.internal.debug.ui.actions.ExpressionInspectAction;
import org.rubypeople.rdt.internal.debug.ui.evaluation.EvaluationExpression;


public class RubyEditorPopupMenuExtension extends ActionGroup {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {

        // 
		super.fillContextMenu(menu);
        MenuManager subMenu = new MenuManager("Inspect...", "group.inspect.template");
        //ISelection sel= getContext().getSelection();
        EvaluationExpression[] expressions = RdtDebugUiPlugin.getDefault().getEvaluationExpressionModel().getEvaluationExpressions() ;
        for (int i = 0; i < expressions.length; i++) {
            ExpressionInspectAction action = new ExpressionInspectAction(expressions[i], this.getContext().getSelection()) ;
            subMenu.add(action) ;			
		}
        
        
        if (!subMenu.isEmpty()) {
            menu.appendToGroup(ITextEditorActionConstants.GROUP_REST, subMenu);
        }
	}
}
