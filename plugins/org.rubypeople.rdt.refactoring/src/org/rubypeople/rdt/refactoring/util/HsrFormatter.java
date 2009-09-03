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
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
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

package org.rubypeople.rdt.refactoring.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.rubypeople.rdt.core.formatter.DefaultCodeFormatterConstants;
import org.rubypeople.rdt.internal.formatter.OldCodeFormatter;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

public class HsrFormatter
{

	public static String format(String document, String replaceText, int replaceStartOffset, int replaceEndOffset)
	{
		StringBuilder docStringBuilder = new StringBuilder(document);
		docStringBuilder.delete(replaceStartOffset, replaceEndOffset);
		return format(docStringBuilder.toString(), replaceText, replaceStartOffset);
	}
	
	public static String format(String document, String insertText, int insertOffset)
	{
		if(insertText.length() == 0)
			return insertText;
		String lineDelimiter = "\n";
		if(document.length() == 0)
			return formatString(insertText, lineDelimiter);
		StringBuilder docStringBuilder = new StringBuilder(document);
		insertText = lineDelimiter + insertText + lineDelimiter;
		if(insertOffset == document.length()) {
			docStringBuilder.append(insertText);
		}
		else {
			docStringBuilder.insert(insertOffset, insertText);
		}
		int end = insertOffset + insertText.length() - 1;
		insertOffset += lineDelimiter.length();
		int linesToSort = getLineCount(docStringBuilder.toString(), insertOffset, end, lineDelimiter);
		int lnNr = getLnNr(docStringBuilder.toString(), insertOffset - 1, lineDelimiter);
		document = docStringBuilder.toString();
		String formattedDocument = formatString(document, lineDelimiter);
		String text = getSubstring(formattedDocument, lnNr, linesToSort, lineDelimiter);
		return text;
	}
	
	private static String formatString(String code, String lineDelimiter) {

		Document doc = new Document(code);
		try {
			//the first parameter "kind" seems to be unused
			getFormatter().format(0, code, 0, code.length(), 0, lineDelimiter).apply(doc);
			return doc.get();	
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}

	private static OldCodeFormatter getFormatter()
	{
		if (RubyPlugin.getDefault() != null) {
			return RubyPlugin.getDefault().getCodeFormatter();
		}
	
		Map<String, String> options = new HashMap<String, String>();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, "space"); //$NON-NLS-1$
		options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "2"); //$NON-NLS-1$
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2"); //$NON-NLS-1$
		return new OldCodeFormatter(options);
	}

	private static String getSubstring(String str, int lnNr, int lnCount, String lineDelimiter)
	{

		int start = -1;
		int stop = 0;
		for (int i = 0; i < lnNr; i++)
		{
			start = str.indexOf(lineDelimiter, start + 1);
		}
		start++;
		stop = start - 2;
		for (int i = 0; i < lnCount; i++)
		{
			int tmpStop = str.indexOf(lineDelimiter, stop + 1);
			if(tmpStop != -1) {
				stop = tmpStop;
			}
			else {
				 break;
			}
		}
		str = str.substring(start, stop);
		return str;
	}

	private static int getLineCount(String text, int offset, int end, String lineDelimiter)
	{
		int count = 0;
		int pos = offset - 1;
		while ((pos = text.indexOf(lineDelimiter, pos + 1)) <= end && pos != -1)
		{
			count++;
		}
		return ++count;
	}

	private static int getLnNr(String text, int offset, String lineDelimiter)
	{
		return getLineCount(text, 0, offset, lineDelimiter) - 1;
	}
}
