package org.rubypeople.rdt;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.rubypeople.rdt.core.formatter.TC_EditableFormatHelper;
import org.rubypeople.rdt.core.formatter.TestReWriteVisitor;
import org.rubypeople.rdt.core.formatter.rewriter.TestBooleanStateStack;
import org.rubypeople.rdt.core.tests.model.BufferTests;
import org.rubypeople.rdt.core.util.TS_CoreUtil;
import org.rubypeople.rdt.internal.TS_Internal;

public class TS_RdtCore
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Tests for org.rubypeople.rdt.core");
		// $JUnit-BEGIN$
		suite.addTestSuite(BufferTests.class);
		suite.addTest(TS_CoreUtil.suite());
		suite.addTest(TS_Internal.suite());
		suite.addTestSuite(TestBooleanStateStack.class);
		suite.addTestSuite(TC_EditableFormatHelper.class);
		suite.addTestSuite(TestReWriteVisitor.class);
		// $JUnit-END$
		return suite;
	}

}
