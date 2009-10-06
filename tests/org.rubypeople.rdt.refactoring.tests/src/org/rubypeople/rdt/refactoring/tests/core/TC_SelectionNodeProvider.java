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

package org.rubypeople.rdt.refactoring.tests.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringContext;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;
import org.rubypeople.rdt.refactoring.tests.FileTestData;

public class TC_SelectionNodeProvider extends FileTestCase {
	
	public TC_SelectionNodeProvider(String fileName) {
		super(fileName);
	}
	
	private String[] getEnclosingNodeClasses(FileTestData data, IRefactoringContext selection) {
		Node selected = SelectionNodeProvider.getSelectedNodes(data.getActiveFileRootNode(), selection);
		
		ArrayList<String> classes = new ArrayList<String>();
		Iterator<Node> it = NodeProvider.getAllNodes(selected).iterator();
		while(it.hasNext()) {
			classes.add(it.next().getClass().getSimpleName());
		}

		return classes.toArray(new String[]{});
	}

	private void assertLists(String[] found, String[] expected) {
		StringBuilder expectedStr = new StringBuilder(), foundStr = new StringBuilder();
		
		for(String s : expected) {
			expectedStr.append(s + ' ');
		}
		for(String s : found) {
			foundStr.append(s + ' ');
		}
		
		assertEquals(expectedStr.toString(), foundStr.toString());
	}
	
	@Override
	protected void runTest() throws Throwable {
		FileTestData data = new FileTestData(getName(), ".rb", ".rb");
		IRefactoringContext selection = new RefactoringContext(data.getIntProperty("begin"), data.getIntProperty("end"), "");
		assertLists(getEnclosingNodeClasses(data, selection), data.getCommaSeparatedStringArray("expected"));
	}
}

