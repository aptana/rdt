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

import java.util.Collection;

import org.jruby.ast.ConstNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.rubypeople.rdt.refactoring.core.ModuleNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ModuleNodeWrapper;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;

public class TC_ModuleNodeProvider extends FileTestCase {

	public TC_ModuleNodeProvider() {
		super("Module Node Provider");
	}

	private ModuleNodeWrapper findModule(String file, int position) {
		return ModuleNodeProvider.getSelectedModuleNode(getRootNode(file),position);
	}
	
	private Collection<ModuleNodeWrapper> findModules(String file, ModuleNodeWrapper moduleNode) {
		return ModuleNodeProvider.findOtherParts(new StringDocumentProvider("TC_ModuleNodeProvider", getSource(file)), moduleNode);
	}
	
	public void testSimpleModule() {
		ModuleNodeWrapper moduleNode = findModule("TC_ModuleNodeProvider_SimpleModule.rb", 8);
		
		assertNull(moduleNode.getParentModule());
		assertNotNull(moduleNode.getWrappedNode());
		assertEquals("Modul", moduleNode.getName());
		assertEquals("Modul", moduleNode.getFullName());
	}

	public void testModuleWithParent() {
		ModuleNodeWrapper moduleNode = findModule("TC_ModuleNodeProvider_ModuleWithParent.rb", 31);
		
		assertNotNull(moduleNode.getParentModule());
		assertNotNull(moduleNode.getParentModule().getWrappedNode());
		assertNull(moduleNode.getParentModule().getParentModule());
		assertNotNull(moduleNode.getWrappedNode());
		assertEquals("Modul", moduleNode.getName());
		assertEquals("OuterModule::Modul", moduleNode.getFullName());
	}
	
	public void testModuleWithMultipleParents() {
		ModuleNodeWrapper moduleNode = findModule("TC_ModuleNodeProvider_ModuleWithMultipleParents.rb", 68);

		assertEquals("M5", moduleNode.getName());
		assertEquals("M1::M2::M3::M4::M5", moduleNode.getFullName());
	}
	
	public void testTwoSimpleModules() {
		ModuleNodeWrapper[] nodeWrappers = getModules("TC_ModuleNodeProvider_TwoSimpleModules.rb", 8).toArray(new ModuleNodeWrapper[]{});

		assertEquals(2, nodeWrappers.length);
		assertEquals("Modul", nodeWrappers[0].getFullName());	
		assertEquals("Modul", nodeWrappers[1].getFullName());	
	}

	private Collection<ModuleNodeWrapper> getModules(String fileName, int pos) {
		ModuleNodeWrapper moduleNode = findModule(fileName, pos);
		return findModules(fileName, moduleNode);
	}

	public void testFindSingleMethod() {
		Collection<ModuleNodeWrapper> modules = getModules("TC_ModuleNodeProvider_FindSingleMethod.rb", 8);
		ConstNode[] nodes = ModuleNodeProvider.getAllModuleMethodDefinitions(modules).toArray(new ConstNode[]{});
		
		assertEquals(2, nodes.length);
		assertEquals("Modul", nodes[0].getName());
		assertEquals(new IDESourcePosition("TC_ModuleNodeProvider", 2, 2, 19, 24), nodes[0].getPosition());
		
		assertEquals("Modul", nodes[1].getName());
		assertEquals(new IDESourcePosition("TC_ModuleNodeProvider", 14, 14, 145, 150), nodes[1].getPosition());
	}
	
	public void testFindMultipleMethods() {
		Collection<ModuleNodeWrapper> modules = getModules("TC_ModuleNodeProvider_FindMultipleMethods.rb", 8);
		ConstNode[] nodes = ModuleNodeProvider.getAllModuleMethodDefinitions(modules).toArray(new ConstNode[]{});
		
		assertEquals(4, nodes.length);
		assertEquals("Modul", nodes[0].getName());
		assertEquals(new IDESourcePosition("TC_ModuleNodeProvider", 2, 2, 19, 24), nodes[0].getPosition());
		
		assertEquals("Modul", nodes[1].getName());
		assertEquals(new IDESourcePosition("TC_ModuleNodeProvider", 14, 14, 145, 150), nodes[1].getPosition());
		
		assertEquals("Modul", nodes[2].getName());
		assertEquals(new IDESourcePosition("TC_ModuleNodeProvider", 16, 16, 175, 180), nodes[2].getPosition());
		
		assertEquals("Modul", nodes[3].getName());
		assertEquals(new IDESourcePosition("TC_ModuleNodeProvider", 18, 18, 207, 212), nodes[3].getPosition());
	}
}
