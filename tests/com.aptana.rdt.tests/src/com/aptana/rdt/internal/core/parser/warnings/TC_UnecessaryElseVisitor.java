package com.aptana.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.internal.parser.warnings.UnecessaryElseVisitor;


public class TC_UnecessaryElseVisitor extends WarningVisitorTest {
	
	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new UnecessaryElseVisitor(code){

			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		};
	}
	
	public void testSimpleIfElse() throws Exception {
		parse("if true\nreturn\nelse\nputs 'Hi'\nend\n");
		assertEquals(1, numberOfProblems());
	}
	
	public void testIfWithNoElseDoesntGetMarked() throws Exception {
		parse("if true\nputs 'Hi'\nend\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testIfElseWithNoExplicitReturnDoesntGetMarked() throws Exception {
		parse("if true\n'Hi'\nelse\n'Hello'\nend\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testNestedIfs() throws Exception {
		parse("if true\n" +
			  "  if false\n" +
			  "    return\n" +
			  "  end\n" +
			  "  'Hi'\n" +
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testNestedIfsB() throws Exception {
		parse("if true\n" +
			  "  if false\n" +
			  "    return\n" +
			  "  end\n" +
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testNestedIfsAllWithExplicitReturns() throws Exception {
		parse("if true\n" +
			  "  if false\n" +
			  "    return\n" +
			  "  else\n" +
			  "    return\n" +
			  "  end\n" +
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(2, numberOfProblems());
	}
	
	public void testIfModifierIsntProblem() throws Exception {
		parse("puts 'hello' if true\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testUnlessModifierIsntProblem() throws Exception {
		parse("puts 'hello' unless true\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testUnlessWithEachCanHaveProblem() throws Exception {
		parse("unless true\n" +
				  "  if false\n" +
				  "    return\n" +
				  "  else\n" +
				  "    return\n" +
				  "  end\n" +
				  "else\n" +
				  "  'Hello'\n" +
				  "end\n");
		assertEquals(2, numberOfProblems());
	}

	public void testSwitchInsideIfWithAllExplicitReturns() throws Exception {
		parse("if true\n" +
			  "  case thing\n" +
		      "    when comparison1\n" +
		      "      return\n" +
		      "    when comparison2\n" +
		      "      return\n" +
		      "  end\n" +				
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(1, numberOfProblems());
	}
	
	public void testSwitchInsideIfWithoutAllHavingExplicitReturns() throws Exception {
		parse("if true\n" +
			  "  case thing\n" +
		      "    when comparison1\n" +
		      "      return\n" +
		      "    when comparison2\n" +
		      "      puts 'Hello world!'\n" +
		      "  end\n" +				
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(0, numberOfProblems());
	}
	
	public void testSwitchInsideUnlessWithAllExplicitReturns() throws Exception {
		parse("unless true\n" +
			  "  case thing\n" +
		      "    when comparison1\n" +
		      "      return\n" +
		      "    when comparison2\n" +
		      "      return\n" +
		      "  end\n" +				
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(1, numberOfProblems());
	}
	
	public void testSwitchInsideUnlessWithoutAllHavingExplicitReturns() throws Exception {
		parse("unless true\n" +
			  "  case thing\n" +
		      "    when comparison1\n" +
		      "      return\n" +
		      "    when comparison2\n" +
		      "      puts 'Hello world!'\n" +
		      "  end\n" +				
			  "else\n" +
			  "  'Hello'\n" +
			  "end\n");
		assertEquals(0, numberOfProblems());
	}
	
}
