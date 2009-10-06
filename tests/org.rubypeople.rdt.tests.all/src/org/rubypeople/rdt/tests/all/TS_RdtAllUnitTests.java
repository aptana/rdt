package org.rubypeople.rdt.tests.all;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rubypeople.rdt.TS_RdtCore;
import org.rubypeople.rdt.debug.ui.tests.TS_DebugUi;
import org.rubypeople.rdt.internal.launching.TS_InternalLaunching;
import org.rubypeople.rdt.internal.ui.TS_InternalUi;
import org.rubypeople.rdt.refactoring.tests.TS_All;

import com.aptana.rdt.TS_Aptana;

public class TS_RdtAllUnitTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("RDT all unit tests");
		//$JUnit-BEGIN$

		// org.rubypeople.rdt.core.tests
		suite.addTest(TS_RdtCore.suite());
		
		// org.rubypeople.rdt.launching.tests
		suite.addTest(TS_InternalLaunching.suite());
		
		// org.rubypeople.rdt.ui.tests
		suite.addTest(TS_InternalUi.suite());
		
		// org.rubypeople.rdt.debug.ui.tests
		suite.addTest(TS_DebugUi.suite());
		
		// com.aptana.rdt.tests
		suite.addTest(TS_Aptana.suite());

		// org.rubypeople.rdt.refactoring.tests
		suite.addTest(TS_All.suite());
		
		//$JUnit-END$
		return suite;
	}
}
