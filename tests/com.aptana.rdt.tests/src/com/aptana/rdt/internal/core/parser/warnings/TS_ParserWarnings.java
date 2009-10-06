package com.aptana.rdt.internal.core.parser.warnings;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_ParserWarnings
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Parser warnings");
		// $JUnit-BEGIN$
		suite.addTestSuite(TC_SimilarVariableNameVisitor.class);
		suite.addTestSuite(TC_UnecessaryElseVisitor.class);
		suite.addTestSuite(TC_CodeComplexity.class);
		suite.addTestSuite(TC_CodeComplexityTooManyArguments.class);
		suite.addTestSuite(TC_CodeComplexityMaxLocals.class);
		suite.addTestSuite(TC_CodeComplexityTooManyReturns.class);
		suite.addTestSuite(TC_ComparableInclusionVisitor.class);
		suite.addTestSuite(TC_EnumerableInclusionVisitor.class);
		suite.addTestSuite(ControlCoupleTest.class);
		suite.addTestSuite(FeatureEnvyTest.class);
		suite.addTestSuite(UncommunicativeNameTest.class);
		suite.addTestSuite(LocalsMaskingMethodsVisitorTest.class);
		suite.addTestSuite(DynamicVariableAliasesLocalTest.class);
		suite.addTestSuite(ConstantNamingConventionTest.class);
		suite.addTestSuite(AccidentalBooleanAssignmentVisitorTest.class);
		// $JUnit-END$
		return suite;
	}

}
