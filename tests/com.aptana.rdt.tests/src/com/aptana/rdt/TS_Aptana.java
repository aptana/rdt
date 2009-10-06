package com.aptana.rdt;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.aptana.rdt.core.rspec.RSpecStructureCreatorTest;
import com.aptana.rdt.internal.core.gems.AllTests;
import com.aptana.rdt.internal.core.parser.warnings.TS_ParserWarnings;

public class TS_Aptana
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for com.aptana.rdt");
		suite.addTest(TS_ParserWarnings.suite());
		suite.addTestSuite(RSpecStructureCreatorTest.class);
		suite.addTest(AllTests.suite());
		return suite;
	}
}
