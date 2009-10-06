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

package org.rubypeople.rdt.refactoring.tests.util;

import java.util.ArrayList;

import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;
import org.rubypeople.rdt.refactoring.util.NameHelper;

public class TC_NameHelper extends FileTestCase {

	public TC_NameHelper() {
		super("Name Helper");
	}

	public void testCreateName() {
		assertEquals("string1", NameHelper.createName("string"));
		assertEquals("string2", NameHelper.createName("string1"));
		assertEquals("@string2", NameHelper.createName("@string1"));
		assertEquals("@@string2", NameHelper.createName("@@string1"));
		assertEquals("string100", NameHelper.createName("string99"));
		assertEquals("one1string100", NameHelper.createName("one1string99"));
		assertEquals("a2", NameHelper.createName("a1"));
	}

	public void testFindDuplicates1() {
		String[] first = new String[] {"a", "b", "c"};
		String[] second = new String[] {"a", "b", "c"};
	
		assertEqualContent(new String[] {"a", "b", "c"}, NameHelper.findDuplicates(first, second));
	}

	public void testFindDuplicates2() {
		String[] first = new String[] {"a", "b", "c"};
		String[] second = new String[] {"b", "c"};
	
		assertEqualContent(new String[] {"b", "c"}, NameHelper.findDuplicates(first, second));
	}

	public void testFindDuplicates3() {
		String[] first = new String[] {"a", "b", "c"};
		String[] second = new String[] {};
	
		assertEqualContent(new String[] {}, NameHelper.findDuplicates(first, second));
	}

	private static void assertEqualContent(String[] first, ArrayList<String> name) {
		assertEquals(first.length, name.size());
		
		for (int i = 0; i < first.length; i++) {
			assertEquals(first[i], name.get(i));
		}
	}
	
	public void testModulePrefix() {
		final RootNode rootNode = getRootNode("TC_NameHelper_ModulePrefix.rb");
		
		Node node = getLastNode(14, rootNode);
		assertEquals("M1", NameHelper.getEncosingModulePrefix(rootNode, node));
		
		node = getLastNode(27, rootNode);
	
		assertEquals("", NameHelper.getEncosingModulePrefix(rootNode, node));
		
		node = getLastNode(62, rootNode);
	
		assertEquals("M1::M3", NameHelper.getEncosingModulePrefix(rootNode, node));
		
		node = getLastNode(173, rootNode);
	
		assertEquals("OuterModule", NameHelper.getEncosingModulePrefix(rootNode, node));
	}

	private Node getLastNode(int pos, RootNode rootNode) {
		Node[] nodes = SelectionNodeProvider.getSelectedNodesOfType(rootNode, pos, Node.class).toArray(new Node[]{});
		assertTrue(nodes.length > 0);
		return nodes[nodes.length - 1];
	}
}
