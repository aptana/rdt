/*
 * Copyright (c) 2005 RubyPeople.
 *
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
 * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
 * RDT except in compliance with the License. For further information see 
 * org.rubypeople.rdt/rdt.license.
 */
package org.rubypeople.rdt.internal.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rubypeople.rdt.internal.core.builder.TS_InternalCoreBuilder;
import org.rubypeople.rdt.internal.core.parser.TS_InternalCoreParser;
import org.rubypeople.rdt.internal.core.search.AllTests;
import org.rubypeople.rdt.internal.core.util.TS_Util;

public class TS_InternalCore {

	public static Test suite() {
		TestSuite suite = new TestSuite("Core");
		suite.addTestSuite(TC_RubyCore.class);
		suite.addTestSuite(TC_RubyProject.class);
		suite.addTestSuite(TC_LoadPathEntry.class);
		suite.addTest(TS_InternalCoreBuilder.suite());
		suite.addTest(TS_InternalCoreParser.suite());
		suite.addTest(AllTests.suite());
		suite.addTest(TS_Util.suite());
		return suite;
	}
}