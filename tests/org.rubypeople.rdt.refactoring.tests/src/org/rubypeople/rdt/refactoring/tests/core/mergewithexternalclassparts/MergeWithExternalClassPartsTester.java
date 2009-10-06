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

package org.rubypeople.rdt.refactoring.tests.core.mergewithexternalclassparts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.ClassPartTreeItem;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.ExternalClassPartsMerger;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.MergeWithExternalClassPartConfig;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.MergeWithExternalClassPartsConditionChecker;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.ExternalClassPartsMerger.MergeTreeClassItem;
import org.rubypeople.rdt.refactoring.core.mergewithexternalclassparts.ExternalClassPartsMerger.MergeTreeFileItem;
import org.rubypeople.rdt.refactoring.tests.MultiFileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;


public class MergeWithExternalClassPartsTester extends RefactoringTestCase{

	public MergeWithExternalClassPartsTester(String testName) {
		super(testName);
	}
	
	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException
	{
		MultiFileTestData testData = new MultiFileTestData(getName());
		MergeWithExternalClassPartConfig config = new MergeWithExternalClassPartConfig(testData);
		MergeWithExternalClassPartsConditionChecker checker = new MergeWithExternalClassPartsConditionChecker(config);
		
		if(!checker.shouldPerform()) {
			fail();
		}
		ExternalClassPartsMerger merger = new ExternalClassPartsMerger(config);
		
		ArrayList<ClassPartTreeItem> selection = initSelection(testData, merger);
		merger.setSelectedItems(selection.toArray());
		
		checkMultiFileEdits(merger, testData);
	}

	private ArrayList<ClassPartTreeItem> initSelection(MultiFileTestData testContext, ExternalClassPartsMerger merger) {
		ArrayList<ClassPartTreeItem> selection = new ArrayList<ClassPartTreeItem>();
		ArrayList<MergeTreeClassItem> classItems = merger.getTreeItems();
		int sourceClassPartNumber = testContext.getIntProperty("sourceClassPartNumber");
		for(MergeTreeClassItem currentItem : classItems){
			if(currentItem.toString().equals(testContext.getProperty("targetClass"))){
				if(--sourceClassPartNumber == 0){
					addClassToSelection(testContext, selection, currentItem);
				}
			}
		}
		
		return selection;
	}

	private void addClassToSelection(MultiFileTestData testContext, ArrayList<ClassPartTreeItem> selection, MergeTreeClassItem currentItem) {
		
		selection.add(currentItem);

		for(String currentFileName : testContext.getIncludedFileNames()){
			addClassPartsToSelection(testContext, selection, currentItem, currentFileName);
		}
	}

	private void addClassPartsToSelection(MultiFileTestData testContext, ArrayList<ClassPartTreeItem> selection, MergeTreeClassItem currentItem, String currentFileName) {
		int currentFilePartNumber = getDestinationClassPartNumber(currentFileName, testContext);
		for(MergeTreeFileItem currentFileItem : currentItem.getClassParts()){
			if(currentFileItem.toString().equals(currentFileName)){
				if(--currentFilePartNumber == 0){
					selection.add(currentFileItem);
				}
			}
		}
	}
	
	private int getDestinationClassPartNumber(String fileName, MultiFileTestData testContext){
		ArrayList<Integer> partNumbers = new ArrayList<Integer>();
		
		String[] destFiles = testContext.getIncludedFileNames();

		String destPartNumbers = testContext.getProperty("destinationPartNumbers");
		StringTokenizer tokenizer = new StringTokenizer(destPartNumbers, ",", false);
		
		for(String currentFileName : destFiles){
			Integer currentPart = new Integer(tokenizer.nextToken().trim());
			if(currentFileName.equals(fileName)){
				partNumbers.add(currentPart);
			}
		}
			
		return partNumbers.get(0).intValue();
	}
}
