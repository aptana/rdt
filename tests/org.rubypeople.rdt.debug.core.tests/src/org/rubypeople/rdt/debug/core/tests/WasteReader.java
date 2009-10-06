package org.rubypeople.rdt.debug.core.tests;

import junit.framework.Assert;

import org.rubypeople.rdt.internal.debug.core.parsing.AbstractReadStrategy;
import org.rubypeople.rdt.internal.debug.core.parsing.XmlStreamReader;
import org.xmlpull.v1.XmlPullParser;

public class WasteReader extends XmlStreamReader {

	private String name;
	public WasteReader(XmlPullParser xpp) {
		super(xpp);
	}

	public WasteReader(AbstractReadStrategy readStrategy) {
		super(readStrategy);
	}

	@Override
	protected boolean processStartElement(XmlPullParser xpp) {
		name = xpp.getName() ;
		if (name.equals("exception") ) {
			String exceptionType = xpp.getAttributeValue("", "type") ;
			String exceptionMessage = xpp.getAttributeValue("", "message") ;
			// Unfortunately JUnit does not stop the test because this assertion is thrown in another thread
			Assert.fail("Exception " + exceptionType + " occurred: " + exceptionMessage) ;
		}
		return checkNAme();
	}

	private boolean checkNAme() {
		return name.equals("error") || name.equals("message") || name.equals("frame") ;
	}

	@Override
	public void processContent(String text) {
		if (name.equals("error")) {
			throw new RuntimeException("Error in test: " + text) ;
		}
	}
	@Override
	protected boolean processEndElement(XmlPullParser xpp) {
		name = xpp.getName() ;
		return checkNAme();
	}
	
	

}
