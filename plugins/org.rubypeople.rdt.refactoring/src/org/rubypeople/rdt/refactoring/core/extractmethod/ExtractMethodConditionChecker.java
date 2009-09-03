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

package org.rubypeople.rdt.refactoring.core.extractmethod;

import java.util.Collection;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.BreakNode;
import org.jruby.ast.CaseNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.ForNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.NextNode;
import org.jruby.ast.Node;
import org.jruby.ast.RedoNode;
import org.jruby.ast.RetryNode;
import org.jruby.ast.RootNode;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SuperNode;
import org.jruby.ast.WhenNode;
import org.jruby.ast.WhileNode;
import org.jruby.ast.YieldNode;
import org.jruby.ast.ZSuperNode;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class ExtractMethodConditionChecker extends RefactoringConditionChecker {

	private ExtractMethodConfig config;

	public ExtractMethodConditionChecker(ExtractMethodConfig config) {
		super(config);
	}
	
	public void init(IRefactoringConfig configObj) {
		this.config = (ExtractMethodConfig) configObj;
		initEnclosingNodes();
		if (!NodeProvider.isEmptyNode(config.getSelectedNodes()) && config.getExtractMethodHelper() == null ) {
			config.setExtractedMethodHelper(new ExtractedMethodHelper(config));
		}
	}
	
	private void initEnclosingNodes() {
		RootNode rootNode = config.getDocumentProvider().getActiveFileRootNode();
		config.setRootNode(rootNode);
		config.setEnclosingScopeNode(SelectionNodeProvider.getEnclosingScope(rootNode, config.getSelection().getStartOffset()));
		config.setEnclosingMethodNode((MethodDefNode) SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), MethodDefNode.class));
		config.setSelectedNodes(getSelectedNodes(rootNode));
		
		Node classNode = SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), ClassNode.class, SClassNode.class);
		try {
			config.setEnclosingClassNode(PartialClassNodeWrapper.getPartialClassNodeWrapper(classNode, rootNode));
		} catch (NoClassNodeException e) {
			/*don't care*/
		}
	}

	private Node getSelectedNodes(RootNode rootNode) {
		Node selectedNode = SelectionNodeProvider.getSelectedNodes(rootNode, config.getSelection());
		
		//If selected node is an WhenNode, take the enclosing CaseNode as selectedNode.
		if (NodeUtil.nodeAssignableFrom(selectedNode, WhenNode.class)) {
			selectedNode = SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), CaseNode.class);
		}
		if(NodeUtil.nodeAssignableFrom(selectedNode, ArrayNode.class)) {
			WhenNode enclosingWhen = (WhenNode) SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), WhenNode.class);
			if(enclosingWhen != null && SelectionNodeProvider.nodeEnclosesNode(enclosingWhen.getExpressionNodes(), selectedNode)) {
				selectedNode = SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), CaseNode.class);
			}
		}
		
		//Check on loopControlNodes (Break, Redo, Next, Retry)
		if(containsLoopControlNode(selectedNode)) {
			selectedNode = getLoopOrItsParent(rootNode, selectedNode);
		}
		
		//Check if content of an ArgsNode is selected
		if(SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), ArgsNode.class) != null) {
			return SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), MethodDefNode.class);
		}
		
		//Check if the selected Node is an argumentNode
		if(NodeUtil.nodeAssignableFrom(selectedNode, ArgumentNode.class)) {
			selectedNode = config.getEnclosingMethodNode();
		}
		
		//check if the selected Nodes are contained in an enclosing arrayNode
		//if not the fowllowing checks arent necessary anymore.
		ArrayNode enclosingArrayNode = (ArrayNode) SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), ArrayNode.class);
		if(!sectedNodesInArrayNode(enclosingArrayNode, selectedNode) || !NodeUtil.nodeAssignableFrom(selectedNode, ArrayNode.class)) {
			return selectedNode;
		}
		
		//Check if enclosingArrayNode is the argsNode of a MethodCallNode.
		Node enclosingMethodCallNode = SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), MethodCallNodeWrapper.METHOD_CALL_NODE_CLASSES());
		MethodCallNodeWrapper enclosingMethodCall = new MethodCallNodeWrapper(enclosingMethodCallNode);
		if(NodeUtil.nodeAssignableFrom(enclosingMethodCall.getArgsNode(), ArrayNode.class)) {
			ArrayNode enclosingMethodCallArgs = (ArrayNode) enclosingMethodCall.getArgsNode();
			if(enclosingArrayNode == enclosingMethodCallArgs)
				return enclosingMethodCallNode;
		}
		
		//Check if enclosingArrayNode is the receiver node of a multiAsgnNode
		MultipleAsgnNode asgnNode = (MultipleAsgnNode) SelectionNodeProvider.getEnclosingNode(rootNode, config.getSelection(), MultipleAsgnNode.class);
		if(asgnNode != null && NodeUtil.nodeAssignableFrom(asgnNode.getHeadNode(), ArrayNode.class)) {
//			ArrayNode multiAsgnHeadNode = (ArrayNode) asgnNode.getHeadNode();
//			if(enclosingArrayNode == multiAsgnHeadNode) {
//				return asgnNode;
//			}
		}
		return selectedNode;
	}

	private Node getLoopOrItsParent(RootNode rootNode, Node selectedNode) {
		Node loopNode = NodeProvider.getEnclosingNodeOfType(rootNode, selectedNode, WhileNode.class, ForNode.class, IterNode.class);
		if(loopNode != null) {
			selectedNode = loopNode;
		}
		if(NodeUtil.nodeAssignableFrom(loopNode, IterNode.class)) {
			selectedNode = NodeProvider.findParentNode(rootNode, loopNode);
		}
		return selectedNode;
	}

	private boolean containsLoopControlNode(Node selectedNode) {
		return !NodeProvider.getSubNodes(selectedNode, BreakNode.class, RedoNode.class, NextNode.class, RetryNode.class).isEmpty();
	}

	private boolean sectedNodesInArrayNode(ArrayNode arrayNode, Node selectedNode) {
		if(arrayNode == null) {
			return false;
		}
		Collection<Node> arrayChilds = NodeProvider.getAllNodes(arrayNode);
		for(Object actSelectedNode : selectedNode.childNodes()) {
			if(!arrayChilds.contains(actSelectedNode)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void checkFinalConditions() {
		checkNewMethodName();
	}

	private void checkNewMethodName() {
		String newMethodName = config.getHelper().getMethodName();
		PartialClassNodeWrapper enclosingClassNode = config.getEnclosingClassNode();
		if (enclosingClassNode != null) {
			Collection<Node> methodNodes = NodeProvider.getSubNodes(enclosingClassNode.getWrappedNode(), DefnNode.class);
			for (Node aktNode : methodNodes) {
				if (((DefnNode)aktNode).getName().equals(newMethodName)) {
					addError(Messages.ExtractMethodConditionChecker_MethodAlreadyExists);
				}
			}
		}
	}

	@Override
	protected void checkInitialConditions() {
		if(!existSelectedNodes()) {
			addError(Messages.ExtractMethodConditionChecker_NothingToDo);
		} else if (containsYieldStatements()) {
			addError(Messages.ExtractMethodConditionChecker_NotPossibleContainsYield);
		} else if (containsSuperStatement()) {
			addError(Messages.ExtractMethodConditionChecker_NotPossibleContainsSuper);
		} else if (isModuleInSelection()) {
			addError(Messages.ExtractMethodConditionChecker_NotPossibleModule);
		} else if (isClassInSelection()) {
			addError(Messages.ExtractMethodConditionChecker_MustNotContainAClass);
		} else if (isMethodInSelction()) {
			addError(Messages.ExtractMethodConditionChecker_MustNotContainAMethod);
		} else {
			checkInternalMethods();
		}
	}

	private boolean existSelectedNodes() {
		return !NodeProvider.isEmptyNode(config.getSelectedNodes());
	}
	
	private boolean containsYieldStatements() {
		return NodeProvider.hasSubNodes(config.getSelectedNodes(), YieldNode.class);
	}

	private boolean containsSuperStatement() {
		return NodeProvider.hasSubNodes(config.getSelectedNodes(), SuperNode.class, ZSuperNode.class);
	}	
	
	private boolean isModuleInSelection() {
		Node selctedNodes = config.getSelectedNodes();
		Collection<Node> moduleNodes = NodeProvider.getSubNodes(selctedNodes, ModuleNode.class);
		return !moduleNodes.isEmpty();
	}

	private boolean isClassInSelection() {
		Node selctedNodes = config.getSelectedNodes();
		Collection<Node> classNodes = NodeProvider.getSubNodes(selctedNodes, ClassNode.class);
		return !classNodes.isEmpty();
	}

	private boolean isMethodInSelction() {
		Node selctedNodes = config.getSelectedNodes();
		Collection<Node> methodNodes = NodeProvider.getSubNodes(selctedNodes, MethodDefNode.class);
		return !methodNodes.isEmpty();
	}

	private void checkInternalMethods() {
		if (config.hasEnclosingClassNode() && !config.hasEnclosingMethodNode()) {
			addError(Messages.ExtractMethodConditionChecker_NotInsideAMethod);
		}
		if (config.hasEnclosingClassNode() && NodeProvider.hasSubNodes(NodeUtil.getBody(config.getEnclosingScopeNode()), DefnNode.class)) {
			addError(Messages.ExtractMethodConditionChecker_MustNotContainSubmethods);
		}

	}

}
