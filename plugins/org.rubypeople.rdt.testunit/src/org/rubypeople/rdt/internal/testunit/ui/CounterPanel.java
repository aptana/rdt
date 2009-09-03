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


import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel with counters for the number of Runs, Errors and Failures.
 */
public class CounterPanel extends Composite {
	protected Label fNumberOfErrors;
	protected Label fNumberOfFailures;
	protected Label fNumberOfRuns;
	protected int fTotal;
	
	private final Image fErrorIcon= TestUnitView.createImage("ovr16/error_ovr.gif"); //$NON-NLS-1$
	private final Image fFailureIcon= TestUnitView.createImage("ovr16/failed_ovr.gif"); //$NON-NLS-1$
			
	public CounterPanel(Composite parent) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout= new GridLayout();
		gridLayout.numColumns= 9;
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth= 0;
		setLayout(gridLayout);
		
		fNumberOfRuns= createLabel(TestUnitMessages.CounterPanel_label_runs, null, " 0/0  ");  //$NON-NLS-1$
		fNumberOfErrors= createLabel(TestUnitMessages.CounterPanel_label_errors, fErrorIcon, " 0 "); //$NON-NLS-1$
		fNumberOfFailures= createLabel(TestUnitMessages.CounterPanel_label_failures, fFailureIcon, " 0 "); //$NON-NLS-1$

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});
	}
 
	private void disposeIcons() {
		fErrorIcon.dispose();
		fFailureIcon.dispose();
	}

	private Label createLabel(String name, Image image, String init) {
		Label label= new Label(this, SWT.NONE);
		if (image != null) {
			image.setBackground(label.getBackground());
			label.setImage(image);
		}
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label= new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		label.setFont(JFaceResources.getBannerFont());
		
		Label value= new Label(this, SWT.READ_ONLY);
		value.setText(init);
		// bug: 39661 Junit test counters do not repaint correctly [JUnit] 
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		return value;
	}

	public void reset() {
		setErrorValue(0);
		setFailureValue(0);
		setRunValue(0);
		fTotal= 0;
	}
	
	public void setTotal(int value) {
		fTotal= value;
	}
	
	public int getTotal(){
		return fTotal;
	}
	
	public void setRunValue(int value) {
		String runString= TestUnitMessages.getFormattedString(TestUnitMessages.CounterPanel_runcount, new String[] { Integer.toString(value), Integer.toString(fTotal) });
		fNumberOfRuns.setText(runString);

		fNumberOfRuns.redraw();
		redraw();
	}
	
	public void setErrorValue(int value) {
		fNumberOfErrors.setText(Integer.toString(value));
		redraw();
	}
	
	public void setFailureValue(int value) {
		fNumberOfFailures.setText(Integer.toString(value));
		redraw();
	}
}
