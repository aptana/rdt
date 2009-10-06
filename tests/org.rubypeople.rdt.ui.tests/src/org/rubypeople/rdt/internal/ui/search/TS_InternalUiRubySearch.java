/* Copyright (c) 2005 RubyPeople.
* 
* Author: Markus
* 
* This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT
* is subject to the "Common Public License (CPL) v 1.0". You may not use RDT
* except in compliance with the License. For further information see
* org.rubypeople.rdt/rdt.license.
* 
*/

package org.rubypeople.rdt.internal.ui.search;

import junit.framework.TestSuite;

public class TS_InternalUiRubySearch {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("org.rubypeople.rdt.internal.ui.search");
		suite.addTestSuite(MarkOccurrencesTest.class);
        return suite;
    }
}
