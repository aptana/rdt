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

import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class FieldNodeWrapper implements INodeWrapper {

	public static final int INVALID_TYPE = -1;
	public static final int INST_ASGN_NODE = 1;
	public static final int INST_VAR_NODE = 2;
	public static final int CLASS_VAR_ASGN_NODE = 3;
	public static final int CLASS_VAR_NODE = 4;
	public static final int SYMBOL_NODE = 5;
	public static final int CLASS_VAR_DECL_NODE = 6;
	
	final static Class[] FIELD_NODE_CLASSES = { InstAsgnNode.class, InstVarNode.class, ClassVarAsgnNode.class, ClassVarNode.class, ClassVarDeclNode.class, SymbolNode.class };
	static final Class[] FIELD_NODE_CLASSES_WITHOUT_SYMBOL_NODE = { InstAsgnNode.class, InstVarNode.class, ClassVarAsgnNode.class, ClassVarNode.class, ClassVarDeclNode.class };

	public static final String ATTR_NAME = "attr"; //$NON-NLS-1$
	
	private Node wrappedNode;
	private int nodeType;
	private String name;

	public FieldNodeWrapper(Node node) {

		nodeType = INVALID_TYPE;

		if (NodeUtil.nodeAssignableFrom(node, InstAsgnNode.class)) {
			nodeType = INST_ASGN_NODE;
			InstAsgnNode instAsgnNode = (InstAsgnNode) node;
			name = instAsgnNode.getName();
		} else if (NodeUtil.nodeAssignableFrom(node, InstVarNode.class)) {
			InstVarNode instVarNode = (InstVarNode) node;
			name = instVarNode.getName();
			nodeType = INST_VAR_NODE;
		} else if (NodeUtil.nodeAssignableFrom(node, ClassVarAsgnNode.class)) {
			ClassVarAsgnNode classVarAsgnNode = (ClassVarAsgnNode) node;
			name = classVarAsgnNode.getName();
			nodeType = CLASS_VAR_ASGN_NODE;
		} else if (NodeUtil.nodeAssignableFrom(node, ClassVarNode.class)) {
			ClassVarNode classVarNode = (ClassVarNode) node;
			name = classVarNode.getName();
			nodeType = CLASS_VAR_NODE;
		} else if (NodeUtil.nodeAssignableFrom(node, SymbolNode.class)) {
			SymbolNode symbolNode = (SymbolNode) node;
			name = symbolNode.getName();
			nodeType = SYMBOL_NODE;
		} else if (NodeUtil.nodeAssignableFrom(node, ClassVarDeclNode.class)) {
			ClassVarDeclNode classVarDeclNode = (ClassVarDeclNode) node;
			name = classVarDeclNode.getName();
			nodeType = CLASS_VAR_DECL_NODE;
		}

		wrappedNode = node;
	}

	public Node getWrappedNode() {
		return wrappedNode;
	}

	public String getName() {
		return name;
	}
	
	public String getNameWithoutAts() {
		return name.replaceFirst("^@{1,2}", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int getNodeType() {
		return nodeType;
	}

	public boolean isInstVar() {
		return (nodeType == INST_ASGN_NODE || nodeType == INST_VAR_NODE || nodeType == SYMBOL_NODE);
	}

	public boolean isClassVar() {
		return (nodeType == CLASS_VAR_ASGN_NODE || nodeType == CLASS_VAR_NODE || nodeType == CLASS_VAR_DECL_NODE);
	}

	public boolean isAsgnNode() {
		return nodeType == INST_ASGN_NODE || nodeType == CLASS_VAR_ASGN_NODE || nodeType == CLASS_VAR_DECL_NODE;
	}
	
	public ISourcePosition getPosition() {
		return wrappedNode.getPosition();
	}

	public static Class[] fieldNodeClasses() {
		return FIELD_NODE_CLASSES.clone();
	}
}
