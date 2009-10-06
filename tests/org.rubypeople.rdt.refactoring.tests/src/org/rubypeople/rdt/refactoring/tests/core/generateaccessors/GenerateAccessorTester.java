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

package org.rubypeople.rdt.refactoring.tests.core.generateaccessors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator;
import org.rubypeople.rdt.refactoring.core.generateaccessors.GeneratedAccessor;
import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator.TreeClass;
import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator.TreeClass.TreeAttribute;
import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator.TreeClass.TreeAttribute.TreeAccessor;
import org.rubypeople.rdt.refactoring.tests.FilePropertyData;
import org.rubypeople.rdt.refactoring.tests.FileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;

public class GenerateAccessorTester extends RefactoringTestCase {

	private AccessorsGenerator accessorsGenerator;

	private Collection<AccessorSelection> selections = new ArrayList<AccessorSelection>();

	public GenerateAccessorTester(String fileName) {
		super(fileName);
	}

	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException {
		FileTestData testData;
		testData = new FileTestData(getName());
		int type = getAccessorType(testData);
		accessorsGenerator = new AccessorsGenerator(testData, type);
		Collection<String> strSelections = testData.getNumberedProperty("selection");
		for (String aktSelection : strSelections) {
			String[] selection = FilePropertyData.seperateString(aktSelection);
			addSelection(selection[0], selection[1], FilePropertyData.getBoolValue(selection[2]), FilePropertyData.getBoolValue(selection[3]));
		}
		setSelection(selections);
		createEditAndCompareResult(testData.getSource(), testData.getExpectedResult(), accessorsGenerator);
	}

	private int getAccessorType(FileTestData testData) {
		String typeStr = testData.getProperty("type");
		if ("TYPE_METHOD_ACCESSOR".equals(typeStr)) {
			return GeneratedAccessor.TYPE_METHOD_ACCESSOR;
		} else if ("TYPE_SIMPLE_ACCESSOR".equals(typeStr)) {
			return GeneratedAccessor.TYPE_SIMPLE_ACCESSOR;
		}
		fail();
		return -1;
	}

	protected void addSelection(String name, String attributeName, boolean readerSelected, boolean writerSelected) {
		selections.add(new AccessorSelection(name, attributeName, readerSelected, writerSelected));
	}

	private void setSelection(Collection<AccessorSelection> selections) {
		Collection<TreeAccessor> selection = new ArrayList<TreeAccessor>();
		for (AccessorSelection sel : selections) {
			selection.addAll(getTreeAccessors(sel));
		}
		accessorsGenerator.setSelectedItems(selection.toArray());
	}

	private Collection<TreeAccessor> getTreeAccessors(AccessorSelection selection) {
		for (Object o : accessorsGenerator.getElements(null)) {
			if (o instanceof TreeClass) {
				TreeClass aktClass = (TreeClass) o;
				if (aktClass.toString().equals(selection.getClassName()))
					return getTreeAccessors(selection, aktClass);
			}
		}
		return new ArrayList<TreeAccessor>();
	}

	private Collection<TreeAccessor> getTreeAccessors(AccessorSelection selection, TreeClass klass) {

		for (Object o : klass.getChildren()) {
			if (o instanceof TreeAttribute) {
				TreeAttribute aktAttr = (TreeAttribute) o;
				if (aktAttr.toString().equals(selection.getAttributeName()))
					return getTreeAccessors(selection, aktAttr);
			}
		}
		return new ArrayList<TreeAccessor>();
	}

	private Collection<TreeAccessor> getTreeAccessors(AccessorSelection selection, TreeAttribute attr) {
		Collection<TreeAccessor> result = new ArrayList<TreeAccessor>();
		for (Object o : attr.getChildren()) {
			if (o instanceof TreeAccessor) {
				TreeAccessor aktAccessor = (TreeAccessor) o;
				if (aktAccessor.isReader() && selection.isReaderSelected()) {
					result.add(aktAccessor);
					attr.setReaderSelected();
				}
				if (aktAccessor.isWriter() && selection.isWriterSelected()) {
					result.add(aktAccessor);
					attr.setWriterSelected();
				}
			}
		}
		return result;
	}

	private static class AccessorSelection {
		private String className;

		private String attributeName;

		private boolean readerSelected;

		private boolean writerSelected;

		public AccessorSelection(String className, String attributeName, boolean readerSelected, boolean writerSelected) {
			this.className = className;
			this.attributeName = attributeName;
			this.readerSelected = readerSelected;
			this.writerSelected = writerSelected;
		}

		String getAttributeName() {
			return attributeName;
		}

		String getClassName() {
			return className;
		}

		boolean isReaderSelected() {
			return readerSelected;
		}

		boolean isWriterSelected() {
			return writerSelected;
		}
	}
}
