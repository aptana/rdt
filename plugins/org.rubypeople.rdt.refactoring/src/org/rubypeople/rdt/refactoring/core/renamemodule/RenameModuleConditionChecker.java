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

package org.rubypeople.rdt.refactoring.core.renamemodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.SClassNode;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.ModuleNodeProvider;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.nodewrapper.ModuleNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NameHelper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class RenameModuleConditionChecker extends RefactoringConditionChecker {
	
	public static final String DEFAULT_ERROR = "Please select the name of a module definition.";

	private RenameModuleConfig config;

	public RenameModuleConditionChecker(IRefactoringConfig config) {
		super(config);
	}

	@Override
	protected void checkInitialConditions() {
		if (config.getSelectedModule() == null) {
			addError(DEFAULT_ERROR);
		}
	}

	@Override
	protected void checkFinalConditions() {
		if (config.getOriginalName().equals(config.getNewName())) {
			addWarning("You did not change the name.");
		}
		if(config.getAllModuleNames().contains(config.getNewName())) {
			addWarning("The name you chose is already in use.");
		}
	}

	@Override
	public void init(IRefactoringConfig configObj) {
		config = (RenameModuleConfig) configObj;
		
		ModuleNodeWrapper selectedModule = ModuleNodeProvider.getSelectedModuleNode(config.getDocumentProvider().getActiveFileRootNode(), config.getCarretPosition());
		if(selectedModule == null || caretIsNotOnModuleName(selectedModule)) {
			return;
		}
		config.setSelectedModule(selectedModule);
		config.setNewName(config.getOriginalName());
		config.setModuleParts(ModuleNodeProvider.findOtherParts(config.getDocumentProvider(), config.getSelectedModule()));

		config.setIncludes(new ModuleIncludeFinder(config.getDocumentProvider()).find(config.getOriginalFullName()));
		
		config.setPossibleCalls(findPossibleCalls());
		config.setSelectedCalls(config.getPossibleCalls());
		
		config.setAllModuleNames(getAllModuleNames());
	}

	private Collection<String> getAllModuleNames() {
		Collection<String> names = new ArrayList<String>();
		for(ModuleNodeWrapper module : ModuleNodeProvider.findAllModules(config.getDocumentProvider())) {
			names.add(module.getFullName());
		}
		return names;
	}

	private ArrayList<ModuleSpecifierWrapper> findPossibleCalls() {
		ArrayList<ModuleSpecifierWrapper> calls = new ArrayList<ModuleSpecifierWrapper>();
		
		Collection<Node> toSkip = collectAllModulePartsAndIncludeNameNodes();

		for(String file : config.getDocumentProvider().getFileNames()) {
			RootNode rootNode = config.getDocumentProvider().getRootNode(file);
			for (final Node node : NodeProvider.getSubNodes(rootNode, ConstNode.class, Colon2Node.class)) {
				if(toSkip.contains(node) 
						|| NodeProvider.findParentNode(rootNode, node) instanceof ClassNode 
						|| NodeProvider.findParentNode(rootNode, node) instanceof SClassNode) {
					continue;
				}
				ModuleSpecifierWrapper module = ModuleSpecifierWrapper.create(node, NameHelper.getEncosingModulePrefix(rootNode, node));
				
				if(module.getFullName().equals(config.getOriginalFullName())) {		
					calls.add(module);
				}
			}
		}
		
		return calls;
	}

	private Collection<Node> collectAllModulePartsAndIncludeNameNodes() {
		Collection<Node> toSkip = new HashSet<Node>();
		
		for(ModuleNodeWrapper part : config.getModuleParts()) {
			toSkip.add(part.getWrappedNode().getCPath());
		}
		for(ModuleSpecifierWrapper include : config.getIncludes()) {
			toSkip.add(include.getWrappedNode());
		}
		return toSkip;
	}

	private boolean caretIsNotOnModuleName(ModuleNodeWrapper selectedModule) {
		return !NodeUtil.positionIsInNode(config.getCarretPosition(), selectedModule.getWrappedNode().getCPath());
	}
}
