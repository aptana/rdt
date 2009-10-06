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

import org.jruby.ast.MethodDefNode;
import org.rubypeople.rdt.refactoring.core.inlinemethod.MethodFinder;
import org.rubypeople.rdt.refactoring.tests.core.MultipleDocumentsInOneProvider;

public class TC_MethodFinder extends FinderTestsBase {

	public void testFind() {
		MethodDefNode node = findDefinition(61, "test1");
		assertEquals("test", node.getName());
		assertNotNull(node.getNameNode());
		assertNotNull(node.getArgsNode());
		assertNotNull(node.getBodyNode());
	}
	
	public void testFind2() {
		MethodDefNode node = findDefinition(83, "test2");
		assertEquals("test", node.getName());
		assertNotNull(node.getNameNode());
		assertNotNull(node.getArgsNode());
		assertNotNull(node.getBodyNode());
	}

	public void testFind3() {
		MethodDefNode node = findDefinition(165, "test3");
		assertEquals("test", node.getName());
		assertNotNull(node.getNameNode());
		assertNotNull(node.getArgsNode());
		assertNotNull(node.getBodyNode());
	}
	
	public void testFindInherited() {
		MethodDefNode node = findDefinition(155, "test4");
		assertEquals("method_to_find", node.getName());
		assertNotNull(node.getNameNode());
		assertNotNull(node.getArgsNode());
		assertNull(node.getBodyNode());
	}
	
	public void testFindSimplyInherited() {
		MethodDefNode node = findDefinition(100, "test5");
		assertEquals("method_to_find", node.getName());
		assertNotNull(node.getNameNode());
		assertNotNull(node.getArgsNode());
		assertNull(node.getBodyNode());
	}
	
	public void testFindUnknownClass() {
		doc.setActive("test3");
		assertNull(new MethodFinder().find("Tes", "", doc));
	}
	
	public void testFindUnknownMethod() {
		doc.setActive("test3");
		assertNull(new MethodFinder().find("Test", "", doc));
	}

	@Override
	protected void setUp() throws Exception {
		doc = new MultipleDocumentsInOneProvider("TC_MethodFinder.source.rb", this.getClass());
	}
}
