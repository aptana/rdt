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

package org.rubypeople.rdt.refactoring.core.overridemethod;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.classnodeprovider.IncludedClassesProvider;
import org.rubypeople.rdt.refactoring.core.overridemethod.MethodsOverrider.TreeClass.TreeMethod;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditAndTreeContentProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProviderGroups;
import org.rubypeople.rdt.refactoring.editprovider.ITreeClass;
import org.rubypeople.rdt.refactoring.exception.UnknownClassNameException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.signatureprovider.ClassSignatureProvider;
import org.rubypeople.rdt.refactoring.signatureprovider.IClassSignatureProvider;
import org.rubypeople.rdt.refactoring.signatureprovider.MethodSignature;
import org.rubypeople.rdt.refactoring.ui.IItemSelectionReceiver;
import org.rubypeople.rdt.refactoring.ui.IParentProvider;

public class MethodsOverrider extends EditAndTreeContentProvider implements IItemSelectionReceiver {

	private ClassNodeProvider superClassNodeProvider;

	private Object[] selectedTreeItems;

	public MethodsOverrider(DocumentProvider docProvider) {
		superClassNodeProvider = docProvider.getClassNodeProvider();
		IncludedClassesProvider includedClassesProvider = new IncludedClassesProvider(docProvider);
		if (includedClassesProvider != null)
			superClassNodeProvider.addClassNodeProvider(includedClassesProvider);
		initTreeClasses(docProvider.getClassNodeProvider());
	}


	@Override
	public Collection<EditProvider> getEditProviders() {
		EditProviderGroups editProviderGroups = new EditProviderGroups();
		if (selectedTreeItems != null) {
			for (Object o : selectedTreeItems) {
				if (o instanceof TreeMethod) {
					TreeMethod treeMethod = (TreeMethod) o;
					editProviderGroups.add(treeMethod.getSignatureGroup(), treeMethod.getOverriddenMethod());
				}
			}
		}
		return editProviderGroups.getAllEditProviders();
	}

	private IClassSignatureProvider getSuperSignatureProvider(ClassNodeWrapper classNode) throws UnknownClassNameException {
		String superClassName = ""; //$NON-NLS-1$
		superClassName = classNode.getSuperClassName();
		return ClassSignatureProvider.getClassSignatureProvider(superClassName, superClassNodeProvider);
	}

	public void setSelectedItems(Object[] checkedElements) {
		selectedTreeItems = checkedElements.clone();
	}

	public class TreeClass implements org.rubypeople.rdt.refactoring.ui.IChildrenProvider, org.rubypeople.rdt.refactoring.editprovider.ITreeClass {
		private ClassNodeWrapper classNode;

		private Collection<TreeMethod> treeMethods;

		public TreeClass(ClassNodeWrapper classNode) {
			this.classNode = classNode;
			treeMethods = new ArrayList<TreeMethod>();
			try {
				IClassSignatureProvider superClassSignatureProvider = getSuperSignatureProvider(classNode);
				for (MethodSignature sign : superClassSignatureProvider.getMethodSignatures()) {
					treeMethods.add(new TreeMethod(sign));
				}
			} catch (UnknownClassNameException e) {
				/*don't care*/
			}
		}

		public String toString() {
			return classNode.getName();
		}

		public Object[] getChildren() {
			return treeMethods.toArray();
		}

		public boolean hasChildren() {
			return !treeMethods.isEmpty();
		}

		public class TreeMethod implements IParentProvider {
			private MethodSignature methodSignature;

			private String name;

			public TreeMethod(MethodSignature methodSignature) {
				this.methodSignature = methodSignature;
				name = getName();
			}

			public String getSignatureGroup() {
				if (methodSignature.isConstructor()) {
					return "c_" + TreeClass.this.toString(); //$NON-NLS-1$
				}
				return TreeClass.this.toString();
			}

			public OverriddenMethod getOverriddenMethod() {
				return new OverriddenMethod(classNode, methodSignature);
			}

			private String getName() {

				return methodSignature.getNameWithArgs();
			}

			public String toString() {
				return name;
			}

			public Object getParent() {
				return TreeClass.this;
			}
		}
	}

	@Override
	protected ITreeClass createTreeClass(ClassNodeWrapper classNode) {
		return new TreeClass(classNode);
	}
}
