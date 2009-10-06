package org.rubypeople.rdt.internal.debug.ui;
import junit.framework.Test;
import junit.framework.TestSuite;


public class TS_InternalDebugUi {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.rubypeople.rdt.internal.debug.ui");
        suite.addTestSuite(TC_RubyConsoleTracker.class);
        suite.addTestSuite(TC_RubySourceLocator.class);
        return suite;
    }
}
