package org.rubypeople.rdt.internal.ti;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_TypeInference {
	public static Test suite() {
		TestSuite suite = new TestSuite("Type Inference");
		suite.addTestSuite(DataFlowTypeInferrerTest.class);
		suite.addTestSuite(ReferenceMatchTest.class);
		suite.addTestSuite(TypeInferrerTest.class);
		return suite;
	}
}
