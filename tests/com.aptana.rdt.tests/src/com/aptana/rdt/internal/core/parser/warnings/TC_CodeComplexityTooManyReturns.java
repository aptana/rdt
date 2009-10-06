package com.aptana.rdt.internal.core.parser.warnings;

import java.util.Map;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.parser.warnings.TooManyArgumentsVisitor;
import com.aptana.rdt.internal.parser.warnings.TooManyReturnsVisitor;


public class TC_CodeComplexityTooManyReturns extends WarningVisitorTest {

	private static final int MAX_RETURNS;
	static {
		Map<String,String> options = AptanaRDTPlugin.getDefault().getOptions();
		int max;
		try {
			max = Integer.parseInt((String) options.get(AptanaRDTPlugin.COMPILER_PB_MAX_RETURNS));
		}catch (NumberFormatException e) {
			max = TooManyReturnsVisitor.DEFAULT_MAX_RETURNS;
		}
		MAX_RETURNS = max;
	}
	
	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new TooManyReturnsVisitor(code){

			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		};
	}
	
	public void testTooManyReturns() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_RETURNS + 1; i++) {
			buffer.append("  return ");
			buffer.append(i);
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(1, numberOfProblems());
	}
	
	public void testEqualToMaxReturns() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_RETURNS; i++) {
			buffer.append("  return ");
			buffer.append(i);
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}
	
	public void testLessThanMaxReturns() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_RETURNS - 1; i++) {
			buffer.append("  return ");
			buffer.append(i);
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}

}
