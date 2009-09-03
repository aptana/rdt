/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.action;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.ActionGroup;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.RefactoringContext;
import org.rubypeople.rdt.refactoring.core.convertlocaltofield.ConvertLocalToFieldRefactoring;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.EncapsulateFieldRefactoring;
import org.rubypeople.rdt.refactoring.core.extractconstant.ExtractConstantRefactoring;
import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractMethodRefactoring;
import org.rubypeople.rdt.refactoring.core.generateaccessors.GenerateAccessorsRefactoring;
import org.rubypeople.rdt.refactoring.core.generateconstructor.GenerateConstructorRefactoring;
import org.rubypeople.rdt.refactoring.core.inlineclass.InlineClassRefactoring;
import org.rubypeople.rdt.refactoring.core.inlinelocal.InlineLocalRefactoring;
import org.rubypeople.rdt.refactoring.core.inlinemethod.InlineMethodRefactoring;
import org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile.MergeClassPartsInFileRefactoring;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.MergeWithExternalClassPartsRefactoring;
import org.rubypeople.rdt.refactoring.core.movefield.MoveFieldRefactoring;
import org.rubypeople.rdt.refactoring.core.movemethod.MoveMethodRefactoring;
import org.rubypeople.rdt.refactoring.core.overridemethod.OverrideMethodRefactoring;
import org.rubypeople.rdt.refactoring.core.pullup.PullUpRefactoring;
import org.rubypeople.rdt.refactoring.core.pushdown.PushDownRefactoring;
import org.rubypeople.rdt.refactoring.core.rename.RenameRefactoring;
import org.rubypeople.rdt.refactoring.core.splitlocal.SplitTempRefactoring;
import org.rubypeople.rdt.ui.actions.RubyActionGroup;

public class RefactoringActionGroup extends ActionGroup {

	public void fillContextMenu(IMenuManager menu) {
		IRefactoringContext selectionProvider = new RefactoringContext(null);
		IMenuManager source = menu.findMenuUsingPath(RubyActionGroup.MENU_ID);
		addSourceMenuItems(source, selectionProvider);
		menu.insertAfter(RubyActionGroup.MENU_ID, getRefactorMenu(selectionProvider));
	}

	private IMenuManager getRefactorMenu(IRefactoringContext selectionProvider) {
		IMenuManager submenu = new MenuManager(Messages.RefactoringActionGroup);
		submenu.add(new RefactoringAction(ConvertLocalToFieldRefactoring.class, ConvertLocalToFieldRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(EncapsulateFieldRefactoring.class, EncapsulateFieldRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(ExtractMethodRefactoring.class, ExtractMethodRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(ExtractConstantRefactoring.class, ExtractConstantRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(InlineClassRefactoring.class, InlineClassRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(InlineLocalRefactoring.class, InlineLocalRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(InlineMethodRefactoring.class, InlineMethodRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(MergeClassPartsInFileRefactoring.class, MergeClassPartsInFileRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(MergeWithExternalClassPartsRefactoring.class, MergeWithExternalClassPartsRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(MoveFieldRefactoring.class, MoveFieldRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(MoveMethodRefactoring.class, MoveMethodRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(PushDownRefactoring.class, PushDownRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(PullUpRefactoring.class, PullUpRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(RenameRefactoring.class, RenameRefactoring.NAME, selectionProvider));
		submenu.add(new RefactoringAction(SplitTempRefactoring.class, SplitTempRefactoring.NAME, selectionProvider));
		return submenu;
	}
	
	private void addSourceMenuItems(IMenuManager source, IRefactoringContext selectionProvider) {		
		source.add(new RefactoringAction(GenerateAccessorsRefactoring.class, GenerateAccessorsRefactoring.NAME, selectionProvider));
		source.add(new RefactoringAction(GenerateConstructorRefactoring.class, GenerateConstructorRefactoring.NAME, selectionProvider));
		source.add(new RefactoringAction(OverrideMethodRefactoring.class, OverrideMethodRefactoring.NAME, selectionProvider));
	}
}
