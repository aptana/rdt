package com.aptana.rdt.internal.core.parser.warnings;

import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.UncommunicativeName;

public class UncommunicativeNameTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new UncommunicativeName(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testOneCharDefnName()
	{
		String src = "def i(quoted)\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitDefnName()
	{
		String src = "def i9(quoted)\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharDefsName()
	{
		String src = "def self.i(quoted)\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitDefsName()
	{
		String src = "def self.i9(quoted)\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharBlockVarName()
	{
		String src = "3.times {|i| puts i }";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitBlockVarName()
	{
		String src = "3.times {|i0| puts i0 }";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharInstanceVarName()
	{
		String src = "def method\n" + "  @i0 = 0\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitInstanceVarName()
	{
		String src = "def method\n" + "  @i = 0\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOnlyWarnsOnceAboutInstanceVariablesWithShortNames()
	{
		String src = "def method\n" + "  @i = 0\n" + "  puts @i\n" + "  @i = 1\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}
	
	public void testOnlyWarnsOnceAboutClassVariablesWithShortNames()
	{
		String src = "class Chris\n  def method\n    @@i = 0\n    @@i = 1\n  end\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharClassVarName()
	{
		String src = "def method\n" + "  @@i0 = 0\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitClassVarName()
	{
		String src = "def method\n" + "  @@i = 0\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharClassName()
	{
		String src = "def C\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitClassName()
	{
		String src = "class C1\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharModuleName()
	{
		String src = "module C\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

	public void testOneCharWithDigitModuleName()
	{
		String src = "module C1\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.UncommunicativeName, problems.get(0).getID());
	}

}
