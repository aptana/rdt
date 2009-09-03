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

import org.jruby.ast.AttrAssignNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class MethodCallNodeWrapper implements INodeWrapper {

	static final Class[] METHOD_CALL_NODE_CLASSES = { CallNode.class, VCallNode.class, FCallNode.class , AttrAssignNode.class};

	public static final int INVALID_TYPE = -1;

	public static final int CALL_NODE = 1;

	public static final int V_CALL_NODE = 2;

	public static final int F_CALL_NODE = 3;

	private int nodeType;

	private Node wrappedNode;

	private Node receiverNode;

	private Node argsNode;

	private String name;

	public MethodCallNodeWrapper(Node node) {
		this.wrappedNode = node;
		if (NodeUtil.nodeAssignableFrom(node, CallNode.class)) {
			CallNode callNode = (CallNode) node;
			nodeType = CALL_NODE;
			receiverNode = callNode.getReceiverNode();
			argsNode = callNode.getArgsNode();
			name = callNode.getName();
		} else if (NodeUtil.nodeAssignableFrom(node, AttrAssignNode.class)) {
			AttrAssignNode callNode = (AttrAssignNode) node;
			nodeType = CALL_NODE;
			receiverNode = callNode.getReceiverNode();
			argsNode = callNode.getArgsNode();
			name = callNode.getName();
		} else if (NodeUtil.nodeAssignableFrom(node, VCallNode.class)) {
			nodeType = V_CALL_NODE;
			name = ((VCallNode) node).getName();
		} else if (NodeUtil.nodeAssignableFrom(node, FCallNode.class)) {
			FCallNode fCallNode = (FCallNode) node;
			nodeType = F_CALL_NODE;
			name = fCallNode.getName();
			argsNode = fCallNode.getArgsNode();
		} else {
			nodeType = INVALID_TYPE;
		}
	}

	public boolean isCallNode() {
		return nodeType == CALL_NODE;
	}

	public boolean isVCallNode() {
		return nodeType == V_CALL_NODE;
	}

	public boolean isFCallNode() {
		return nodeType == F_CALL_NODE;
	}

	public Node getReceiverNode() {
		return receiverNode;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return getPosition().getFile();
	}

	public ISourcePosition getPosition() {
		return wrappedNode.getPosition();
	}

	public Node getArgsNode() {
		return argsNode;
	}

	public int getType() {
		return nodeType;
	}

	public boolean isCallToClassMethod() {
		return NodeUtil.nodeAssignableFrom(receiverNode, ConstNode.class);
	}

	public Node getWrappedNode() {
		return wrappedNode;
	}

	public int getArgsCount() {
		if (argsNode == null) {
			return 0;
		}
		return argsNode.childNodes().size();
	}

	public String getReceiverName() {
		if (receiverNode instanceof INameNode) {
			return ((INameNode) receiverNode).getName();
		}
		return null;
	}

	public static Class[] METHOD_CALL_NODE_CLASSES() {
		return METHOD_CALL_NODE_CLASSES.clone();
	}
}
