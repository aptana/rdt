package org.rubypeople.rdt.internal.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.rubypeople.rdt.core.RubyCore;

public class TC_LoadPathEntry extends TestCase {

	public TC_LoadPathEntry(String name) {
		super(name);
	}

	public void testToXml() {
		LoadpathEntry entry = (LoadpathEntry) RubyCore.newProjectEntry(new Path("/myLocation"));
		
		String expected = "<pathentry type=\"project\" path=\"/myLocation\"/>";
		assertEquals(expected, entry.toXML());
	}
}
