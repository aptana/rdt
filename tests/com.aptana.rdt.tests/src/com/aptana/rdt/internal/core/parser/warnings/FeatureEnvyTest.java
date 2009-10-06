package com.aptana.rdt.internal.core.parser.warnings;

import java.util.List;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.AbstractRubyLintVisitorTestCase;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.internal.parser.warnings.FeatureEnvy;

public class FeatureEnvyTest extends AbstractRubyLintVisitorTestCase
{

	@Override
	protected RubyLintVisitor createVisitor(String src)
	{
		return new FeatureEnvy(src)
		{
			@Override
			protected String getSeverity()
			{
				return RubyCore.WARNING;
			}
		};
	}

	public void testEnvyOfAnotherReceiver()
	{
		String src = "class Cart\n" +
				    "  def price\n" +
				    "    @item.price + @item.tax\n" +
				    "  end\n" +
				    "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(1, problems.size());
		assertEquals(IProblem.FeatureEnvy, problems.get(0).getID());
	}
	
	public void testNoEnvyIfJustOneReferenceByDefault()
	{
		String src = "class Cart\n" +
				    "  def price\n" +
				    "    File.exist?('something.rb')\n" +
				    "  end\n" +
				    "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}

	public void testNoEnvyofImplicitSelf()
	{
		String src = "class Cart\n" +
				    "  def boot!\n" +
				    "    unless booted?\n" +
				    "      preinitialize\n" +
				    "      pick_boot.run\n" +
				    "    end\n" +
				    "  end\n" +
				    "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
		
		src = "class Cart\n" +
	    "  def preinitialize\n" +
	    "    load(preinitializer_path) if File.exist?(preinitializer_path)\n" +
	    "  end\n" +
	    "end";
		problems = getProblems(src);
		assertEquals(0, problems.size());
	}
	
	public void testNoEnvyofDynamicVars()
	{
		String src = "class Cart\n" +
				    "  def price\n" +
				    "    3.times {|i| i.something; i.something_else }\n" +
				    "  end\n" +
				    "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}
	
	public void testNoEnvyofExplicitSelf()
	{
		String src = "class Cart\n" +
				    "  def load_initializer\n" +
				    "    self.class.load_rubygems\n" +
				    "    load_rails_gem\n" +
				    "    require 'initializer'\n" +
				    "  end\n" +
				    "end";
		List<CategorizedProblem> problems = getProblems(src);
		assertEquals(0, problems.size());
	}
}
