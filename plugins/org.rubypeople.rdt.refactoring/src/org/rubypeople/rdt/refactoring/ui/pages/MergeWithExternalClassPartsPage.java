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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.ClassPartTreeItem;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.ExternalClassPartsMerger;
import org.rubypeople.rdt.refactoring.ui.NotifiedContainerCheckedTree;
import org.rubypeople.rdt.refactoring.ui.RdtCodeViewer;
import org.rubypeople.rdt.refactoring.ui.util.SwtUtils;

public class MergeWithExternalClassPartsPage extends RefactoringWizardPage {
	private static final String TITLE = Messages.MergeWithExternalClassPartsPage_SelectParts;

	private NotifiedContainerCheckedTree tree;

	private ExternalClassPartsMerger merger;

	private RdtCodeViewer classView;

	public MergeWithExternalClassPartsPage(ExternalClassPartsMerger merger) {
		super(TITLE);
		setTitle(TITLE);
		this.merger = merger;
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);

		initLayout(control);
	}

	private void initLayout(Composite control) {
		setControl(control);
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		control.setLayout(layout);

		this.tree = initTree(control);

		Composite sidePanel = new Composite(control, SWT.NONE);
		FillLayout sidePanelLayout = new FillLayout(SWT.VERTICAL);
		sidePanel.setLayout(sidePanelLayout);

		// TODO Improve the sentence to make it easier to read.
		String explTitle = Messages.MergeWithExternalClassPartsPage_Description;
		String explText = Messages.MergeWithExternalClassPartsPage_DescriptionText;
		SwtUtils.initExplanation(sidePanel, explTitle, explText);

		initClassPartView(sidePanel);
	}

	private void initClassPartView(Composite sidePanel) {
		classView = RdtCodeViewer.create(sidePanel);

		tree.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				TreeSelection selection = (TreeSelection) event.getSelectionProvider().getSelection();
				ClassPartTreeItem treeItem = (ClassPartTreeItem) selection.getFirstElement();

				Node classNode = treeItem.getClassPartWrapper().getWrappedNode();
				classView.setPreviewText(ReWriteVisitor.createCodeFromNode(NodeFactory.createNewLineNode(classNode), "")); //$NON-NLS-1$
			}
		});
	}

	private NotifiedContainerCheckedTree initTree(Composite c) {
		return  new NotifiedContainerCheckedTree(c, merger, merger);
	}
}
