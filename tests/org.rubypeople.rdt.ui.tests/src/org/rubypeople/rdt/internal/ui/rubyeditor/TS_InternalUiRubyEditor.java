package org.rubypeople.rdt.internal.ui.rubyeditor;

import junit.framework.TestSuite;

public class TS_InternalUiRubyEditor {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("org.rubypeople.rdt.internal.ui.rubyeditor");
        suite.addTestSuite(TC_TabConverter.class);
        return suite;
    }
}
