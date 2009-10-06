package org.rubypeople.rdt.debug.core.tests;

import junit.framework.TestSuite;
/*
 * purpose of this test suite is to provide small temporary test suites for development
 */
public class FTC_Single extends TestSuite {
	public static junit.framework.TestSuite suite() {

		TestSuite suite = new TestSuite();
		//suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testBreakpointAddAndRemove"));
		//suite.addTest(new FTC_RubyDebugCommunicationTest("testBreakpointAddAndRemove"));
		suite.addTest(new FTC_RubyDebugCommunicationTest("testInspectError"));
		
		//suite.addTest(classicSuite()) ;
		//suite.addTest(rdebugSuite()) ;
		return suite ;
	}
	
	public static TestSuite classicSuite() {
		TestSuite suite = new TestSuite() ;
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testStepOver"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testStepOverFrames"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testStepOverInDifferentFrame"));
		
		
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testBreakpointOnFirstLine"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testBreakpointAddAndRemove"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testSimpleCycleSteppingWorks"));
		
		return suite ;
	}
	
	public static TestSuite rdebugSuite() {
		TestSuite suite = new TestSuite() ;
		suite.addTest(new FTC_RubyDebugCommunicationTest("testStepOver"));
		suite.addTest(new FTC_RubyDebugCommunicationTest("testStepOverFrames"));
		suite.addTest(new FTC_RubyDebugCommunicationTest("testStepOverInDifferentFrame"));
		suite.addTest(new FTC_RubyDebugCommunicationTest("testBreakpointOnFirstLine"));
		suite.addTest(new FTC_RubyDebugCommunicationTest("testBreakpointAddAndRemove"));
		suite.addTest(new FTC_RubyDebugCommunicationTest("testSimpleCycleSteppingWorks"));
		return suite ;
	}
	
}
