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

package org.rubypeople.rdt.refactoring.core.inlinemethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.types.INameNode;
import org.jruby.parser.StaticScope;
import org.rubypeople.rdt.refactoring.classnodeprovider.IncludedClassesProvider;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class InlineMethodConditionChecker extends RefactoringConditionChecker {

	private InlineMethodConfig config;
	

	public InlineMethodConditionChecker(InlineMethodConfig config) {
		super(config);
	}
	
	public void init(IRefactoringConfig configObj) {
		this.config = (InlineMethodConfig) configObj;

		if(!(findSelectedCall(config.getPos()) && findTargetClass(config.getTargetClassFinder()) && findMethodDefinition())) {
			return;
		}
		config.setCallParent(NodeProvider.findParentNode(config.getDocumentProvider().getActiveFileRootNode(), config.getSelectedCall().getWrappedNode()));
		
		replaceParameters();
		
		if(resultIsAssigned()) {
			replaceReturnStatements();
		}
		
		createInlinedMethodBody(config.getDocumentProvider());
		renameDuplicates(config.getDocumentProvider());
	}
	
	private void renameDuplicates(IDocumentProvider doc) {
		StaticScope parent = NodeUtil.getScope(SelectionNodeProvider.getEnclosingScope(doc.getActiveFileRootNode(), config.getSelectedCall().getWrappedNode()));
		
		ArrayList<String> localNames = new ArrayList<String>();
		if(parent.getVariables() != null) {
			for(String name : parent.getVariables()) {
				localNames.add(name);
			}
		}
		
		if(resultIsAssigned()) {
			AssignableNode callParent = (AssignableNode) config.getCallParent();
			String name;
			if(callParent instanceof INameNode) {
				name = ((INameNode) callParent).getName();
				localNames.remove(name);
			} else {
				ListNode head = ((MultipleAsgnNode) callParent).getHeadNode();
				localNames.removeAll(head.childNodes());
			}
		}
		config.setMethodDefDoc(new RenameDuplicatedVariables().rename(new StringDocumentProvider(config.getMethodDefDoc()), localNames.toArray(new String[localNames.size()])));
	}

	private boolean findSelectedCall(int pos) {
		config.setSelectedCall(new SelectedCallFinder().findSelectedCall(pos, config.getDocumentProvider()));
		return config.getSelectedCall() != null;
	} 

	private boolean findTargetClass(ITargetClassFinder targetClassFinder) {
		config.setClassName(targetClassFinder.findTargetClass(config.getSelectedCall(), config.getDocumentProvider()));
		return config.getClassName() != null && !"".equals(config.getClassName()); //$NON-NLS-1$
	}

	private boolean findMethodDefinition() {
		config.setMethodDefinitionNode(new MethodFinder().find(config.getClassName(), config.getSelectedCall().getName(), config.getDocumentProvider()));
		return config.getMethodDefinitionNode() != null;
	}

	private void replaceParameters() {
		config.setMethodDefDoc(new ParameterReplacer().replace(config.getDocumentProvider(), config.getSelectedCall(), config.getMethodDefinitionNode()));
	}

	private void createInlinedMethodBody(IDocumentProvider doc) {
		MethodBodyStatementReplacer bodyReplacer = new MethodBodyStatementReplacer();
		if(config.getSelectedCall().getReceiverNode() != null) {
			final String name = ((INameNode)config.getSelectedCall().getReceiverNode()).getName();
			config.setMethodDefDoc(bodyReplacer.replaceSelfWithObject(config.getMethodDefDoc(), name));
			config.setMethodDefDoc(bodyReplacer.prefixCallsWithObject(config.getMethodDefDoc(),  new IncludedClassesProvider(doc), config.getClassName(), name));
			Collection<String> usedMembers = new HashSet<String>();
			config.setMethodDefDoc(bodyReplacer.replaceVarsWithAccessor(config.getMethodDefDoc(), name, usedMembers));
			config.setUsedMembers(usedMembers);
		}
		config.setMethodDefDoc(bodyReplacer.removeReturnStatements(config.getMethodDefDoc()));
	}

	private void replaceReturnStatements() {
		IReturnStatementReplacer returnReplacer = new ReturnStatementReplacer();
		config.setSingleReturnStatement(Boolean.valueOf((returnReplacer.singleReturnOnLastLine(config.getMethodDefDoc()))));
		if(returnReplacer.singleReturnOnLastLine(config.getMethodDefDoc()) ) {
			config.setMethodDefDoc(returnReplacer.replaceReturn(config.getMethodDefDoc(), (AssignableNode) config.getCallParent()));
		}
	}
	
	private boolean resultIsAssigned() {
		return config.getCallParent() instanceof AssignableNode;
	}

	@Override
	protected void checkInitialConditions() {
		if(config.getSelectedCall() == null) {
			addError(Messages.InlineMethodConditionChecker_NoMethodCall); 
		} else if (config.getClassName() == null || "".equals(config.getClassName())) { //$NON-NLS-1$
			addError(Messages.InlineMethodConditionChecker_CannotGuessType);
		} else if (config.getMethodDefinitionNode() == null) {
			addError(Messages.InlineMethodConditionChecker_CannotFindDefinition);
		} else if (config.isSingleReturnStatement() != null && config.isSingleReturnStatement().booleanValue() == false) {
			addError(Messages.InlineMethodConditionChecker_ToManyReturns);
		}
	}

	@Override
	protected void checkFinalConditions() {
	}
}
