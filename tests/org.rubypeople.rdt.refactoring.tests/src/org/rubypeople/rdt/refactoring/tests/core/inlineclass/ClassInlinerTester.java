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

package org.rubypeople.rdt.refactoring.tests.core.inlineclass;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.core.inlineclass.ClassInliner;
import org.rubypeople.rdt.refactoring.core.inlineclass.InlineClassConditionChecker;
import org.rubypeople.rdt.refactoring.core.inlineclass.InlineClassConfig;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.tests.MultiFileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;

public class ClassInlinerTester extends RefactoringTestCase {

	private MultiFileTestData testData;

	public ClassInlinerTester(String fileName) {
		super(fileName);
	}

	private PartialClassNodeWrapper getTargetClassPart(String fileName, int classPos) {
		Node rootNode = testData.getRootNode(fileName);
		try {
			return SelectionNodeProvider.getSelectedClassNode(rootNode, classPos).getFirstPartialClassNode();
		} catch (NoClassNodeException e) {
			assertTrue(false);
			return null;

		}
	}

	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException {
		testData = new MultiFileTestData(getName());
		int caretPosition = testData.getIntProperty("caretPosition");
		
		InlineClassConfig config = new InlineClassConfig(testData, caretPosition);
		InlineClassConditionChecker checker = new InlineClassConditionChecker(config);
		if(!checker.shouldPerform()){
			fail();
		}
		ClassInliner inliner = new ClassInliner(config);
		
		PartialClassNodeWrapper targetClassPart = getTargetClassPart(testData.getProperty("targetClassFile"), testData.getIntProperty("targetClassPos"));
		config.setTargetClassPart(targetClassPart);
		
		checkMultiFileEdits(inliner, testData);
	}
}
