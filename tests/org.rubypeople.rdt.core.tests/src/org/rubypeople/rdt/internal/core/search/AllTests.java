package org.rubypeople.rdt.internal.core.search;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.rubypeople.rdt.internal.core.search");
		//$JUnit-BEGIN$
		suite.addTestSuite(MethodPatternParserTest.class);
		//$JUnit-END$
		return suite;
	}

}
