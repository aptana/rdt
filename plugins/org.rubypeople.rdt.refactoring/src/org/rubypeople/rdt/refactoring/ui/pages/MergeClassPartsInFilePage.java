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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile.MergeClassPartInFileConfig;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.RdtCodeViewer;
import org.rubypeople.rdt.refactoring.ui.util.SwtUtils;

public class MergeClassPartsInFilePage extends RefactoringWizardPage {
	private static final String TITLE = Messages.MergeClassPartsInFilePage_SelectClassParts;

	private RdtCodeViewer classView;

	private String activeFileName;

	private MergeClassPartInFileConfig config;

	public MergeClassPartsInFilePage(MergeClassPartInFileConfig config) {
		super(TITLE);
		setTitle(TITLE);
		activeFileName = config.getDocumentProvider().getActiveFileName();
		this.config = config;
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		FillLayout baseLayout = new FillLayout();
		baseLayout.spacing = 5;
		control.setLayout(baseLayout);
		initList(control);
		initSidePanel(control);
		setControl(control);
	}

	private void initSidePanel(Composite control) {
		Composite sidePanel = new Composite(control, SWT.NONE);
		FillLayout sidePanelLayout = new FillLayout(SWT.VERTICAL);
		sidePanelLayout.spacing = 5;
		sidePanel.setLayout(sidePanelLayout);

		String explTitle = Messages.MergeClassPartsInFilePage_Description;
		String explText = Messages.MergeClassPartsInFilePage_Explanation;
		SwtUtils.initExplanation(sidePanel, explTitle, explText);

		classView = RdtCodeViewer.create(sidePanel);

	}

	private void initList(Composite control) {

		Composite listSide = new Composite(control, SWT.NONE);
		FillLayout listSideLayout = new FillLayout(SWT.VERTICAL);
		listSideLayout.spacing = 5;
		listSide.setLayout(listSideLayout);

		final List classSelection = new List(listSide, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		final Collection<ClassNodeWrapper> selectableClasses = config.getSelectableClasses();

		for (ClassNodeWrapper currentClass : selectableClasses) {
			classSelection.add(currentClass.getName());
		}

		final Table partTable = new Table(listSide, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.CHECK | SWT.SINGLE);

		classSelection.addSelectionListener(createClassSelectionListener(classSelection, selectableClasses, partTable));
		partTable.addListener(SWT.Selection, createPartSelectionListener(partTable));
	}

	private Listener createPartSelectionListener(final Table partTable) {
		return new Listener(){

			private void setClassView(final Table partTable) {
				TableItem selectedItem = partTable.getSelection()[0];
				PartialClassNodeWrapper classPart = (PartialClassNodeWrapper) selectedItem.getData();
				Node classNode = classPart.getWrappedNode();

				classView.setPreviewText(ReWriteVisitor.createCodeFromNode(NodeFactory.createNewLineNode(classNode), "")); //$NON-NLS-1$
			}

			public void handleEvent(Event event) {
			
				ArrayList<PartialClassNodeWrapper> checkedParts = new ArrayList<PartialClassNodeWrapper>();
				for (TableItem currentItem : partTable.getItems()) {
					if (currentItem.getChecked()) {
						checkedParts.add((PartialClassNodeWrapper) currentItem.getData());
					}
				}
				partTable.setSelection((TableItem)event.item);
				setClassView(partTable);
				
				config.setCheckedClassParts(checkedParts);
				config.setSelectedClassPart((PartialClassNodeWrapper) partTable.getSelection()[0].getData());
			
			}
		};
	}

	private SelectionListener createClassSelectionListener(final List classSelection, final Collection<ClassNodeWrapper> selectableClasses,
			final Table partTable) {
		return new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {

				if (config.getSelectedClassPart() != null
						&& classSelection.getSelection()[0].equals(config.getSelectedClassPart().getClassName())) {
					return;
				}

				partTable.removeAll();
				String currentSelection = classSelection.getSelection()[0];
				for (ClassNodeWrapper currentClass : selectableClasses) {
					if (currentClass.getName().equals(currentSelection)) {
						fillClassPartTable(partTable, currentClass);
					}
					resetClassView();
				}
			}

			private void fillClassPartTable(final Table partTable, ClassNodeWrapper currentClass) {
				for (PartialClassNodeWrapper currentPart : currentClass.getPartialClassNodes()) {
					if (currentPart.getFile().equals(activeFileName)) {
						final TableItem currentItem = new TableItem(partTable, SWT.NONE);
						currentItem.setData(currentPart);
						String itemText = createItemCaption(currentPart);
						currentItem.setText(itemText.trim());
					}
				}
			}

			private String createItemCaption(PartialClassNodeWrapper currentPart) {
				StringBuilder itemText = new StringBuilder();
				int lineCount = 0;
				for (MethodNodeWrapper method : currentPart.getMethods()) {
					itemText.append(method.getSignature().getNameWithArgs()).append("\n"); //$NON-NLS-1$
					if (lineCount >= 2) {
						itemText.append("..."); //$NON-NLS-1$
						break;
					}
					lineCount++;
				}
				return itemText.toString();
			}
		};
	}

	private void resetClassView() {
		classView.setPreviewText(""); //$NON-NLS-1$
		config.setCheckedClassParts(new ArrayList<PartialClassNodeWrapper>());
		config.setSelectedClassPart(null);

	}
}
