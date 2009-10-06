package com.aptana.rdt.internal.core.parser.warnings;

import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.ControlCouple;

public class ControlCoupleTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new ControlCouple(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testControlCouple()
	{
		String src = "def write(quoted)\n" +
					"  if quoted\n" +
					"    write_quoted(@value)\n" +
					"  else\n" +
					"    puts @value\n" +
					"  end\n" +
					"end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.ControlCouple, problems.get(0).getID());
	}

}
