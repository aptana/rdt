package com.aptana.rdt.internal.core.parser.warnings;

import java.util.Map;

import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.parser.warnings.TooManyLinesVisitor;
import com.aptana.rdt.internal.parser.warnings.TooManyLocalsVisitor;


public class TC_CodeComplexityMaxLocals extends WarningVisitorTest {

	private static final int MAX_LOCALS;
	static {
		Map<String,String> options = AptanaRDTPlugin.getDefault().getOptions();
		int max;
		try {
			max = Integer.parseInt((String) options.get(AptanaRDTPlugin.COMPILER_PB_MAX_LOCALS));
		}catch (NumberFormatException e) {
			max = TooManyLocalsVisitor.DEFAULT_MAX_LOCALS;
		}
		MAX_LOCALS = max;
	}
	
	@Override
	protected RubyLintVisitor createVisitor(String code) {
		return new TooManyLocalsVisitor(code){

			@Override
			protected String getSeverity() {
				return RubyCore.WARNING;
			}
		};
	}
	
	public void testTooManyLocals() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_LOCALS + 1; i++) {
			buffer.append(" local_");
			buffer.append(i);
			buffer.append(" = 1");
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(1, numberOfProblems());
	}
	
	public void testEqualToMaxLocals() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_LOCALS; i++) {
			buffer.append(" local_");
			buffer.append(i);
			buffer.append(" = 1");
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}
	
	public void testLessThanMaxLocals() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("def method\n");
		for (int i = 1; i <= MAX_LOCALS - 1; i++) {
			buffer.append(" local_");
			buffer.append(i);
			buffer.append(" = 1");
			buffer.append("\n");
		}
		buffer.append("end\n");
		parse(buffer.toString());
		assertEquals(0, numberOfProblems());
	}

}
