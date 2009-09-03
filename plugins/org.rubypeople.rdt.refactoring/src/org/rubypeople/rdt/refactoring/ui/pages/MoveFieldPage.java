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

package org.rubypeople.rdt.refactoring.ui.pages;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rubypeople.rdt.refactoring.core.movefield.MoveFieldConfig;
import org.rubypeople.rdt.refactoring.core.movefield.MoveFieldRefactoring;

public class MoveFieldPage extends RefactoringWizardPage {

	private final class ReferenceSelectionListener implements SelectionListener {
		private final Combo combo;

		public ReferenceSelectionListener(final Combo combo) {
			this.combo = combo;
		}

		public void widgetDefaultSelected(final SelectionEvent event) {
		}

		public void widgetSelected(final SelectionEvent event) {
			config.setTargetReference(combo.getText());
		}
	}

	private final class ClassNameSelectionListener implements SelectionListener {
		private final Combo combo;

		public ClassNameSelectionListener(final Combo combo) {
			this.combo = combo;
		}

		public void widgetDefaultSelected(final SelectionEvent event) {}

		public void widgetSelected(final SelectionEvent event) {
			config.setTargetClass(combo.getText());
		}
	}

	private final MoveFieldConfig config;

	public MoveFieldPage(final MoveFieldConfig config) {
		super(MoveFieldRefactoring.NAME);
		this.config = config;
		setTitle(MoveFieldRefactoring.NAME);
	}

	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.None);
		composite.setLayout(new GridLayout(2, false));
		
		createTitleLabel(composite);
		createClassLabel(composite);
		createClassComboBox(composite);
		createReferenceLabel(composite);
		createReferenceComboBox(composite);
		
		setControl(composite);
	}

	private void createReferenceComboBox(final Composite composite) {
		final Combo combobox = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combobox.setVisibleItemCount(10);
		
		for (String fieldName : config.getReferenceCandidates()) {
			if (config.getTargetReference() == null) {
				config.setTargetReference(fieldName);
				combobox.setText(fieldName);
			}
			combobox.add(fieldName);
		}
		combobox.select(0);
		combobox.addSelectionListener(new ReferenceSelectionListener(combobox));
	}

	private void createReferenceLabel(final Composite composite) {
		final Label referenceLabel = new Label(composite, SWT.NONE);
		// what should we ask?
		//referenceLabel.setText("Class is referenced by:");
		referenceLabel.setText(Messages.MoveFieldPage_AccessibleBy);
	}

	private void createClassComboBox(final Composite composite) {
		final Combo combobox = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combobox.setVisibleItemCount(10);
		
		for(String name : config.getTargetClassCandidates()) {
			if(config.getTargetClass() == null) {
				config.setTargetClass(name);
			}
			combobox.add(name);
		}
		combobox.select(0);
		combobox.addSelectionListener(new ClassNameSelectionListener(combobox));
	}

	private void createClassLabel(final Composite composite) {
		final Label moveToClassLabel = new Label(composite, SWT.NONE);
		moveToClassLabel.setText(Messages.MoveFieldPage_MoveToClass);
	}

	private void createTitleLabel(final Composite composite) {
		final Label title = new Label(composite, SWT.NONE);
		title.setText(Messages.MoveFieldPage_Target + config.getSelectedFieldName() + "':"); //$NON-NLS-1$
		final GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		title.setLayoutData(gridData);
	}
}
