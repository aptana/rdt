package org.rubypeople.rdt.debug.core.tests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class FTS_Debug {

	public static Test suite() {
		TestSuite suite = new TestSuite();
	
		suite.addTestSuite(FTC_ClassicDebuggerCommunicationTest.class);
		suite.addTestSuite(FTC_DebuggerProxyTest.class) ;
		suite.addTestSuite(FTC_ReadStrategyTest.class) ;
		suite.addTestSuite(FTC_DebuggerLaunch.class) ;
		return suite;
	}
}