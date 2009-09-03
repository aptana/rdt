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

package org.rubypeople.rdt.refactoring.core.renamelocal;

import java.util.Collection;

import org.jruby.ast.ArgumentNode;
import org.jruby.ast.AssignableNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NameValidator;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class RenameLocalConditionChecker extends RefactoringConditionChecker {

	private static final String ALREADY_EXISTS = Messages.RenameLocalConditionChecker_NameAlreadyExists;

	private static final String INVALID_NAME = Messages.RenameLocalConditionChecker_NameInvalid;

	private static final String NO_VARIABLE_SELECTED = Messages.RenameLocalConditionChecker_NoSelection;

	private static final String NO_LOCAL_VARIABLES = Messages.RenameLocalConditionChecker_NoLocalVariable;

	private static final Class[] SELECTED_NODE_TYPES = {LocalVarNode.class, LocalAsgnNode.class, ArgumentNode.class,
		BlockArgNode.class, DVarNode.class, DAsgnNode.class};

	public static final String DEFAULT_ERROR = NO_LOCAL_VARIABLES;
	
	private RenameLocalConfig config;

	public RenameLocalConditionChecker(RenameLocalConfig config) {
		super(config);
	}
	
	public void init(IRefactoringConfig configObj) {
		config = (RenameLocalConfig) configObj;
		RootNode rootNode = config.getDocumentProvider().getActiveFileRootNode();
		Node selectedNode = SelectionNodeProvider.getSelectedNodeOfType(rootNode, config.getCaretPosition(), SELECTED_NODE_TYPES);
		if(selectedNode instanceof AssignableNode) {
			int start = selectedNode.getPosition().getStartOffset();
			int end = start + ((INameNode) selectedNode).getName().length();
			if(config.getCaretPosition() < start || config.getCaretPosition() > end) {
				return;
			}
		}
		
		config.setSelectedNode(selectedNode);
		if(selectedNode == null) {
			Collection<MethodDefNode> methodNodes = NodeProvider.getMethodNodes(rootNode);
			config.setSelectedMethod(SelectionNodeProvider.getSelectedNodeOfType(methodNodes, config.getCaretPosition(), MethodDefNode.class));
		} else {
			config.setSelectedMethod(SelectionNodeProvider.getEnclosingScope(rootNode, selectedNode));
		}
		if(config.getSelectedMethod() != null) {
			config.setLocalNames(NodeUtil.getScope(config.getSelectedMethod()).getVariables());
		}
	}
	
	@Override
	protected void checkInitialConditions() {
		if ((!config.hasSelectedNode() || !isSelectedNodeLocalVar()) && !isSelectionInMethodDefinition()) {
			addError(NO_LOCAL_VARIABLES);
		}
	}
	
	private boolean isSelectionInMethodDefinition() {
		if(config.getSelectedMethod() == null) {
			return false;
		}
		ISourcePosition position = ((MethodDefNode) config.getSelectedMethod()).getArgsNode().getPosition();
		return position.getStartOffset() <= config.getCaretPosition() && position.getEndOffset() >= config.getCaretPosition();
	}

	private boolean isSelectedNodeLocalVar() {
		Node selected = config.getSelectedNode();
		if(NodeUtil.nodeAssignableFrom(selected, LocalNodeWrapper.LOCAL_NODES_CLASSES)){
			return true;
		}
		if(NodeUtil.nodeAssignableFrom(selected, ArgumentNode.class, BlockArgNode.class) && NodeUtil.nodeAssignableFrom(config.getSelectedMethod(), MethodDefNode.class)) {
			MethodDefNode methodNode = (MethodDefNode) config.getSelectedMethod();
			return methodNode.getNameNode() != selected;
		}
		return false;
	}

	@Override
	protected void checkFinalConditions() {
		RenameLocalEditProvider editProvider = config.getRenameEditProvider();
		if (editProvider.getSelectedVariableName().equals("") && editProvider.getNewVariableName().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			addError(NO_VARIABLE_SELECTED);
		}

		if (!NameValidator.isValidLocalVariableName(editProvider.getNewVariableName())) {
			addError(INVALID_NAME);
		}

		if (editProvider.getSelectedVariableName().equals(editProvider.getNewVariableName())) {
			addError(Messages.RenameLocalConditionChecker_SameName);
		}

		for (String s : config.getLocalNames()) {
			if (editProvider.getNewVariableName().equals(s)) {
				addError(ALREADY_EXISTS);
			}
		}
	}

}
