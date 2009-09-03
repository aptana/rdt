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

import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;

public class AttrAccessorNodeWrapper implements INodeWrapper {

	private Collection<FCallNode> callNodes;

	private AccessorType accessorType;

	private String name;

	private String fileName;

	private SymbolNode symbolNode;

	public static final String ATTR_WRITER = "attr_writer"; //$NON-NLS-1$

	public static final String ATTR_READER = "attr_reader"; //$NON-NLS-1$

	public static final String ATTR_ACCESSOR = "attr_accessor"; //$NON-NLS-1$

	private static final String[] typeNames = { ATTR_WRITER, ATTR_READER, ATTR_ACCESSOR };

	public AttrAccessorNodeWrapper(FCallNode callNode, SymbolNode symbolNode) {
		this.symbolNode = symbolNode;
		callNodes = new ArrayList<FCallNode>();
		callNodes.add(callNode);
		accessorType = new AccessorType(callNode.getName());
		name = getName();
		fileName = callNode.getPosition().getFile();
	}

	private String getName() {
		return (symbolNode.getName().charAt(0) == '@') ? symbolNode.getName().substring(1) : symbolNode.getName();
	}

	public String getAccessorTypeName() {
		return accessorType.getTypeName();
	}

	public String getAttrName() {
		return name;
	}

	public boolean isWriter() {
		return accessorType.isWriter();
	}

	public boolean isReader() {
		return accessorType.isReader();
	}

	public void addAccessorType(AttrAccessorNodeWrapper otherAccessorNode) {
		accessorType.addOtherType(otherAccessorNode.getAccessorTypeName());
		callNodes.addAll(otherAccessorNode.callNodes);
	}

	private static class AccessorType {

		private static final int TYPE_WRITER = 1;

		private static final int TYPE_READER = 2;

		private static final int TYPE_ACCESSOR = 3;

		private int type;

		public AccessorType(String typeName) {
			type = evaluateType(typeName);
		}

		private int evaluateType(String typeName) {
			if (typeName.equals(ATTR_WRITER)) {
				return TYPE_WRITER;
			}
			return (typeName.equals(ATTR_READER)) ? TYPE_READER : TYPE_ACCESSOR;
		}

		public void addOtherType(String otherTypeName) {
			int otherType = evaluateType(otherTypeName);
			type = type | otherType;
		}

		public String getTypeName() {
			return typeNames[type - 1];
		}

		public boolean isWriter() {
			return (type & TYPE_WRITER) != 0;
		}

		public boolean isReader() {
			return (type & TYPE_READER) != 0;
		}

		public boolean contains(AccessorType otherType) {
			int andResult = otherType.type & type;
			return andResult == otherType.type;
		}
	}

	public Collection<FCallNode> getAccessorNodes() {
		return callNodes;
	}

	public String getFileName() {
		return fileName;
	}
	
	public SymbolNode getSymbolNode() {
		return symbolNode;
	}

	public boolean containsAccessor(AttrAccessorNodeWrapper otherAccessor) {
		if(otherAccessor.getAttrName().equals(getAttrName())) {
			if(accessorType.contains(otherAccessor.accessorType)) {
				return true;
			}
		}
		return false;
	}

	public Node getWrappedNode() {
		return callNodes.toArray(new Node[callNodes.size()])[0];
	}
}
