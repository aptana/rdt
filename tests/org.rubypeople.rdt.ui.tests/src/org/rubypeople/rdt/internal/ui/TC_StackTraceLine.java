/*******************************************************************************
 * Copyright (c) 2005 David Corbin and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * 	   David Corbin: dcorbin@users.sourceforge.net 
 *******************************************************************************/
package org.rubypeople.rdt.internal.ui;

import org.rubypeople.eclipse.shams.resources.ShamProject;
import org.rubypeople.rdt.internal.ui.util.StackTraceLine;

import junit.framework.TestCase;

public class TC_StackTraceLine extends TestCase {
	private static final String BACKSLASH_FILE_PATH = "C:\\ruby\\lib\\ruby\\gems\\1.8\\gems\\activesupport-1.4.2\\lib/active_support/dependencies.rb:376:in `new_constants_in': undefined method `empty?' for nil:NilClass (NoMethodError)";
	private static final String RUBY_CONSOLE_TEST_FAILURE 	= "testA(BTest) [/RdtTestLib/anotherFile.rb:12]:"; 
	private static final String TEST_UNIT_VIEW_BACKTRACE  	= "   /RdtTestLib/anotherFile.rb:12"; 
	private static final String BACKTRACE_WITH_IN 			= "   /RdtTestLib/anotherFile.rb:12:in `testB'";
	private static final String WITH_FROM 					= "\tfrom /RdtTestLib/anotherFile.rb:4";
    private static final String ODD_WITH_FROM				= " ^	from /RdtTestLib/anotherFile.rb:4";
	private static final String WITH_OUT_FROM 				= "/RdtTestLib/anotherFile.rb:4";
	private static final String WITH_TRAILING_SPACE 		= "/RdtTestLib/anotherFile.rb:4 ";
	private static final String LOOKS_ABSOLUTE 		= "\t/app/controllers/tags_controller.rb:5:in `index'";

    public void testWithFrom() {
		assertFalse("has a stack trace", StackTraceLine.isTraceLine(WITH_TRAILING_SPACE));
	}
    
    public void testRelativePathWithPeriod() {
		assertTrue("has a stack trace", StackTraceLine.isTraceLine(" ./content/scripts/WatirScripts/byFeature/../lib/lib_atf_base.rb:240:in `treeNavigation'"));
	}
    
    public void testRelativePathWithoutPeriod() {
		assertTrue("has a stack trace", StackTraceLine.isTraceLine("content/scripts/WatirScripts/byFeature/serialized_framework_validation_IT13_script.rb:43:in `test_tc85'"));
	}

    public void testWithTrailingSpace() {
		assertTrue("has a stack trace", StackTraceLine.isTraceLine(WITH_FROM));
		StackTraceLine traceLine = new StackTraceLine(WITH_FROM);
		
		assertEquals("Filename", "/RdtTestLib/anotherFile.rb", traceLine.getFilename());
		assertEquals("Line Number", 4, traceLine.getLineNumber());
		assertEquals("Offset", 6, traceLine.offset());
		assertEquals("Length", 28, traceLine.length());
	}

    public void testWithOutFrom() {
		assertTrue("has a stack trace", StackTraceLine.isTraceLine(WITH_OUT_FROM));
		StackTraceLine traceLine = new StackTraceLine(WITH_OUT_FROM);
		
		assertEquals("Filename", "/RdtTestLib/anotherFile.rb", traceLine.getFilename());
		assertEquals("Line Number", 4, traceLine.getLineNumber());
		assertEquals("Offset", 0, traceLine.offset());
		assertEquals("Length", 28, traceLine.length());
	}

    public void testOddWithFrom() {
		assertTrue("has a stack trace", StackTraceLine.isTraceLine(ODD_WITH_FROM));
		StackTraceLine traceLine = new StackTraceLine(ODD_WITH_FROM);
		
		assertEquals("Filename", "/RdtTestLib/anotherFile.rb", traceLine.getFilename());
		assertEquals("Line Number", 4, traceLine.getLineNumber());
		assertEquals("Offset",  8, traceLine.offset());
		assertEquals("Length", 28, traceLine.length());
	}

    public void testBacktraceWithInTestFailure() {
		assertTrue("has a stack trace", StackTraceLine.isTraceLine(BACKTRACE_WITH_IN));
		StackTraceLine traceLine = new StackTraceLine(BACKTRACE_WITH_IN);
		
		assertEquals("Filename", "/RdtTestLib/anotherFile.rb", traceLine.getFilename());
		assertEquals("Line Number", 12, traceLine.getLineNumber());
		assertEquals("Offset", 3, traceLine.offset());
		assertEquals("Length", 29, traceLine.length());

	}
	
    public void testConsoleTestFailure() {
		assertTrue(StackTraceLine.isTraceLine(RUBY_CONSOLE_TEST_FAILURE));
		StackTraceLine traceLine = new StackTraceLine(RUBY_CONSOLE_TEST_FAILURE);
		
		assertEquals("Filename", "/RdtTestLib/anotherFile.rb", traceLine.getFilename());
		assertEquals("Line Number", 12, traceLine.getLineNumber());
		assertEquals("Offset", 14, traceLine.offset());
		assertEquals("Length", 29, traceLine.length());

	}

	public void testTestUnitViewBackTrace() {
		StackTraceLine traceLine = new StackTraceLine(TEST_UNIT_VIEW_BACKTRACE);
		
		assertEquals("Filename", "/RdtTestLib/anotherFile.rb", traceLine.getFilename());
		assertEquals("Line Number", 12, traceLine.getLineNumber());
		assertEquals("Offset", 3, traceLine.offset());
		assertEquals("Length", 29, traceLine.length());
	}
	
	public void testLooksAbsoluteButIsRelativeToProject() {
		StackTraceLine traceLine = new StackTraceLine(LOOKS_ABSOLUTE, new ShamProject("testing"));
		
		assertEquals("Filename", "/testing/app/controllers/tags_controller.rb", traceLine.getFilename());
		assertEquals("Line Number", 5, traceLine.getLineNumber());
		assertEquals("Offset", 1, traceLine.offset());
		assertEquals("Length", 37, traceLine.length());
	}
	
	public void testBackslashInFilePath() {
		StackTraceLine traceLine = new StackTraceLine(BACKSLASH_FILE_PATH, new ShamProject("testing"));
		
		assertEquals("Filename", "C:\\ruby\\lib\\ruby\\gems\\1.8\\gems\\activesupport-1.4.2\\lib/active_support/dependencies.rb", traceLine.getFilename());
		assertEquals("Line Number", 376, traceLine.getLineNumber());
		assertEquals("Offset", 0, traceLine.offset());
		assertEquals("Length", 89, traceLine.length());
	}

}
