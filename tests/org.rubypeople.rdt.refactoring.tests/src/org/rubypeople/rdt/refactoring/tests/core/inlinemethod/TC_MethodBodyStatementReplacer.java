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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.tests.core.inlinemethod;

import java.util.ArrayList;

import org.rubypeople.rdt.refactoring.core.inlinemethod.MethodBodyStatementReplacer;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.core.MultipleDocumentsInOneProvider;

public class TC_MethodBodyStatementReplacer extends FinderTestsBase {

	public void testReplaceOneSelf() {
		replace("test1", "replacement", "result1");
	}
	
	public void testReplaceTwoSelfs() {
		replace("test2", "replacement", "result2");
	}

	public void testReplaceSelfInCall() {
		replace("test3", "replacement", "result3");
	}

	public void testReturnStatement() {
		replaceReturn("test4", "result4");
	}

	public void testReturnStatementFac() {
		replaceReturn("test5", "result5");
	}
	
	public void testReturnCallToMember() {
		replaceCallToMember("test6", "result6", "a");
	}

	private void replace(String testName, String newName, String resultName) {
		IDocumentProvider result = new MethodBodyStatementReplacer().replaceSelfWithObject(doc.setActive(testName), newName);
		compareResults(resultName, result);
	}

	private void replaceReturn(String testName, String resultName) {
		IDocumentProvider result = new MethodBodyStatementReplacer().removeReturnStatements(doc.setActive(testName));
		compareResults(resultName, result);
	}

	private void replaceCallToMember(String testName, String resultName, String objName) {
		IDocumentProvider result = new MethodBodyStatementReplacer().replaceVarsWithAccessor(doc.setActive(testName), objName, new ArrayList<String>());
		compareResults(resultName, result);
	}

	private void compareResults(String resultName, IDocumentProvider result) {
		assertEquals(doc.setActive(resultName).getActiveFileContent(), result.getActiveFileContent());
	}
	
	@Override
	protected void setUp() throws Exception {
		doc = new MultipleDocumentsInOneProvider("TC_MethodBodyStatementReplacer.source.rb", this.getClass());
	}
}
