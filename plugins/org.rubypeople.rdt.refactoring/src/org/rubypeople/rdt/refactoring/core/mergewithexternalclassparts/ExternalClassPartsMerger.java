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

package org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiFileEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.IChildrenProvider;
import org.rubypeople.rdt.refactoring.ui.IItemSelectionReceiver;
import org.rubypeople.rdt.refactoring.ui.TreeContentProvider;

public class ExternalClassPartsMerger extends TreeContentProvider implements IItemSelectionReceiver,  IMultiFileEditProvider {

	private ArrayList<MergeTreeClassItem> treeItems;
	private String activeFile;
	private MergeWithExternalClassPartConfig config;
	private Object[] checkedElements;

	public ExternalClassPartsMerger(MergeWithExternalClassPartConfig config) {
		this.config = config;
		activeFile = config.getDocumentProvider().getActiveFileName();
		initClassesTree(config.getClassNodeProvider());
	}

	private void initClassesTree(ClassNodeProvider classesProvider) {

		treeItems = new ArrayList<MergeTreeClassItem>();
		Collection<ClassNodeWrapper> classes = classesProvider.getAllClassNodes();

		for (ClassNodeWrapper currentClassNode : classes) {
			addTreeItem(currentClassNode);
		}
	}

	private void addTreeItem(ClassNodeWrapper classWrapper) {
		Collection<PartialClassNodeWrapper> classParts = classWrapper.getPartialClassNodes();
		Collection<PartialClassNodeWrapper> localParts = classWrapper.getPartialClassNodesOfFile(activeFile);
		classParts.removeAll(localParts);

		if (!classParts.isEmpty()) {
			for (PartialClassNodeWrapper currentClassPart : localParts)

				treeItems.add(new MergeTreeClassItem(currentClassPart, classWrapper));

		}
	}

	public Collection<FileMultiEditProvider> getFileEditProviders() {
		MultiFileEditProvider multiProvider = new MultiFileEditProvider();

		for (Object item : checkedElements) {
			FileEditProvider editProvider = null;
			if (item instanceof MergeTreeFileItem) {
				MergeTreeFileItem fileItem = (MergeTreeFileItem) item;
				editProvider = new FileEditProvider(fileItem.toString(), fileItem);
				if (fileItem.getInsertedNodeWrapper().getClassBodyNode() == null) {
					continue;
				}
			} else if (item instanceof MergeTreeClassItem) {
				MergeTreeClassItem classItem = (MergeTreeClassItem) item;
				editProvider = new FileEditProvider(classItem.getPath(), classItem);
			}
			multiProvider.addEditProvider(editProvider);
		}
		return multiProvider.getFileEditProviders();
	}

	@Override
	public Object[] getElements(Object inputElement) {

		return treeItems.toArray();
	}

	public void setSelectedItems(Object[] checkedElements) {
		this.checkedElements = checkedElements.clone();
		config.setSelectionEmpty(checkedElements.length == 0);
	}

	public class MergeTreeClassItem extends DeleteEditProvider implements IChildrenProvider, ClassPartTreeItem {

		PartialClassNodeWrapper classPart;

		ClassNodeWrapper wholeClass;

		ArrayList<MergeTreeFileItem> classParts;

		public MergeTreeClassItem(PartialClassNodeWrapper classPart, ClassNodeWrapper wholeClass) {
			super(classPart.getWrappedNode());
			this.classPart = classPart;
			this.wholeClass = wholeClass;
			initClassParts();
		}

		private void initClassParts() {
			classParts = new ArrayList<MergeTreeFileItem>();

			Collection<PartialClassNodeWrapper> partialClasses = wholeClass.getPartialClassNodes();

			for (PartialClassNodeWrapper currentClassPart : partialClasses) {
				String file = currentClassPart.getWrappedNode().getPosition().getFile();

				if (activeFile.equals(file)) {
					continue;
				}
				classParts.add(new MergeTreeFileItem(currentClassPart, classPart));
			}

		}

		public Object[] getChildren() {
			return classParts.toArray();
		}

		public Collection<MergeTreeFileItem> getClassParts() {
			return classParts;
		}

		public boolean hasChildren() {
			return true;
		}

		private String getPath() {
			return config.getDocumentProvider().getActiveFileName();
		}

		public String toString() {
			return classPart.getClassName();
		}

		public PartialClassNodeWrapper getClassPartWrapper() {
			return classPart;
		}
	}

	public static class MergeTreeFileItem extends ClassInsertProvider implements ClassPartTreeItem {

		private PartialClassNodeWrapper classNode;

		private PartialClassNodeWrapper insertedNode;

		public MergeTreeFileItem(PartialClassNodeWrapper classNode, PartialClassNodeWrapper classPart) {
			super(classPart.getClassBodyNode(), classNode);
			this.classNode = classNode;
			this.insertedNode = classPart;
		}

		public String toString() {
			return classNode.getWrappedNode().getPosition().getFile();
		}

		public PartialClassNodeWrapper getClassPartWrapper() {
			return classNode;
		}

		public PartialClassNodeWrapper getInsertedNodeWrapper() {
			return insertedNode;
		}

	}

	public ArrayList<MergeTreeClassItem> getTreeItems() {
		return treeItems;
	}
}