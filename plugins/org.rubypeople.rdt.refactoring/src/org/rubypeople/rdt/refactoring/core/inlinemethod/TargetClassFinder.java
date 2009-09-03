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

import java.util.Collection;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NameHelper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class TargetClassFinder implements ITargetClassFinder {
	
	public String findTargetClass(final MethodCallNodeWrapper call, final IDocumentProvider doc) {
		
		String name = ""; //$NON-NLS-1$
		
		if(call.getReceiverNode() == null) {
			name = getSurroundingClass(call, doc);
		}
		
		final AssignableNode type = getAssignableNode(call, doc);
		
		if(createsNewInstance(type)) {
			Node receiver = ((CallNode) type.getValueNode()).getReceiverNode();
			name = NameHelper.getFullyQualifiedName(receiver);
		}
		
		return name;
	}
	
	private String getSurroundingClass(final MethodCallNodeWrapper call, final IDocumentProvider doc) {
		ClassNode classNode = ((ClassNode) NodeProvider.getEnclosingNodeOfType(doc.getActiveFileRootNode(), call.getWrappedNode(), ClassNode.class));
		if(classNode != null) {
			return classNode.getCPath().getName();
		}
		return ""; //$NON-NLS-1$
	}

	private AssignableNode getAssignableNode(final MethodCallNodeWrapper call, final IDocumentProvider doc) {
		AssignableNode receiverType = null;
		
		if(call.getReceiverNode() instanceof LocalVarNode) {
			receiverType = localAsgnFromLocalVar((LocalVarNode) call.getReceiverNode(), doc);
		} else if(call.getReceiverNode() instanceof InstVarNode) {
			receiverType = instVarFromCall((InstVarNode) call.getReceiverNode(), doc);
		}
		return receiverType;
	}

	private boolean createsNewInstance(final AssignableNode receiverType) {
		return receiverType != null
		&& receiverType.getValueNode() instanceof CallNode 
		&& "new".equals(((CallNode) receiverType.getValueNode()).getName()); //$NON-NLS-1$
	}

	/**
	 * Finds the corresponding InstAsgnNode node to the supplied InstVarNode
	 */
	public InstAsgnNode instVarFromCall(final InstVarNode node, final IDocumentProvider doc) {
		InstAsgnNode decoratedNode = null;
		
		try {
			final ClassNodeWrapper selectedClassNode = SelectionNodeProvider.getSelectedClassNode(doc.getActiveFileRootNode(), node.getPosition().getStartOffset());
			final ClassNodeWrapper allClassNodes = new ClassNodeProvider(doc).getClassNode((selectedClassNode.getName()));
			
			if(allClassNodes == null) {
				throw new NoClassNodeException();
			}
			
			for (FieldNodeWrapper field : allClassNodes.getFields()) {
				if (field.getName().equals(node.getName()) && field.getNodeType() == FieldNodeWrapper.INST_ASGN_NODE) {
					decoratedNode = (InstAsgnNode) field.getWrappedNode();
				}
			}
		} catch (NoClassNodeException e) {
			decoratedNode = findInstVarInScope(node, doc, null);
		}

		return decoratedNode;
	}

	private InstAsgnNode findInstVarInScope(final InstVarNode node, final IDocumentProvider doc, InstAsgnNode decoratedNode) {
		Collection<Node> assignments = NodeProvider.getSubNodes(doc.getActiveFileRootNode(), InstAsgnNode.class);
		for (Node assignment : assignments) {
			if(((InstAsgnNode) assignment).getName().equals(node.getName()) && assignment.getPosition().getStartOffset() < node.getPosition().getStartOffset()) {
				decoratedNode = (InstAsgnNode) assignment;
			}
		}
		return decoratedNode;
	}

	/**
	 * Finds the corresponding LocalAsgnNode node to the supplied LocalVarNode
	 */
	public LocalAsgnNode localAsgnFromLocalVar(final LocalVarNode node, final IDocumentProvider doc) {
		
		Node enclosingScope = SelectionNodeProvider.getEnclosingScope(doc.getActiveFileRootNode(), node);
		LocalAsgnNode asgnNode = findLastAssignmentToVar(node, NodeUtil.getBody(enclosingScope));
		if(asgnNode != null) {
			return asgnNode;
		}

		do {
			enclosingScope = SelectionNodeProvider.getEnclosingScope(doc.getActiveFileRootNode(), NodeProvider.findParentNode(doc.getActiveFileRootNode(), enclosingScope));
			asgnNode = findLastAssignmentToVar(node, NodeUtil.getBody(enclosingScope));
		} while(!(enclosingScope instanceof RootNode || enclosingScope instanceof MethodDefNode) && asgnNode == null);
		
		return asgnNode;
	}

	private LocalAsgnNode findLastAssignmentToVar(final LocalVarNode node, final Node enclosingScope) {
		LocalAsgnNode localAsgnNode = null;
		for (LocalAsgnNode asgnNode : NodeProvider.gatherLocalAsgnNodes(enclosingScope)) {
			if(asgnNode.getIndex() == node.getIndex() && asgnNode.getName().equals(node.getName()) && asgnNode.getPosition().getStartOffset() < node.getPosition().getStartOffset()) {
				localAsgnNode = asgnNode;
			}
		}
		return localAsgnNode;
	}

}
