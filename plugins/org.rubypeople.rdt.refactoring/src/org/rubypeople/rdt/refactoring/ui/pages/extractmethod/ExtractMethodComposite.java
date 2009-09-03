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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.ui.pages.extractmethod;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.rubypeople.rdt.refactoring.ui.LabeledTextField;
import org.rubypeople.rdt.refactoring.ui.RdtCodeViewer;

public class ExtractMethodComposite extends Composite {

	private Group accessModifierGroup;

	private Composite modifySelectionEditorComposite;

	private Label modifySelectionLabel;

	private Button privateAccessRadioButton;

	private Button protectedAccessRadioButton;

	private Button publicAccessRadioButton;

	private Label methodSignaturePreviewLabel;

	private Label methodSignatureLabel;

	private Button replaceAllCheckbox;

	private Label parametersLabel;

	private Button editParametersButton;

	private TableColumn parametersTableNameColumns;

	private Table parametersTable;

	private Button downParametersButton;

	private Button upParametersButton;

	private LabeledTextField newMethodName;

	private RdtCodeViewer selectionPreview;

	private final IValidationController validationController;

	private ParametersTableCellEditorListener cellEditorListener;

	private Button noneAccessRadioButton;

	public ExtractMethodComposite(Composite parent, IValidationController validationController, boolean hasParameters, boolean needsAccessModifiers) {
		super(parent, SWT.NONE);
		this.validationController = validationController;
		setLayout(new GridLayout());

		createNewMethodNameComposite(this);
		if (needsAccessModifiers) {
			createAccessModifierComposite(this);
		}
		if (hasParameters) {
			createParametersComposite(this);
		}
		createMethodSignatureComposite(this);
		createModifySelectionComposite(this);

		layout();
	}

	public LabeledTextField getNewMethodNameText() {
		return newMethodName;
	}

	private void createModifySelectionComposite(Composite control) {
		Composite modifySelectionComposite = new Composite(control, SWT.FLAT);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.makeColumnsEqualWidth = true;
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		compositeLData.grabExcessVerticalSpace = true;
		compositeLData.verticalAlignment = GridData.FILL;
		modifySelectionComposite.setLayoutData(compositeLData);
		modifySelectionComposite.setLayout(compositeLayout);

		modifySelectionLabel = new Label(modifySelectionComposite, SWT.NONE);
		GridData labelLData = new GridData();
		labelLData.verticalAlignment = GridData.BEGINNING;
		modifySelectionLabel.setLayoutData(labelLData);
		modifySelectionLabel.setText(Messages.ExtractMethodComposite_SelectedCode);

		modifySelectionEditorComposite = new Composite(modifySelectionComposite, SWT.NONE);
		FormLayout composite8Layout = new FormLayout();
		GridData composite8LData = new GridData();
		composite8LData.horizontalAlignment = GridData.FILL;
		composite8LData.grabExcessHorizontalSpace = true;
		composite8LData.grabExcessVerticalSpace = true;
		composite8LData.verticalAlignment = GridData.FILL;
		modifySelectionEditorComposite.setLayoutData(composite8LData);
		modifySelectionEditorComposite.setLayout(composite8Layout);

		selectionPreview = RdtCodeViewer.create(modifySelectionEditorComposite);
		FormData textLData = new FormData(0, 0);
		textLData.right = new FormAttachment(1000, 1000, 0);
		textLData.left = new FormAttachment(0, 1000, 0);
		textLData.top = new FormAttachment(0, 1000, 0);
		textLData.bottom = new FormAttachment(1000, 1000, 0);
		selectionPreview.getTextWidget().setLayoutData(textLData);

		Composite modifySelectionButtonComposite = new Composite(modifySelectionEditorComposite, SWT.NONE);
		GridLayout composite9Layout = new GridLayout();
		composite9Layout.makeColumnsEqualWidth = true;
		composite9Layout.marginHeight = 0;
		FormData composite9LData = new FormData();
		composite9LData.width = 80;
		composite9LData.right = new FormAttachment(1000, 1000, 0);
		composite9LData.top = new FormAttachment(0, 1000, 0);
		composite9LData.bottom = new FormAttachment(1005, 1000, 0);
		modifySelectionButtonComposite.setLayoutData(composite9LData);
		modifySelectionButtonComposite.setLayout(composite9Layout);

		Label selectionHelpLabel = new Label(modifySelectionComposite, SWT.NONE);
		GridData labelData = new GridData();
		labelData.verticalAlignment = GridData.BEGINNING;
		selectionHelpLabel.setLayoutData(labelData);

		FontData fontData = selectionHelpLabel.getFont().getFontData()[0];
		Font font = new Font(getFont().getDevice(), fontData.getName(), fontData.getHeight(), fontData.getStyle() | SWT.ITALIC);
		selectionHelpLabel.setFont(font);

		selectionHelpLabel.setText(Messages.ExtractMethodComposite_ExpansionHint);
	}

	private Button createButton(Composite parent, String name) {
		Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
		GridData buttonLData = new GridData();
		buttonLData.horizontalAlignment = GridData.FILL;
		buttonLData.grabExcessHorizontalSpace = true;
		button.setLayoutData(buttonLData);
		button.setText(name);
		return button;
	}

	private void createMethodSignatureComposite(Composite control) {
		Composite methodSignatureComposite = new Composite(control, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.makeColumnsEqualWidth = true;
		GridData compositeLData = new GridData();
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessHorizontalSpace = true;
		methodSignatureComposite.setLayoutData(compositeLData);
		methodSignatureComposite.setLayout(compositeLayout);

		methodSignatureLabel = new Label(methodSignatureComposite, SWT.NONE);
		GridData labelLData = new GridData();
		labelLData.horizontalAlignment = GridData.FILL;
		labelLData.grabExcessHorizontalSpace = true;
		methodSignatureLabel.setLayoutData(labelLData);
		methodSignatureLabel.setText(Messages.ExtractMethodComposite_SignaturePreview);

		methodSignaturePreviewLabel = new Label(methodSignatureComposite, SWT.NONE);
		GridData methodSignaturePreviewData = new GridData();
		methodSignaturePreviewData.horizontalAlignment = GridData.FILL;
		methodSignaturePreviewData.grabExcessHorizontalSpace = true;
		methodSignaturePreviewData.verticalAlignment = GridData.FILL;
		methodSignaturePreviewData.grabExcessVerticalSpace = true;
		methodSignaturePreviewLabel.setLayoutData(methodSignaturePreviewData);
		methodSignaturePreviewLabel.setText("def"); //$NON-NLS-1$

	}

	private void createParametersComposite(Composite control) {

		Composite parametersComposite = new Composite(control, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.makeColumnsEqualWidth = true;
		GridData compositeLData = new GridData();
		compositeLData.grabExcessHorizontalSpace = true;
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessVerticalSpace = true;
		compositeLData.verticalAlignment = GridData.FILL;
		parametersComposite.setLayoutData(compositeLData);
		parametersComposite.setLayout(compositeLayout);

		createParametersLabel(parametersComposite);
		createParametersTable(parametersComposite);

		replaceAllCheckbox = new Button(parametersComposite, SWT.CHECK | SWT.LEFT);
		GridData buttonLData = new GridData();
		buttonLData.grabExcessHorizontalSpace = true;
		buttonLData.horizontalAlignment = GridData.FILL;
		replaceAllCheckbox.setLayoutData(buttonLData);
		replaceAllCheckbox.setText(Messages.ExtractMethodComposite_ReplaceAll);
		replaceAllCheckbox.setEnabled(false);
	}

	private void createParametersButton(Composite parametersTableComposite) {
		Composite parametersButtonComposite = new Composite(parametersTableComposite, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.makeColumnsEqualWidth = true;
		FormData compositeLData = new FormData(0, 0);
		compositeLData.width = 80;
		compositeLData.bottom = new FormAttachment(1000, 1000, 0);
		compositeLData.right = new FormAttachment(1000, 1000, 0);
		compositeLData.top = new FormAttachment(0, 1000, 0);
		parametersButtonComposite.setLayoutData(compositeLData);
		parametersButtonComposite.setLayout(compositeLayout);

		editParametersButton = createButton(parametersButtonComposite, Messages.ExtractMethodComposite_ButtonEdit);
		editParametersButton.setEnabled(false);
		upParametersButton = createButton(parametersButtonComposite, Messages.ExtractMethodComposite_ButtonUp);
		upParametersButton.setEnabled(false);
		downParametersButton = createButton(parametersButtonComposite, Messages.ExtractMethodComposite_ButtonDown);
		downParametersButton.setEnabled(false);
	}

	private Composite createParametersTable(Composite parametersComposite) {
		Composite parametersTableComposite = new Composite(parametersComposite, SWT.NONE);
		FormLayout compositeLayout = new FormLayout();
		GridData compositeLData = new GridData();
		compositeLData.grabExcessHorizontalSpace = true;
		compositeLData.horizontalAlignment = GridData.FILL;
		compositeLData.grabExcessVerticalSpace = true;
		compositeLData.verticalAlignment = GridData.FILL;
		parametersTableComposite.setLayoutData(compositeLData);
		parametersTableComposite.setLayout(compositeLayout);

		parametersTable = new Table(parametersTableComposite, SWT.BORDER);
		FormData tableLData = new FormData();
		tableLData.bottom = new FormAttachment(1000, 1000, 0);
		tableLData.left = new FormAttachment(0, 1000, 0);
		tableLData.right = new FormAttachment(1000, 1000, -80);
		tableLData.top = new FormAttachment(0, 1000, 4);
		parametersTable.setLayoutData(tableLData);
		parametersTable.setHeaderVisible(true);

		parametersTableNameColumns = new TableColumn(parametersTable, SWT.NONE);
		parametersTableNameColumns.setText(Messages.ExtractMethodComposite_Name);
		parametersTableNameColumns.setWidth(60);

		createParametersButton(parametersTableComposite);

		cellEditorListener = new ParametersTableCellEditorListener(parametersTable, validationController);

		parametersTable.addListener(SWT.MouseDoubleClick, cellEditorListener);
		editParametersButton.addListener(SWT.Selection, cellEditorListener);

		return parametersTableComposite;
	}

	private void createParametersLabel(Composite parametersComposite) {
		parametersLabel = new Label(parametersComposite, SWT.NONE);
		GridData labelLData = new GridData();
		labelLData.grabExcessHorizontalSpace = true;
		labelLData.horizontalAlignment = GridData.FILL;
		parametersLabel.setLayoutData(labelLData);
		parametersLabel.setText(Messages.ExtractMethodComposite_Parameters);
	}

	private void createAccessModifierComposite(Composite control) {
		Composite accessModifierComposite = new Composite(control, SWT.NONE);
		FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
		GridData compositeLData = new GridData();
		compositeLData.grabExcessHorizontalSpace = true;
		compositeLData.horizontalAlignment = GridData.FILL;
		accessModifierComposite.setLayoutData(compositeLData);
		accessModifierComposite.setLayout(compositeLayout);

		accessModifierGroup = new Group(accessModifierComposite, SWT.SHADOW_NONE | SWT.NONE);
		RowLayout groupLayout = new RowLayout(SWT.HORIZONTAL);
		groupLayout.fill = true;
		accessModifierGroup.setLayout(groupLayout);
		accessModifierGroup.setText(Messages.ExtractMethodComposite_AccessModifier);

		publicAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		publicAccessRadioButton.setText("public"); //$NON-NLS-1$

		protectedAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		protectedAccessRadioButton.setText("protected"); //$NON-NLS-1$

		privateAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		privateAccessRadioButton.setText("private"); //$NON-NLS-1$

		noneAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		noneAccessRadioButton.setText(Messages.ExtractMethodComposite_SameAsSource);
	}

	private void createNewMethodNameComposite(Composite control) {
		Composite methodNameComposite = new Composite(control, SWT.NONE);
		FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		methodNameComposite.setLayoutData(gridData);
		methodNameComposite.setLayout(compositeLayout);
		newMethodName = new LabeledTextField(methodNameComposite, Messages.ExtractMethodComposite_MethodName);
	}

	public RdtCodeViewer getCodeViewer() {
		return selectionPreview;
	}

	public Table getParametersTable() {
		return parametersTable;
	}

	public Button getDownParametersButton() {
		return downParametersButton;
	}

	public Button getEditParametersButton() {
		return editParametersButton;
	}

	public Button getUpParametersButton() {
		return upParametersButton;
	}

	public ParametersTableCellEditorListener getCellEditorListener() {
		return cellEditorListener;
	}

	public Button getPrivateAccessRadioButton() {
		return privateAccessRadioButton;
	}

	public Button getProtectedAccessRadioButton() {
		return protectedAccessRadioButton;
	}

	public Button getPublicAccessRadioButton() {
		return publicAccessRadioButton;
	}

	public Label getMethodSignaturePreviewLabel() {
		return methodSignaturePreviewLabel;
	}

	public Button getNoneAccessRadioButton() {
		return noneAccessRadioButton;
	}
}
