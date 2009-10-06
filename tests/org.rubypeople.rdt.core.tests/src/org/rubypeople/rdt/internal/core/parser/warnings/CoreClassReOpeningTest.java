package org.rubypeople.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class CoreClassReOpeningTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new CoreClassReOpening(null, src)
		{
			@Override
			protected boolean methodExistsOnType(String typeName, String methodName)
			{
				return typeName.equals("String") && methodName.equals("to_s");
			}

			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testRedefiningCoreClassMethod() throws Exception
	{
		String src = "class String\n" + "  def to_s\n" + "    1\n" + "  end\n" + "end\n";
		assertEquals(1, getProblems(src).size());
	}
	
	public void testAddingNewMethodToCoreClassIsntAProblem() throws Exception
	{
		String src = "class String\n" + "  def new_method\n" + "    1\n" + "  end\n" + "end\n";
		assertEquals(0, getProblems(src).size());
	}

}
