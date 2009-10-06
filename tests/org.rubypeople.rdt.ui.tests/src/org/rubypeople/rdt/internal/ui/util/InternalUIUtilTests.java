package org.rubypeople.rdt.internal.ui.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InternalUIUtilTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.internal.ui.util");
		// $JUnit-BEGIN$
		suite.addTestSuite(TwoArrayQuickSorterTest.class);
		suite.addTestSuite(StringMatcherTest.class);
		// $JUnit-END$
		return suite;
	}

}
