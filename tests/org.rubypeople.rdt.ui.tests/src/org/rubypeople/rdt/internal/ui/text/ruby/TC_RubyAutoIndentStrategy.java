package org.rubypeople.rdt.internal.ui.text.ruby;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.rubypeople.rdt.internal.ui.text.TestDocumentCommand;

public class TC_RubyAutoIndentStrategy extends TestCase {
	
	private IDocument d;
	
	public void testInsertsIndentAndEndAfterClassDefinitionLine() throws Exception {
		DocumentCommand c = addNewline("class Chris");
		assertEquals(15, c.caretOffset);
		assertEquals(false, c.shiftsCaret);
		assertEquals("\r\n  \r\nend", c.text);
	}
	
	public void testInsertsIndentAndEndAfterMethodDefinitionLine() throws Exception {
		DocumentCommand c = addNewline("def bob");
		assertEquals(11, c.caretOffset);
		assertEquals(false, c.shiftsCaret);
		assertEquals("\r\n  \r\nend", c.text);
	}
	
	public void testHandlesReturnAfterEndOfClosedCaseWithProperIndents() throws Exception {
		DocumentCommand c = addNewline("class Chris\r\n" +
"  case condition\r\n" +
"    when comparison1\r\n" +
"      comparison1_body\r\n" +
"    when comparison2\r\n" +
"      comparison2_body\r\n" +
"  end\r\n" +
"end", 128);
		assertEquals("\r\n  ", c.text);
		assertEquals("class Chris\r\n" +
				"  case condition\r\n" +
				"    when comparison1\r\n" +
				"      comparison1_body\r\n" +
				"    when comparison2\r\n" +
				"      comparison2_body\r\n" +
				"  end\r\n" +
				"  \r\n" +
				"end", d.get());
	}
	
	public void testHandlesReturnAfterEndOfClosedCaseWithBadEndIndent() throws Exception {
		DocumentCommand c = addNewline("class Chris\r\n" +
"  case condition\r\n" +
"    when comparison1\r\n" +
"      comparison1_body\r\n" +
"    when comparison2\r\n" +
"      comparison2_body\r\n" +
"      end\r\n" +
"end", 132);
		assertEquals("\r\n  ", c.text);
		assertEquals("class Chris\r\n" +
				"  case condition\r\n" +
				"    when comparison1\r\n" +
				"      comparison1_body\r\n" +
				"    when comparison2\r\n" +
				"      comparison2_body\r\n" +
				"  end\r\n" +
				"  \r\n" +
				"end", d.get());
	}

	public void testHandlesElsifAfterIfProperly() throws Exception {
		DocumentCommand c = addNewline("def if_else_test\r\n" +
"  if a == true\r\n" +
"    elsif false\r\n" +
"  end\r\n" +
"end", 49);
		assertEquals("\r\n    ", c.text);
		assertEquals("def if_else_test\r\n" +
				"  if a == true\r\n" +
				"  elsif false\r\n" +
				"  end\r\n" +
				"end", d.get());
	}
	
	private DocumentCommand addNewline(String source, int offset) {
		RubyAutoIndentStrategy strategy = new RubyAutoIndentStrategy(null, null);
		DocumentCommand c = createNewLineCommandAt(offset);
		d = new Document(source);
		strategy.customizeDocumentCommand(d, c);
		return c;
	}

	private DocumentCommand addNewline(String source) {
		return addNewline(source, source.length());
	}

	private DocumentCommand createNewLineCommandAt(int offset) {
		DocumentCommand c = new TestDocumentCommand();	
		c.text = "\r\n";
		c.length = 0;
		c.doit = true;
		c.caretOffset = -1;
		c.offset = offset;
		c.shiftsCaret = true;
		return c;
	}	

}
