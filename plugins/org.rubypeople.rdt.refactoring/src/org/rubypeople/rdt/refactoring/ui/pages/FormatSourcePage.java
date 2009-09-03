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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.rubypeople.rdt.core.formatter.EditableFormatHelper;
import org.rubypeople.rdt.refactoring.core.formatsource.PreviewGenerator;

public class FormatSourcePage extends RefactoringWizardPage {

	private static final String title = ""; //$NON-NLS-1$

	private StyledText previewText;

	private EditableFormatHelper formatter;

	private PreviewGenerator previewGenererator;

	public FormatSourcePage(EditableFormatHelper formatter, PreviewGenerator previewGenerator) {
		super(title);
		setTitle(title);
		this.formatter = formatter;
		this.previewGenererator = previewGenerator;
	}

	private void createMethodsTab(TabFolder categoryTab) {
		TabItem methodsTabItem = createTabItem(categoryTab, Messages.FormatSourcePage_Methods);

		Composite optionComposite = createCompositeWithGridLayout(categoryTab);
		methodsTabItem.setControl(optionComposite);

		Group callArgumentsGroup = createGroupWithGridLayout(optionComposite, Messages.FormatSourcePage_MethodCallArguments);

		final Button callArgumentsParanthesizeWhereNecessary = createButton(callArgumentsGroup, SWT.RADIO, Messages.FormatSourcePage_ParenthesizeWhereNecesary);
		callArgumentsParanthesizeWhereNecessary.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setAlwaysParanthesizeMethodCalls(!callArgumentsParanthesizeWhereNecessary.getEnabled());
				generatePreview();
			}
		});

		final Button callArgumentsParanthesizeAlways = createButton(callArgumentsGroup, SWT.RADIO, Messages.FormatSourcePage_AlwaysParenthesize);
		callArgumentsParanthesizeAlways.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setAlwaysParanthesizeMethodCalls(callArgumentsParanthesizeAlways.getEnabled());
				generatePreview();
			}
		});

		Group defArgumentsGroup = createGroupWithGridLayout(optionComposite, Messages.FormatSourcePage_MethodDefArguments);

		final Button defArgumentsParanthesizeWhereNecessary = createButton(defArgumentsGroup, SWT.RADIO, Messages.FormatSourcePage_ParenthesizeWhereNecesary);
		defArgumentsParanthesizeWhereNecessary.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setAlwaysParanthesizeMethodDefs(!defArgumentsParanthesizeWhereNecessary.getSelection());
				generatePreview();
			}
		});

		final Button defArgumentsParanthesizeAlways = createButton(defArgumentsGroup, SWT.RADIO, Messages.FormatSourcePage_AlwaysParenthesize);
		defArgumentsParanthesizeAlways.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setAlwaysParanthesizeMethodDefs(defArgumentsParanthesizeAlways.getSelection());
				generatePreview();
			}
		});

		final Button newlineBetweenClassBodyElements = createButton(optionComposite, SWT.CHECK, Messages.FormatSourcePage_NewlineBetweenClassElements);
		newlineBetweenClassBodyElements.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setNewlineBetweenClassBodyElements(newlineBetweenClassBodyElements.getSelection());
				generatePreview();
			}
		});
	}

	private void createBlocksTab(TabFolder categoryTab) {
		TabItem misc = createTabItem(categoryTab, Messages.FormatSourcePage_Blocks);

		Composite composite = createCompositeWithGridLayout(categoryTab);
		misc.setControl(composite);

		final Button spaceBeforeIterBrackets = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpaceBeforeIterBrackets);
		spaceBeforeIterBrackets.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpaceBeforeIterBrackets(spaceBeforeIterBrackets.getSelection());
				generatePreview();
			}
		});

		final Button spaceBeforeClosingIterBrackets = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpaceBeforeClosingIterBracket);
		spaceBeforeClosingIterBrackets.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpaceBeforeClosingIterBrackets(spaceBeforeClosingIterBrackets.getSelection());
				generatePreview();
			}
		});

		final Button spaceBeforeIterVars = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpaceBeforeIterVars);
		spaceBeforeIterVars.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpaceBeforeIterVars(spaceBeforeIterVars.getSelection());
				generatePreview();
			}
		});

		final Button spaceAfterIterVars = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpaceAfterIterVars);
		spaceAfterIterVars.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpaceAfterIterVars(spaceAfterIterVars.getSelection());
				generatePreview();
			}
		});
	}

	private void createSpacesTab(TabFolder categoryTab) {
		TabItem spaces = createTabItem(categoryTab, Messages.FormatSourcePage_Spaces);

		Composite composite = createCompositeWithGridLayout(categoryTab);
		spaces.setControl(composite);

		final Button spaceAfterComma = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpaceAfterComma);
		spaceAfterComma.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpaceAfterCommaInListings(spaceAfterComma.getSelection());
				generatePreview();
			}
		});

		final Button spacesAroundHashAss = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpacesAroundHashOperator);
		spacesAroundHashAss.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpacesAroundHashAssignment(spacesAroundHashAss.getSelection());
				generatePreview();
			}
		});

		final Button spacesAroundHashContent = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpacesAroundHash);
		spacesAroundHashContent.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpacesBeforeAndAfterHashContent(spacesAroundHashContent.getSelection());
				generatePreview();
			}
		});

		final Button spacesAroundAssignments = createButton(composite, SWT.CHECK, Messages.FormatSourcePage_SpacesAroundAssignment);
		spacesAroundAssignments.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setSpacesBeforeAndAfterAssignments(spacesAroundAssignments.getSelection());
				generatePreview();
			}
		});
	}

	private void createGeneralTab(TabFolder categoryTab) {
		TabItem spaces = createTabItem(categoryTab, Messages.FormatSourcePage_General);

		Composite composite = createCompositeWithGridLayout(categoryTab);
		spaces.setControl(composite);

		Group callArgumentsGroup = createGroupWithGridLayout(composite, Messages.FormatSourcePage_Indentation);

		final Button tabInsteadOfSpaces = createButton(callArgumentsGroup, SWT.CHECK, Messages.FormatSourcePage_UseTab);
		tabInsteadOfSpaces.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setTabInsteadOfSpaces(tabInsteadOfSpaces.getSelection());
				generatePreview();
			}
		});

		Composite indentationComposite = new Composite(callArgumentsGroup, SWT.NONE);
		indentationComposite.setLayout(new RowLayout());

		final Spinner indentationSteps = new Spinner(indentationComposite, SWT.BORDER);
		indentationSteps.setMinimum(0);
		indentationSteps.setMaximum(100);
		indentationSteps.setSelection(2);
		indentationSteps.setIncrement(1);
		indentationSteps.pack();
		indentationSteps.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				formatter.setIndentationSteps(indentationSteps.getSelection());
				generatePreview();
			}
		});

		Label label = new Label(indentationComposite, SWT.NONE);
		label.setText(Messages.FormatSourcePage_IndentationSteps);

	}

	public void createControl(Composite parent) {
		SashForm mainSashForm = new SashForm(parent, SWT.NONE);
		TabFolder categoryTab = new TabFolder(mainSashForm, SWT.NONE);

		createGeneralTab(categoryTab);
		createMethodsTab(categoryTab);
		createSpacesTab(categoryTab);
		createBlocksTab(categoryTab);

		previewText = new StyledText(mainSashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		generatePreview();

		parent.layout();
		setControl(parent);
	}

	private void generatePreview() {
		previewText.setText(previewGenererator.getPreview(formatter));
	}

	private TabItem createTabItem(TabFolder parent, String name) {
		TabItem item = new TabItem(parent, SWT.NONE);
		item.setText(name);
		return item;
	}

	private Composite createCompositeWithGridLayout(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = true;
		composite.setLayout(layout);
		return composite;
	}

	private GridData createFillingGrid() {
		GridData groupData = new GridData();
		groupData.verticalAlignment = GridData.BEGINNING;
		groupData.grabExcessHorizontalSpace = true;
		groupData.horizontalAlignment = GridData.FILL;
		return groupData;
	}

	private Group createGroupWithGridLayout(Composite parent, String groupText) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.makeColumnsEqualWidth = true;
		group.setLayout(groupLayout);
		group.setLayoutData(createFillingGrid());
		group.setText(groupText);
		return group;
	}

	private Button createButton(Composite parent, int style, String text) {
		Button button = new Button(parent, style | SWT.LEFT);
		button.setText(text);
		return button;
	}
}
