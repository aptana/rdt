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

package org.rubypeople.rdt.refactoring.core.splitlocal;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.core.inlinemethod.TargetClassFinder;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class LocalVarFinder implements ILocalVarFinder {

	private Node enclosingMethod;

	public Collection<LocalVarUsage> findLocalUsages(IDocumentProvider doc, int caretPosition) {

		Node rootNode = doc.getActiveFileRootNode();

		INameNode selectedAssignment = findAssignment(doc, caretPosition, rootNode);
		if (selectedAssignment == null) {
			return null;
		}

		enclosingMethod = SelectionNodeProvider.getEnclosingScope(rootNode, (Node) selectedAssignment);
		assert enclosingMethod != null;

		return createLocalVariableUsages(gatherLocalAssignments(selectedAssignment));
	}

	private INameNode findAssignment(IDocumentProvider doc, int caretPosition, Node rootNode) {
		INameNode selectedAssignment = (INameNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, caretPosition, LocalAsgnNode.class, DAsgnNode.class);

		if (selectedAssignment == null) {
			
			final LocalVarNode selectedLocalVar = (LocalVarNode) SelectionNodeProvider.getSelectedNodeOfType(rootNode, caretPosition, LocalVarNode.class);
			
			if(selectedLocalVar == null)
				return null;
			
			selectedAssignment = new TargetClassFinder().localAsgnFromLocalVar(selectedLocalVar, doc);
		}
		
		return selectedAssignment;
	}

	private ArrayList<LocalVarUsage> createLocalVariableUsages(ArrayList<AssignableNode> myAsgns) {

		ArrayList<LocalVarUsage> foundNodes = new ArrayList<LocalVarUsage>();
		
		AssignableNode[] assignments = myAsgns.toArray(new AssignableNode[myAsgns.size()]);
		for (int i = 0; i < assignments.length; i++) {
			
			LocalVarUsage var = createLocalVarUsageFromNode(assignments, i);
			
			if (isLastAssignment(assignments, i)) {
				setPositionToScopeEnd(var);
			} else {
				setEndPositionBeforeNextNode(assignments, i, var);
			}

			foundNodes.add(var);
		}
		return foundNodes;
	}

	private void setEndPositionBeforeNextNode(AssignableNode[] assignments, int i, LocalVarUsage var) {
		var.setToPosition(assignments[i + 1].getPosition().getStartOffset() - 1);
	}

	private void setPositionToScopeEnd(LocalVarUsage var) {
		var.setToPosition(enclosingMethod.getPosition().getEndOffset());
	}

	private boolean isLastAssignment(AssignableNode[] assignments, int i) {
		return i >= assignments.length - 1;
	}

	private LocalVarUsage createLocalVarUsageFromNode(AssignableNode[] assignments, int i) {
		LocalVarUsage var = new LocalVarUsage();
		var.setFromPosition(assignments[i].getPosition().getStartOffset());
		var.setNode(assignments[i]);
		var.setName(((INameNode) assignments[i]).getName());
		return var;
	}

	private ArrayList<AssignableNode> gatherLocalAssignments(INameNode selectedNodeOfType) {
	
		ArrayList<AssignableNode> myAsgns = new ArrayList<AssignableNode>();
		
		Collection<LocalAsgnNode> allLocalAsgnNodes = NodeProvider.gatherLocalAsgnNodes(NodeUtil.getBody(enclosingMethod));
		for (LocalAsgnNode node : allLocalAsgnNodes) {
			if (node.getName().equals(selectedNodeOfType.getName()) && !nodeAssignsToItself(node)) {
				myAsgns.add(node);
			}
		}
		
		Collection<Node> nodes = new ArrayList<Node>();
		
		if(enclosingMethod instanceof IterNode) {
			nodes.addAll(NodeProvider.gatherNodesOfTypeInAktScopeNode(((IterNode) enclosingMethod).getVarNode(), DAsgnNode.class));
		}
		
		nodes.addAll(NodeProvider.gatherNodesOfTypeInAktScopeNode(NodeUtil.getBody(enclosingMethod), DAsgnNode.class));

		for (Node node : nodes) {
			if (((DAsgnNode) node).getName().equals(selectedNodeOfType.getName()) && !nodeAssignsToItself((DAsgnNode) node)) {
				myAsgns.add((DAsgnNode) node);
			}
		}
		return myAsgns;
	}

	private boolean nodeAssignsToItself(AssignableNode node) {
		Collection<Node> allNodes = NodeProvider.getAllNodes(node);
		for (Node child : allNodes) {
			if (child instanceof LocalVarNode && node instanceof LocalAsgnNode) {
				LocalVarNode localVarNode = (LocalVarNode) child;
				LocalAsgnNode localAsgnNode = (LocalAsgnNode) node;
				if (localAsgnNode.getIndex() == localVarNode.getIndex()) {
					return true;
				}
			} else if (child instanceof DVarNode && node instanceof DAsgnNode) {
				DVarNode localVarNode = (DVarNode) child;
				DAsgnNode localAsgnNode = (DAsgnNode) node;
				if (localAsgnNode.getName().equals(localVarNode.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public Node getScopeNode() {
		return enclosingMethod;
	}

}
