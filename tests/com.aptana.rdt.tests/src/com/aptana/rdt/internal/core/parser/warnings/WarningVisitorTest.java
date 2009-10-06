package com.aptana.rdt.internal.core.parser.warnings;

import junit.framework.TestCase;

import org.jruby.ast.Node;
import org.rubypeople.rdt.core.compiler.IProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.RubyParser;

public abstract class WarningVisitorTest extends TestCase
{

	private RubyParser parser;
	private RubyLintVisitor visitor;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		parser = new RubyParser();
	}

	protected void parse(String code)
	{
		Node root = parser.parse(code).getAST();
		visitor = createVisitor(code);
		root.accept(visitor);
	}

	public int numberOfProblems()
	{
		return visitor.getProblems().size();
	}

	protected IProblem getProblemAtLine(int i)
	{
		return visitor.getProblems().get(i);
	}

	abstract protected RubyLintVisitor createVisitor(String code);
}
