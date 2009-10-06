package org.rubypeople.rdt.internal.corext.util;

import org.rubypeople.rdt.internal.corext.util.RDocUtil;

import junit.framework.TestCase;

public class RDocUtiltest extends TestCase {
	
	public void testHTML() throws Exception {
		String input = " List entries look like:\n" +
    "  *       text\n" +
    "  1.      text\n" +
    "  [label] text\n" +
    "  label:: text\n" +
    "\n" +
    " Flag it as a list entry, and\n" +
    " work out the indent for subsequent lines";
		String html = RDocUtil.getHTMLDocumentation(input);
		String expected = "<p>\n" +
		"List entries look like:\n" +
		"</p>\n" +
			"<pre>\n" +
			" *       text\n" +
			 " 1.      text\n" +
			 " [label] text\n" +
			 " label:: text\n" +
			"</pre>\n" +
			"<p>\n" +
			"Flag it as a list entry, and work out the indent for subsequent lines\n" +
			"</p>\n";
		assertEquals(expected, html);
	}
}
