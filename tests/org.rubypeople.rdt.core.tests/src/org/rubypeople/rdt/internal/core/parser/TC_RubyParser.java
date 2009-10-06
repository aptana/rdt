/*
 * Author: David Corbin
 *
 * Copyright (c) 2005 RubyPeople.
 *
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
 * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
 * RDT except in compliance with the License. For further information see 
 * org.rubypeople.rdt/rdt.license.
 */
package org.rubypeople.rdt.internal.core.parser;

import junit.framework.TestCase;

import org.jruby.ast.Node;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.parser.DefaultRubyParser;
import org.jruby.parser.ParserConfiguration;
import org.jruby.parser.RubyParserResult;
import org.rubypeople.eclipse.shams.resources.ShamFile;

public class TC_RubyParser extends TestCase
{

	public void testParse() throws Exception
	{
		ShamDefaultRubyParser defaultRubyParser = new ShamDefaultRubyParser();
		RubyParserResult rubyParserResult = new RubyParserResult();
		Node rootNode = new ShamNode();
		rubyParserResult.setAST(rootNode);
		defaultRubyParser.setParserResult(rubyParserResult);
		TestRubyParser parser = new TestRubyParser();
		parser.setDefaultRubyParser(defaultRubyParser);
		ShamFile file = new ShamFile("testFile.rb");
		file.setContents("foobar");

		Node node = parser.parse(file);
		assertEquals(rootNode, node);

		file.assertContentStreamClosed();
	}

	private static class TestRubyParser extends RubyParser
	{

		private DefaultRubyParser defaultRubyParser;

		protected DefaultRubyParser getDefaultRubyParser(ParserConfiguration config)
		{
			return defaultRubyParser;
		}

		public void setDefaultRubyParser(DefaultRubyParser defaultRubyParser)
		{
			this.defaultRubyParser = defaultRubyParser;
		}

		// do NOT return the ShamDefaultRubyParser to the parser pool
		@Override
		protected void returnBorrowedParser(@SuppressWarnings("unused") DefaultRubyParser parser)
		{
		}
	}

	private static class ShamDefaultRubyParser extends DefaultRubyParser
	{
		private RubyParserResult result;

		public RubyParserResult parse(ParserConfiguration config, LexerSource source)
		{
			return result;
		}

		public void setParserResult(RubyParserResult rubyParserResult)
		{
			result = rubyParserResult;
		}
	}

}
