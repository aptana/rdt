package org.rubypeople.rdt.internal.codeassist;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_InternalCodeAssist {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.rubypeople.rdt.internal.codeassist");
		//$JUnit-BEGIN$
		suite.addTestSuite(CompletionEngineTest.class);
		suite.addTestSuite(CompletionContextTest.class);
		//$JUnit-END$
		return suite;
	}

}
