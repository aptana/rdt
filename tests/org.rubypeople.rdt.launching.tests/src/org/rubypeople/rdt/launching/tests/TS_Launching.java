package org.rubypeople.rdt.launching.tests;

import org.rubypeople.rdt.internal.launching.TS_InternalLaunching;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_Launching {

	public static Test suite() {
		TestSuite suite = new TestSuite("Launching");
		suite.addTest(TS_InternalLaunching.suite());
		return suite;
	}
}