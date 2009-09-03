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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.rubypeople.rdt.refactoring.core.inlineclass.InlineClassConfig;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;

public class InlineClassPage extends RefactoringWizardPage {


	public static final String TITLE = Messages.InlineClassPage_InlineTemp;
	private InlineClassConfig config;

	public InlineClassPage(InlineClassConfig config) {
		super(TITLE);
		this.config = config;
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		initBaseLayout(control);
		initList(control);
		
		setControl(control);
		
	}

	private void initBaseLayout(Composite control) {
		GridLayout baseLayout = new GridLayout();
		baseLayout.numColumns = 1;
		
		control.setLayout(baseLayout);
	}

	private void initList(Composite control) {
		RowLayout panelLayout = new RowLayout(SWT.VERTICAL);
		panelLayout.wrap = false;
		

		
		Label selectText = new Label(control, SWT.NONE);
		selectText.setText(Messages.InlineClassPage_SelectTargetClass);
		
		List classList = new List(control, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		setListLayout(classList);
		
		fillList(classList);
				
		initSelectionListener(classList);
	}

	private void fillList(List classList) {
		for(ClassNodeWrapper currentClass : config.getPossibleTargetClasses()){
			classList.add(currentClass.getName());
		}
	}

	private void setListLayout(List classList) {
		GridData listData = new GridData();
		listData.grabExcessHorizontalSpace = true;
		listData.grabExcessVerticalSpace = true;
		listData.horizontalAlignment = GridData.FILL;
		listData.verticalAlignment = GridData.FILL;
		classList.setLayoutData(listData);
	}

	private void initSelectionListener(final List classList) {
		
		classList.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String selection = classList.getSelection()[0];
				ClassNodeWrapper selectedClass = config.getDocumentProvider().getClassNodeProvider().getClassNode(selection);
				config.setTargetClassPart(selectedClass.getFirstPartialClassNode());
			}	
		});
	}

}
