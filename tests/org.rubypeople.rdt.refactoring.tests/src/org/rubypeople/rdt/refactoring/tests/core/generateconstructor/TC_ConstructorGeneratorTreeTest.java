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

package org.rubypeople.rdt.refactoring.tests.core.generateconstructor;

import org.rubypeople.rdt.refactoring.core.generateconstructor.ConstructorsGenerator;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.TreeProviderTester;

public class TC_ConstructorGeneratorTreeTest extends TreeProviderTester
{
	private final static String TEST_DOCUMENT_SIMPLE =	"#classComment\n" +
														"class X\n" +
														"  #methodComment\n" + 
														"  def a(a)\n" +
														"    @a = a\n" +
														"    @b = a\n" +
														"  end\n" +
														"end";
	
	private final static String TEST_DOCUMENT_NO_ATTRS =	"class X\n" +
															"  def a(a)\n" +
															"  end\n" +
															"end";

	private final static String TEST_DOCUMENT_ALL_ATTR_DEFINITION =	"class X\n" +
																	"  attr :a\n" +
																	"  def initialize\n" +
																	"    @c = 1\n" +
																	"    @b\n" +
																	"  end\n" +
																	"end";

	private final static String TEST_DOCUMENT_SCLASS_NODE =			"x = \"a\"\n" +
																	"class <<x\n" +
																	"  attr :a\n" +
																	"  def initialize\n" +
																	"    @c = 1\n" +
																	"    @b\n" +
																	"  end\n" +
																	"end";

	public void testSimpleDocument()
	{
		addContent(new String[]{"X", "a"});
		addContent(new String[]{"X", "b"});
		validate(new ConstructorsGenerator(new StringDocumentProvider("generate_constructor_test_testSimpleDocument.rb", TEST_DOCUMENT_SIMPLE)));
	}
	
	public void testDocumentNoAttrs()
	{
		addContent(new String[]{"X"});
		validate(new ConstructorsGenerator(new StringDocumentProvider("generate_constructor_test_testDocumentNoAttrs.rb", TEST_DOCUMENT_NO_ATTRS)));
	}
	
	public void testAllAttrTypes()
	{
		addContent(new String[]{"X", "a"});
		addContent(new String[]{"X", "b"});
		addContent(new String[]{"X", "c"});
		validate(new ConstructorsGenerator(new StringDocumentProvider("generate_constructor_test_testAllAttrTypes.rb", TEST_DOCUMENT_ALL_ATTR_DEFINITION)));
	}
	
	public void testSClassNode()
	{
		addContent(new String[]{"x", "a"});
		addContent(new String[]{"x", "b"});
		addContent(new String[]{"x", "c"});
		validate(new ConstructorsGenerator(new StringDocumentProvider("generate_constructor_test_testSClassNode.rb", TEST_DOCUMENT_SCLASS_NODE)));
	}
}
