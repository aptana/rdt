package org.rubypeople.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class ConstantReassignmentVisitorTest extends AbstractRubyLintVisitorTestCase
{
	public void testCreatesProblemForReassignedConstantInSameNamespace() throws Exception
	{
		String src = "CONSTANT = 1\nCONSTANT = 'hello'\n";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testHandlesNestedNamespaceAndExplicitNamespaceForWrappingClass() throws Exception
	{
		String src = "module A\n  module B\n    class C\n      CONSTANT = 1\n    end\n  end\nend\nclass A::B::C\n  CONSTANT = 3\nend\n";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testHandlesNestedNamespaceAndExplicitNamespace() throws Exception
	{
		String src = "module A\n  module B\n    class C\n      CONSTANT = 1\n    end\n  end\nend\nA::B::C::CONSTANT = 3";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testHandlesExplicitGlobalNamespace() throws Exception
	{
		String src = "module A\n  module B\n    class C\n      ::CONSTANT = 1\n    end\n  end\nend\nCONSTANT = 3";
		assertEquals(1, getProblems(src).size());
	}

	public void testDoesntCreateProblemForReassignedConstantInDifferentNamespace() throws Exception
	{
		String src = "CONSTANT = 1\nmodule Chris\n  CONSTANT = 'hello'\nend\n";
		assertEquals(0, getProblems(src).size());
	}

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new ConstantReassignmentVisitor(src);
	}
}
