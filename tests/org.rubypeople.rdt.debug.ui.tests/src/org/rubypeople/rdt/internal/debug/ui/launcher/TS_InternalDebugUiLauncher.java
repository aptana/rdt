package org.rubypeople.rdt.internal.debug.ui.launcher;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_InternalDebugUiLauncher {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.internal.debug.ui.launcher");
		suite.addTestSuite(TC_RubyArgumentsTab.class);
		suite.addTestSuite(TC_RubyApplicationShortcut.class);
		suite.addTestSuite(TC_RubyEntryPointTab.class);
		suite.addTestSuite(TC_RubyEnvironmentTab.class);
		return suite;
	}
}
