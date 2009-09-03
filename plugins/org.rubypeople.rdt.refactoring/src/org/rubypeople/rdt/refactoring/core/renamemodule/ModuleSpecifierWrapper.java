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
 * Copyright (C) 2007 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.core.renamemodule;

import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.INodeWrapper;
import org.rubypeople.rdt.refactoring.util.NameHelper;

public abstract class ModuleSpecifierWrapper implements INodeWrapper {
	
	protected String modulePrefix;
	
	private static class Colon2IncludeWrapper extends ModuleSpecifierWrapper {
		protected Colon2Node node;

		public Colon2IncludeWrapper(Colon2Node node, String modulePrefix) {
			this.node = node;
			this.modulePrefix = modulePrefix;
		}

		@Override
		public String getIncludeName() {
			return NameHelper.getFullyQualifiedName(node);
		}

		@Override
		public Node getWrappedNode() {
			return node;
		}

		@Override
		public void setNewName(String oldName, String newName) {
			for (Node node : NodeProvider.getSubNodes(this.node, Colon2Node.class, ConstNode.class)) {
				INameNode nameNode = (INameNode) node;
				if(!nameNode.getName().equals(oldName)) {
					continue;
				}
				
				if(node instanceof Colon2Node) {
					((Colon2Node) node).setName(newName);
					return;
				} else {
					((ConstNode) node).setName(newName);
					return;
				}
			}
		}
	}
	
	private static class ConstIncludeWrapper extends ModuleSpecifierWrapper {
		protected ConstNode node;

		public ConstIncludeWrapper(ConstNode node, String modulePrefix) {
			this.node = node;
			this.modulePrefix = modulePrefix;
		}

		@Override
		public String getIncludeName() {
			return node.getName();
		}

		@Override
		public Node getWrappedNode() {
			return node;
		}

		@Override
		public void setNewName(String oldName, String newName) {
			node.setName(newName);
		}
	}
	
	public static ModuleSpecifierWrapper create(Node node, String modulePrefix) {
			
		if(node instanceof Colon2Node) {
			return new Colon2IncludeWrapper((Colon2Node) node, modulePrefix);
		} else {
			return new ConstIncludeWrapper((ConstNode) node, modulePrefix);
		}
	}
	
	public abstract Node getWrappedNode();

	public abstract String getIncludeName();
	
	public String getFullName() {
		if("".equals(modulePrefix)) {
			return  getIncludeName();
		}
		return modulePrefix + "::" + getIncludeName();
	}
	public abstract void setNewName(String oldName, String newName);
}
