package com.aptana.rdt.internal.core.parser.warnings;

import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.AccidentalBooleanAssignmentVisitor;

public class AccidentalBooleanAssignmentVisitorTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new AccidentalBooleanAssignmentVisitor(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testSimpleLocalVarAsgnInIfCondition()
	{
		String src = "a = 2\nif a = 1\n  puts 'hi'\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(9, problems.get(0).getSourceStart());
		assertEquals(13, problems.get(0).getSourceEnd());
	}
	
	public void testSimpleLocalVarAsgnInIfConditionDoesntGenerateWarningIfLocalVarNotAssignedToBefore()
	{
		String src = "if a = 1\n  puts 'hi'\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}
	
	public void testSimpleLocalVarAsgnWithIfModifier()
	{
		String src = "a = 2\nputs 'hi' if a = 1";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(19, problems.get(0).getSourceStart());
		assertEquals(23, problems.get(0).getSourceEnd());
	}
	
	public void testSimpleLocalVarAsgnInUnlessCondition()
	{
		String src = "a = 2\nunless a = 1\n  puts 'hi'\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(13, problems.get(0).getSourceStart());
		assertEquals(17, problems.get(0).getSourceEnd());
	}
	
	public void testSimpleLocalVarAsgnWithUnlessModifier()
	{
		String src = "a = 2\nputs 'hi' unless a = 1";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(23, problems.get(0).getSourceStart());
		assertEquals(27, problems.get(0).getSourceEnd());
	}
	
	public void testConstantAssignmentInIfCondition()
	{
		String src = "if CONSTANT = 1\n  puts 'hi'\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(3, problems.get(0).getSourceStart());
		assertEquals(14, problems.get(0).getSourceEnd());
	}
	
	public void testInstanceVariableAssignmentInIfCondition()
	{
		String src = "class Chris\n  def method\n    if @var = 1\n      puts 'hi'\n    end\n  end\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(32, problems.get(0).getSourceStart());
		assertEquals(39, problems.get(0).getSourceEnd());
	}
	
	public void testClassVariableAssignmentInIfCondition()
	{
		String src = "class Chris\n  def method\n    if @@var = 1\n      puts 'hi'\n    end\n  end\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(32, problems.get(0).getSourceStart());
		assertEquals(40, problems.get(0).getSourceEnd());
	}

	public void testSimpleLocalVarAsgnInWhenExpression()
	{
		String src = "case var\nwhen @a = 2\nputs 'hi'\nelse\n  puts 'yeah'\nend";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.PossibleAccidentalBooleanAssignment, problems.get(0).getID());
		assertEquals(14, problems.get(0).getSourceStart());
		assertEquals(19, problems.get(0).getSourceEnd());
	}
	
	public void testNoFalsePositive()
	{
		String src = "a = 1";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}

}
