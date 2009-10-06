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

package org.rubypeople.rdt.refactoring.tests.core.splitlocal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import junit.framework.TestCase;

import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.rubypeople.rdt.refactoring.core.renamelocal.SingleLocalVariableEdit;
import org.rubypeople.rdt.refactoring.core.splitlocal.LocalVarFinder;
import org.rubypeople.rdt.refactoring.core.splitlocal.LocalVarUsage;
import org.rubypeople.rdt.refactoring.core.splitlocal.SplittedVariableRenamer;
import org.rubypeople.rdt.refactoring.tests.FileTestData;

public class TC_SplittedVariableRenamer extends TestCase {
	
	private SingleLocalVariableEdit[] getEdits(String name, int pos) throws FileNotFoundException, IOException {
		LocalVarFinder finder = new LocalVarFinder();
		Collection<LocalVarUsage> variables = finder.findLocalUsages(new FileTestData(name, "", ""), pos);
		SplittedVariableRenamer variableRenamer = new SplittedVariableRenamer(finder.getScopeNode());
		return variableRenamer.rename(variables).toArray(new SingleLocalVariableEdit[0]);
	}
	
	public void testRename_1() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_1.rb", 0);
		assertEquals(2, edits.length);
		
		assertEquals(LocalAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[1].getNode().getClass());
		assertEquals(1, edits[1].getOffsetLength());
	}
	
	public void testRename_2() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_2.rb", 0);
		assertEquals(4, edits.length);
		
		assertEquals(LocalAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[1].getNode().getClass());
		assertEquals(1, edits[1].getOffsetLength());
		assertEquals(LocalAsgnNode.class, edits[2].getNode().getClass());
		assertEquals(5, edits[2].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[3].getNode().getClass());
		assertEquals(1, edits[3].getOffsetLength());
	}

	public void testRename_3() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_3.rb", 0);
		assertEquals(4, edits.length);
		
		assertEquals(LocalAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[1].getNode().getClass());
		assertEquals(1, edits[1].getOffsetLength());
		assertEquals(LocalAsgnNode.class, edits[2].getNode().getClass());
		assertEquals(5, edits[2].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[3].getNode().getClass());
		assertEquals(1, edits[3].getOffsetLength());
	}
	
	public void testRename_4() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_4.rb", 14);
		assertEquals(4, edits.length);
		
		assertEquals(LocalAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(LocalAsgnNode.class, edits[1].getNode().getClass());
		assertEquals(1, edits[1].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[2].getNode().getClass());
		assertEquals(1, edits[2].getOffsetLength());
		assertEquals(LocalVarNode.class, edits[3].getNode().getClass());
		assertEquals(1, edits[3].getOffsetLength());
	}
	
	public void testRename_5() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_5.rb", 14);
		assertEquals(2, edits.length);

		assertEquals(LocalAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(LocalAsgnNode.class, edits[1].getNode().getClass());
		assertEquals(6, edits[1].getOffsetLength());
	}
	
	public void testRename_6() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_6.rb", 14);
		assertEquals(3, edits.length);

		assertEquals(LocalAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(LocalAsgnNode.class, edits[1].getNode().getClass());
		assertEquals(6, edits[1].getOffsetLength());
		assertEquals(LocalAsgnNode.class, edits[2].getNode().getClass());
		assertEquals(5, edits[2].getOffsetLength());
	}
	
	public void testRename_7() throws FileNotFoundException, IOException {
		
		SingleLocalVariableEdit[] edits = getEdits("testFindLocalUsages_7.rb", 14);
		assertEquals(5, edits.length);

		assertEquals(DAsgnNode.class, edits[0].getNode().getClass());
		assertEquals(5, edits[0].getOffsetLength());
		assertEquals(DVarNode.class, edits[1].getNode().getClass());
		assertEquals(5, edits[1].getOffsetLength());
		assertEquals(DAsgnNode.class, edits[2].getNode().getClass());
		assertEquals(12, edits[2].getOffsetLength());
		assertEquals(DAsgnNode.class, edits[3].getNode().getClass());
		assertEquals(13, edits[3].getOffsetLength());
		assertEquals(DVarNode.class, edits[4].getNode().getClass());
		assertEquals(5, edits[4].getOffsetLength());
	}
}
