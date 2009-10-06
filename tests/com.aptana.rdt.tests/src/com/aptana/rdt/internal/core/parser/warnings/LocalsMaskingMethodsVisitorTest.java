package com.aptana.rdt.internal.core.parser.warnings;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.LocalsMaskingMethodsVisitor;

public class LocalsMaskingMethodsVisitorTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new LocalsMaskingMethodsVisitor(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testLocalVariableMatchesMethodName()
	{
		String src = "class Cart\n" + "  def price\n" + "    price = 1" + "  end\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.LocalMaskingMethod, problems.get(0).getID());
		assertEquals(27, problems.get(0).getSourceStart());
		assertEquals(36, problems.get(0).getSourceEnd());
	}

	public void testLocalVariableMatchesAttrAccessor()
	{
		String src = "class Cart\n" + "  attr_accessor :price\n  def method\n" + "    price = 1" + "  end\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.LocalMaskingMethod, problems.get(0).getID());
		assertEquals(51, problems.get(0).getSourceStart());
		assertEquals(60, problems.get(0).getSourceEnd());
	}

	public void testAssigningToAttrWithWriterProducesNoWarning()
	{
		String src = "class Cart\n" + "  attr :price\n  def method\n" + "    price = 1" + "  end\n" + "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}
	
	public void testOnlyWarnsOnFirstAssignmentInScope()
	{
		String src = "class Struct\n" +
		"  def size\n" +
		"    @size\n" + 
		"  end\n" +
		"  def malloc(size = nil)\n" + 
		"    if( !size )\n" + 
		"      size = @size\n" +
		"    end\n" +
		"  end\n" + 
		"end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.LocalMaskingMethod, problems.get(0).getID());
		assertEquals(53, problems.get(0).getSourceStart());
		assertEquals(63, problems.get(0).getSourceEnd());
	}
	
	public void testOnlyWarnsOnFirstAssignmentPerScope()
	{
		String src = "class Struct\n" +
		"  def size\n" +
		"    @size\n" + 
		"  end\n" +
		"  def malloc(size = nil)\n" + 
		"    if( !size )\n" + 
		"      size = @size\n" +
		"    end\n" +
		"  end\n" + 
		"  def other(size = nil)\n" + 
		"    if( !size )\n" + 
		"      size = @size\n" +
		"    end\n" +
		"  end\n" + 
		"end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(2, problems.size());
		// force order by offsets
		Collections.sort(problems, new Comparator<CategorizedProblem>()
		{

			public int compare(CategorizedProblem o1, CategorizedProblem o2)
			{
				return o1.getSourceStart() - o2.getSourceStart();
			}

		});
		assertEquals(IProblem.LocalMaskingMethod, problems.get(0).getID());
		assertEquals(53, problems.get(0).getSourceStart());
		assertEquals(63, problems.get(0).getSourceEnd());
		
		assertEquals(IProblem.LocalMaskingMethod, problems.get(1).getID());
		assertEquals(126, problems.get(1).getSourceStart());
		assertEquals(136, problems.get(1).getSourceEnd());
	}

}
