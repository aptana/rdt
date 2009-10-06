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

import org.jruby.ast.LocalAsgnNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.rubypeople.rdt.refactoring.core.inlinemethod.ReturnStatementReplacer;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.core.MultipleDocumentsInOneProvider;

public class TC_ReturnStatementReplacer extends FinderTestsBase {

	public void testMultipleReturns() {
		doc.setActive("test1");
		assertFalse(new ReturnStatementReplacer().singleReturnOnLastLine(doc));
	}
	
	public void testSingleReturn() {
		doc.setActive("test2");
		assertTrue(new ReturnStatementReplacer().singleReturnOnLastLine(doc));
	}
	
	public void testReturnNotAtTheEnd() {
		doc.setActive("test3");
		assertFalse(new ReturnStatementReplacer().singleReturnOnLastLine(doc));
	}
	
	public void testReturnLastLine() {
		doc.setActive("test4");
		assertTrue(new ReturnStatementReplacer().singleReturnOnLastLine(doc));
	}

	public void testExplicitReturn() {
		doc.setActive("test5");
		IDocumentProvider resultDocument = new ReturnStatementReplacer().replaceReturn(doc, new LocalAsgnNode(new IDESourcePosition(), "result", 2, null));
		assertEquals("result = var", lastLine(resultDocument));
	}

	public void testImplicitReturn() {
		doc.setActive("test6");
		IDocumentProvider resultDocument = new ReturnStatementReplacer().replaceReturn(doc, new LocalAsgnNode(new IDESourcePosition(), "result", 2, null));
		assertEquals("result = (2 ** 10)", lastLine(resultDocument));
	}

	public void testFactorialReturn() {
		doc.setActive("test7");
		IDocumentProvider resultDocument = new ReturnStatementReplacer().replaceReturn(doc, new LocalAsgnNode(new IDESourcePosition(), "fac", 2, null));
		assertEquals("fac = ((1..10).inject(1) { |i, j| i * j })", lastLine(resultDocument));
	}
	
	public void testReturnFixnum() {
		doc.setActive("test8");
		IDocumentProvider resultDocument = new ReturnStatementReplacer().replaceReturn(doc, new LocalAsgnNode(new IDESourcePosition(), "var", 2, null));
		assertEquals("var = 5", lastLine(resultDocument));
	}

	public void testErroneousDocument() {
		doc.setActive("test3");
		IDocumentProvider resultDocument = new ReturnStatementReplacer().replaceReturn(doc, new LocalAsgnNode(new IDESourcePosition(), "fac", 2, null));
		assertNull(resultDocument);
	}

	public void testNullAssignment() {
		doc.setActive("test7");
		IDocumentProvider resultDocument = new ReturnStatementReplacer().replaceReturn(doc, null);
		assertNull(resultDocument);
	}
	
	private String lastLine(IDocumentProvider document) {
		String[] lines = document.getActiveFileContent().split("\\n");
		return lines[lines.length - 1];
	}

	@Override
	protected void setUp() throws Exception {
		doc = new MultipleDocumentsInOneProvider("TC_ReturnStatementReplacer.source.rb", this.getClass());
	}
}
