package org.rubypeople.rdt.internal.core.parser;

import org.rubypeople.rdt.internal.core.parser.warnings.TS_InternalCoreParserWarnings;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TS_InternalCoreParser {

    public static Test suite() {
        TestSuite suite = new TestSuite("Parser");    
        suite.addTestSuite(TC_TaskParser.class);
        suite.addTestSuite(TC_RubyParser.class);
        suite.addTest(TS_InternalCoreParserWarnings.suite());
        return suite;
    }
}
