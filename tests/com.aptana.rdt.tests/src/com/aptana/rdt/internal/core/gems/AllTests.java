package com.aptana.rdt.internal.core.gems;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for com.aptana.rdt.internal.core.gems");
		//$JUnit-BEGIN$
		suite.addTestSuite(GemParserTest.class);
		suite.addTestSuite(GemOnePointTwoParserTest.class);
		suite.addTestSuite(ShortListingGemParserTest.class);
		suite.addTestSuite(HybridGemParserTest.class);
		suite.addTestSuite(GemManagerTest.class);
		//$JUnit-END$
		return suite;
	}

}
