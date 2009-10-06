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

package org.rubypeople.rdt.refactoring.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.rubypeople.rdt.refactoring.editprovider.EditAndTreeContentProvider;
import org.rubypeople.rdt.refactoring.exception.UnknownClassNameException;
import org.rubypeople.rdt.refactoring.ui.CheckableItem;
import org.rubypeople.rdt.refactoring.ui.IChildrenProvider;
import org.rubypeople.rdt.refactoring.ui.IItemSelectionReceiver;

public abstract class TwoLayerTreeEditProviderTester extends RefactoringTestCase {

	private EditAndTreeContentProvider provider;

	private Collection<ItemSelection> selections;

	private boolean autoCheckChildrenOnParentCheck;

	public TwoLayerTreeEditProviderTester(String fileName, boolean autoCheckChildrenOnParentCheck) {
		super(fileName);
		this.autoCheckChildrenOnParentCheck = autoCheckChildrenOnParentCheck;
		selections = new ArrayList<ItemSelection>();
	}

	protected void check(EditAndTreeContentProvider provider, String document, String expectedDocument) {
		this.provider = provider;
		setSelections();
		try {
			createEditAndCompareResult(document, expectedDocument, provider);
		} catch (MalformedTreeException e) {
			fail();
		} catch (BadLocationException e) {
			fail();
		}
	}

	protected void addSelection(String className) {
		addSelection(className, null);
	}

	protected void addSelection(String className, String childName) {
		selections.add(new ItemSelection(className, childName));
	}

	private void setSelections() {
		assertNotNull(provider.getElements(null));
		if (provider instanceof IItemSelectionReceiver) {
			IItemSelectionReceiver itemSelectionReceiver = (IItemSelectionReceiver) provider;
			setSelections(itemSelectionReceiver);
		} else {
			selectCheckableItems();
		}
	}

	private void selectCheckableItems() {
		for (ItemSelection selection : selections) {
			selectChekableItem(selection);
		}
	}

	private void selectChekableItem(ItemSelection selection) {

		for (Object aktClass : provider.getElements(null)) {
			if (aktClass.toString().equals(selection.getClassName())) {
				((CheckableItem) aktClass).setChecked(true);
				if (selection.hasChild() && autoCheckChildrenOnParentCheck)
					selectChild((IChildrenProvider) aktClass, selection.getChildName());
				break;
			}
		}
	}

	private void setSelections(IItemSelectionReceiver itemSelectionReceiver) {
		Collection<Object> selectedObjects = new ArrayList<Object>();
		try {
			for (ItemSelection selection : selections) {
				Object klass = getClass(selection.getClassName());
				selectedObjects.add(klass);
				if (selection.hasChild()) {
					selectedObjects.add(getSelectedObject(selection.getClassName(), selection.getChildName()));
				} else if (autoCheckChildrenOnParentCheck) {
					addAllClassChilds(selectedObjects, selection, klass);
				}
			}
		} catch (UnknownClassNameException e) {
			e.printStackTrace();
			fail();
		}
		itemSelectionReceiver.setSelectedItems(selectedObjects.toArray());
	}

	private Object getSelectedObject(String className, String childName) throws UnknownClassNameException {

		Object klass = getClass(className);
		StringTokenizer tokanizer = new StringTokenizer(childName);
		childName = tokanizer.nextToken();
		for (Object child : provider.getChildren(klass)) {
			tokanizer = new StringTokenizer(child.toString());
			String methodName = tokanizer.nextToken();
			if (methodName.equals(childName)) {
				return child;
			}
		}
		throw new UnknownClassNameException();
	}

	private void addAllClassChilds(Collection<Object> selectedObjects, ItemSelection selection, Object klass) throws UnknownClassNameException {
		Object[] childs = provider.getChildren(klass);
		for (Object child : childs) {
			selectedObjects.add(getSelectedObject(selection.getClassName(), child.toString()));
		}
	}

	private Object getClass(String className) throws UnknownClassNameException {
		for (Object object : provider.getElements(null)) {
			if (object.toString().equals(className))
				return object;
		}
		throw new UnknownClassNameException();
	}

	private void selectChild(IChildrenProvider klass, String childName) {
		for (Object child : provider.getChildren(klass)) {
			if (child.toString().equals(childName)) {
				((CheckableItem) child).setChecked(true);
			}
		}
	}

	protected void createEditAndCompareResult(String document, String expectedDocument, EditAndTreeContentProvider provider) throws MalformedTreeException,
			BadLocationException {
		this.provider = provider;
		setSelections();
		super.createEditAndCompareResult(document, expectedDocument, provider);
	}

	private static class ItemSelection {
		private String className;

		private String childName;

		public ItemSelection(String className, String childName) {
			this.className = className;
			this.childName = childName;
		}

		public String getClassName() {
			return className;
		}

		public String getChildName() {
			return childName;
		}

		public boolean hasChild() {
			return childName != null && !childName.equals("");
		}
	}
}
