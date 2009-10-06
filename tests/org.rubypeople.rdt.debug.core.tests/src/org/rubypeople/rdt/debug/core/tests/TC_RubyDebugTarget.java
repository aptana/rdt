package org.rubypeople.rdt.debug.core.tests;

import org.eclipse.debug.core.DebugEvent;
import org.rubypeople.rdt.internal.debug.core.model.RubyDebugTarget;
import org.rubypeople.rdt.internal.debug.core.model.ThreadInfo;

import junit.framework.TestCase;

public class TC_RubyDebugTarget extends TestCase {

	
	public void testThread() {
		RubyDebugTarget target = new RubyDebugTarget(null, RubyDebugTarget.DEFAULT_PORT);
		ThreadInfo[] initial = new ThreadInfo[] { new ThreadInfo(1, "run")} ;
		DebugEvent[] events = target.updateThreadsInternal(initial) ;
		assertEquals(1, events.length) ;
		assertEquals(DebugEvent.CREATE, events[0].getKind()) ;
		ThreadInfo[] threadAdded = new ThreadInfo[] { new ThreadInfo(1, "run"), new ThreadInfo(2, "sleep")} ;
		events = target.updateThreadsInternal(threadAdded) ;
		assertEquals(1, events.length) ;
		assertEquals(DebugEvent.CREATE, events[0].getKind()) ;
		events = target.updateThreadsInternal(initial) ;
		assertEquals(1, events.length) ;
		assertEquals(DebugEvent.TERMINATE, events[0].getKind()) ;
		ThreadInfo[] changed = new ThreadInfo[] { new ThreadInfo(1, "sleep")} ;
		events = target.updateThreadsInternal(changed) ;
		assertEquals(1, events.length) ;
		assertEquals(DebugEvent.CHANGE, events[0].getKind()) ;
		
		ThreadInfo[] addAndRemove = new ThreadInfo[] { new ThreadInfo(2, "run")} ;
		events = target.updateThreadsInternal(addAndRemove) ;
		assertEquals(2, events.length) ;
		assertEquals(DebugEvent.CREATE, events[0].getKind()) ;
		assertEquals(DebugEvent.TERMINATE, events[1].getKind()) ;
		
	}
}
