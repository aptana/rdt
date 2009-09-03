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


package org.rubypeople.rdt.refactoring.core.rename;

import org.rubypeople.rdt.refactoring.core.IRefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.renameclass.RenameClassConditionChecker;
import org.rubypeople.rdt.refactoring.core.renameclass.RenameClassConfig;
import org.rubypeople.rdt.refactoring.core.renamefield.RenameFieldConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamefield.RenameFieldConfig;
import org.rubypeople.rdt.refactoring.core.renamelocal.RenameLocalConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamelocal.RenameLocalConfig;
import org.rubypeople.rdt.refactoring.core.renamemethod.RenameMethodConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamemethod.RenameMethodConfig;
import org.rubypeople.rdt.refactoring.core.renamemodule.RenameModuleConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamemodule.RenameModuleConfig;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;

public class RenameConditionChecker extends RefactoringConditionChecker {

	private enum RenameType {
		INVALID, LOCAL, FIELD, METHOD, CLASS, MODULE
	};

	private RenameType selectedType;

	private RenameLocalConditionChecker localConditionChecker;

	private RefactoringConditionChecker fieldConditionChecker;

	private RefactoringConditionChecker methodConditionChecker;

	private RefactoringConditionChecker classConditionChecker;
	
	private RefactoringConditionChecker moduleConditionChecker;

	public RenameConditionChecker(RenameConfig config) {
		super(config);
	}

	@Override
	protected void checkInitialConditions() {
		if (selectedType == RenameType.INVALID) {
			addErrorMessage();
		}
	}

	private void addErrorMessage() {
		addErrorIfNotDefaultError(localConditionChecker, RenameLocalConditionChecker.DEFAULT_ERROR);
		addErrorIfNotDefaultError(fieldConditionChecker, RenameFieldConditionChecker.DEFAULT_ERROR);
		addErrorIfNotDefaultError(methodConditionChecker, RenameMethodConditionChecker.DEFAULT_ERROR);
		addErrorIfNotDefaultError(classConditionChecker, RenameClassConditionChecker.DEFAULT_ERROR);
		addErrorIfNotDefaultError(moduleConditionChecker, RenameClassConditionChecker.DEFAULT_ERROR);
		if (!hasErrors()) {

			addError(Messages.RenameConditionChecker_NothingSelected);

		}
	}

	private void addErrorIfNotDefaultError(RefactoringConditionChecker checker, String defaultError) {
		String firstError = checker.getInitialMessages().get(IRefactoringConditionChecker.ERRORS).toArray(new String[0])[0];
		if (!firstError.equals(defaultError)) {
			addError(firstError);
		}
	}

	@Override
	public void init(IRefactoringConfig configObj) {
		RenameConfig config = (RenameConfig) configObj;
		int offset = config.getOffset();
		IDocumentProvider doc = config.getDocumentProvider();
		
		localConditionChecker = new RenameLocalConditionChecker(new RenameLocalConfig(doc, offset));
		fieldConditionChecker = new RenameFieldConditionChecker(new RenameFieldConfig(doc, offset));
		methodConditionChecker = new RenameMethodConditionChecker(new RenameMethodConfig(doc, offset));
		classConditionChecker = new RenameClassConditionChecker(new RenameClassConfig(doc, offset));
		moduleConditionChecker = new RenameModuleConditionChecker(new RenameModuleConfig(doc, offset));
		
		if (localConditionChecker.shouldPerform()) {
			selectedType = RenameType.LOCAL;
		} else if (fieldConditionChecker.shouldPerform()) {
			selectedType = RenameType.FIELD;
		} else if (methodConditionChecker.shouldPerform()) {
			selectedType = RenameType.METHOD;
		} else if (classConditionChecker.shouldPerform()) {
			selectedType = RenameType.CLASS;
		} else if (moduleConditionChecker.shouldPerform()) {
			selectedType = RenameType.MODULE;
		} else {
			selectedType = RenameType.INVALID;
		}
	}

	public boolean shouldRenameLocal() {
		return selectedType == RenameType.LOCAL;
	}

	public boolean shouldRenameField() {
		return selectedType == RenameType.FIELD;
	}

	public boolean shouldRenameMethod() {
		return selectedType == RenameType.METHOD;
	}

	public boolean shouldRenameClass() {
		return selectedType == RenameType.CLASS;
	}
	
	public boolean shouldRenameModule() {
		return selectedType == RenameType.MODULE;
	}
}
