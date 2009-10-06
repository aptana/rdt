package org.rubypeople.rdt.debug.core.tests;

import org.rubypeople.rdt.internal.debug.core.parsing.AbstractReadStrategy;
import org.rubypeople.rdt.internal.debug.core.parsing.XmlStreamReader;
import org.xmlpull.v1.XmlPullParser;

public class TestXmlStreamReader extends XmlStreamReader {
	private int tagReadCount = 0 ;
	private String tag ;
	
	public TestXmlStreamReader(XmlPullParser xpp) {
		super(xpp);
	}

	public TestXmlStreamReader(AbstractReadStrategy readStrategy) {
		super(readStrategy);
	}

	protected boolean processStartElement(XmlPullParser xpp) {
		System.out.println("Examining " + xpp.getName()) ;
		if (xpp.getName().equals(tag)) {
			tagReadCount += 1 ;
			return true ;	
		}
		return false ;
	}

	public boolean isTagRead() {
		return tagReadCount >= 1 ;
	}

	public void resetTagReadCount() {
		this.tagReadCount = 0;
	}

	public int getTagReadCount() {
		return this.tagReadCount ;
	}


	public String getTag() {
		return tag;
	}

	public void acceptTag(String tag) {
		this.tag = tag;
	}

}
