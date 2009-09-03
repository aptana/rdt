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

package org.rubypeople.rdt.refactoring.core.inlineclass;


import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.classnodeprovider.IncludedClassesProvider;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;


public class InlineClassConfig implements IRefactoringConfig {

	private int caretPosition;
	private IDocumentProvider docProvider;
	private PartialClassNodeWrapper targetClassPart;
	private Collection<ClassNodeWrapper> possibleTargetClasses;
	private ClassNodeWrapper sourceClass;
	
	public InlineClassConfig(DocumentProvider docProvider, int caretPosition) {
		super();
		this.caretPosition = caretPosition;
		this.docProvider = docProvider;
	}
	
	public int getCaretPosition() {
		return caretPosition;
	}
	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}

	public PartialClassNodeWrapper getTargetClassPart() {
		return targetClassPart;
	}

	public void setTargetClassPart(PartialClassNodeWrapper targetClassPart) {
		this.targetClassPart = targetClassPart;
	}
	

	public ClassNodeWrapper getTargetClass() {
		
		IncludedClassesProvider classesProvider = new IncludedClassesProvider(docProvider);
		ClassNodeWrapper classNode = classesProvider.getClassNode(targetClassPart.getClassName());
		
		return classNode;
	}

	public void setPossibleTargetClasses(Collection<ClassNodeWrapper> possibleClassNodes) {
		this.possibleTargetClasses = possibleClassNodes;
	}
	
	public Collection<ClassNodeWrapper> getPossibleTargetClasses(){
		return this.possibleTargetClasses;
	}

	public ClassNodeWrapper getSourceClass() {
		return sourceClass;
	}
	
	public void setSourceClass(ClassNodeWrapper sourceClass){
		this.sourceClass = sourceClass;
	}

	public Collection<AssignableNode> findFieldAsgnsOfSource(MethodNodeWrapper constructorNode) {
		ArrayList<AssignableNode> assignmentsFound = new ArrayList<AssignableNode>();
		
		Collection<Node> assignmentNodes = NodeProvider.getSubNodes(constructorNode.getWrappedNode(), LocalAsgnNode.class, InstAsgnNode.class);
		for(Node currentNode : assignmentNodes){
			
			AssignableNode currentAssignment = (AssignableNode)currentNode;
			Node valueNode = currentAssignment.getValueNode();
			MethodCallNodeWrapper valueWrapper = new MethodCallNodeWrapper(valueNode);
			if(valueWrapper.getType() == MethodCallNodeWrapper.INVALID_TYPE){
				continue;
			}
			
			if(valueWrapper.getName().equals("new") && valueWrapper.getReceiverNode() instanceof ConstNode && ((ConstNode)valueWrapper.getReceiverNode()).getName().equals(sourceClass.getName()) ){ //$NON-NLS-1$
				assignmentsFound.add(currentAssignment);
				
			}
		}
		return assignmentsFound;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.docProvider = doc;
	}
}
