package com.aptana.rdt.internal.core.parser.warnings;

import java.util.Map;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.parser.warnings.TooManyArgumentsVisitor;


public class TC_CodeComplexityTooManyArguments extends WarningVisitorTest {

	private static final int MAX_ARGS;
	static {
		Map<String,String> options = AptanaRDTPlugin.getDefault().getOptions();
		int max;
		try {
			max = Integer.parseInt((String) options.get(AptanaRDTPlugin.COMPILER_PB_MAX_ARGUMENTS));
		}catch (NumberFormatException e) {
			max = TooManyArgumentsVisitor.DEFAULT_MAX_ARGS;
		}
		MAX_ARGS = max;
	}
	
	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new TooManyArgumentsVisitor(code){

			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		};
	}
	
	// TODO Add tests for max branches
	// TODO Add tests for max method params
	// TODO Add tests for max locals
	// TODO Add tests for max returns
	
	public void testTooManyArgs() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method(");
		for (int i = 1; i <= MAX_ARGS + 1; i++) {
			buffer.append(" arg_");
			buffer.append(i);
			if (i < MAX_ARGS + 1) {buffer.append(',');}
		}
		buffer.append(")\n end\n");
		parse(buffer.toString());
		assertEquals(1, numberOfProblems());
	}
	
	public void testEqualToMaxArgs() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method(");
		for (int i = 1; i <= MAX_ARGS; i++) {
			buffer.append(" arg_");
			buffer.append(i);
			if (i < MAX_ARGS) {buffer.append(", ");}
		}
		buffer.append(")\n end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}
	
	public void testLessThanMaxArgs() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method(");
		for (int i = 1; i <= MAX_ARGS - 1; i++) {
			buffer.append(" arg_");
			buffer.append(i);
			if (i < MAX_ARGS - 1) {buffer.append(',');}
		}
		buffer.append(")\n end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}

}
