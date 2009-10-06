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

package org.rubypeople.rdt.refactoring.tests.core.inlinemethod;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.FixnumNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.ZArrayNode;
import org.rubypeople.rdt.refactoring.core.inlinemethod.TargetClassFinder;
import org.rubypeople.rdt.refactoring.tests.core.MultipleDocumentsInOneProvider;

public class TC_TargetClassFinder extends FinderTestsBase {
	
	public void testFindTargetClass() {
		assertEquals("TestClass", findTargetClass(172, "test7"));
	}

	public void testFindTargetClass2() {
		assertEquals("TestClass", findTargetClass(40, "test8"));
	}
	
	public void testFindTargetClass3() {
		assertEquals("", findTargetClass(46, "test9"));
	}

	public void testFindTargetClass4() {
		assertEquals("SomeClass", findTargetClass(69, "test10"));
	}

	public void testFindTargetClass5() {
		assertEquals("Employee", findTargetClass(74, "test11"));
	}	
	
	public void testFindTargetClass6() {
		assertEquals("Test", findTargetClass(21, "test12"));
	}
	
	public void testFindTargetClassInModule() {
		assertEquals("Module::Klass", findTargetClass(36, "test13"));
	}
	
	public void testFindTargetClassInModules() {
		assertEquals("Module1::Module2::Module3::Klass", findTargetClass(55, "test14"));
	}
	

	public void testInstVarFromCall() {
		InstAsgnNode node = findInstAsgnNode(59, "test4");		
		assertEquals("@var", node.getName());
		assertEquals(FixnumNode.class, node.getValueNode().getClass());
	}

	public void testInstVarFromCall2() {
		InstAsgnNode node = findInstAsgnNode(84, "test5");		
		assertEquals("@var", node.getName());
		assertEquals(ArrayNode.class, node.getValueNode().getClass());
	}
	
	public void testInstVarFromCall3() {
		InstAsgnNode node = findInstAsgnNode(163, "test6");		
		assertEquals("@var", node.getName());
		assertEquals(ArrayNode.class, node.getValueNode().getClass());
	}
	
	public void testLocalAsgnFromLocalVar() {
		LocalAsgnNode node = findLocalAsgnNode(30, "test1");		
		assertEquals("var", node.getName());
	}

	public void testLocalAsgnFromLocalVar2() {
		LocalAsgnNode node = findLocalAsgnNode(55, "test2");		
		assertEquals("var", node.getName());
		assertEquals(HashNode.class, node.getValueNode().getClass());
		
		node = findLocalAsgnNode(86, "test2");		
		assertEquals("var", node.getName());
		assertEquals(StrNode.class, node.getValueNode().getClass());
	}

	public void testLocalAsgnFromLocalVar3() {
		LocalAsgnNode node = findLocalAsgnNode(93, "test3");		
		assertEquals("var", node.getName());
		assertEquals(ZArrayNode.class, node.getValueNode().getClass());
	}
	
	private LocalAsgnNode findLocalAsgnNode(int pos, String file) {
		TargetClassFinder finder = new TargetClassFinder();
		return finder.localAsgnFromLocalVar( (LocalVarNode) ((CallNode) findSelected(pos, file).getWrappedNode()).getReceiverNode(), doc);
	}
	
	private InstAsgnNode findInstAsgnNode(int pos, String file) {
		TargetClassFinder finder = new TargetClassFinder();
		return finder.instVarFromCall( (InstVarNode) ((CallNode) findSelected(pos, file).getWrappedNode()).getReceiverNode(), doc);
	}
	
	private String findTargetClass(int pos, String file) {
		return new TargetClassFinder().findTargetClass(findSelected(pos, file), doc);
	}

	@Override
	protected void setUp() throws Exception {
		doc = new MultipleDocumentsInOneProvider("TC_TargetClassFinder.source.rb", this.getClass());
	}
}
