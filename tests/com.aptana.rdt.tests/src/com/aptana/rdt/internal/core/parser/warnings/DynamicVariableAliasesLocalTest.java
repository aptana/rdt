package com.aptana.rdt.internal.core.parser.warnings;

import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.DynamicVariableAliasesLocal;

public class DynamicVariableAliasesLocalTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new DynamicVariableAliasesLocal(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testDynamicVarMatchesLocalVarNameInScope()
	{
		String src = "def price\n  price = 1\n  3.times {|price| puts price }\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.DynamicVariableAliasesLocal, problems.get(0).getID());
		assertEquals(34, problems.get(0).getSourceStart());
		assertEquals(39, problems.get(0).getSourceEnd());
	}

	public void testNoFalsePositiveForNewDynamicVarName()
	{
		String src = "def price\n  price = 1\n  3.times {|i| puts i }\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}
	
	public void testOneDynamicVarInListOfVarsMatchesLocalVarNameInScope()
	{
		String src = "def price\n  y = 1\n  3.times {|x, y| puts x }\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.DynamicVariableAliasesLocal, problems.get(0).getID());
		assertEquals(33, problems.get(0).getSourceStart());
		assertEquals(34, problems.get(0).getSourceEnd());
	}
	
	public void testMultipleDynamicVarInListOfVarsMatchesLocalVarNameInScope()
	{
		String src = "def price\n  y = 1\n  x = 2\n  3.times {|x, y| puts x }\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(2, problems.size());
		assertEquals(IProblem.DynamicVariableAliasesLocal, problems.get(0).getID());
		assertEquals(38, problems.get(0).getSourceStart());
		assertEquals(39, problems.get(0).getSourceEnd());
		
		assertEquals(IProblem.DynamicVariableAliasesLocal, problems.get(1).getID());
		assertEquals(41, problems.get(1).getSourceStart());
		assertEquals(42, problems.get(1).getSourceEnd());
	}

}
