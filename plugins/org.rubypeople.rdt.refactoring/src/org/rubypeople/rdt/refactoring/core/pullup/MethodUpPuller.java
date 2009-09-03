/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.core.pullup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditAndTreeContentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProviderGroups;
import org.rubypeople.rdt.refactoring.editprovider.ITreeClass;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.IItemSelectionReceiver;
import org.rubypeople.rdt.refactoring.ui.IParentProvider;

public class MethodUpPuller extends EditAndTreeContentProvider implements IItemSelectionReceiver {


	private Object[] selectedTreeItems;

	private ClassNodeProvider projectClassNodeProvider;

	private ClassNodeProvider classNodeProvider;

	public MethodUpPuller(DocumentProvider documentProvider) {
		this.projectClassNodeProvider = documentProvider.getProjectClassNodeProvider();
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
					treeClass.addUpPulledMethods(editProviderGroups);
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
		private ClassNodeWrapper superClass;

		public TreeClass(ClassNodeWrapper classNode) {
			this.classNode = classNode;
			downPushableMethods = new ArrayList<DownPushableMethod>();
			superClass = projectClassNodeProvider.getSuperClassOf(classNode.getSuperClassName());

			if (superClass != null) {
				for (MethodNodeWrapper methodNode : classNode.getMethods()) {
					downPushableMethods.add(new DownPushableMethod(methodNode));
				}
			}
		}

		public void addUpPulledMethods(EditProviderGroups editProviderGroups) {
			Collection<MethodNodeWrapper> checkedMethods = getCheckedMethods();
			
			String childClassName = superClass.getName();
			if (classNodeProvider.hasClassNode(childClassName)) {
				ClassNodeWrapper classNode = classNodeProvider.getClassNode(childClassName);
				addUpPulledMethods(classNode, checkedMethods, editProviderGroups);
			} else {
				addUpPulledMethodsClass(childClassName, checkedMethods, editProviderGroups);
			}

			addRemoveEdits(checkedMethods, editProviderGroups);
		}

		private void addRemoveEdits(Collection<MethodNodeWrapper> checkedMethods, EditProviderGroups editProviderGroups) {
			for (MethodNodeWrapper methodNode : checkedMethods) {
				editProviderGroups.add("removeOldMethods", new DeleteEditProvider(methodNode.getWrappedNode()));
			}
		}

		private void addUpPulledMethods(ClassNodeWrapper childClassNode, Collection<MethodNodeWrapper> checkedMethods,
				EditProviderGroups editProviderGroups) {
			Collection<MethodNodeWrapper> constructorNodes = new ArrayList<MethodNodeWrapper>();
			Collection<MethodNodeWrapper> methodNodes = new ArrayList<MethodNodeWrapper>();
			separateConstructors(checkedMethods, methodNodes, constructorNodes);
			if (!constructorNodes.isEmpty()) {
				UpPulledMethods constructors = new UpPulledMethods(constructorNodes, childClassNode, true);
				editProviderGroups.add("constructors_" + childClassNode.getName(), constructors);
			}
			if (!methodNodes.isEmpty()) {
				UpPulledMethods methods = new UpPulledMethods(methodNodes, childClassNode, false);
				editProviderGroups.add("methods_" + childClassNode.getName(), methods);
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

		private void addUpPulledMethodsClass(String className, Collection<MethodNodeWrapper> checkedMethods, EditProviderGroups editProviderGroups) {
			UpPulledMethodsClass methodsClass = new UpPulledMethodsClass(className, checkedMethods);
			editProviderGroups.add("newClasses_", methodsClass);
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
