package org.rubypeople.rdt.internal;

import org.rubypeople.rdt.internal.codeassist.TS_InternalCodeAssist;
import org.rubypeople.rdt.internal.core.TS_InternalCore;
import org.rubypeople.rdt.internal.formatter.TS_InternalFormatter;
import org.rubypeople.rdt.internal.ti.TS_TypeInference;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_Internal {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.internal");
		//$JUnit-BEGIN$
		suite.addTest(TS_InternalCore.suite());
		suite.addTest(TS_InternalCodeAssist.suite());
		suite.addTest(TS_InternalFormatter.suite());
		suite.addTest(TS_TypeInference.suite());
		//$JUnit-END$
		return suite;
	}

}
