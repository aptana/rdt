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

package org.rubypeople.rdt.refactoring.tests;

import java.util.Collection;
import java.util.Map;

import org.rubypeople.rdt.refactoring.core.IRefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;

public abstract class RefactoringConditionTestCase extends RefactoringTestCase {

	private String name;

	protected abstract void createEditProviderAndSetUserInput();
	
	public RefactoringConditionTestCase(String name) {
		super(name);
		this.name = name;
	}
	
	protected void checkConditions(RefactoringConditionChecker checker, FilePropertyData testData) {
		checkInitialConditionCheckResult(checker, testData);
		if(checker.shouldPerform()) {
			createEditProviderAndSetUserInput();
			checkFinalConditionCheckResult(checker, testData);
		} else {
			Collection<String> expectedWarnings = getExpectedStrings(testData, "finalWarning");
			Collection<String> expectedErrors = getExpectedStrings(testData, "finalError");
			assertEquals(0, expectedWarnings.size());
			assertEquals(0,expectedErrors.size());
		}
	}
	
	protected void assertStringCollection(Collection<String> expectedCollection, Collection<String> collection) {
		if(expectedCollection.size() != collection.size()) {
			printArrays(expectedCollection, collection);
			fail("Unequal size of collections. See console output for more details.");
		}
		for(String aktExpected : expectedCollection) {
			if(collection.contains(aktExpected)) {
				collection.remove(expectedCollection);
			} else {
				printArrays(expectedCollection, collection);
				fail("Missing expected string \"" + aktExpected + "\" in actual values");
			}
		}
	}
	
	private void printArrays(Collection<String> expectedCollection, Collection<String> collection) {
		System.err.println("Failure in test: " + name);
		System.err.println("Expected Strings (length is " + expectedCollection.size() + "):");
		for(String expectedString : expectedCollection) {
			System.err.println(expectedString);
		}
		System.err.println();
		System.err.println("Actual Strings (length is " + collection.size() + "):");
		for(String aktString : collection) {
			System.err.println(aktString);
		}
		System.err.println();
	}

	private void checkInitialConditionCheckResult(IRefactoringConditionChecker conditionChecker, FilePropertyData testData) {
		Map<String, Collection<String>> messages = conditionChecker.getInitialMessages();
		checkConditionCheckResult(messages, testData, "initial");
	}

	private void checkFinalConditionCheckResult(IRefactoringConditionChecker conditionChecker, FilePropertyData testData) {
		Map<String, Collection<String>> messages = conditionChecker.getFinalMessages();
		checkConditionCheckResult(messages, testData, "final");
	}

	private void checkConditionCheckResult(Map<String, Collection<String>> messages, FilePropertyData testData, String propertyNameSufix) {
		Collection<String> warnings = messages.get(IRefactoringConditionChecker.WARNING);
		Collection<String> errors = messages.get(IRefactoringConditionChecker.ERRORS);
		Collection<String> expectedWarnings = getExpectedStrings(testData, propertyNameSufix + "Warning");
		Collection<String> expectedErrors = getExpectedStrings(testData, propertyNameSufix + "Error");
		assertStringCollection(expectedWarnings, warnings);
		assertStringCollection(expectedErrors, errors);
	}

	private Collection<String> getExpectedStrings(FilePropertyData testData, String propertyName) {
		return testData.getNumberedProperty(propertyName);
	}
}
