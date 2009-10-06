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

package org.rubypeople.rdt.refactoring.tests.core.renameclass;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.jruby.ast.ClassNode;
import org.rubypeople.rdt.refactoring.core.renameclass.ClassFinder;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentWithIncluding;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.MultiFileTestData;

public class TC_ClassFinder extends TestCase {

	public void testFindAll1() throws FileNotFoundException, IOException {
		IDocumentProvider doc = getDoc("TC_ClassInstanciationFinder_test_1.rb");

		ClassNode[] classNodes = new ClassFinder(doc, "TestClass", "").findParts().toArray(new ClassNode[] {});
		assertEquals(3, classNodes.length);
		for (ClassNode node : classNodes) {
			assertEquals("TestClass", node.getCPath().getName());
		}
	}

	private IDocumentProvider getDoc(String testFile) throws FileNotFoundException, IOException {
		return new DocumentWithIncluding(new MultiFileTestData("", "", "", testFile + ".test_properties"));
	}
	
	public void testFindAll2() throws FileNotFoundException, IOException {
		IDocumentProvider doc = getDoc("TC_ClassInstanciationFinder_test_2.rb");

		ClassNode[] classNodes = new ClassFinder(doc, "TestClass", "").findParts().toArray(new ClassNode[] {});
		assertEquals(1, classNodes.length);
	}
	
	public void testFindAll3() throws FileNotFoundException, IOException {
		IDocumentProvider doc = getDoc("TC_ClassInstanciationFinder_test_3.rb");

		ClassNode[] classNodes = new ClassFinder(doc, "TestClass", "").findParts().toArray(new ClassNode[] {});
		assertEquals(4, classNodes.length);
	}
	
	public void testFindAllWithModule() throws FileNotFoundException, IOException {
		IDocumentProvider doc = getDoc("TC_ClassInstanciationFinder_test_1.rb");

		ClassNode[] classNodes = new ClassFinder(doc, "TestClass", "M::").findParts().toArray(new ClassNode[] {});
		assertEquals(1, classNodes.length);
		for (ClassNode node : classNodes) {
			assertEquals("TestClass", node.getCPath().getName());
		}
	}
	
	public void testFindChildren() throws FileNotFoundException, IOException {
		IDocumentProvider doc = getDoc("TC_ClassInstanciationFinder_test_4.rb");
		ClassNode[] classNodes = new ClassFinder(doc, "TestClass", "").findChildren().toArray(new ClassNode[] {});
		
		assertEquals(4, classNodes.length);
		
		assertEquals("Subclass1", classNodes[0].getCPath().getName());
		assertEquals("Subclass1", classNodes[1].getCPath().getName());
		assertEquals("Subclass2", classNodes[2].getCPath().getName());
		assertEquals("Subclass3", classNodes[3].getCPath().getName());
	}
}

