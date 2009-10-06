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

package org.rubypeople.rdt.refactoring.tests.core.mergeclasspartsinfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.Vector;

import org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile.InFileClassPartsMerger;
import org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile.MergeClassPartInFileConfig;
import org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile.MergeClassPartsInFileConditionChecker;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.tests.FileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;

public class ClassPartSelectorTester extends RefactoringTestCase
{

	private FileTestData testData;

	public ClassPartSelectorTester(String fileName)
	{
		super(fileName);
	}

	private Collection<PartialClassNodeWrapper> getCheckedParts(ClassNodeWrapper classNode)
	{
		Collection<Integer> checkedClassPartNumbers = getCheckedPartNumbers();
		ArrayList<PartialClassNodeWrapper> checkedParts = new ArrayList<PartialClassNodeWrapper>();
		PartialClassNodeWrapper[] classParts = classNode.getPartialClassNodes().toArray(new PartialClassNodeWrapper[0]);

		for (int currentPartNumber : checkedClassPartNumbers)
		{
			checkedParts.add(classParts[currentPartNumber - 1]);
		}
		return checkedParts;
	}

	private Collection<Integer> getCheckedPartNumbers()
	{
		String partsProperty = testData.getProperty("checkedParts");
		StringTokenizer tokenizer = new StringTokenizer(partsProperty, ",");

		Vector<Integer> intValues = new Vector<Integer>();

		while (tokenizer.hasMoreElements())
		{
			intValues.add(Integer.valueOf(tokenizer.nextToken().trim()));
		}
		return intValues;
	}

	private PartialClassNodeWrapper getPart(ClassNodeWrapper selectedClass, int selectedClassPartNumber)
	{
		Collection<PartialClassNodeWrapper> selectableClassNodes = selectedClass.getPartialClassNodes();
		PartialClassNodeWrapper[] partArray = selectableClassNodes.toArray(new PartialClassNodeWrapper[0]);
		return partArray[selectedClassPartNumber - 1];
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		testData = new FileTestData(getName());
	}

	@Override
	protected void tearDown() throws Exception
	{
		testData = null;

		super.tearDown();
	}

	@Override
	public void runTest() throws Exception
	{
		MergeClassPartInFileConfig config = new MergeClassPartInFileConfig(testData);
		MergeClassPartsInFileConditionChecker checker = new MergeClassPartsInFileConditionChecker(config);
		if (!checker.shouldPerform())
		{
			fail();
		}
		InFileClassPartsMerger selector = new InFileClassPartsMerger(config);
		String source = testData.getActiveFileContent();
		String expected = testData.getExpectedResult();

		int selectedClassPartNumber = testData.getIntProperty("selectedPart");
		ClassNodeWrapper selectedClass = config.getClassNode(testData.getProperty("selectedClass"));

		PartialClassNodeWrapper selectedPart = getPart(selectedClass, selectedClassPartNumber);
		Collection<PartialClassNodeWrapper> checkedParts = getCheckedParts(selectedClass);
		config.setCheckedClassParts(checkedParts);
		config.setSelectedClassPart(selectedPart);

		createEditAndCompareResult(source, expected, selector);
	}

}
