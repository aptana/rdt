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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.rubypeople.rdt.refactoring.ui.ConstructorGenerationTree;
import org.rubypeople.rdt.refactoring.ui.util.SwtUtils;

public class ConstructorSelectionPage extends RefactoringWizardPage {
	private static final String title = Messages.ConstructorSelectionPage_SelectConstructor;

	private ConstructorGenerationTree tree;

	private ITreeContentProvider contentProvider;

	public ConstructorSelectionPage(ITreeContentProvider contentProvider) {
		super(title);
		setTitle(title);
		this.contentProvider = contentProvider;
	}

	public void createControl(Composite parent) {

		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FillLayout(SWT.HORIZONTAL));

		initTree(c);
		Composite c2 = new Composite(c, SWT.NONE);
		c2.setLayout(new FillLayout(SWT.VERTICAL));
		initExamples(c2);
		setControl(c);
	}

	private ConstructorGenerationTree initTree(Composite c) {
		tree = new ConstructorGenerationTree(c, contentProvider);
		return tree;
	}

	private Widget[] initExamples(Composite c) {
		Group emptyGroup = SwtUtils.initGroup(c, Messages.ConstructorSelectionPage_EmptyConstructor);
		Label emptyLable = SwtUtils.initLabel(emptyGroup, Messages.ConstructorSelectionPage_EmptyConstructorCode);
		Group paramGroup = SwtUtils.initGroup(c, Messages.ConstructorSelectionPage_ParametrisedConstructor);
		Label paramLabel = SwtUtils.initLabel(paramGroup, Messages.ConstructorSelectionPage_ParametrisedConstructorCode);
		return new Widget[] { emptyGroup, emptyLable, paramGroup, paramLabel };
	}
}
