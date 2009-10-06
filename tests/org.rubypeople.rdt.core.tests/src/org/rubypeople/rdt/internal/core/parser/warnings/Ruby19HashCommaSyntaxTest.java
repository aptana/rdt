package org.rubypeople.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class Ruby19HashCommaSyntaxTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new Ruby19HashCommaSyntax(src);
	}

	public void testComplainsAboutCommaSeparatingKeyAndValue() throws Exception
	{
		String src = "{:key, 'value'}";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testDoesntComplainAboutArrowSeparatingKeyAndValue() throws Exception
	{
		String src = "{:key => 'value'}";
		assertEquals(0, getProblems(src).size());
	}
}
