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

package org.rubypeople.rdt.refactoring.ui.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.rubypeople.rdt.refactoring.core.convertlocaltofield.LocalToFieldConverter;
import org.rubypeople.rdt.refactoring.ui.LabeledTextField;
import org.rubypeople.rdt.refactoring.util.NameValidator;

public class ConvertLocalToFieldPage extends RefactoringWizardPage {

	private static final String TITLE = Messages.ConvertTempToFieldPage_ConvertLocalVariableToField;

	private LocalToFieldConverter converter;

	private ConverterPageParameters pageParameters;

	public ConvertLocalToFieldPage(LocalToFieldConverter converter, ConverterPageParameters parameters) {
		super(TITLE);
		this.converter = converter;
		this.pageParameters = parameters;
	}

	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.None);
		setControl(control);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 8;
		control.setLayout(layout);

		final LabeledTextField labeledText = new LabeledTextField(control, Messages.ConvertTempToFieldPage_FieldName, converter.getLocalVarName());
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		labeledText.setLayoutData(layoutData);
		labeledText.getText().addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String newName = labeledText.getText().getText();
				converter.setNewName(newName);
				checkInput(newName);
			}

			private void checkInput(String newName) {
				if (NameValidator.isValidLocalVariableName(newName)) {
					ConvertLocalToFieldPage.this.setMessage(null);
					ConvertLocalToFieldPage.this.setPageComplete(true);
				} else {
					ConvertLocalToFieldPage.this.setMessage("'" + newName + Messages.ConvertTempToFieldPage_IsNotValid, ConvertLocalToFieldPage.ERROR); //$NON-NLS-1$
					ConvertLocalToFieldPage.this.setPageComplete(false);
				}

			}
		});

		initInitializeRadioGroup(control);

		final Button declareClassField = new Button(control, SWT.CHECK);
		GridData checkData = new GridData();
		declareClassField.setLayoutData(checkData);
		declareClassField.setText(Messages.ConvertTempToFieldPage_DeclareAsClassField);
		declareClassField.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				converter.setIsClassField(declareClassField.getSelection());
			}
		});
	}

	private void initInitializeRadioGroup(Composite control) {
		Group initializeInGroup = new Group(control, SWT.NONE);
		RowLayout radioGroupLayout = new RowLayout(SWT.VERTICAL);
		initializeInGroup.setLayout(radioGroupLayout);
		initializeInGroup.setText(Messages.ConvertTempToFieldPage_InitializeIn);

		Button inCurrentMethod = new Button(initializeInGroup, SWT.RADIO);
		inCurrentMethod.setText(Messages.ConvertTempToFieldPage_CurrentMethod);
		inCurrentMethod.setEnabled(pageParameters.isInCurrentMethodRadioEnabled());
		addRadioListener(inCurrentMethod, LocalToFieldConverter.INIT_IN_METHOD);
		inCurrentMethod.setSelection(true);

		Button inClassConstructor = new Button(initializeInGroup, SWT.RADIO);
		inClassConstructor.setText(Messages.ConvertTempToFieldPage_ClassConstructor);
		inClassConstructor.setEnabled(pageParameters.isInClassConstructorRadioEnabled());
		addRadioListener(inClassConstructor, LocalToFieldConverter.INIT_IN_CONSTRUCTOR);

		GridData groupData = new GridData(GridData.FILL_HORIZONTAL);
		initializeInGroup.setLayoutData(groupData);
	}

	private void addRadioListener(Button button, final int initPlace) {
		button.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				converter.setInitPlace(initPlace);
			}
		});
	}
}
