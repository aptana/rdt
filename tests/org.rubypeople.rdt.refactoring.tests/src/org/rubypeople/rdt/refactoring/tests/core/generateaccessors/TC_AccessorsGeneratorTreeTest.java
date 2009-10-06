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

package org.rubypeople.rdt.refactoring.tests.core.generateaccessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.core.generateaccessors.AccessorsGenerator;
import org.rubypeople.rdt.refactoring.core.generateaccessors.GeneratedAccessor;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.TreeProviderTester;

public class TC_AccessorsGeneratorTreeTest extends TreeProviderTester {
	
	private final static String TEST_DOCUMENT_SIMPLE =	"#ClassComment\n" +
														"class X\n" +
														"  #methodComment\n" +
														"  def a(a)\n" +
														"    @a = a\n" +
														"    @b = a\n" +
														"  end\n" +
														"end";

	private final static String TEST_DOCUMENT_WITH_SIMPLE_ACCESSOR =	"class X\n" +
																		"  attr_accessor :a\n" +
																		"  def a(a)\n" +
																		"    @a = a\n" +
																		"  end\n" +
																		"end";

	private final static String TEST_DOCUMENT_WITH_METHOD_ACCESSOR =	"class X\n" +
																		"  def a(a)\n" +
																		"    @a = a\n" +
																		"  end\n" +
																		"  def a\n" +
																		"    @a\n" +
																		"  end\n" +
																		"  def a=(a)\n" +
																		"    @a=a\n" +
																		"  end\n" +
																		"end";
	
	private final static String TEST_DOCUMENT_WITH_SIMPLE_WRITER =	"class X\n" +
																	"  attr_writer :a\n" +
																	"  def a(a)\n" +
																	"    @a = a\n" +
																	"  end\n" +
																	"end";
	
	private final static String TEST_DOCUMENT_WITH_METHOD_READER =	"class X\n" +
																		"  def a(a)\n" +
																		"    @a = a\n" +
																		"  end\n" +
																		"  def a\n" +
																		"    @a\n" +
																		"  end\n" +
																		"end";
	
	public void testSimpleDocument()
	{
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testSimpleDocument.rb", TEST_DOCUMENT_SIMPLE);
		AccessorsGenerator provider = new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_SIMPLE_ACCESSOR);
		addContentWithBothAccessors(new String[]{"X", "a"});
		addContentWithBothAccessors(new String[]{"X", "b"});
		validate(provider);
		provider.setType(GeneratedAccessor.TYPE_METHOD_ACCESSOR);
		validate(provider);
	}
	
	public void testDocumentWithSimpleAccessorSelectedTypeSimple()
	{
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testDocumentWithSimpleAccessorSelectedTypeSimple.rb", TEST_DOCUMENT_WITH_SIMPLE_ACCESSOR);
		validate(new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_SIMPLE_ACCESSOR));
	}
	
	public void testDocumentWithSimpleAccessorSelectedTypeMethod()
	{
		addContentWithBothAccessors(new String[]{"X", "a"});
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testDocumentWithSimpleAccessorSelectedTypeMethod.rb", TEST_DOCUMENT_WITH_SIMPLE_ACCESSOR);
		validate(new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_METHOD_ACCESSOR));
	}

	public void testDocumentWithMethodAccessorSelectedTypeSimple()
	{
		addContentWithBothAccessors(new String[]{"X", "a"});
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testDocumentWithMethodAccessorSelectedTypeSimple.rb", TEST_DOCUMENT_WITH_METHOD_ACCESSOR);
		validate(new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_SIMPLE_ACCESSOR));
	}
	

	public void testDocumentWithMethodAccessorSelectedTypeMethod()
	{
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testDocumentWithMethodAccessorSelectedTypeMethod.rb", TEST_DOCUMENT_WITH_METHOD_ACCESSOR);
		validate(new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_METHOD_ACCESSOR));
	}
	
	public void testDocumentWithSimpleWriter()
	{
		addContent(new String[]{"X", "a", AccessorsGenerator.READER});
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testDocumentWithSimpleWriter.rb", TEST_DOCUMENT_WITH_SIMPLE_WRITER);
		validate(new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_SIMPLE_ACCESSOR));
	}
	
	public void testDocumentWithMethodReader()
	{
		addContent(new String[]{"X", "a", AccessorsGenerator.WRITER});
		DocumentProvider docProvider = new StringDocumentProvider("generate_accessor_test_testDocumentWithMethodReader.rb", TEST_DOCUMENT_WITH_METHOD_READER);
		validate(new AccessorsGenerator(docProvider, GeneratedAccessor.TYPE_METHOD_ACCESSOR));
	}
	
	
	
	private void addContentWithBothAccessors(String[] content)
	{
		addContent(addToArray(content, AccessorsGenerator.READER));
		addContent(addToArray(content, AccessorsGenerator.WRITER));
	}
	
	private String[] addToArray(String[] content, String additionalContent)
	{
		Collection<String> contentList = new ArrayList<String>(Arrays.asList(content));
		contentList.add(additionalContent);
		return contentList.toArray(new String[]{});
	}
}
