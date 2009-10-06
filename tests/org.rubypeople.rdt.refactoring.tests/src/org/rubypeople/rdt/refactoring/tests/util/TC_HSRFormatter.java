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

package org.rubypeople.rdt.refactoring.tests.util;

import junit.framework.TestCase;

import org.rubypeople.rdt.refactoring.util.HsrFormatter;

public class TC_HSRFormatter extends TestCase {

	private final static String TEST_DOCUMENT_ONLY_NEEDED_NL = "class X\n  def a(a)\n    @a = a\n    @b = a\n  end\nend";
	private final static String TEST_DOCUMENT_ONLY_NEEDED_NL_AND_SPACES = "class X   \n  def a(a)\n    @a = a\n    @b = a\n  end\nend";
	private final static String TEST_DOCUMENT_ADITIONAL_NL = "class X\n\n  def a(a)\n    \n    @a = a\n    \n    @b = a\n  \n  end\n\nend";
	private final static String TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES = "class X  \n  \n  def a(a)\n    \n    @a = a\n    \n    @b = a\n  \n  end\n\nend";
	
	private final static String IN = "def c\n@c\nend";

	private final static String OUT = "  def c\n    @c\n  end";

	public void testDumbIdeas() {
		validate("", "", 0, "");
		validate("", IN, 0, "def c\n  @c\nend");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "", 0, "");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "", 14, "");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "", TEST_DOCUMENT_ONLY_NEEDED_NL.length(), "");
	}

	public void testDumbNewlineIdeas() {
		validate("", "\n", 0, "\n");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n", 0, "\n");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n", 7, "  \n  ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n", 8, "  \n  ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n", 18, "    \n    ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n", 19, "    \n    ");
	}

	public void testOnlyNeededNL() {
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n" + IN, 7, "  \n" + OUT);
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, IN + "\n", 8, OUT + "\n  ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n" + IN + "\n", 7, "  \n" + OUT + "\n  ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL, "\n" + IN + "\n", 8, "  \n" + OUT + "\n  ");
	}

	public void testOnlyNeededNLWithSpaceds() {
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL_AND_SPACES, "\n" + IN, 7, "  \n" + OUT);
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL_AND_SPACES, IN + "\n", 8, OUT + "\n  ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL_AND_SPACES, "\n" + IN + "\n", 7, "  \n" + OUT + "\n  ");
		validate(TEST_DOCUMENT_ONLY_NEEDED_NL_AND_SPACES, "\n" + IN + "\n", 8, "  \n" + OUT + "\n  ");
	}

	public void testAdditionalNL() {
		validate(TEST_DOCUMENT_ADITIONAL_NL, "\n" + IN, 7, "  \n" + OUT);
		validate(TEST_DOCUMENT_ADITIONAL_NL, IN, 8, OUT);
		validate(TEST_DOCUMENT_ADITIONAL_NL, IN + "\n", 9, OUT + "\n  ");
		validate(TEST_DOCUMENT_ADITIONAL_NL, "\n" + IN + "\n", 7, "  \n" + OUT + "\n  ");
		validate(TEST_DOCUMENT_ADITIONAL_NL, "\n" + IN + "\n", 8, "  \n" + OUT + "\n  ");
		validate(TEST_DOCUMENT_ADITIONAL_NL, "\n" + IN + "\n", 9, "  \n" + OUT + "\n  ");
	}

	public void testAdditionalNLWithSpaceds() {
		validate(TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES, "\n" + IN, 7, "  \n" + OUT);
		validate(TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES, IN, 8, OUT);
		validate(TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES, IN + "\n", 9, OUT + "\n  ");
		validate(TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES, "\n" + IN + "\n", 7, "  \n" + OUT + "\n  ");
		validate(TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES, "\n" + IN + "\n", 8, "  \n" + OUT + "\n  ");
		validate(TEST_DOCUMENT_ADITIONAL_NL_AND_SPACES, "\n" + IN + "\n", 9, "  \n" + OUT + "\n  ");
	}

	public void testInsertIntoEmptyDocument() {
		validate("", "class X\ndef a(a)\n@a = a\n@b = a\nend\nend", 0, TEST_DOCUMENT_ONLY_NEEDED_NL);
	}

	private void validate(String document, String insertText, int offset, String expectedFormatedInsertTextResult) {
		String formattedInsertString = HsrFormatter.format(document, insertText, offset);
		assertEquals(expectedFormatedInsertTextResult, formattedInsertString);
	}
}
