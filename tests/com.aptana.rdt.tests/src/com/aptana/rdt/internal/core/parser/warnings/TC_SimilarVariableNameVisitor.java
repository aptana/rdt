package com.aptana.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.internal.parser.warnings.SimilarVariableNameVisitor;

public class TC_SimilarVariableNameVisitor extends WarningVisitorTest {

	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new SimilarVariableNameVisitor(code){
		
			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		
		};
	}
	
	public void testEmptyHasNoProblems() throws Exception {
		String code = "";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
	public void testReferToLocalWithSimilarName() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    local = 1\n" +
				"    puts lcal\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(1, numberOfProblems());
	}
	
	public void testTranspositionDoesntPushSmallVariablesAboveThreshold() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    local = 1\n" +
				"    puts lcoal\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(1, numberOfProblems());
	}
	
	public void testLocalDoesntClashWithInstance() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    local = 1\n" +
				"    puts @local\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
	public void testLocalDoesntClashWithClassVar() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    localcalifragillistic = 1\n" +
				"    puts @@localcalifragillistic\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
	public void testInstanceDoesntClashWithClassVar() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    @localcalifragillistic = 1\n" +
				"    puts @@localcalifragillistic\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
	public void testReallySmallVariablesDontTriggerProblem() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    @ca = 1\n" +
				"    puts @cb\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
	public void testReallySmallClassVariablesDontTriggerProblem() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    @@a = 1\n" +
				"    puts @@b\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
	public void testTooDisimilarNameWontTriggerProblem() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    local = 1\n" +
				"    puts llal\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(0, numberOfProblems());
	}
	
//	 TODO Also watch for similarity in constant names?

	public void testReferToInstanceVarWithSimilarName() throws Exception {
		String code = "class Ralph\n" +
				"  def name\n" +
				"    @local = 1\n" +
				"    puts @lcal\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(1, numberOfProblems());
	}
	
	public void testReferToClassVarWithSimilarName() throws Exception {
		String code = "class Ralph\n" +
				"  def self.name\n" +
				"    @@local = 1\n" +
				"    puts @@lcal\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(1, numberOfProblems());
	}
	
	public void testHandleInstanceVariableToClassScoping() throws Exception {
		String code = "class Ralph\n" +
				"  def initialize(name)\n" +
				"    @name = name\n" +
				"  end\n" +
				"  def name\n" +
				"    @namee\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(1, numberOfProblems());
	}
	
	public void testHandleVarsOutsideMethods() throws Exception {
		String code = "class Ralph\n" +
				"  @@class_var = 1\n" +
				"  def initialize(name)\n" +
				"    @name = name\n" +
				"  end\n" +
				"  def name\n" +
				"    @@class_val\n" +
				"  end\n" +
				"end\n";
		parse(code);
		assertEquals(1, numberOfProblems());
	}

}
