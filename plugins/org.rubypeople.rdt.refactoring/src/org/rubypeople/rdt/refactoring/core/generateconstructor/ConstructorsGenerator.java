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

package org.rubypeople.rdt.refactoring.core.generateconstructor;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditAndTreeContentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.CheckableItem;
import org.rubypeople.rdt.refactoring.ui.IParentProvider;

public class ConstructorsGenerator extends EditAndTreeContentProvider {

	private Collection<TreeClass> classes;

	public ConstructorsGenerator(DocumentProvider docProvider) {
		classes = new ArrayList<TreeClass>();
		ClassNodeProvider provider = docProvider.getClassNodeProvider();
		if (provider != null) {
			for (ClassNodeWrapper node : provider.getAllClassNodes()) {
				classes.add(new TreeClass(node));
			}
		}
	}

	public Object[] getElements(Object inputElement) {
		return classes.toArray();
	}

	public Collection<EditProvider> getEditProviders() {
		Collection<EditProvider> constructors = new ArrayList<EditProvider>();
		for (TreeClass treeClass : classes) {
			if (treeClass.isChecked())
				constructors.add(treeClass.getGeneratedConstructor());
		}
		return constructors;
	}

	public static class TreeClass extends CheckableItem implements org.rubypeople.rdt.refactoring.ui.IChildrenProvider {
		private ClassNodeWrapper classNode;

		private Collection<TreeAttribute> attrs;

		private Collection<Node> attrNodes;

		public TreeClass(ClassNodeWrapper classNode) {
			super(false, false, true);
			this.classNode = classNode;
			attrNodes = classNode.getAttrNodes();
			attrs = new ArrayList<TreeAttribute>();
			for (Node node : attrNodes) {
				attrs.add(new TreeAttribute((INameNode) node));
			}
		}

		public String toString() {
			return classNode.getName();
		}

		public Object[] getChildren() {
			return attrs.toArray();
		}

		public boolean hasChildren() {
			return !attrs.isEmpty();
		}

		public GeneratedConstructor getGeneratedConstructor() {

			Collection<String> checkedAttrNodes = new ArrayList<String>();
			for (TreeAttribute treeAttr : attrs) {
				if (treeAttr.isChecked())
					checkedAttrNodes.add(treeAttr.getAttrNode().getName().substring(1));
			}
			return new GeneratedConstructor(classNode, checkedAttrNodes);
		}

		public class TreeAttribute extends CheckableItem implements IParentProvider {
			private INameNode node;

			private String name;

			public TreeAttribute(INameNode node) {
				super(false, true, false);
				name = node.getName();
				if (name.indexOf('@') == 0)
					name = name.substring(1);
				this.node = node;
			}

			public String toString() {
				return name;
			}

			public INameNode getAttrNode() {
				return node;
			}

			public Object getParent() {
				return TreeClass.this;
			}
		}
	}
}
