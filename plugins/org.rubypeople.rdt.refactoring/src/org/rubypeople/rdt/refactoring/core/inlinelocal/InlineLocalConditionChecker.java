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

package org.rubypeople.rdt.refactoring.core.inlinelocal;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.ClassNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;
import org.rubypeople.rdt.refactoring.util.JRubyRefactoringUtils;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class InlineLocalConditionChecker extends RefactoringConditionChecker {

	private InlineLocalConfig config;
	private RootNode rootNode;

	public InlineLocalConditionChecker(InlineLocalConfig config) {
		super(config);
	}
	
	public void init(IRefactoringConfig configObj) {
		this.config = (InlineLocalConfig) configObj;
		rootNode = config.getDocumentProvider().getActiveFileRootNode();
		int caretPosition = config.getCaretPosition();
		config.setEnclosingMethod((MethodDefNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, caretPosition, MethodDefNode.class));
		
		config.setEnclosingScopeNode(SelectionNodeProvider.getEnclosingScope(rootNode, caretPosition));
		
		Node locVarNode = SelectionNodeProvider.getSelectedNodeOfType(rootNode, caretPosition, LocalNodeWrapper.LOCAL_NODES_CLASSES);
		if (locVarNode == null) {
			return;
		}

		config.setSelectedItem(new LocalNodeWrapper(locVarNode));
		config.setSelectedItemName(LocalNodeWrapper.getLocalNodeName(config.getSelectedItem()));

		initDefinitionNode();
		initLocalOccurrences();
	}
	private void initDefinitionNode() {
		Collection<LocalNodeWrapper> asgnNodes = LocalNodeWrapper.gatherLocalAsgnNodes(config.getEnclosingScopeNode());
		for (LocalNodeWrapper currentAsgnNode : asgnNodes) {
			if (currentAsgnNode.getName().equals(config.getSelectedItemName())) {
				config.setDefinitionNode(currentAsgnNode);
			}
		}
	}

	private void initLocalOccurrences() {
		Collection<LocalNodeWrapper> localOccurrences = new ArrayList<LocalNodeWrapper>();
		Collection<LocalNodeWrapper> nodesInMethod = LocalNodeWrapper.gatherLocalVarNodes(config.getEnclosingScopeNode());
		for (LocalNodeWrapper currentLocalNode : nodesInMethod) {
			String currentNodeName = LocalNodeWrapper.getLocalNodeName(currentLocalNode);
			if (currentNodeName.equals(config.getSelectedItemName())) {
				localOccurrences.add(currentLocalNode);
			}
		}
		config.setLocalOccurences(localOccurrences);
	}
	
	@Override
	protected void checkFinalConditions() {
		if (!isNewMethodNameUnique()) {
			addError(Messages.InlineLocalConditionChecker_NameNotUnique);
		}
	}

	@Override
	protected void checkInitialConditions() {
		if (config.getSelectedItem() == null) {
			addError(Messages.InlineLocalConditionChecker_NoLocalVariable);
		} else if (isTempParameter()) {
			addError(Messages.InlineLocalConditionChecker_CannotMethodParameters);
		} else if (isBlockArgument()) {
			addError(Messages.InlineLocalConditionChecker_CannotBlockArgument);
		} else if (isTempMultiassigned()) {
			addError(Messages.InlineLocalConditionChecker_CannotMultiAssigned);
		} else if (defintiontionContainsItself()) {
			addError(Messages.InlineLocalConditionChecker_CannotSelfReferencing);
		} else if (isMultipleAsgnNode()) {
			addError(Messages.InlineLocalConditionChecker_CannotMultipleAssignments);
		} else if (!hasTarget())	{
			addError(Messages.InlineLocalConditionChecker_NoTarget);
		}
	}
	
	private boolean hasTarget() {
		return !config.getLocalOccurrences().isEmpty();
	}

	private boolean isBlockArgument() {
		IterNode enclosingIterNode = (IterNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, config.getCaretPosition(), IterNode.class);
		if(enclosingIterNode == null) {
			return false;
		}
		Node varNode = enclosingIterNode.getVarNode();
		return config.getSelectedItem().getWrappedNode().equals(varNode);
	}

	private boolean isMultipleAsgnNode() {
		Node enclosingMultipleAssignmentNode = SelectionNodeProvider.getSelectedNodeOfType(config.getEnclosingScopeNode(), config.getCaretPosition(), MultipleAsgnNode.class);
		return enclosingMultipleAssignmentNode != null;
	}

	private boolean isTempParameter() {
		if (config.getEnclosingMethod() == null || !config.getSelectedItem().hasValidId()) {
			return false;
		}
		return JRubyRefactoringUtils.isParameter(config.getSelectedItem(), config.getEnclosingMethod());
	}

	private boolean isNewMethodNameUnique() {

		Node environment = SelectionNodeProvider.getSelectedNodeOfType(config.getDocumentProvider().getActiveFileRootNode(), config.getCaretPosition(), ClassNode.class, RootNode.class);
		Collection<MethodDefNode> methodNodes = NodeProvider.gatherMethodDefinitionNodes(NodeUtil.getBody(environment));

		for (MethodDefNode currentDefnNode : methodNodes) {
			if (currentDefnNode.getName().equals(config.getNewMethodName())) {
				return false;
			}
		}
		return true;
	}

	private boolean isTempMultiassigned() {
		Collection<LocalNodeWrapper> nodesInMethod = LocalNodeWrapper.gatherLocalAsgnNodes(config.getEnclosingScopeNode());
		int countOccurrence = 0;
		for (LocalNodeWrapper currentLocalNode : nodesInMethod) {
			String currentNodeName = LocalNodeWrapper.getLocalNodeName(currentLocalNode);
			if (currentNodeName.equals(config.getSelectedItemName())) {
				countOccurrence++;
			}
		}
		return countOccurrence > 1;
	}
	
	private boolean defintiontionContainsItself() {
		if(config.getDefinitionNode() == null) {
			return false;
		}
		ISourcePosition defPosition = config.getDefinitionNode().getWrappedNode().getPosition();

		for (LocalNodeWrapper currentOccurrence : config.getLocalOccurrences()) {
			ISourcePosition occurrencePosition = currentOccurrence.getWrappedNode().getPosition();
			if (defPosition.getStartOffset() <= occurrencePosition.getStartOffset() && defPosition.getEndOffset() >= occurrencePosition.getEndOffset()) {
				return true;
			}
		}
		return false;
	}

}
