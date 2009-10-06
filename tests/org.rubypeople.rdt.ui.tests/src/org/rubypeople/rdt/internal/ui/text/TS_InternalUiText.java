package org.rubypeople.rdt.internal.ui.text;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rubypeople.rdt.internal.ui.text.ruby.TC_RubyTokenScanner;

public class TS_InternalUiText {
    public static Test suite() {
        TestSuite suite = new TestSuite("org.rubypeople.rdt.internal.ui.text");      
        suite.addTestSuite(TC_RubyPartitionScanner.class);
        suite.addTestSuite(TC_RubyWordFinder.class);
        suite.addTestSuite(TC_RubyTokenScanner.class);
        return suite;
    }
}
