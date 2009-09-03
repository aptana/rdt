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

package org.rubypeople.rdt.refactoring.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.rubypeople.rdt.core.formatter.DefaultCodeFormatterConstants;
import org.rubypeople.rdt.internal.ui.preferences.formatter.RubyScriptPreview;

public class RdtCodeViewer extends RubyScriptPreview {

	public static RdtCodeViewer create(Composite parent) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, " "); //$NON-NLS-1$
		return new RdtCodeViewer(map, parent);
	}

	protected RdtCodeViewer(Map workingValues, Composite parent) {
		super(workingValues, parent);
		getTextWidget().setEditable(false);
	}

	protected void doFormatPreview() {
		if (fPreviewText == null) {
			fPreviewDocument.set(""); //$NON-NLS-1$
			return;
		}
		fPreviewDocument.set(fPreviewText);
	}

	public void setBackgroundColor(int start, int length, RGB color) {
		setBackgroundColor(start, length, new Color(Display.getCurrent(), color));
	}

	public void setBackgroundColor(int start, int length, int color) {
		setBackgroundColor(start, length, Display.getCurrent().getSystemColor(color));
	}

	public void setBackgroundColor(int start, int length, Color color) {
		StyleRange styleRangeNode = new StyleRange();
		styleRangeNode.start = start;
		styleRangeNode.length = length;
		styleRangeNode.background = color;
		getTextWidget().setStyleRange(styleRangeNode);
	}

}
