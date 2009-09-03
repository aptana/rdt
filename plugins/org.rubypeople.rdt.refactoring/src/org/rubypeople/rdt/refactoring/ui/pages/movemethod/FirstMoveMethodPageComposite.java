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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.rubypeople.rdt.refactoring.core.movemethod.MoveMethodConfig;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;

public class FirstMoveMethodPageComposite extends Composite {

	private MoveMethodConfig config;
	
	public FirstMoveMethodPageComposite(Composite parent, MoveMethodConfig config) {
		super(parent, SWT.NONE);
		this.config = config;
		initialize();
	}

	private void initialize() {
        GridLayout gridLayout = new GridLayout(2, false);
        setLayout(gridLayout);
        createSelectionGroup();
        createClassSelection();
        createLeaveDelegateMethodCheck();
			
	}

	private void createSelectionGroup() {
		String selectedMethodName = config.getMethodNode().getName();
		String selectedMethodVisibility = VisibilityNodeWrapper.getVisibilityName(config.getMethodVisibility());
		String selectedClassName = config.getSourceClassNode().getName();
		
		Group group = new Group(this, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.FirstMoveMethodPageComposite_Selection);
		group.setLayoutData(getGridData(2, true));
		Label selectedMethodLabel = new Label(group, SWT.NONE);
		selectedMethodLabel.setText(Messages.FirstMoveMethodPageComposite_SelectedMethod + selectedMethodName);
		Label visibilityLabel = new Label(group, SWT.NONE);
		visibilityLabel.setText(Messages.FirstMoveMethodPageComposite_Visibility + selectedMethodVisibility);
		visibilityLabel.setLayoutData(getGridData(1, true));
		Label selcetecClassLabel = new Label(group, SWT.NONE);
		selcetecClassLabel.setText(Messages.FirstMoveMethodPageComposite_SelectedClass + selectedClassName);
		selcetecClassLabel.setLayoutData(getGridData(1, true));
	}

	private GridData getGridData(int span, boolean fill) {
		GridData gridData = new GridData();
		if(span > 1) {
			gridData.horizontalSpan = span;
		}
		if(fill) {
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
		}
		return gridData;
	}

	private void createClassSelection() {
        Label moveToClassLabel = new Label(this, SWT.NONE);
        moveToClassLabel.setText(Messages.FirstMoveMethodPageComposite_MoveToClass);
        final Combo classSelectionCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        classSelectionCombo.setVisibleItemCount(10);
		
		for(String aktClassName :config.getTargetClassNames()) {
			if(config.getDestinationClassNode() == null) {
				config.setDestinationClassNode(aktClassName);
			}
			classSelectionCombo.add(aktClassName);
		}
		classSelectionCombo.select(0);
		classSelectionCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				config.setDestinationClassNode(classSelectionCombo.getText());
			}});
	}
	
	private void createLeaveDelegateMethodCheck() {
		final Button delegateCheck = new Button(this, SWT.CHECK);
		delegateCheck.setLayoutData(getGridData(2, true));
		delegateCheck.setText(Messages.FirstMoveMethodPageComposite_LeaveDelegate + config.getSourceClassNode().getName() + Messages.FirstMoveMethodPageComposite_DelegatesCalls
				+ config.getMethodNode().getName() + "\"."); //$NON-NLS-1$
		delegateCheck.setSelection(config.leaveDelegateMethodInSource());
		if(config.canCreateDelegateMethod()) {
			delegateCheck.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {}

				public void widgetSelected(SelectionEvent e) {
					config.setLeaveDelegateMethodInSource(delegateCheck.getSelection());
				}});
		} else {
			delegateCheck.setEnabled(false);
		}
	}

}
