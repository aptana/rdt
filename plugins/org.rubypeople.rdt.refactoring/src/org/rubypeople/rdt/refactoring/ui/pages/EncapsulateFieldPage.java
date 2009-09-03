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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.FieldEncapsulator;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.pages.encapsulatefield.EncapsulateFieldAccessorComposite;
import org.rubypeople.rdt.refactoring.ui.pages.encapsulatefield.IVisibilitySelectionListener;
import org.rubypeople.rdt.refactoring.ui.util.AbstractSelectionListener;

public class EncapsulateFieldPage extends RefactoringWizardPage {

	private static final String TITLE = Messages.EncapsulateFieldPage_Title;

	private FieldEncapsulator fieldEncapsulator;

	public EncapsulateFieldPage(FieldEncapsulator fieldEncapsulator) {
		super(TITLE);
		this.fieldEncapsulator = fieldEncapsulator;
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1, true));
		initFieldInfoGroup(control);
		initReaderAccessorControl(control);
		initWriterAccessorControl(control);

		setControl(control);
	}

	private void initWriterAccessorControl(Composite parent) {
		boolean isOptional = fieldEncapsulator.isWriterGenerationOptional();
		VisibilityNodeWrapper.METHOD_VISIBILITY visibility = fieldEncapsulator.getWriterVisibility();
		final EncapsulateFieldAccessorComposite accessorControl = new EncapsulateFieldAccessorComposite(parent, Messages.EncapsulateFieldPage_Writer, visibility, isOptional);
		accessorControl.enableVisibilityGroup(false);
		if (isOptional) {
			accessorControl.addEnableDisableListener(new AbstractSelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					fieldEncapsulator.setWriterDisabled(accessorControl.isDisabled());
					accessorControl.enableVisibilityGroup(!accessorControl.isDisabled());
				}
			});
		}
		accessorControl.addVisibilitySelectionListener(new IVisibilitySelectionListener() {

			public void visibilitySelected(VisibilityNodeWrapper.METHOD_VISIBILITY visibility) {
				fieldEncapsulator.setWriterVisibility(visibility);
			}
		});
	}

	private void initReaderAccessorControl(Composite parent) {
		boolean isOptional = fieldEncapsulator.isReaderGenerationOptional();
		VisibilityNodeWrapper.METHOD_VISIBILITY visibility = fieldEncapsulator.getReaderVisibility();
		final EncapsulateFieldAccessorComposite accessorControl = new EncapsulateFieldAccessorComposite(parent, Messages.EncapsulateFieldPage_Reader, visibility, isOptional);
		accessorControl.enableVisibilityGroup(false);
		if (isOptional) {
			accessorControl.addEnableDisableListener(new AbstractSelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					fieldEncapsulator.setReaderDisabled(accessorControl.isDisabled());
					accessorControl.enableVisibilityGroup(!accessorControl.isDisabled());
				}
			});
		}
		accessorControl.addVisibilitySelectionListener(new IVisibilitySelectionListener() {

			public void visibilitySelected(VisibilityNodeWrapper.METHOD_VISIBILITY visibility) {
				fieldEncapsulator.setReaderVisibility(visibility);
			}
		});
	}

	private void initFieldInfoGroup(Composite control) {
		Group fieldInfoGroup = new Group(control, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		fieldInfoGroup.setLayoutData(gridData);
		fieldInfoGroup.setLayout(new GridLayout(1, true));
		fieldInfoGroup.setText(Messages.EncapsulateFieldPage_SelectedField);
		Label fieldNameLabel = new Label(fieldInfoGroup, SWT.NONE);
		fieldNameLabel.setText(Messages.EncapsulateFieldPage_Name + fieldEncapsulator.getSelectedFieldName());
		Label existingAccessorLabel = new Label(fieldInfoGroup, SWT.NONE);
		existingAccessorLabel.setText(Messages.EncapsulateFieldPage_FieldAccessor + fieldEncapsulator.getExistingAccessorName());
	}

}
