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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class LocalNodeWrapper implements INodeWrapper {


	public static final int INVALID_ID = -1;

	public static final int LOCAL_ASGN_VAR_NODE = 1;

	public static final int LOCAL_VAR_NODE = 2;

	public static final int D_VAR_NODE = 3;

	public static final int D_ASGN_NODE = 4;

	public static final Class[] LOCAL_NODES_CLASSES = { LocalAsgnNode.class, LocalVarNode.class, DVarNode.class, DAsgnNode.class };

	private Node wrappedNode;

	private int nodeType;

	private Node valueNode;

	private String name;

	private int id; // aka count

	public LocalNodeWrapper(Node node) {
		id = INVALID_ID;
		if (NodeUtil.nodeAssignableFrom(node, LocalVarNode.class)) {
			id = ((LocalVarNode) node).getIndex();
			name = ((LocalVarNode) node).getName();
			nodeType = LOCAL_VAR_NODE;
		} else if (NodeUtil.nodeAssignableFrom(node, LocalAsgnNode.class)) {
			LocalAsgnNode localAsgnNode = (LocalAsgnNode) node;
			id = localAsgnNode.getIndex();
			name = localAsgnNode.getName();
			nodeType = LOCAL_ASGN_VAR_NODE;
			valueNode = localAsgnNode.getValueNode();
		} else if (NodeUtil.nodeAssignableFrom(node, DAsgnNode.class)) {
			DAsgnNode dAsgnNode = (DAsgnNode) node;
			name = dAsgnNode.getName();
			nodeType = D_ASGN_NODE;
			valueNode = dAsgnNode.getValueNode();
		} else if (NodeUtil.nodeAssignableFrom(node, DVarNode.class)) {
			DVarNode dVarNode = (DVarNode) node;
			name = dVarNode.getName();
			nodeType = D_VAR_NODE;
		}
		wrappedNode = node;
	}

	public Node getWrappedNode() {
		return wrappedNode;
	}

	public boolean hasValidId() {
		return id != INVALID_ID;
	}

	public int getId() {
		return id;
	}

	public int getNodeType() {
		return nodeType;
	}

	public Node getValueNode() {
		return valueNode;
	}

	public boolean hasValueNode() {
		return valueNode != null;
	}

	public boolean hasName() {
		return name != null;
	}

	public String getName() {
		return name;
	}

	public boolean isAsgnNode() {
		return nodeType == LOCAL_ASGN_VAR_NODE || nodeType == D_ASGN_NODE;
	}

	public boolean isDVarNode() {
		return nodeType == D_VAR_NODE || nodeType == D_ASGN_NODE;
	}

	public void setName(String name) {
		if (nodeType != LOCAL_VAR_NODE) {
			this.name = name;
		}
		if (NodeUtil.nodeAssignableFrom(wrappedNode, LocalVarNode.class)) {
			LocalVarNode localVarNode = (LocalVarNode) wrappedNode;
			localVarNode.setName(name);
		} else if (NodeUtil.nodeAssignableFrom(wrappedNode, LocalAsgnNode.class)) {
			LocalAsgnNode localAsgnNode = (LocalAsgnNode) wrappedNode;
			localAsgnNode.setName(name);
		} else if (NodeUtil.nodeAssignableFrom(wrappedNode, DAsgnNode.class)) {
			DAsgnNode dAsgnNode = (DAsgnNode) wrappedNode;
			dAsgnNode.setName(name);
		} else if (NodeUtil.nodeAssignableFrom(wrappedNode, DVarNode.class)) {
			DVarNode dVarNode = (DVarNode) wrappedNode;
			dVarNode.setName(name);
		}
	}

	public static String getLocalNodeName(LocalNodeWrapper node) {
		return node.getName();
	}

	public static Collection<LocalNodeWrapper> gatherLocalNodes(Node baseNode) {
		return gatherLocalNodes(baseNode, LOCAL_NODES_CLASSES);
	}

	public static Collection<LocalNodeWrapper> gatherLocalVarNodes(Node baseNode) {
		return gatherLocalNodes(baseNode, DVarNode.class, LocalVarNode.class);
	}

	public static Collection<LocalNodeWrapper> gatherLocalAsgnNodes(Node baseNode) {
		return gatherLocalNodes(baseNode, DAsgnNode.class, LocalAsgnNode.class);
	}

	private static Collection<LocalNodeWrapper> gatherLocalNodes(Node baseNode, Class... klasses) {
		Collection<LocalNodeWrapper> localNodes = new ArrayList<LocalNodeWrapper>();
		for (Node aktNode : NodeProvider.getSubNodes(baseNode, klasses)) {
			localNodes.add(new LocalNodeWrapper(aktNode));
		}
		return localNodes;
	}

	public static Collection<LocalNodeWrapper> createLocalNodes(Collection<Node> nodes) {

		Collection<LocalNodeWrapper> localNodes = new ArrayList<LocalNodeWrapper>();
		for (Node aktNode : nodes) {
			if (NodeUtil.nodeAssignableFrom(aktNode, LOCAL_NODES_CLASSES)) {
				localNodes.add(new LocalNodeWrapper(aktNode));
			}
		}
		return localNodes;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((wrappedNode == null) ? 0 : wrappedNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LocalNodeWrapper) {
			LocalNodeWrapper localNode = (LocalNodeWrapper) obj;
			return localNode.getWrappedNode().equals(getWrappedNode());
		}
		return false;
	}
}
