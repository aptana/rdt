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

package org.rubypeople.rdt.refactoring.tests.core.encapsulatefield;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.EncapsulateFieldConditionChecker;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.EncapsulateFieldConfig;
import org.rubypeople.rdt.refactoring.core.encapsulatefield.FieldEncapsulator;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;
import org.rubypeople.rdt.refactoring.tests.FileTestData;
import org.rubypeople.rdt.refactoring.tests.RefactoringTestCase;

public class EncapsulateFieldTester extends RefactoringTestCase {

	public EncapsulateFieldTester(String fileName) {
		super(fileName);
	}

	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException {
		FileTestData testData = new FileTestData(getName());
			
		EncapsulateFieldConfig config = new EncapsulateFieldConfig(testData, testData.getIntProperty("cursorPosition"));
		EncapsulateFieldConditionChecker checker = new EncapsulateFieldConditionChecker(config);
		if(!checker.shouldPerform()) {
			fail();
		}
		FieldEncapsulator encapsulator = new FieldEncapsulator(config);
		if(testData.hasProperty("enableReaderGeneration") && encapsulator.isReaderGenerationOptional()) {
			encapsulator.setReaderDisabled(!testData.getBoolProperty("enableReaderGeneration"));
		}
		if(testData.hasProperty("enableWriterGeneration") && encapsulator.isWriterGenerationOptional()) {
			encapsulator.setWriterDisabled(!testData.getBoolProperty("enableWriterGeneration"));
		}
		if(testData.hasProperty("readerVisibility") && !config.isReaderGenerationDisabled()) {
			encapsulator.setReaderVisibility(getVisibility(testData.getProperty("readerVisibility")));
		}
		if(testData.hasProperty("writerVisibility") && !config.isWriterGenerationDisabled()) {
			encapsulator.setWriterVisibility(getVisibility(testData.getProperty("writerVisibility")));
		}
		createEditAndCompareResult(testData.getSource(), testData.getExpectedResult(), encapsulator);
	}
	
	private METHOD_VISIBILITY getVisibility(String visibility) {
		if(visibility.equals("public")) {
			return METHOD_VISIBILITY.PUBLIC;
		}
		if(visibility.equals("protected")) {
			return METHOD_VISIBILITY.PROTECTED;
		}
		if(visibility.equals("private")) {
			return METHOD_VISIBILITY.PRIVATE;
		}
		fail();
		return METHOD_VISIBILITY.NONE;
	}
}
