package org.rubypeople.rdt.internal.formatter;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.rubypeople.rdt.core.formatter.CodeFormatter;
import org.xml.sax.SAXException;

public class TC_ASTBasedCodeFormatter extends AbstractCodeFormatterTestCase
{

	public TC_ASTBasedCodeFormatter(String name) throws SAXException, IOException, ParserConfigurationException,
			FactoryConfigurationError
	{
		super(name);
	}

	@Override
	protected CodeFormatter getCodeFormatter()
	{
		return new ASTBasedCodeFormatter();
	}

	@Override
	protected InputStream getInputDataStream()
	{
		return this.getClass().getResourceAsStream("TC_ASTBasedCodeFormatter_Data.xml");
	}

	public void testMethodDefBadIndent()
	{
		doTest("MethodDefinitionStartsTooFarIn");
	}
}
