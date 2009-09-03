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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rubypeople.rdt.refactoring.core.inlinelocal.InlineLocalConfig;
import org.rubypeople.rdt.refactoring.core.inlinelocal.InlineLocalRefactoring;
import org.rubypeople.rdt.refactoring.ui.LabeledTextField;
import org.rubypeople.rdt.refactoring.util.NameValidator;

public class InlineLocalPage extends RefactoringWizardPage {

	private InlineLocalConfig config;

	private int occurencesCount;

	private String selectedItemName;

	private LabeledTextField newMethodName;

	private Button checkQuery;

	public InlineLocalPage(InlineLocalConfig config, int occurencesCount, String selectedItemName) {
		super(InlineLocalRefactoring.NAME + "..."); //$NON-NLS-1$

		this.config = config;
		this.occurencesCount = occurencesCount;
		this.selectedItemName = selectedItemName;
	}

	@Override
	public void pageIsEnabled() {
		super.pageIsEnabled();
		newMethodName.setEnabled(checkQuery.getSelection());
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		setControl(control);
		initPage(control);
	}

	private void initPage(Composite control) {

		GridLayout baseLayout = new GridLayout();
		baseLayout.numColumns = 1;
		baseLayout.verticalSpacing = 25;
		control.setLayout(baseLayout);

		initLabel(control);
		initExtractArea(control);
	}

	private void initExtractArea(Composite control) {
		Group queryGroup = initGroup(control);

		checkQuery = new Button(queryGroup, SWT.CHECK);
		checkQuery.setText(Messages.InlineTempPage_ReplaceTempWithQuery);
		checkQuery.setEnabled(true);

		newMethodName = new LabeledTextField(queryGroup, Messages.InlineTempPage_NewMethodName);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		newMethodName.setLayoutData(textData);

		createSelectionListener(checkQuery, newMethodName);
		createModifyListener(newMethodName.getText());
	}

	private void createModifyListener(final Text newMethodName) {
		
		newMethodName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String newName = newMethodName.getText();
				config.setNewMethodName(newName);

				checkInput(newName);
			}

			private void checkInput(String newName) {
				if (NameValidator.isValidMethodName(newName)) {
					InlineLocalPage.this.setMessage(null);
					InlineLocalPage.this.setPageComplete(true);
				} else {
					InlineLocalPage.this.setMessage("'" + newName + Messages.InlineTempPage_IsNotValidName, ConvertLocalToFieldPage.ERROR); //$NON-NLS-1$
					InlineLocalPage.this.setPageComplete(false);
				}

			}

		});
	}

	private void createSelectionListener(final Button checkQuery, final LabeledTextField newMethodName) {
		checkQuery.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				boolean doReplaceTempWithQuery = checkQuery.getSelection();
				newMethodName.setEnabled(doReplaceTempWithQuery);
				config.setReplaceTempWithQuery(doReplaceTempWithQuery);
				if (!doReplaceTempWithQuery) {
					setMessage(null);
				}
				setPageComplete(!doReplaceTempWithQuery);
			}
		});
	}

	private Group initGroup(Composite control) {
		Group queryGroup = new Group(control, SWT.NONE);
		queryGroup.setText(Messages.InlineTempPage_ExtractToMethod);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 1;
		groupLayout.verticalSpacing = 15;

		queryGroup.setLayout(groupLayout);
		GridData groupData = new GridData(GridData.FILL_HORIZONTAL);
		queryGroup.setLayoutData(groupData);
		return queryGroup;
	}

	private void initLabel(Composite control) {
		Label infoLabel = new Label(control, SWT.NONE);
		infoLabel.setText(Messages.InlineTempPage_Replace + occurencesCount + Messages.InlineTempPage_Occurences + selectedItemName + "'."); //$NON-NLS-1$
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		infoLabel.setLayoutData(layoutData);
	}

}
