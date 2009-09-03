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

package org.rubypeople.rdt.refactoring.core.renamemethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.core.renamefield.FieldProvider;
import org.rubypeople.rdt.refactoring.core.renamefield.InstVarAccessesFinder;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentWithIncluding;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.INodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;

public class RenameMethodConditionChecker extends RefactoringConditionChecker{

	public static final String DEFAULT_ERROR = Messages.RenameMethodConditionChecker_NoMethodSelected;
	private RenameMethodConfig config;
	
	public RenameMethodConditionChecker(RenameMethodConfig config) {
		super(config);
	}

	@Override
	public void init(IRefactoringConfig configObj) {
		this.config = (RenameMethodConfig)configObj;
		config.setDocProvider(new DocumentWithIncluding(config.getDocumentProvider()));
		
		Node rootNode = config.getDocumentProvider().getActiveFileRootNode();
		try {
			this.config.setClassNode(SelectionNodeProvider.getSelectedClassNode(rootNode, this.config.getCaretPosition()));
		} catch (NoClassNodeException e) {/* ClassNode stays null */}
		setSelectedMethodNode(rootNode);
		if(config.getTargetMethod().getWrappedNode() != null) {
			this.config.setPossibleCalls(getAllCallCandidates());
		}
	}

	private void setSelectedMethodNode(Node rootNode) {
		
		MethodDefNode methodNode = (MethodDefNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, this.config.getCaretPosition(), MethodDefNode.class);
		if(methodNode == null) {
			SymbolNode selectedSymbolNode = (SymbolNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, config.getCaretPosition(), SymbolNode.class);
			if(selectedSymbolNode != null && config.getSelectedClass() != null) {
				MethodNodeWrapper method = config.getSelectedClass().getMethod(selectedSymbolNode.getName());
				if(method != null) {
					methodNode = method.getWrappedNode();
				}
			}
		}
		
		MethodNodeWrapper targetMethod = new MethodNodeWrapper(methodNode, config.getSelectedClass());
		this.config.setTargetMethod(targetMethod);
		if(methodNode != null && config.getNewName() == null) {
			this.config.setNewName(targetMethod.getName());
		}
	}

	private Collection<INodeWrapper> getAllCallCandidates() {

		Collection<Node> allNodes = config.getDocumentProvider().getAllNodes();
		ArrayList<INodeWrapper> possibleCalls = new ArrayList<INodeWrapper>();

		for(Node currentNode : allNodes){
			MethodCallNodeWrapper callNode = new MethodCallNodeWrapper(currentNode);
			if(isPossibleCall(callNode)){
				possibleCalls.add(callNode);
			}
		}
		
		if(config.getTargetMethod().isAccessor() && config.renameFields()) {
			String name;
			
			if(config.getTargetMethod().isWriter()) {
				name = config.getTargetMethod().getName().replace("=", "");
				
			} else {
				name = config.getTargetMethod().getName();
			}
			
			possibleCalls.addAll(InstVarAccessesFinder.find(config.getDocumentProvider(), name));
			possibleCalls.addAll(new FieldProvider(config.getSelectedClass(), config.getDocumentProvider()).getFieldItems(name, false));
			config.setSelectedCalls(new FieldProvider(config.getSelectedClass(), config.getDocumentProvider()).getFieldItems(name, false));
		}
		
		return possibleCalls;
	}

	private boolean isPossibleCall(MethodCallNodeWrapper callNode) {
		if(config.getTargetMethod().isClassMethod() != callNode.isCallToClassMethod()){
			return false;			
		}
		
		if(callNode.getType() == MethodCallNodeWrapper.INVALID_TYPE){
			return false;
		}
		
		if(callNode.getName().equals(config.getTargetMethod().getName())){
			ArgsNodeWrapper targetNodeArgs = config.getTargetMethod().getArgsNode();
			if(targetNodeArgs.argsCountMatches(callNode)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void checkFinalConditions() {
		if(config.getNewName().equals(config.getTargetMethod().getName())) {
			addWarning(Messages.RenameMethodConditionChecker_NotChanged);
		} else if(getAlreadyUsedNames().contains(config.getNewName())){
			addError(Messages.RenameMethodConditionChecker_AlreadyExists);
		}
	}


	private boolean checkMethodIsBeyondClasses(MethodDefNode currentMethod, ClassNodeProvider classes) {
		
		String methodFile = currentMethod.getPosition().getFile();
		int methodStart = currentMethod.getPosition().getStartOffset();
		int methodEnd = currentMethod.getPosition().getEndOffset();
		
		for(ClassNodeWrapper currentClass : classes.getAllClassNodes()){
			for(PartialClassNodeWrapper currentPart : currentClass.getPartialClassNodes()){
				if(checkIsInClassPart(methodFile, methodStart, methodEnd, currentPart)){
					return false;
				} 
			}
		}
		return true;
	}


	private boolean checkIsInClassPart(String methodFile, int methodStart, int methodEnd, PartialClassNodeWrapper currentPart) {
		String partFile = currentPart.getWrappedNode().getPosition().getFile();
		int partStart = currentPart.getWrappedNode().getPosition().getStartOffset();
		int partEnd = currentPart.getWrappedNode().getPosition().getEndOffset();
		
		if(methodFile.equals(partFile) && (methodStart > partStart) && (methodEnd < partEnd)){
			return true;
		}
		return false;
	}


	@Override
	protected void checkInitialConditions() {
		Node methodNode = config.getTargetMethod().getWrappedNode();
		if(methodNode == null || !isSelectionInMethodName()){
			addError(DEFAULT_ERROR);
		}
	}


	private boolean isSelectionInMethodName() {
		return SelectionNodeProvider.nodeContainsPosition(config.getTargetMethod().getWrappedNode().getNameNode(), config.getCaretPosition());
	}


	public Collection<String> getAlreadyUsedNames() {
		HashSet<String> usedNames = new HashSet<String>();
		
		if(config.getSelectedClass() != null)  {
			for (MethodNodeWrapper currentMethod : config.getSelectedClass().getMethods()){
				if(isSameTypeAsSelectedMethod(currentMethod)){
					usedNames.add(currentMethod.getName());
				}
			}
		} else {
			
			Node rootNode = config.getDocumentProvider().getActiveFileRootNode();
			Collection<MethodDefNode> methods = NodeProvider.getMethodNodes(rootNode);
			ClassNodeProvider classes = new ClassNodeProvider(config.getDocumentProvider());
			
			for(MethodDefNode currentMethod : methods){
				if(checkMethodIsBeyondClasses(currentMethod, classes))
					usedNames.add(currentMethod.getName());
			}
		}
		
		return usedNames;
	}


	private boolean isSameTypeAsSelectedMethod(MethodNodeWrapper currentMethod) {
		return currentMethod.isClassMethod() == config.getTargetMethod().isClassMethod();
	}
}
