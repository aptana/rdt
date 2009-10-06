package org.rubypeople.rdt.debug.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_UnitTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.debug.core.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TC_RubyDebugTarget.class);
		//$JUnit-END$
		return suite;
	}

}
