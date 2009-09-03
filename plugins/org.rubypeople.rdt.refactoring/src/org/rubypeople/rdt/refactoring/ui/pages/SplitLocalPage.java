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

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rubypeople.rdt.refactoring.core.splitlocal.ISplittedNamesReceiver;
import org.rubypeople.rdt.refactoring.core.splitlocal.LocalVarUsage;
import org.rubypeople.rdt.refactoring.core.splitlocal.SplitTempRefactoring;
import org.rubypeople.rdt.refactoring.ui.RdtCodeViewer;
import org.rubypeople.rdt.refactoring.util.NameValidator;

public class SplitLocalPage extends RefactoringWizardPage {

	private static final int SEPARATOR_AT = 200;

	private static final int BORDER_WIDTH = 5;

	private static RGB[] colors;

	private final Collection<LocalVarUsage> localUsages;

	private final String source;

	private final ISplittedNamesReceiver receiver;

	private Text[] names;
	
	static {
		colors = new RGB[]{
			new RGB(173, 193, 217),
			new RGB(223, 167, 166),
			new RGB(206, 221, 176),
			new RGB(191, 178, 207),
			new RGB(165, 213, 226),
			new RGB(250, 206, 170),
			new RGB(132, 162, 198),
			new RGB(207, 123, 121),
			new RGB(181, 204, 136),
			new RGB(159, 140, 183),
			new RGB(120, 192, 212),
			new RGB(247, 181, 128), 
			new RGB(143, 164, 190)
		};
	}
	
	public SplitLocalPage(Collection<LocalVarUsage> localUsages, String source, ISplittedNamesReceiver receiver) {
		super(SplitTempRefactoring.NAME + "..."); //$NON-NLS-1$
		this.localUsages = localUsages;
		this.source = source;
		this.receiver = receiver;
		setTitle(SplitTempRefactoring.NAME);
	}

	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);

		FormLayout thisLayout = new FormLayout();
		main.setLayout(thisLayout);
		Composite variableNames = new Composite(main, SWT.NONE);
		GridLayout variableNamesLayout = new GridLayout();
		variableNamesLayout.makeColumnsEqualWidth = true;
		FormData variableNamesLayoutData = new FormData();
		variableNamesLayoutData.top = new FormAttachment(0, 1000, BORDER_WIDTH);
		variableNamesLayoutData.bottom = new FormAttachment(1000, 1000, -BORDER_WIDTH);
		variableNamesLayoutData.left = new FormAttachment(0, 1000, BORDER_WIDTH);
		variableNamesLayoutData.width = SEPARATOR_AT - 3 * BORDER_WIDTH;
		variableNames.setLayoutData(variableNamesLayoutData);
		variableNames.setLayout(variableNamesLayout);

		Label newNameLabel = new Label(variableNames, SWT.NONE);
		newNameLabel.setText(Messages.SplitTempPage_ChooseNewNames);

		if(localUsages != null) {
			names = new Text[localUsages.size()];
	
			for (int i = 0; i < localUsages.size(); i++) {
				Text text = new Text(variableNames, SWT.BORDER);
				GridData textLData = new GridData();
				textLData.horizontalAlignment = GridData.FILL;
				textLData.grabExcessHorizontalSpace = true;
				text.setLayoutData(textLData);
				text.setText(localUsages.toArray(new LocalVarUsage[localUsages.size()])[i].getName());
				text.setBackground(new Color(getShell().getDisplay(), colors[i]));
				text.addModifyListener(new ModifyListener() {
	
					public void modifyText(ModifyEvent e) {
						setNewNames();
					}
				});
	
				names[i] = text;
			}
	
			RdtCodeViewer sourceWidget = RdtCodeViewer.create(main);
			FormData layoutData = new FormData();
			layoutData.top = new FormAttachment(0, 1000, BORDER_WIDTH);
			layoutData.bottom = new FormAttachment(1000, 1000, -BORDER_WIDTH);
			layoutData.right = new FormAttachment(1000, 1000, -BORDER_WIDTH);
			layoutData.left = new FormAttachment(0, 1000, SEPARATOR_AT);
			sourceWidget.getTextWidget().setLayoutData(layoutData);
			sourceWidget.getTextWidget().setText(source);
	
			int colorIndex = 0;
			for (LocalVarUsage var : localUsages) {
				sourceWidget.setBackgroundColor(var.getFromPosition(), var.getName().length(), colors[colorIndex++]);
			}
			setNewNames();
			sourceWidget.getTextWidget().setSelection(localUsages.toArray(new LocalVarUsage[localUsages.size()])[localUsages.size() - 1].getFromPosition());
		}
		main.layout();
		setControl(main);
	}

	private void setNewNames() {
		assert names.length == localUsages.size();

		String[] newNames = new String[localUsages.size()];

		boolean allOk = true;

		for (int i = 0; i < names.length; i++) {
			if (names[i].getText().equals("")) { //$NON-NLS-1$
				setErrorMessage(Messages.SplitTempPage_PleaseEnterName);
				allOk = false;
			} else if (!NameValidator.isValidLocalVariableName(names[i].getText())) {
				setErrorMessage(names[i].getText() + Messages.SplitTempPage_InvalidVariableName);
				allOk = false;
			}
			newNames[i] = names[i].getText();
		}

		setPageComplete(allOk);
		if (allOk) {
			setErrorMessage(null);
		}

		receiver.setNewNames(newNames);
	}

}
