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

package org.rubypeople.rdt.refactoring.tests.core.inlinelocal;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.rubypeople.rdt.refactoring.core.inlinelocal.InlineLocalConditionChecker;
import org.rubypeople.rdt.refactoring.core.inlinelocal.InlineLocalConfig;
import org.rubypeople.rdt.refactoring.core.inlinelocal.LocalVariableInliner;
import org.rubypeople.rdt.refactoring.tests.FileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;

public class LocalInlinerTester extends RefactoringTestCase {

	public LocalInlinerTester(String fileName) {
		super(fileName);
	}

	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException {
		FileTestData data = new FileTestData(getName());
		InlineLocalConfig config = new InlineLocalConfig(data, data.getIntProperty("caretPosition"));
		InlineLocalConditionChecker checker = new InlineLocalConditionChecker(config);
		if (!checker.shouldPerform()) {
			fail();
		}
		LocalVariableInliner inliner = new LocalVariableInliner(config);
		config.setReplaceTempWithQuery(data.getBoolProperty("replaceWithQuery"));
		config.setNewMethodName(data.getProperty("newMethodName"));
		
		createEditAndCompareResult(data.getActiveFileContent(), data.getExpectedResult(), inliner);
	}
}
