package org.rubypeople.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class EmptyStatementVisitorTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new EmptyStatementVisitor(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testComplainsAboutEmptyMethod() throws Exception
	{
		String src = "def method\nend";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntComplainAboutMethodWithBody() throws Exception
	{
		String src = "def method\n  puts 'hello'\nend\n";
		assertEquals(0, getProblems(src).size());
	}
	
	public void testComplainsAboutEmptySingletonMethod() throws Exception
	{
		String src = "class Chris\n  def Chris.method\n  end\nend\n";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntComplainAboutSingletonMethodWithBody() throws Exception
	{
		String src = "class Chris\n  def Chris.method\n    puts 'hello'\n  end\nend\n";
		assertEquals(0, getProblems(src).size());
	}

	public void testComplainsAboutEmptyIfBody() throws Exception
	{
		String src = "if true\n" + "end";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntComplainAboutIfWithBody() throws Exception
	{
		String src = "if true\n  puts 'hello'\nend\n";
		assertEquals(0, getProblems(src).size());
	}

	public void testComplainsAboutEmptyUnlessBody() throws Exception
	{
		String src = "unless false\nend";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntComplainAboutUnlessWithBody() throws Exception
	{
		String src = "unless false\n  puts 'hello'\nend\n";
		assertEquals(0, getProblems(src).size());
	}
	
	public void testComplainsAboutEmptyBlock() throws Exception
	{
		String src = "3.times {|i| }\n";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntComplainAboutBlockWithBody() throws Exception
	{
		String src = "3.times {|i| puts i}\n";
		assertEquals(0, getProblems(src).size());
	}
	
	public void testComplainsAboutEmptyWhen() throws Exception
	{
		String src = "case number\nwhen 1\nelse\n  puts number\nend\n";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntComplainAboutWhenWithBody() throws Exception
	{
		String src = "case number\nwhen 1\n  puts 'whooo!'\nelse\n  puts number\nend\n";
		assertEquals(0, getProblems(src).size());
	}
	
	public void testDoesntComplainAboutIfWithBodyContainingUnless() throws Exception
	{
		String src = "if layout\n  puts 'hello' unless false\nend\n";
		assertEquals(0, getProblems(src).size());
	}
	
	public void testDoesComplainAboutIfWithNoBodyElseContainingUnless() throws Exception
	{
		String src = "if layout\nelse\n  puts 'hello' unless false\nend\n";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testDoesComplainAboutUnlessWithNoBodyElseContainingUnless() throws Exception
	{
		String src = "unless layout\nelse\n  puts 'hello' unless false\nend\n";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testDoesntComplainAboutUnlessWithBodyContainingUnless() throws Exception
	{
		String src = "unless layout\n  puts 'hello' unless false\nend\n";
		assertEquals(0, getProblems(src).size());
	}
}
