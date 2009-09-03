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

package org.rubypeople.rdt.refactoring.core.pushdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.classnodeprovider.AllFilesClassNodeProvider;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentWithIncluding;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditAndTreeContentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProviderGroups;
import org.rubypeople.rdt.refactoring.editprovider.ITreeClass;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.IItemSelectionReceiver;
import org.rubypeople.rdt.refactoring.ui.IParentProvider;

public class MethodDownPusher extends EditAndTreeContentProvider implements IItemSelectionReceiver {


	private Object[] selectedTreeItems;

	private ClassNodeProvider projectClassNodeProvider;

	private ClassNodeProvider classNodeProvider;

	public MethodDownPusher(DocumentProvider documentProvider) {
		this.projectClassNodeProvider = new AllFilesClassNodeProvider(new DocumentWithIncluding(documentProvider));
		this.classNodeProvider = documentProvider.getClassNodeProvider();

		initTreeClasses(classNodeProvider);
	}

	@Override
	protected ITreeClass createTreeClass(ClassNodeWrapper classNode) {
		return new TreeClass(classNode);
	}

	@Override
	public Collection<EditProvider> getEditProviders() {
		EditProviderGroups editProviderGroups = new EditProviderGroups();
		if (selectedTreeItems != null) {
			for (Object o : selectedTreeItems) {
				if (o instanceof TreeClass) {
					TreeClass treeClass = (TreeClass) o;
					treeClass.addDownPushedMethods(editProviderGroups);
				}
			}
		}
		return editProviderGroups.getAllEditProviders();
	}

	public void setSelectedItems(Object[] checkedElements) {
		selectedTreeItems = checkedElements.clone();
	}

	public class TreeClass implements org.rubypeople.rdt.refactoring.ui.IChildrenProvider, ITreeClass {
		private ClassNodeWrapper classNode;

		private Collection<DownPushableMethod> downPushableMethods;

		private Collection<ClassNodeWrapper> childClassNodes;

		public TreeClass(ClassNodeWrapper classNode) {
			this.classNode = classNode;
			downPushableMethods = new ArrayList<DownPushableMethod>();
			childClassNodes = projectClassNodeProvider.getSubClassesOf(classNode.getName());

			if (!childClassNodes.isEmpty()) {
				for (MethodNodeWrapper methodNode : classNode.getMethods()) {
					downPushableMethods.add(new DownPushableMethod(methodNode));
				}
			}
		}

		public void addDownPushedMethods(EditProviderGroups editProviderGroups) {
			Collection<MethodNodeWrapper> checkedMethods = getCheckedMethods();

			for (ClassNodeWrapper childClassNode : childClassNodes) {
				String childClassName = childClassNode.getName();
				if (classNodeProvider.hasClassNode(childClassName)) {
					ClassNodeWrapper classNode = classNodeProvider.getClassNode(childClassName);
					addDownPushedMethods(classNode, checkedMethods, editProviderGroups);
				} else {
					addDownPushedMethodsClass(childClassName, checkedMethods, editProviderGroups);
				}
			}
			addRemoveEdits(checkedMethods, editProviderGroups);
		}

		private void addRemoveEdits(Collection<MethodNodeWrapper> checkedMethods, EditProviderGroups editProviderGroups) {
			for (MethodNodeWrapper methodNode : checkedMethods) {
				editProviderGroups.add(Messages.MethodDownPusher_RemoveOldMethods, new DeleteEditProvider(methodNode.getWrappedNode()));
			}
		}

		private void addDownPushedMethods(ClassNodeWrapper childClassNode, Collection<MethodNodeWrapper> checkedMethods,
				EditProviderGroups editProviderGroups) {
			Collection<MethodNodeWrapper> constructorNodes = new ArrayList<MethodNodeWrapper>();
			Collection<MethodNodeWrapper> methodNodes = new ArrayList<MethodNodeWrapper>();
			separateConstructors(checkedMethods, methodNodes, constructorNodes);
			if (!constructorNodes.isEmpty()) {
				DownPushedMethods constructors = new DownPushedMethods(constructorNodes, childClassNode, true);
				editProviderGroups.add(Messages.MethodDownPusher_Constructors + childClassNode.getName(), constructors);
			}
			if (!methodNodes.isEmpty()) {
				DownPushedMethods methods = new DownPushedMethods(methodNodes, childClassNode, false);
				editProviderGroups.add(Messages.MethodDownPusher_Methods + childClassNode.getName(), methods);
			}
		}

		private void separateConstructors(Collection<MethodNodeWrapper> nodes, Collection<MethodNodeWrapper> methodNodes,
				Collection<MethodNodeWrapper> constructorNodes) {
			for (MethodNodeWrapper method : nodes) {
				if (method.getSignature().isConstructor())
					constructorNodes.add(method);
				else
					methodNodes.add(method);
			}
		}

		private Collection<MethodNodeWrapper> getCheckedMethods() {
			Collection<Object> allCheckedItems = Arrays.asList(selectedTreeItems);
			Collection<MethodNodeWrapper> checkedMethods = new ArrayList<MethodNodeWrapper>();
			for (DownPushableMethod method : downPushableMethods) {
				if (allCheckedItems.contains(method))
					checkedMethods.add(method.getMethodNode());
			}
			return checkedMethods;
		}

		private void addDownPushedMethodsClass(String className, Collection<MethodNodeWrapper> checkedMethods, EditProviderGroups editProviderGroups) {
			DownPushedMethodsClass methodsClass = new DownPushedMethodsClass(className, checkedMethods);
			editProviderGroups.add(Messages.MethodDownPusher_NewClasses, methodsClass);
		}

		public String toString() {
			return classNode.getName();
		}

		public Object[] getChildren() {
			return downPushableMethods.toArray();
		}

		public boolean hasChildren() {
			return !downPushableMethods.isEmpty();
		}

		public class DownPushableMethod implements IParentProvider {
			private MethodNodeWrapper methodNode;

			public DownPushableMethod(MethodNodeWrapper methodNode) {
				this.methodNode = methodNode;
			}

			public String toString() {
				return methodNode.getName();
			}

			public Object getParent() {
				return TreeClass.this;
			}

			public TreeClass getTreeClass() {
				return TreeClass.this;
			}

			public MethodNodeWrapper getMethodNode() {
				return methodNode;
			}
		}
	}
}
