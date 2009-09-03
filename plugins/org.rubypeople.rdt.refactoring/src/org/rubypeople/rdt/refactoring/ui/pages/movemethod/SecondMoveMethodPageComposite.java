/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.refactoring.ui.pages.movemethod;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rubypeople.rdt.refactoring.core.movemethod.MoveMethodConfig;

public class SecondMoveMethodPageComposite extends Composite implements Observer {

	private MoveMethodConfig config;

	private Label infoTextLabel;

	public SecondMoveMethodPageComposite(Composite parent, MoveMethodConfig config) {
		super(parent, SWT.NONE);
		this.config = config;
		config.addObserver(this);
		setLayout(new GridLayout(1, true));
		init();
	}

	private void init() {
		initInformationText();
		initFieldSelection();
	}

	private void initInformationText() {
		infoTextLabel = new Label(this, SWT.WRAP);
		infoTextLabel.setLayoutData(getGridData(true));
	}

	private GridData getGridData(boolean grab) {
		GridData gridData = new GridData();
		if (grab) {
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
		}
		return gridData;
	}

	private void initFieldSelection() {

		final Combo fieldSelectionCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		fieldSelectionCombo.setVisibleItemCount(10);
		
		fieldSelectionCombo.setLayoutData(getGridData(false));
		for (String fieldName : config.getFieldInSourceClassOfTypeDestinationClassNames()) {
			if (config.getFieldInSourceClassOfTypeDestinationClass() == null) {
				config.setFieldInSourceClassOfTypeDestinationClass(fieldName);
				fieldSelectionCombo.setText(fieldName);
			}
			fieldSelectionCombo.add(fieldName);
		}
		fieldSelectionCombo.select(0);
		fieldSelectionCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				config.setFieldInSourceClassOfTypeDestinationClass(fieldSelectionCombo.getText());
			}
		});
	}

	public void update(Observable arg0, Object arg1) {
		String selectedMethodName = config.getMethodNode().getName();
		String sourceClassName = config.getSourceClassNode().getName();
		String destClassName = config.getDestinationClassNode().getName();
		String infoText = Messages.SecondMoveMethodPageComposite_MaintainCalls + selectedMethodName + Messages.SecondMoveMethodPageComposite_FieldReference + destClassName
				+ Messages.SecondMoveMethodPageComposite_RequiredInClass + sourceClassName + Messages.SecondMoveMethodPageComposite_SelectField;
		infoTextLabel.setText(infoText);
	}
}
