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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.exception.UnknownReferenceException;

public class SClassNodeWrapper extends PartialClassNodeWrapper {
	private SClassNode wrappedNode;

	Map<Integer, Node> references;

	public SClassNodeWrapper(Node node, Node rootNode) {
		super(node);
		wrappedNode = (SClassNode) node;
		this.references = buildReferences(rootNode);
	}

	@Override
	public String getSuperClassName() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getClassName() {
		Node receiverNode = wrappedNode.getReceiverNode();
		if (receiverNode instanceof LocalVarNode) {
			LocalVarNode localVarNode = (LocalVarNode) receiverNode;
			Node referencedNode;
			try {
				referencedNode = getReferencedNode(localVarNode.getIndex(), references);
				if (referencedNode instanceof INameNode) {
					return getModulePrefix() + ((INameNode) referencedNode).getName();
				}
			} catch (UnknownReferenceException e) {
				e.printStackTrace();
			}
		} else if (receiverNode instanceof VCallNode) {
			VCallNode vCallNode = (VCallNode) receiverNode;
			return getModulePrefix() + vCallNode.getName();
		}
		return Messages.SClassNodeWrapper_UnknownNode + receiverNode.toString();
	}

	private Node getReferencedNode(int id, Map<Integer, Node> references) throws UnknownReferenceException {
		if (references.containsKey(Integer.valueOf(id))) {
			return references.get(Integer.valueOf(id));
		}
		throw new UnknownReferenceException();
	}

	@Override
	public Node getClassBodyNode() {
		return wrappedNode.getBodyNode();
	}

	@Override
	public Node getDeclarationEndNode() {
		return wrappedNode.getReceiverNode();
	}

	private Map<Integer, Node> buildReferences(Node root) {
		Map<Integer, Node> references = new HashMap<Integer, Node>();
		Collection<Node> referencedNodes = NodeProvider.getSubNodes(root, LocalAsgnNode.class);
		for (Node node : referencedNodes) {
			if (node instanceof LocalAsgnNode) {
				LocalAsgnNode localAsgnNode = (LocalAsgnNode) node;
				references.put(Integer.valueOf(localAsgnNode.getIndex()), node);
			}
		}
		return references;
	}
}
