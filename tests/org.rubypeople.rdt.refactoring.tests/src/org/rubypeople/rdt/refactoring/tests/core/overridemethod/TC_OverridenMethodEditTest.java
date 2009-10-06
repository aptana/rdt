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

package org.rubypeople.rdt.refactoring.tests.core.overridemethod;

import org.rubypeople.rdt.refactoring.core.overridemethod.MethodsOverrider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.TwoLayerTreeEditProviderTester;

public class TC_OverridenMethodEditTest extends TwoLayerTreeEditProviderTester {

	private static int test_count = 0;

	public TC_OverridenMethodEditTest() {
		super("Overriden Method Edit Test", true);
	}

	private final static String BASE_DOCUMENT_SIMPLE =	"class A < X\n" +
														"end";
	
	private final static String BASE_DOCUMENT_MESSY = 	"#Comment\n" +
														"class A < X\n" +
														"  attr_accessor :a\n" +
														"  #Comment\n" +
														"  def method a\n" +
														"    x = a\n" +
														"  end\n" +
														"  attr_accessor :b\n" +
														"end";

	private final static String SUPER_CLASS_EMPTY =	"class X\n" +
													"end";
	
	private final static String SUPER_CLASS_NO_CONSTRUCTOR_ARGS =	"class X\n" +
																	"  def initialize\n" +
																	"    @a = 1\n" +
																	"  end\n" +
																	"end";

	private final static String SUPER_CLASS_ONE_CONSTRUCTOR_ARGS =	"class X\n" +
																	"  def initialize a\n" +
																	"    @a = 1\n" +
																	"  end\n" +
																	"end";

	private final static String SUPER_CLASS_TWO_CONSTRUCTOR_ARGS =	"class X\n" +
																	"  def initialize a, b\n" +
																	"    @a = 1\n" +
																	"  end\n" +
																	"end";

	private final static String SUPER_CLASS_ONE_METHOD =	"class X\n" +
															"  def method a, b\n" +
															"    @a = 1\n" +
															"  end\n" +
															"end";

	private final static String SUPER_CLASS_TWO_METHODS =	"class X\n" +
															"  def method0 a, b\n" +
															"    @a = 1\n" +
															"    @b = 2\n" +
															"  end\n" +
															"  def method1 a\n" +
															"    @a = 1\n" +
															"  end\n" +
															"end";

	private final static String SUPER_CLASS_TWO_METHODS_AND_CONSTRUCTOR =	"#Comment\n" +
																			"class X\n" +
																			"  def initialize\n" +
																			"    @a = 1\n" +
																			"  end\n" +
																			"  #Comment\n" +
																			"  def method0\n" +
																			"    @a = 1\n" +
																			"  end\n" +
																			"  def method1 a\n" +
																			"    @a = 1\n" +
																			"  end\n" +
																			"end";
	

	
	public void testSuperClassWithNoConstructors() 
	{
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_EMPTY, "class A < X\nend");
	}
	
	public void testSuperClassWithSimpleConstructor() 
	{
		addSelection("A");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_NO_CONSTRUCTOR_ARGS, "class A < X\n\ndef initialize\nsuper\nend\n\nend");
	}
	
	public void testSuperClassWithOneConstructorArgs() 
	{
		addSelection("A");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_ONE_CONSTRUCTOR_ARGS, "class A < X\n\ndef initialize a\nsuper a\nend\n\nend");
	}
	
	public void testSuperClassWithTwoConstructorArgs() 
	{
		addSelection("A");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_TWO_CONSTRUCTOR_ARGS, "class A < X\n\ndef initialize a, b\nsuper(a, b)\nend\n\nend");
	}
	
	public void testSuperClassWithOneMethod()
	{
		addSelection("A");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_ONE_METHOD, "class A < X\n\ndef method a, b\nsuper(a, b)\nend\n\nend");
	}
	
	public void testSuperClassWithTwoMethods()
	{
		addSelection("A");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_TWO_METHODS, "class A < X\n\ndef method0 a, b\nsuper(a, b)\nend\n\ndef method1 a\nsuper a\nend\n\nend");
	}

	public void testSuperClassWithTwoMethodsOneSelected()
	{
		addSelection("A", "method0");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_TWO_METHODS, "class A < X\n\ndef method0 a, b\nsuper(a, b)\nend\n\nend");
	
	}
	
	public void testSuperClassWithTwoMethodsAndConstructor()
	{
		addSelection("A");
		check(BASE_DOCUMENT_SIMPLE, SUPER_CLASS_TWO_METHODS_AND_CONSTRUCTOR, 	"class A < X\n\n" +
																				"def initialize\nsuper\nend\n\n" +
																				"def method0\nsuper\nend\n\n" +
																				"def method1 a\nsuper a\nend\n\n" +
																				"end");
	}
	
	public void testSuperClassWithTwoMethodsAndConstructorMessyBase()
	{
		addSelection("A");
		check(BASE_DOCUMENT_MESSY, SUPER_CLASS_TWO_METHODS_AND_CONSTRUCTOR, 	"#Comment\n" +
																			"class A < X\n" +
																			"attr_accessor :a\n\n" +
																			"def initialize\nsuper\nend\n\n" +
																			"#Comment\n" +
																			"def method a\nx = a\nend\n\n" +
																			"def method0\nsuper\nend\n\n" +
																			"def method1 a\nsuper a\nend\n\n" +
																			"attr_accessor :b\n" +
																			"end");
	}
	
	private void check(String classDocument, String superClassDocument, String expectedDocument) {
		
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_edit_test_nr_" + ++test_count, classDocument);
		if(superClassDocument != null) {
			docProvider.addFile("superClassDocument", superClassDocument);
		}
		check(new MethodsOverrider(docProvider), classDocument, expectedDocument);
	}
}
