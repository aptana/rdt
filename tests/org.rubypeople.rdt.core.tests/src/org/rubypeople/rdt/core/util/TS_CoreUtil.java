package org.rubypeople.rdt.core.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_CoreUtil
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.core.util");
		//$JUnit-BEGIN$
		suite.addTestSuite(UtilTest.class);
		//$JUnit-END$
		return suite;
	}

}
