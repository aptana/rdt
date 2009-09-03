package com.aptana.rdt.internal.ui.infoviews;

import com.aptana.rdt.ui.BrowserView;


public class RubyCoreAPIView extends BrowserView {

	private static final String URL = "http://www.ruby-doc.org/core/";

	@Override
	protected String getURL() {
		return URL;
	}
}
