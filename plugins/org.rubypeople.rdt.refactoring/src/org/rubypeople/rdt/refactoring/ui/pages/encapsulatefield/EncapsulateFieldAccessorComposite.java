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

package org.rubypeople.rdt.refactoring.ui.pages.encapsulatefield;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;

public class EncapsulateFieldAccessorComposite extends Group {

	private Button enableDisableCheckBox;

	private IVisibilitySelectionListener visibilitySelectionListener;

	private VisibilityNodeWrapper.METHOD_VISIBILITY selectedVisibility;

	private Button publicButton;

	private Button protectedButton;

	private Button privateButton;

	public EncapsulateFieldAccessorComposite(Composite parent, String name, VisibilityNodeWrapper.METHOD_VISIBILITY selectedVisibility, boolean isOptional) {
		super(parent, SWT.NONE);

		this.selectedVisibility = selectedVisibility;
		setLayoutData(getDefaultGridData());
		setLayout(new GridLayout(1, true));
		setText(name);

		if (isOptional) {
			initEnableDisableCheckBox(name);
		}
		initAccessModifierGroup();
	}

	private void initAccessModifierGroup() {
		Group accessModifierGroup = new Group(this, SWT.SHADOW_NONE | SWT.NONE);
		RowLayout groupLayout = new RowLayout(SWT.HORIZONTAL);
		groupLayout.fill = true;
		accessModifierGroup.setLayout(groupLayout);
		accessModifierGroup.setLayoutData(getDefaultGridData());
		accessModifierGroup.setText(Messages.EncapsulateFieldAccessorComposite_AccessModifier);

		publicButton = initAccessModifierButton(accessModifierGroup, "public", VisibilityNodeWrapper.METHOD_VISIBILITY.PUBLIC); //$NON-NLS-1$
		protectedButton = initAccessModifierButton(accessModifierGroup, "protected", VisibilityNodeWrapper.METHOD_VISIBILITY.PROTECTED); //$NON-NLS-1$
		privateButton = initAccessModifierButton(accessModifierGroup, "private", VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE); //$NON-NLS-1$
	}

	private GridData getDefaultGridData() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		return gridData;
	}

	private Button initAccessModifierButton(Group accessModifierGroup, String accessModifierName, final VisibilityNodeWrapper.METHOD_VISIBILITY visibility) {
		Button accessModifierButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		accessModifierButton.setText(accessModifierName);
		if (visibility.equals(selectedVisibility)) {
			accessModifierButton.setSelection(true);
		}
		accessModifierButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				visibilitySelectionListener.visibilitySelected(visibility);
			}
		});
		return accessModifierButton;
	}

	private void initEnableDisableCheckBox(String name) {
		enableDisableCheckBox = new Button(this, SWT.CHECK);
		enableDisableCheckBox.setLayoutData(getDefaultGridData());
		enableDisableCheckBox.setText(Messages.EncapsulateFieldAccessorComposite_Generate + name);
	}

	public void addEnableDisableListener(SelectionListener listener) {
		enableDisableCheckBox.addSelectionListener(listener);
	}

	public boolean isDisabled() {

		return (enableDisableCheckBox != null) ? !enableDisableCheckBox.getSelection() : false;
	}

	public void addVisibilitySelectionListener(IVisibilitySelectionListener visibilitySelectionListener) {
		this.visibilitySelectionListener = visibilitySelectionListener;
	}

	@Override
	protected void checkSubclass() {
	}

	public void enableVisibilityGroup(boolean enabled) {
		publicButton.setEnabled(enabled);
		protectedButton.setEnabled(enabled);
		privateButton.setEnabled(enabled);
	}
}
