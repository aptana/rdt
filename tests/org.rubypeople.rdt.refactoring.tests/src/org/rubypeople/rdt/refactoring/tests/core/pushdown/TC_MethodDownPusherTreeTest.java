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

package org.rubypeople.rdt.refactoring.tests.core.pushdown;

import org.rubypeople.rdt.refactoring.core.pushdown.MethodDownPusher;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.tests.TreeProviderTester;

public class TC_MethodDownPusherTreeTest  extends TreeProviderTester
{
	private final static String ONE_DOCUMENT_PUSHDOWN =	"class A < X\n" +
																	"end\n" +
																	"class X\n" +
																	"  def method a\n" +
																	"    a\n" +
																	"  end\n" +
																	"end";


	private final static String ONE_DOCUMENT_PUSHDOWN_MULTIPLE =	"class A < X\n" +
																	"end\n" +
																	"class B < X\n" +
																	"end\n" +
																	"class C < Y\n" +
																	"end\n" +
																	"class X\n" +
																	"  def method a\n" +
																	"    a\n" +
																	"  end\n" +
																	"end\n" +
																	"class Y\n" +
																	"  def method a, b\n" +
																	"    a\n" +
																	"  end\n" +
																	"end";

	private final static String DOCUMENT_A_EXTENDS_X =	"require \"DOCUMENT_X.rb\"\n" +
														"class A < X\n" +
														"end";
	
	private final static String DOCUMENT_B_EXTENDS_X =	"require \"DOCUMENT_X.rb\"\n" +
														"class B < X\n" +
														"end";

	private final static String DOCUMENT_X =	"class X\n" +
												"  def method a\n" +
												"    a\n" +
												"  end\n" +
												"end";

	public void testSimpleOneDocumentPushDown()
	{
		addContent(new String[]{"X", "method"});
		DocumentProvider docProvider = new StringDocumentProvider("push_down_test_testSimpleOneDocumentPushDown.rb", ONE_DOCUMENT_PUSHDOWN);
		validate(new MethodDownPusher(docProvider));
	}
	
	public void testOneDocumentPushDown()
	{
		addContent(new String[]{"X", "method"});
		addContent(new String[]{"Y", "method"});
		DocumentProvider docProvider = new StringDocumentProvider("push_down_test_testOneDocumentPushDown.rb", ONE_DOCUMENT_PUSHDOWN_MULTIPLE);
		validate(new MethodDownPusher(docProvider));
	}
	
	public void testTwoDocumentPushDown()
	{
		addContent(new String[]{"X", "method"});
		StringDocumentProvider docProvider = new StringDocumentProvider("DOCUMENT_X.rb", DOCUMENT_X);
		docProvider.addFile("DOCUMENT_A_EXTENDS_X.rb", DOCUMENT_A_EXTENDS_X);
		validate(new MethodDownPusher(docProvider));
	}
	
	public void testTreeDocumentPushDown()
	{
		addContent(new String[]{"X", "method"});
		StringDocumentProvider docProvider = new StringDocumentProvider("DOCUMENT_X.rb", DOCUMENT_X);
		docProvider.addFile("DOCUMENT_A_EXTENDS_X.rb", DOCUMENT_A_EXTENDS_X);
		docProvider.addFile("DOCUMENT_B_EXTENDS_X.rb", DOCUMENT_B_EXTENDS_X);
		validate(new MethodDownPusher(docProvider));
	}
}
