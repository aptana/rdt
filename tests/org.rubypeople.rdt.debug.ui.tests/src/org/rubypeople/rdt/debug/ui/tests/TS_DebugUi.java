package org.rubypeople.rdt.debug.ui.tests;
import org.rubypeople.rdt.internal.debug.ui.TS_InternalDebugUi;
import org.rubypeople.rdt.internal.debug.ui.launcher.TS_InternalDebugUiLauncher;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TS_DebugUi {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.debug.ui");
//        suite.addTest(TS_InternalDebugUiLauncher.suite());
        suite.addTest(TS_InternalDebugUi.suite());
        return suite;
    }
}
