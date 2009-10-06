package org.rubypeople.rdt.internal.core.parser.warnings;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_InternalCoreParserWarnings
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.internal.core.parser.warnings");
		// $JUnit-BEGIN$
		suite.addTestSuite(Ruby19WhenStatementsTest.class);
		suite.addTestSuite(ConstantReassignmentVisitorTest.class);
		suite.addTestSuite(CoreClassReOpeningTest.class);
		suite.addTestSuite(Ruby19HashCommaSyntaxTest.class);
		suite.addTestSuite(EmptyStatementVisitorTest.class);
		// $JUnit-END$
		return suite;
	}

}
