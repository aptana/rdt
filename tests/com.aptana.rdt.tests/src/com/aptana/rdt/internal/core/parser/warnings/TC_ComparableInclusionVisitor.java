package com.aptana.rdt.internal.core.parser.warnings;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.internal.parser.warnings.ComparableInclusionVisitor;

public class TC_ComparableInclusionVisitor extends WarningVisitorTest {

	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new ComparableInclusionVisitor(code) {
			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		};
	}
	
	public void testBasicCase() {
		parse("class Chris\n" +
				"  include Comparable\n" +
				"end\n");
		assertEquals(1, numberOfProblems());
	}
	
	public void testNoFalsePositive() {
		parse("class Chris\n" +
				"  include Comparable\n" +
				"  def <=>(other)\n" +
				"    @var <=> other.var\n" +
				"  end\n" +
				"end\n");
		assertEquals(0, numberOfProblems());
	}

}
