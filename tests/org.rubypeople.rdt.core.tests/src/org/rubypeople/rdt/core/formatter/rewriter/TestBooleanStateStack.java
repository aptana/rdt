package org.rubypeople.rdt.core.formatter.rewriter;

import junit.framework.TestCase;

public class TestBooleanStateStack extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testBooleanStateStack() {
		BooleanStateStack s = new BooleanStateStack(true, true);
		assertTrue(s.isTrue());
		s.revert();
		assertTrue(s.isTrue());
		
		s = new BooleanStateStack(false, false);
		assertFalse(s.isTrue());
		s.revert();
		assertFalse(s.isTrue());
	}

	public void testSet() {
		BooleanStateStack s = new BooleanStateStack(true, true);
		assertTrue(s.isTrue());
		s.set(false);
		assertFalse(s.isTrue());
		s.revert();
		assertTrue(s.isTrue());
	}
}
