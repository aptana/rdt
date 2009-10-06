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

package org.rubypeople.rdt.refactoring.tests.core.splitlocal;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.rubypeople.rdt.refactoring.core.splitlocal.SplitLocalConditionChecker;
import org.rubypeople.rdt.refactoring.core.splitlocal.SplitLocalConfig;
import org.rubypeople.rdt.refactoring.core.splitlocal.SplitTempEditProvider;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;
import org.rubypeople.rdt.refactoring.tests.FileTestData;

public class SplitLocalTester extends FileTestCase {
	
	public SplitLocalTester(String fileName) {
		super(fileName);
	}
	
	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException {
		
		FileTestData testData = new FileTestData(getName(), ".source.rb", ".result.rb");

		SplitLocalConfig config = new SplitLocalConfig(testData, testData.getIntProperty("pos"));
		SplitLocalConditionChecker checker = new SplitLocalConditionChecker(config);
		
		if(!checker.shouldPerform()) {
			fail();
		}
		
		SplitTempEditProvider splitTempEditProvider = new SplitTempEditProvider(config);
		
		String[] names = testData.getCommaSeparatedStringArray("names");
		
		assertEquals("Error in property file, wrong number of names.", splitTempEditProvider.getLocalUsages().size(), names.length);
		
		splitTempEditProvider.setNewNames(names);
		
		createEditAndCompareResult(testData.getSource(), testData.getExpectedResult(), splitTempEditProvider);
	}
}
