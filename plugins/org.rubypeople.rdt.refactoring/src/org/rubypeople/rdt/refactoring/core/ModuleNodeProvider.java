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

package org.rubypeople.rdt.refactoring.core;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.ConstNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ModuleNodeWrapper;

public abstract class ModuleNodeProvider {
	
	private interface IModuleAcceptor {
		boolean accept(ModuleNodeWrapper wrapper);
	}

	public static ModuleNodeWrapper getSelectedModuleNode(Node root, int pos) {
		
		ModuleNode module = (ModuleNode) SelectionNodeProvider.getSelectedNodeOfType(root, pos, ModuleNode.class);
		if(module == null) {
			return null;
		}
		return createModuleNodeWrapper(root, module);
	}
	
	private static Collection<ModuleNodeWrapper> findModules(IDocumentProvider doc, IModuleAcceptor acceptor) {
		ArrayList<ModuleNodeWrapper> modules = new ArrayList<ModuleNodeWrapper>();
		
		for (String file : doc.getFileNames()) {
			for (Node node : NodeProvider.getSubNodes(doc.getRootNode(file), ModuleNode.class)) {
				ModuleNode moduleNode = (ModuleNode) node;
				ModuleNodeWrapper wrapper = createModuleNodeWrapper(doc.getRootNode(file), moduleNode);
				if(acceptor.accept(wrapper)) {
					modules.add(wrapper);
				}
			}
		}
		
		return modules;
	}
	
	public static Collection<ModuleNodeWrapper> findOtherParts(IDocumentProvider doc, final ModuleNodeWrapper module) {
		return findModules(doc, new IModuleAcceptor(){

			public boolean accept(ModuleNodeWrapper wrapper) {
				return wrapper.getFullName().equals(module.getFullName());
			}});
	}
	
	public static Collection<ModuleNodeWrapper> findAllModules(IDocumentProvider doc) {
		return findModules(doc, new IModuleAcceptor(){

			public boolean accept(ModuleNodeWrapper wrapper) {
				return true;
			}});
	}
	
	public static Collection<ConstNode> getAllModuleMethodDefinitions(Collection<ModuleNodeWrapper> modules) {
		ArrayList<ConstNode> methods = new ArrayList<ConstNode>();
		
		for (ModuleNodeWrapper wrapper : modules) {
			methods.addAll(wrapper.getModuleMethodConstNodes());
		}
		
		return methods;
	}

	private static ModuleNodeWrapper createModuleNodeWrapper(Node root, ModuleNode module) {
		ArrayList<ModuleNode> modules = new ArrayList<ModuleNode>();
		modules.add(module);
		ModuleNode parent = null;
		
		while(true) {
			parent = (ModuleNode) NodeProvider.findParentNode(root, module, ModuleNode.class);
			if(parent == null)
				break;
			modules.add(0, parent);
			module = parent;
		}
		
		ModuleNodeWrapper previousWrapper = null;
		for (ModuleNode node : modules) {
			ModuleNodeWrapper nodeWrapper = new ModuleNodeWrapper(node, previousWrapper);
			previousWrapper = nodeWrapper;
		}
		return previousWrapper;
	}
}
