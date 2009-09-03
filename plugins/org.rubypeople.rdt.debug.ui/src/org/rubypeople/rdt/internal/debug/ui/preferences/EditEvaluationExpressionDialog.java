/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 */

package org.rubypeople.rdt.internal.debug.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.evaluation.EvaluationExpression;
import org.rubypeople.rdt.internal.ui.dialogs.StatusDialog;

public class EditEvaluationExpressionDialog extends StatusDialog {
	protected EvaluationExpression evaluationExpression;
	protected Text txtName ;
    protected Text txtDescription ;
    protected Text txtExpression ;


	public EditEvaluationExpressionDialog(Shell parentShell, String aDialogTitle, EvaluationExpression expr) {
		super(parentShell);
		setTitle(aDialogTitle);
        evaluationExpression = expr ;
	}


	protected void okPressed() {
		evaluationExpression.setName(txtName.getText()) ;
        evaluationExpression.setDescription(txtDescription.getText()) ;
        evaluationExpression.setExpression(txtExpression.getText()) ;
		super.okPressed();
	}
    
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH)) ;

        new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.EditEvaluationExpression_name_label);
        txtName = new Text(composite, SWT.SINGLE | SWT.BORDER);
        txtName.setLayoutData(new GridData(GridData.FILL_BOTH));   
        txtName.setText(evaluationExpression.getName());
		
        new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.EditEvaluationExpression_description_label);
        txtDescription = new Text(composite, SWT.SINGLE | SWT.BORDER);
        txtDescription.setLayoutData(new GridData(GridData.FILL_BOTH)) ;
        txtDescription.setText(evaluationExpression.getDescription()) ;
        
        new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.EditEvaluationExpression_expression_label);
        txtExpression = new Text(composite, SWT.SINGLE | SWT.BORDER);
        txtExpression.setLayoutData(new GridData(GridData.FILL_BOTH)) ;
        txtExpression.setText(evaluationExpression.getExpression()) ;
        
		return composite;
	}

}