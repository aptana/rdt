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

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.util.HsrFormatter;

public abstract class RefactoringTestCase extends TestCase {

	public RefactoringTestCase(String name) {
		super(name);
	}
	
	protected void createEditAndCompareResult(String document, String expectedDocument, IEditProvider editProvider) throws BadLocationException {
		String result;
		if(editProvider != null) {
			TextEdit edit = editProvider.getEdit(document);
			Document doc = new Document(document);
			edit.apply(doc);
			result = doc.get();
			result = formatText(result);
			expectedDocument = formatText(expectedDocument);
		} else {
			result = document;
		}
		assertEquals(expectedDocument, result);
	}
	
	private String formatText(String expectedText) {
		String formatted = HsrFormatter.format("", expectedText, 0);
		// Make all \r\n into \n (Normalize newlines)
		formatted = formatted.replaceAll("\r\n", "\n");		
		return formatted;
	}

	protected void checkMultiFileEdits(IMultiFileEditProvider multiFileEditProvider, MultiFileTestData testData) throws BadLocationException {
		Collection<FileMultiEditProvider> editProviders = multiFileEditProvider.getFileEditProviders();
		for(String aktFileName : testData.getFileNames()) {
			String sourceDocument = testData.getSource(aktFileName);
			String resultDocument = testData.getResult(aktFileName);
			FileMultiEditProvider aktEditProvider = findEditProvider(editProviders, aktFileName);
			createEditAndCompareResult(sourceDocument, resultDocument, aktEditProvider);
		}
	}

	private FileMultiEditProvider findEditProvider(Collection<FileMultiEditProvider> editProviders, String aktFileName) {
		for(FileMultiEditProvider aktEditProvider : editProviders) {
			if(aktEditProvider.getFileName().equals(aktFileName)) {
				return aktEditProvider;
			}
		}
		return null;
	}
	
	
}
