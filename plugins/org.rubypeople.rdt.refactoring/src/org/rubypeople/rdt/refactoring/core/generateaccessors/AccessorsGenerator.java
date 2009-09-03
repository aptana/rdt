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

package org.rubypeople.rdt.refactoring.core.generateaccessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator.TreeClass.TreeAttribute;
import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator.TreeClass.TreeAttribute.TreeAccessor;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditAndTreeContentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.IItemSelectionReceiver;

public class AccessorsGenerator extends EditAndTreeContentProvider implements IItemSelectionReceiver {

	private Collection<TreeClass> classes;

	private int type;

	private Object[] selectedTreeItems;
	
	public static final String WRITER = Messages.AccessorsGenerator_Writer;

	public static final String READER = Messages.AccessorsGenerator_Reader;

	public AccessorsGenerator(DocumentProvider documentProvider, int type) {
		this.type = type;
		selectedTreeItems = new Object[] {};
		initTreeClasses(documentProvider.getClassNodeProvider());
	}

	protected void initTreeClasses(ClassNodeProvider classNodeProvider) {
		classes = new ArrayList<TreeClass>();
		if (classNodeProvider != null) {
			for (ClassNodeWrapper node : classNodeProvider.getAllClassNodes()) {
				classes.add(new TreeClass(node));
			}
		}
	}

	public Object[] getElements(Object inputElement) {
		Collection<TreeClass> elements = new ArrayList<TreeClass>();
		for (TreeClass klass : classes) {
			if (klass.hasChildren())
				elements.add(klass);
		}
		return elements.toArray();
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setSelectedItems(Object[] selected) {
		this.selectedTreeItems = selected.clone();
	}

	public Collection<EditProvider> getEditProviders() {
		Collection<TreeAccessor> treeAccessors = getTreeAccessors();
		LinkedHashSet<TreeAttribute> attribtes = getTreeAttributes(treeAccessors);
		Collection<EditProvider> generatedAccessors = getGeneratedAccessors(attribtes);
		return generatedAccessors;
	}

	private Collection<EditProvider> getGeneratedAccessors(LinkedHashSet<TreeAttribute> attribtes) {
		Collection<EditProvider> providers = new ArrayList<EditProvider>();
		for (TreeAttribute attr : attribtes) {
			providers.addAll(attr.getGeneratedAccessors());
		}
		return providers;
	}

	private LinkedHashSet<TreeAttribute> getTreeAttributes(Collection<TreeAccessor> treeAccessors) {
		LinkedHashSet<TreeAttribute> attribtes = new LinkedHashSet<TreeAttribute>();
		for (TreeAccessor accessor : treeAccessors) {
			setSelection(accessor);
			attribtes.add(accessor.getAttribute());
		}
		return attribtes;
	}

	private void setSelection(TreeAccessor accessor) {
		if (accessor.isReader())
			accessor.getAttribute().setReaderSelected();
		else if (accessor.isWriter())
			accessor.getAttribute().setWriterSelected();
	}

	private Collection<TreeAccessor> getTreeAccessors() {
		Collection<TreeAccessor> treeAccessors = new ArrayList<TreeAccessor>();
		for (Object o : selectedTreeItems) {
			if (o instanceof TreeAccessor) {
				TreeAccessor accessor = ((TreeAccessor) o);
				accessor.getAttribute().clearSelection();
				treeAccessors.add(accessor);
			}
		}
		return treeAccessors;
	}

	public class TreeClass implements org.rubypeople.rdt.refactoring.ui.IChildrenProvider {
		private ClassNodeWrapper classNode;

		private Collection<TreeAttribute> attrs;

		private final Collection<AttrAccessorNodeWrapper> existingSimpleAccessorNodes;

		private final Collection<MethodNodeWrapper> existingMethodAccessorNodes;

		public TreeClass(ClassNodeWrapper classNode) {
			this.classNode = classNode;
			existingSimpleAccessorNodes = classNode.getAccessorNodes();
			existingMethodAccessorNodes = classNode.getMethods();
			attrs = new ArrayList<TreeAttribute>();
			for (Node attrNode : classNode.getAttrNodes()) {
				attrs.add(new TreeAttribute((INameNode) attrNode, classNode));
			}
		}

		public String toString() {
			return classNode.getName();
		}

		public Object[] getChildren() {
			Collection<TreeAttribute> children = new ArrayList<TreeAttribute>();
			for (TreeAttribute attr : attrs) {
				if (attr.hasChildren())
					children.add(attr);
			}
			return children.toArray();
		}

		public boolean hasChildren() {
			for (TreeAttribute attr : attrs) {
				if (attr.hasChildren())
					return true;
			}
			return false;
		}

		public ClassNodeWrapper getClassNode() {
			return classNode;
		}

		public class TreeAttribute implements Comparable, org.rubypeople.rdt.refactoring.ui.IChildrenProvider {
			private ClassNodeWrapper classNode;

			private TreeAccessor reader;

			private TreeAccessor writer;

			private boolean readerSelected;

			private boolean writerSelected;

			private String name;

			public TreeAttribute(INameNode node, ClassNodeWrapper classNode) {
				this.classNode = classNode;
				name = node.getName();
				if (name.indexOf('@') == 0)
					name = name.substring(1);
				reader = new TreeAccessor(name, true);
				writer = new TreeAccessor(name, false);
				readerSelected = false;
				writerSelected = false;
			}

			public String toString() {
				return name;
			}

			public String getName() {
				return name;
			}

			public Object[] getChildren() {

				return getChlidren().toArray();
			}

			private Collection<TreeAccessor> getChlidren() {
				Collection<TreeAccessor> accessors = new ArrayList<TreeAccessor>();
				if (!existsSameAccessor(reader, type))
					accessors.add(reader);
				if (!existsSameAccessor(writer, type))
					accessors.add(writer);
				return accessors;
			}

			private boolean existsSameAccessor(TreeAccessor accessor, int type) {
				if (type == GeneratedAccessor.TYPE_SIMPLE_ACCESSOR) {
					return existsSimpleAccessor(accessor);
				}
				return existsMethodAccessor(accessor);
			}

			private boolean existsSimpleAccessor(TreeAccessor treeAccessor) {
				for (AttrAccessorNodeWrapper aktAccessorNode : existingSimpleAccessorNodes) {
					if (isSameSimpleAccessorType(treeAccessor, aktAccessorNode)) {
						if (aktAccessorNode.getAttrName().equals(treeAccessor.getAttributeName())) {
							return true;
						}
					}
				}
				return false;
			}

			private boolean isSameSimpleAccessorType(TreeAccessor accessor, AttrAccessorNodeWrapper node) {
				return (node.getAccessorTypeName().equals(AttrAccessorNodeWrapper.ATTR_ACCESSOR))
						|| (node.getAccessorTypeName().equals(AttrAccessorNodeWrapper.ATTR_READER) && accessor.isReader())
						|| (node.getAccessorTypeName().equals(AttrAccessorNodeWrapper.ATTR_WRITER) && accessor.isWriter());
			}

			private boolean existsMethodAccessor(TreeAccessor accessor) {
				for (MethodNodeWrapper node : existingMethodAccessorNodes) {
					if (isSameReader(accessor, node))
						return true;
					else if (isSameWriter(accessor, node))
						return true;
				}
				return false;
			}

			private boolean isSameWriter(TreeAccessor accessor, MethodNodeWrapper methodNode) {
				if (methodNode.getName().equals(accessor.getAttributeName() + '=') && accessor.isWriter()) {
					ArgsNodeWrapper argsNode = methodNode.getArgsNode();
					if (argsNode.getArgsList().size() == 1 && argsNode.getOptArgs() == null && argsNode.getBlockArgNode() == null) {
						String argName = argsNode.getArgsList().iterator().next();
						if (argName.equals(accessor.getAttributeName()))
							return true;
					}
				}
				return false;
			}

			private boolean isSameReader(TreeAccessor accessor, MethodNodeWrapper node) {
				return node.getName().equals(accessor.getAttributeName()) && accessor.isReader() && !node.getArgsNode().hasArgs();
			}

			public boolean hasChildren() {
				return !getChlidren().isEmpty();
			}

			public int compareTo(Object arg0) {
				String thisStr = classNode.getName() + name;
				String otherStr;
				if (arg0 instanceof String) {
					otherStr = (String) arg0;
				} else {
				  TreeAttribute otherAttr = (TreeAttribute) arg0;
				  ClassNodeWrapper otherClassNode = otherAttr.getTreeClass().getClassNode();
				  otherStr = otherClassNode.getName() + otherAttr.toString();
				}
				return thisStr.compareTo(otherStr);
			}
			
			public boolean equals(Object o) {
				return o != null && o instanceof TreeAttribute && compareTo(o) == 0;
			}

			@Override
			public int hashCode() {
				return 0; //linear search
			}

			public void clearSelection() {
				readerSelected = false;
				writerSelected = false;
			}

			public void setReaderSelected() {
				readerSelected = true;
			}

			public void setWriterSelected() {
				writerSelected = true;
			}

			public TreeClass getTreeClass() {
				return TreeClass.this;
			}

			public Collection<EditProvider> getGeneratedAccessors() {
				Collection<EditProvider> accessors = new ArrayList<EditProvider>();
				if (readerSelected && writerSelected)
					accessors.add(new GeneratedAccessor(AttrAccessorNodeWrapper.ATTR_ACCESSOR, toString(), type, classNode));
				else if (readerSelected)
					accessors.add(new GeneratedAccessor(AttrAccessorNodeWrapper.ATTR_READER, toString(), type, classNode));
				else if (writerSelected)
					accessors.add(new GeneratedAccessor(AttrAccessorNodeWrapper.ATTR_WRITER, toString(), type, classNode));
				return accessors;
			}

			public class TreeAccessor {


				private boolean isReader;

				private String name;

				public TreeAccessor(String name, boolean isReader) {
					this.name = name;
					this.isReader = isReader;
				}

				public String toString() {
					return ((isReader()) ? READER : WRITER);
				}

				public boolean isWriter() {
					return !isReader;
				}

				public boolean isReader() {
					return isReader;
				}

				public String getAttributeName() {
					return name;
				}

				public TreeAttribute getAttribute() {
					return TreeAttribute.this;
				}
			}
		}
	}
}
