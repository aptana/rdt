package org.rubypeople.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class Ruby19WhenStatementsTest extends AbstractRubyLintVisitorTestCase
{
	public void testCreatesProblemForColonInsteadOfThen() throws Exception
	{
		String src = "case $age\n" + "when 0..2: \"baby\";\n" + "else\n" + "  \"adult\"\n" + "end\n";
		assertEquals(1, getProblems(src).size());
	}

	public void testCreatesNoProblemForThen() throws Exception
	{
		String src = "case $age\n" + "when 0..2 then \"baby\";\n" + "else\n" + "  \"adult\"\n" + "end\n";
		assertEquals(0, getProblems(src).size());
	}

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new Ruby19WhenStatements(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

}
