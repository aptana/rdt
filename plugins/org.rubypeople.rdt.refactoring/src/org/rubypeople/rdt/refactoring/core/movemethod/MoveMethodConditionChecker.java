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

package org.rubypeople.rdt.refactoring.core.movemethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;

public class MoveMethodConditionChecker extends RefactoringConditionChecker {

	private MoveMethodConfig config;

	public MoveMethodConditionChecker(MoveMethodConfig config) {
		super(config);
	}

	@Override
	public void init(IRefactoringConfig configObj) {
		this.config = (MoveMethodConfig) configObj;
		Node rootNode = config.getDocumentProvider().getActiveFileRootNode();
		int caretPos = config.getCaretPosition();
		config.setAllClassesNodeProvider(config.getDocumentProvider().getProjectClassNodeProvider());
		try {
			ClassNodeWrapper selectedClassPart = SelectionNodeProvider.getSelectedClassNode(rootNode, caretPos);
			config.setSourceClassNode(selectedClassPart.getName());
		} catch (NoClassNodeException e) {
			/*don't care*/
		}
		MethodDefNode methodDefNode = (MethodDefNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, caretPos, MethodDefNode.class);
		MethodNodeWrapper methodNode = (methodDefNode == null) ? null : new MethodNodeWrapper(methodDefNode, config.getSourceClassNode());
		config.setMethodNode(methodNode);
		initTargetClassNames();
		if(config.getSourceClassNode() != null) {
			initFieldInSourceClassOfTypeDestinationClassNames();
		}
	}

	private void initTargetClassNames() {
		Collection<ClassNodeWrapper> forbiddenClassNodes = new ArrayList<ClassNodeWrapper>();
		forbiddenClassNodes.addAll(config.getAllClassesNodeProvider().getClassAndAllSubClasses(config.getSourceClassNode()));
		forbiddenClassNodes.addAll(config.getAllClassesNodeProvider().getClassAndAllSuperClasses(config.getSourceClassNode()));
		Collection<String> classNames = new TreeSet<String>();
		for(ClassNodeWrapper aktClassNode : config.getAllClassesNodeProvider().getAllClassNodes()) {
			if(!forbiddenClassNodes.contains(aktClassNode)) {
				classNames.add(aktClassNode.getName());
			}
		}
		config.setTargetClassNames(classNames);
	}
	
	private void initFieldInSourceClassOfTypeDestinationClassNames() {
		Collection<String> names = new LinkedHashSet<String>();
		for(FieldNodeWrapper aktField : config.getSourceClassNode().getFields()) {
			if(aktField.getNodeType() == FieldNodeWrapper.SYMBOL_NODE) {
				names.add('@' + aktField.getName());
			} else if(aktField.isInstVar()) {
				names.add(aktField.getName());
			}
		}
		config.setFieldInSourceClassOfTypeDestinationClassNames(names);
	}
	
	@Override
	protected void checkInitialConditions() {

		if(config.getSourceClassNode() == null) {
			addError(Messages.MoveMethodConditionChecker_NeedsToBeInsideClass);
		} else if (config.getMethodNode() == null) {
			addError(Messages.MoveMethodConditionChecker_NeedsToBeInsideMethod);
		} else if (isConstructor()) {
			addError(Messages.MoveMethodConditionChecker_CannotMoveConstructor);
		} else if (!hasTargetClass()) {
			addError(Messages.MoveMethodConditionChecker_NoTarget);
		} else if (!hasFieldToSelect()) {
			addError(Messages.MoveMethodConditionChecker_NoFieldOfTargetType);
		}
	}

	private boolean hasFieldToSelect() {
		return !(config.needsSecondPage() && config.getFieldInSourceClassOfTypeDestinationClassNames().isEmpty());
	}

	private boolean hasTargetClass() {
		return !config.getTargetClassNames().isEmpty();
	}

	private boolean isConstructor() {
		MethodNodeWrapper selectedMethod = config.getMethodNode();
		return selectedMethod != null && selectedMethod.isConstructor();
	}

	@Override
	protected void checkFinalConditions() {
		checkMethodContainsClassFields();
		checkIsMethodPublicAndNoDelegateMethod();
		addConfigWarnings();
	}

	private void checkIsMethodPublicAndNoDelegateMethod() {
		boolean isPrivate = config.getSourceClassNode().getMethodVisibility(config.getMethodNode()).equals(METHOD_VISIBILITY.PRIVATE);
		if(!isPrivate && !config.leaveDelegateMethodInSource()) {
			addWarning(Messages.MoveMethodConditionChecker_TheMethod + config.getMethodNode().getName() 
					+ Messages.MoveMethodConditionChecker_CanBeCalledFromOutside + config.getSourceClassNode() 
					+ Messages.MoveMethodConditionChecker_MightNotGetReplaced + config.getDestinationClassNode().getName() + "."); //$NON-NLS-1$
		}
	}

	private void addConfigWarnings() {
		for(String aktWarning : config.getWarnings()) {
			addWarning(aktWarning);
		}
	}

	private void checkMethodContainsClassFields() {
		for(FieldNodeWrapper aktField : NodeProvider.getFieldNodes(config.getMethodNode().getWrappedNode())) {
			if(aktField.isClassVar()) {
				addWarning(Messages.MoveMethodConditionChecker_TheMethod + config.getMethodNode().getName() + Messages.MoveMethodConditionChecker_ContainsClassField + aktField.getName()
						+ Messages.MoveMethodConditionChecker_MovingMightAffectTheFunctionality + config.getSourceClassNode().getName() + "\".");  //$NON-NLS-1$
			}
		}
	}
}
