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

package org.rubypeople.rdt.refactoring.tests.core.extractmethod;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.text.BadLocationException;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.RefactoringContext;
import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractMethodConditionChecker;
import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractMethodConfig;
import org.rubypeople.rdt.refactoring.core.extractmethod.MethodExtractor;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;
import org.rubypeople.rdt.refactoring.tests.FileTestData;

public class ExtractMethodTester extends FileTestCase {

	public ExtractMethodTester(String fileName) {
		super(fileName);
	}
		
	private VisibilityNodeWrapper.METHOD_VISIBILITY getVisibility(FileTestData data) {
		
		if(!data.hasProperty("visibility")) {
			return METHOD_VISIBILITY.NONE;
		}
		String v = data.getProperty("visibility");

		if(v.equals("public")) {
			return METHOD_VISIBILITY.PUBLIC;
		} else if(v.equals("protected")) {
			return METHOD_VISIBILITY.PROTECTED;
		} else if(v.equals("private")){
			return METHOD_VISIBILITY.PRIVATE;
		} else {
			return METHOD_VISIBILITY.NONE;
		}
	}
	
	@Override
	public void runTest() throws FileNotFoundException, IOException, BadLocationException {
		
		FileTestData testData = new FileTestData(getName(), ".source.rb", ".result.rb");

		IRefactoringContext selection = new RefactoringContext(testData.getIntProperty("start"), testData.getIntProperty("end"), testData.getIntProperty("start"), "");
		ExtractMethodConfig config = new ExtractMethodConfig(testData , selection);
		ExtractMethodConditionChecker checker = new ExtractMethodConditionChecker(config);
		if(!checker.shouldPerform()) {
			fail();
		}
		
		MethodExtractor methodExtractor = new MethodExtractor(config);
		config.getHelper().setMethodName(testData.getProperty("name"));
		config.getHelper().setVisibility(getVisibility(testData));
		

		if(testData.hasProperty("order")) {
			for(String orderInstruction : testData.getCommaSeparatedStringArray("order")) {
				String[] instruction = orderInstruction.split("\\s+");
				assertEquals(2, instruction.length);
				int index = Integer.parseInt(instruction[0]);
				if(instruction[1].equals("up")) {
					config.getHelper().changeParameter(index, index - 1);
				} else if(instruction[1].equals("down")) {
					config.getHelper().changeParameter(index, index + 1);
				}
			}
		}
		
		if(testData.hasProperty("names")) {
			String[] names = testData.getCommaSeparatedStringArray("names");
			
			assertEquals(names.length, names.length);
			
			for(int i = 0; i < names.length; i++) {
				config.getHelper().changeParameter(i, names[i]);
			}
		}
		createEditAndCompareResult(testData.getSource(), testData.getExpectedResult(), methodExtractor);
	}
	
}
