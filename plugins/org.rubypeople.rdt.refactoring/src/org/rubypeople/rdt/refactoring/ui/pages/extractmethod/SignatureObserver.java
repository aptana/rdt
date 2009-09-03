/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.refactoring.ui.pages.extractmethod;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Label;
import org.rubypeople.rdt.refactoring.core.extractmethod.ExtractedMethodHelper;

public class SignatureObserver implements Observer {

	private final Label signatureLabel;

	private final ExtractedMethodHelper methodHelper;

	public SignatureObserver(Label signatureLabel, ExtractedMethodHelper methodHelper) {
		this.signatureLabel = signatureLabel;
		this.methodHelper = methodHelper;
		setPreviewText();
	}

	public void update(Observable observable, Object object) {
		setPreviewText();
	}

	private void setPreviewText() {
		if ("".equals(methodHelper.getMethodName())) { //$NON-NLS-1$
			return;
		}

		StringBuilder string = new StringBuilder("def " + methodHelper.getMethodName() + ' '); //$NON-NLS-1$

		Iterator<String> it = methodHelper.getArguments().iterator();
		while (it.hasNext()) {
			string.append(it.next());
			if (it.hasNext()) {
				string.append(", "); //$NON-NLS-1$
			}
		}

		signatureLabel.setText(string.toString());
	}
}