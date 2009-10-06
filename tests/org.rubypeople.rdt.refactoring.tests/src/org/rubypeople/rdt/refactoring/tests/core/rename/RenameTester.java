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

package org.rubypeople.rdt.refactoring.tests.core.rename;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.rubypeople.rdt.refactoring.core.rename.RenameConditionChecker;
import org.rubypeople.rdt.refactoring.core.rename.RenameConfig;
import org.rubypeople.rdt.refactoring.tests.FileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;

public class RenameTester extends RefactoringTestCase {
	
	private static final String NONE = "none";

	private static final String RENAME_CLASS = "renameClass";
	
	private static final String RENAME_MODULE = "renameModule";

	private static final String RENAME_METHOD = "renameMethod";

	private static final String RENAME_FIELD = "renameField";

	private static final String RENAME_LOCAL = "renameLocal";

	public RenameTester(String fileName) {
		super(fileName);
	}

	@Override
	public void runTest() throws FileNotFoundException, IOException {
		FileTestData testData = new FileTestData(getName(), ".source.rb", ".source.rb");
		RenameConfig config = new RenameConfig(testData, testData.getIntProperty("cursorPosition"));
		RenameConditionChecker checker = new RenameConditionChecker(config);
		
		String delegateRenameRefactoring = testData.getProperty("delegateRenameRefactoring");
		
		assertTrue(isValidParam(delegateRenameRefactoring));
		
		check(!checker.shouldPerform(), delegateRenameRefactoring.equals(NONE));
		check(checker.shouldRenameClass(), delegateRenameRefactoring.equals(RENAME_CLASS));
		check(checker.shouldRenameModule(), delegateRenameRefactoring.equals(RENAME_MODULE));
		check(checker.shouldRenameMethod(), delegateRenameRefactoring.equals(RENAME_METHOD));
		check(checker.shouldRenameField(), delegateRenameRefactoring.equals(RENAME_FIELD));
		check(checker.shouldRenameLocal(), delegateRenameRefactoring.equals(RENAME_LOCAL));
	}

	private void check(boolean shouldPerform, boolean isRightRefactoring) {
		if(isRightRefactoring) {
			assertTrue(shouldPerform);
		}
	}

	private boolean isValidParam(String paramName) {
		return paramName.equals(NONE)
				|| paramName.equals(RENAME_CLASS)
				|| paramName.equals(RENAME_MODULE)
				|| paramName.equals(RENAME_METHOD)
				|| paramName.equals(RENAME_FIELD)
				|| paramName.equals(RENAME_LOCAL);
	}

}
