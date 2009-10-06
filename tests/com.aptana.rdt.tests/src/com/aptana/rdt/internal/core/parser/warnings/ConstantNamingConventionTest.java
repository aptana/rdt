package com.aptana.rdt.internal.core.parser.warnings;

import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.ConstantNamingConvention;

public class ConstantNamingConventionTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new ConstantNamingConvention(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testCamelCaseConstantNameGeneratesWarning()
	{
		String src = "ConstantWithMixedCase = 1";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.ConstantNamingConvention, problems.get(0).getID());
		assertEquals(0, problems.get(0).getSourceStart());
		assertEquals(21, problems.get(0).getSourceEnd());
	}

	public void testNoFalsePositive()
	{
		String src = "CONSTANT_WITH_UNDERSCORES = 1";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}

}
