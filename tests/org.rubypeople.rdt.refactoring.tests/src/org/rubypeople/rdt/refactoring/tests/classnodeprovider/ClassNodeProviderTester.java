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

package org.rubypeople.rdt.refactoring.tests.classnodeprovider;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.classnodeprovider.AllFilesClassNodeProvider;
import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;



public abstract class ClassNodeProviderTester extends FileTestCase {
	private Collection<String> expectedMethodSignatures;
	private Collection<String> expectedClasses;
	private StringDocumentProvider docProvider;
	
	public ClassNodeProviderTester() {
		super("ClassNodeProviderTester");
		init();
	}
	
	protected void init() {
		docProvider = new StringDocumentProvider("empty_document","");
		expectedMethodSignatures = new ArrayList<String>();
		expectedClasses = new ArrayList<String>();
	}
	
	public void addTestRubyFile(String fileName) {
		docProvider.addFile(fileName, getSource(fileName));
	}

	public void addExpectedMethod(String signature) {
		expectedMethodSignatures.add(signature);
	}

	public void addExpectedClass(String name) {
		expectedClasses.add(name);
	}
	
	public void validateClasses() {
		ClassNodeProvider provider = new AllFilesClassNodeProvider(docProvider);
		Collection<ClassNodeWrapper> classes = provider.getAllClassNodes();

		ClassNodeWrapper[] classesArray = classes.toArray(new ClassNodeWrapper[0]);
		String[] expectedClassesArray = expectedClasses.toArray(new String[0]);
		ArrayList<String> availableClasses = new ArrayList<String>();
		
		for(int i = 0; i < classesArray.length; i++) {
			Collection<PartialClassNodeWrapper> partialClassNodes = classesArray[i].getPartialClassNodes();
			for(PartialClassNodeWrapper partialClass : partialClassNodes) {
				availableClasses.add(partialClass.getClassName());
			}
		}
		
		
		for(int i = 0; i < expectedClassesArray.length; i++) {
			assertEquals(expectedClassesArray[i], availableClasses.get(i));
		}
		
	}
	
	public void validateMethods(String className) {
		ClassNodeProvider provider = new AllFilesClassNodeProvider(docProvider);
		Collection<MethodNodeWrapper> methods = provider.getClassNode(className).getMethods();
		assertEquals(expectedMethodSignatures.size(), methods.size());

		MethodNodeWrapper[] methodsArray = methods.toArray(new MethodNodeWrapper[0]);
		String[] expectedMethodNodeSignaturesArray = expectedMethodSignatures.toArray(new String[0]);
		
		for(int i = 0; i < methodsArray.length; i++) {
			assertEquals(expectedMethodNodeSignaturesArray[i], methodsArray[i].getSignature().getNameWithArgs());
		}
	}
}
