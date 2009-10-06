package org.rubypeople.rdt.internal.core.parser.warnings;

import java.util.List;

import junit.framework.TestCase;

import org.jruby.ast.Node;
import org.jruby.parser.RubyParserResult;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.RubyParser;

public abstract class AbstractRubyLintVisitorTestCase extends TestCase
{

	public AbstractRubyLintVisitorTestCase()
	{
		super();
	}

	public AbstractRubyLintVisitorTestCase(String name)
	{
		super(name);
	}

	protected List<CategorizedProblem> getProblems(String src)
	{
		RubyParser parser = new RubyParser();
		RubyParserResult result = parser.parse(src);
		RubyLintVisitor visitor = createVisitor(src);
		Node ast = result.getAST();
		ast.accept(visitor);
		return visitor.getProblems();
	}

	protected abstract RubyLintVisitor createVisitor(String src);

}