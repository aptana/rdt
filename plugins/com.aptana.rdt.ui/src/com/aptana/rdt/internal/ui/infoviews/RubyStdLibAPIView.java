package com.aptana.rdt.internal.ui.infoviews;

import com.aptana.rdt.ui.BrowserView;


public class RubyStdLibAPIView extends BrowserView {

	static final String URL = "http://www.ruby-doc.org/stdlib/";
	
	@Override
	protected String getURL() {
		return URL;
	}
}
