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

package org.rubypeople.rdt.refactoring.core.encapsulatefield;

import org.jruby.ast.DefnNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;

public class EncapsulateFieldConditionChecker extends RefactoringConditionChecker {

	private EncapsulateFieldConfig config;
	private Node rootNode;

	public EncapsulateFieldConditionChecker(EncapsulateFieldConfig config) {
		super(config);
	}

	public void init(IRefactoringConfig configObj) {
		config = (EncapsulateFieldConfig) configObj;
		rootNode = config.getDocumentProvider().getActiveFileRootNode();
		config.setSelectedInstNode(findSelectedInstNode(config.getCaretPosition()));
		if (!config.hasSelectedInstNode()) {
			return;
		}
		config.setSelectedAccessor(findSelectedAccessor(config.getSelectedInstNode()));
		try {
			config.setEnclosingClassNode(SelectionNodeProvider.getSelectedClassNode(rootNode, config.getCaretPosition()));
		} catch (NoClassNodeException e) {
			/*don't care*/
		}
	}
	
	private AttrAccessorNodeWrapper findSelectedAccessor(INameNode selectedInstNode) {
		return SelectionNodeProvider.getSelectedAccessorNode(rootNode, selectedInstNode);
	}
	
	private INameNode findSelectedInstNode(int caretPosition) {
		return (INameNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, caretPosition, InstVarNode.class, InstAsgnNode.class, SymbolNode.class);
	}
	
	public void checkFinalConditions() {
		if (config.isWriterGenerationDisabled() && config.isReaderGenerationDisabled()) {
			addError(Messages.EncapsulateFieldConditionChecker_NothingToRefactor);
		}
		String readerMethodName = config.getReaderMethodName();
		String writerMethodName = config.getWriterMethodName();
		for (MethodNodeWrapper aktMethodNode : config.getEnclosingClassNode().getMethods()) {
			if (aktMethodNode.getName().equals(readerMethodName) && !config.isReaderGenerationDisabled()) {
				addWarning(Messages.EncapsulateFieldConditionChecker_MethodWithName + aktMethodNode.getName() + Messages.EncapsulateFieldConditionChecker_AlreadyExists);
			}
			if (aktMethodNode.getName().equals(writerMethodName) && !config.isWriterGenerationDisabled()) {
				addWarning(Messages.EncapsulateFieldConditionChecker_MethodWithName + aktMethodNode.getName() + Messages.EncapsulateFieldConditionChecker_AlreadyExists);
			}
		}
	}

	public void checkInitialConditions() {
		if (!config.hasSelectedAccessor() && !config.hasSelectedInstNode()) {
			addError(Messages.EncapsulateFieldConditionChecker_NoInstanceVariableSelected);
		} else if (config.getEnclosingClassNode() == null) {
			addError(Messages.EncapsulateFieldConditionChecker_NotInsideAClass);
		} else if (selectedNodeIsInstVarNodeAndNotInMethod()) {
			addError(Messages.EncapsulateFieldConditionChecker_NotInsideAMethod);
		}
	}

	private boolean selectedNodeIsInstVarNodeAndNotInMethod() {
		Node selectedVarNode = SelectionNodeProvider.getSelectedNodeOfType(config.getDocumentProvider().getActiveFileRootNode(), config.getCaretPosition(), InstVarNode.class, InstAsgnNode.class);
		if (selectedVarNode == null) {
			return false;
		}
		Node enclosingMethod = SelectionNodeProvider.getSelectedNodeOfType(config.getDocumentProvider().getActiveFileRootNode(), config.getCaretPosition(), DefnNode.class);
		return enclosingMethod == null;
	}
}
