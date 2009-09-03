/*
 * Author: C.Williams
 * 
 * Copyright (c) 2004 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
 * can get copy of the GPL along with further information about RubyPeople and
 * third party software bundled with RDT in the file
 * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
 * http://www.rubypeople.org/RDT.license.
 * 
 * RDT is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.rubypeople.rdt.testunit.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.testunit.ui.TestUnitMessages;
import org.rubypeople.rdt.internal.ui.util.FileSelector;
import org.rubypeople.rdt.internal.ui.util.RubyProjectSelector;
import org.rubypeople.rdt.ui.RubyElementLabelProvider;

/**
 * @author Chris
 *  @deprecated Please do not use anymore
 */
public class RubyClassSelector {

	protected final static String EMPTY_STRING = "";
	private Composite composite;
	private Button browseButton;
	private Text textField;
	protected String browseDialogMessage = EMPTY_STRING;
	protected String browseDialogTitle = EMPTY_STRING;
	protected String validatedSelectionText = EMPTY_STRING;
	private FileSelector fileSelector;
	private RubyProjectSelector projectSelector;

	/**
	 * @param parent
	 * @param fileSelector
	 */
	public RubyClassSelector(Composite parent, FileSelector fileSelector, RubyProjectSelector projectSelector) {
		this.fileSelector = fileSelector;
		this.projectSelector = projectSelector;
		composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.marginWidth = 0;
		compositeLayout.marginHeight = 0;
		compositeLayout.numColumns = 2;
		composite.setLayout(compositeLayout);

		textField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validatedSelectionText = validateResourceSelection();
			}
		});

		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Search...");
		browseButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleBrowseSelected();
			}
		});

		browseDialogTitle = TestUnitMessages.RubyClassSelector_Title;

	}

	/**
	 *  
	 */
	protected void handleBrowseSelected() {
		IType[] types = getTypesInSelectedFile();
		if (types == null) types = getTypesInSelectedProject();
		if (types == null) types = getAllTypes();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new RubyElementLabelProvider());
		dialog.setElements(types);
		dialog.setTitle(browseDialogTitle);
		dialog.setMessage(browseDialogMessage);
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			textField.setText(((IRubyElement) dialog.getFirstResult()).getElementName());
		}
	}

	private IType[] getTypesInSelectedProject() {
		IProject rubyProject = projectSelector.getSelection();
		if (rubyProject == null) return null;
		return TestSearchEngine.findTests(rubyProject);
	}

	private IType[] getTypesInSelectedFile() {
		String relativeFilePath = fileSelector.getValidatedSelectionText();
		if (relativeFilePath == null || relativeFilePath.trim().length() == 0) return null;
		IProject rubyProject = projectSelector.getSelection();
		if (rubyProject == null) return null;
		IFile file = rubyProject.getFile(relativeFilePath);
		return TestSearchEngine.findTests(file);
	}

	/**
	 * @return
	 */
	private IType[] getAllTypes() {
		List<IType> typeList = new ArrayList<IType>();
		IProject[] projects = RubyCore.getRubyProjects();
		for (int i = 0; i < projects.length; i++) {
			IType[] types = TestSearchEngine.findTests(projects[i]);
			typeList.addAll(Arrays.asList(types));
		}
		IType[] allTypes = new IType[typeList.size()];
		System.arraycopy(typeList.toArray(), 0, allTypes, 0, allTypes.length);
		return allTypes;
	}

	/**
	 * @return
	 */
	protected String validateResourceSelection() {
		String selection = textField.getText();
		return selection == null ? EMPTY_STRING : selection;
	}

	protected Shell getShell() {
		return composite.getShell();
	}

	public void setLayoutData(Object layoutData) {
		composite.setLayoutData(layoutData);
	}

	public void addModifyListener(ModifyListener aListener) {
		textField.addModifyListener(aListener);
	}

	public void setBrowseDialogMessage(String aMessage) {
		browseDialogMessage = aMessage;
	}

	public void setBrowseDialogTitle(String aTitle) {
		browseDialogTitle = aTitle;
	}

	public void setEnabled(boolean enabled) {
		composite.setEnabled(enabled);
		textField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	public String getSelectionText() {
		return textField.getText();
	}

	public String getValidatedSelectionText() {
		return validatedSelectionText;
	}

	public void setSelectionText(String newText) {
		textField.setText(newText);
	}

}