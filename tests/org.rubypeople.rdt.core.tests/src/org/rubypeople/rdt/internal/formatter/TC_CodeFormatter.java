package org.rubypeople.rdt.internal.formatter;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.rubypeople.rdt.core.formatter.CodeFormatter;
import org.xml.sax.SAXException;

public class TC_CodeFormatter extends AbstractCodeFormatterTestCase
{

	public TC_CodeFormatter(String name) throws SAXException, IOException, ParserConfigurationException,
			FactoryConfigurationError
	{
		super(name);
	}

	protected CodeFormatter getCodeFormatter()
	{
		return new OldCodeFormatter();
	}

	protected InputStream getInputDataStream()
	{
		return this.getClass().getResourceAsStream("FormatTestData.xml");
	}

	public void testBeginRescueWithParenthesesInsideBeginBlock()
	{
		doTest("BeginRescueWithParenthesesInsideBeginBlock");
	}

	public void testHashKeysLineUpAcrossMultipleLines()
	{
		doTest("HashKeysLineUpAcrossMultipleLines");
	}

	public void testMethodDefBadIndent()
	{
		doTest("MethodDefinitionStartsTooFarIn");
	}

}
