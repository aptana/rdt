/*
 * Author: Chris
 *
 * Copyright (c) 2005 RubyPeople.
 *
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
 * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
 * RDT except in compliance with the License. For further information see 
 * org.rubypeople.rdt/rdt.license.
 */

package org.rubypeople.rdt.internal.core.parser;

import java.io.Reader;
import java.io.StringReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.jruby.CompatVersion;
import org.jruby.ast.Node;
import org.jruby.common.IRubyWarnings;
import org.jruby.common.NullWarnings;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.parser.DefaultRubyParser;
import org.jruby.parser.ParserConfiguration;
import org.jruby.parser.ParserSupport;
import org.jruby.parser.RubyParserPool;
import org.jruby.parser.RubyParserResult;
import org.jruby.util.KCode;
import org.rubypeople.rdt.internal.core.builder.IoUtils;
import org.rubypeople.rdt.internal.core.util.Util;

/**
 * @author cawilliams
 */
public class RubyParser
{

	private final RubyParserPool pool;
	private IRubyWarnings warnings;

	private static boolean isDebug;
	private static int count = 0;

	public RubyParser()
	{
		this(new NullWarnings());
	}

	public RubyParser(IRubyWarnings warnings)
	{
		this.warnings = warnings;
		this.pool = RubyParserPool.getInstance();
	}

	private RubyParserResult parse(String fileName, Reader content)
	{
		if (fileName == null)
		{
			fileName = "";
		}
		DefaultRubyParser parser = null;
		try
		{
			ParserConfiguration config = getParserConfig();
			parser = getDefaultRubyParser(config);
			parser.setWarnings(warnings);
			if (isDebug)
				System.out.println("Parse count: " + count++ + " on file: " + fileName);
			LexerSource lexerSource = LexerSource.getSource(fileName, content, null, config);
			RubyParserResult result = parser.parse(config, lexerSource);
			// XXX Do a pass through the comments and associate the comment nodes to the normal AST nodes?!
			postProcessResult(result);
			return result;
		}
		catch (SyntaxException e)
		{
			throw e;
		}
		finally
		{
			IoUtils.closeQuietly(content);
			returnBorrowedParser(parser);
		}
	}

	/**
	 * Hook for subclasses to do extra work on the resulting AST, comments, etc. Meant as a way for the refactoring code
	 * to associate comments to the AST node, but to only do so in those cases where we actually care about the
	 * comments.
	 * 
	 * @param result
	 */
	protected void postProcessResult(RubyParserResult result)
	{
		// do nothing
	}

	protected void returnBorrowedParser(DefaultRubyParser parser)
	{
		pool.returnParser(parser);
	}

	protected DefaultRubyParser getDefaultRubyParser(ParserConfiguration config)
	{
		ParserSupport support = new ParserSupport();
		support.setConfiguration(config);
		return new DefaultRubyParser(support);
	}

	protected ParserConfiguration getParserConfig()
	{
		return new ParserConfiguration(KCode.NIL, 0, true, false, CompatVersion.RUBY1_8);
	}

	public static void setDebugging(boolean b)
	{
		isDebug = b;
	}

	public static boolean isDebugging()
	{
		return isDebug;
	}

	public Node parse(IFile file) throws CoreException
	{
		return parse(file.getName(), new String(Util.getResourceContentsAsCharArray(file))).getAST();
	}

	/**
	 * If possible, please use {@link #parse(String, String)} to provide the filename as well (useful for debugging).
	 * 
	 * @param source
	 * @return
	 */
	public RubyParserResult parse(String source)
	{
		return parse((String) null, source);
	}

	public RubyParserResult parse(IFile file, String source)
	{
		String name = "";
		if (file != null)
			name = file.getName();
		return parse(name, source);
	}

	public RubyParserResult parse(String fileName, String source)
	{
		return parse(fileName, source, false);
	}

	/**
	 * @param fileName
	 * @param source
	 * @param bypassCache
	 *            boolean indicating if we want to force a parse and bypass any cached results.
	 * @return
	 */
	public RubyParserResult parse(String fileName, String source, boolean bypassCache)
	{
		if (source == null)
			return new NullParserResult();
		RubyParserResult ast = parse(fileName, new StringReader(source));
		if (ast == null)
			ast = new NullParserResult();
		return ast;
	}
}