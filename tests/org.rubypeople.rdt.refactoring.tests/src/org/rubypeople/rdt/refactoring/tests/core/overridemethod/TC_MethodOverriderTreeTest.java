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
import org.rubypeople.rdt.refactoring.tests.TreeProviderTester;

public class TC_MethodOverriderTreeTest extends TreeProviderTester
{
	private static final String superFileName = "super.rb";

	private final static String A_EXTENDS_X_DOCUMENT =	"require \"" + superFileName + "\"\n" +
														"class A < X\n" +
														"end";
	
	private final static String TWO_CLASSES = 	"require \"" + superFileName + "\"\n" +
												"class A < X\n" +
												"end\n" +
												"class B < Y\n" +
												"end\n";

	private final static String TEST_DOCUMENT_SIMPLE =	"class X\n" +
														"  def method arg\n" +
														"    @a = a\n" +
														"    @b = a\n" +
														"  end\n" +
														"end";
	
	private final static String TEST_DOCUMENT_NO_CONSTRUCTOR_ARGS =	"class X\n" +
																	"  def initialize\n" +
																	"    @a = 1\n" +
																	"  end\n" +
																	"end";

	private final static String TEST_DOCUMENT_ONE_CONSTRUCTOR_ARG =	"class X\n" +
																		"  def initialize a\n" +
																		"    @a = a\n" +
																		"  end\n" +
																		"end";
	

	private final static String TEST_DOCUMENT_TREE_ONSTRUCTOR_ARGS =	"class X\n" +
																		"  def initialize a, b, c\n" +
																		"    @a = a\n" +
																		"  end\n" +
																		"end";

	private final static String TEST_DOCUMENT_TWO_CLASSES =	"#Comment\n" +
																"class X\n" +
																"  def initialize a\n" +
																"    @a = a\n" +
																"  end\n" +
																"end\n" +
																"class Y\n" +
																"  #Comment\n" +
																"  def initialize b, c\n" +
																"    @x = b + c\n" +
																"  end\n" +
																"end";
	
	private static final String TEST_DOCUMENT_METHOD_AND_CONSTRUCTOR = 	"class X\n" +
																		"  def initialize arg0, arg1\n" +
																		"    @a = a\n" +
																		"  end\n" +
																		"  def method arg\n" +
																		"    @a = a\n" +
																		"    @b = a\n" +
																		"  end\n" +
																		"end";
	
	private static final String TEST_DOCUMENT_TWO_METHOD_AND_TWO_CONSTRUCTOR = 	"class X\n" +
																				"  def initialize arg0, arg1\n" +
																				"    @a = a\n" +
																				"  end\n" +
																				"  def initialize arg\n" +
																				"    @a = a\n" +
																				"  end\n" +
																				"  def method0 arg\n" +
																				"    @b = a\n" +
																				"  end\n" +
																				"  def method1 arg\n" +
																				"    @b = a\n" +
																				"  end\n" +
																				"end";
	
	public void testDocumentOneMethod()
	{
		addContent(new String[]{"A", "method arg"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentOneMethod.rb", A_EXTENDS_X_DOCUMENT);
		docProvider.addFile(superFileName, TEST_DOCUMENT_SIMPLE);
		validate(new MethodsOverrider(docProvider));
	}
	
	public void testDocumentNoConstructorArgs()
	{
		addContent(new String[]{"A", "initialize"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentNoConstructorArgs.rb", A_EXTENDS_X_DOCUMENT);
		docProvider.addFile(superFileName, TEST_DOCUMENT_NO_CONSTRUCTOR_ARGS);
		validate(new MethodsOverrider(docProvider));
	}
	
	public void testDocumentOneConstructorArg()
	{
		addContent(new String[]{"A", "initialize a"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentOneConstructorArg.rb", A_EXTENDS_X_DOCUMENT);
		docProvider.addFile(superFileName, TEST_DOCUMENT_ONE_CONSTRUCTOR_ARG);
		validate(new MethodsOverrider(docProvider));
	}
	
	public void testDocumentTreeConstructorArgs()
	{
		addContent(new String[]{"A", "initialize a, b, c"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentTreeConstructorArgs.rb", A_EXTENDS_X_DOCUMENT);
		docProvider.addFile(superFileName, TEST_DOCUMENT_TREE_ONSTRUCTOR_ARGS);
		validate(new MethodsOverrider(docProvider));
	}
	
	public void testDocumentTwoClasses()
	{
		addContent(new String[]{"A", "initialize a"});
		addContent(new String[]{"B", "initialize b, c"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentTwoClasses.rb", TWO_CLASSES);
		docProvider.addFile(superFileName, TEST_DOCUMENT_TWO_CLASSES);
		validate(new MethodsOverrider(docProvider));
	}
	
	public void testDocumentMethodAndConstructor()
	{
		addContent(new String[]{"A", "initialize arg0, arg1"});
		addContent(new String[]{"A", "method arg"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentMethodAndConstructor.rb", A_EXTENDS_X_DOCUMENT);
		docProvider.addFile(superFileName, TEST_DOCUMENT_METHOD_AND_CONSTRUCTOR);
		validate(new MethodsOverrider(docProvider));
	}
	
	public void testDocumentMethodsAndTwoConstructors()
	{
		addContent(new String[]{"A", "initialize arg"});
		addContent(new String[]{"A", "method0 arg"});
		addContent(new String[]{"A", "method1 arg"});
		StringDocumentProvider docProvider = new StringDocumentProvider("override_method_test_testDocumentMethodsAndTwoConstructors.rb", A_EXTENDS_X_DOCUMENT);
		docProvider.addFile(superFileName, TEST_DOCUMENT_TWO_METHOD_AND_TWO_CONSTRUCTOR);
		validate(new MethodsOverrider(docProvider));
	}
}
