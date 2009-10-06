package com.aptana.rdt.internal.core.parser.warnings;

import java.util.Map;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.parser.warnings.TooManyLinesVisitor;


public class TC_CodeComplexity extends WarningVisitorTest {

	private static final int MAX_LINES;
	static {
		Map<String,String> options = AptanaRDTPlugin.getDefault().getOptions();
		int max;
		try {
			max = Integer.parseInt((String) options.get(AptanaRDTPlugin.COMPILER_PB_MAX_LINES));
		}catch (NumberFormatException e) {
			max = TooManyLinesVisitor.DEFAULT_MAX_LINES;
		}
		MAX_LINES = max;
	}
	
	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new TooManyLinesVisitor(code){

			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		};
	}
	
	// TODO Add tests for max branches
	
	public void testTooManyLines() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_LINES + 1; i++) {
			buffer.append("  # line ");
			buffer.append(i);
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(1, numberOfProblems());
	}
	
	public void testEqualToMaxLines() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_LINES; i++) {
			buffer.append("  # line ");
			buffer.append(i);
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}
	
	public void testLessThanMaxLines() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_LINES - 1; i++) {
			buffer.append("  # line ");
			buffer.append(i);
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}

}
