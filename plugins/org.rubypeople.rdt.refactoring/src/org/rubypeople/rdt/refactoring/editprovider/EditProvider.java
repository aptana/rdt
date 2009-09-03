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
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
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

package org.rubypeople.rdt.refactoring.editprovider;

import org.eclipse.text.edits.TextEdit;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.formatter.EditableFormatHelper;
import org.rubypeople.rdt.core.formatter.FormatHelper;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.util.FileHelper;
import org.rubypeople.rdt.refactoring.util.HsrFormatter;

public abstract class EditProvider implements IEditProvider
{

	protected boolean lastEditInGroup;
	protected boolean firstEditInGroup;

	private boolean doFormat;
	private boolean doTrim;

	public abstract TextEdit getEdit(String document);

	protected abstract int getOffset(String document);

	protected abstract Node getEditNode(int offset, String document);

	public EditProvider(boolean doFormat, boolean doTrim)
	{
		this.doFormat = doFormat;
		this.doTrim = doTrim;
	}

	protected FormatHelper getFormatHelper()
	{
		return new EditableFormatHelper();
	}

	protected String getFormatedNode(String document)
	{
		int offset = getOffset(document);
		Node insertNode = getEditNode(offset, document);
		String text = ReWriteVisitor.createCodeFromNode(insertNode, document, getFormatHelper());
		if (doFormat)
		{
			text = HsrFormatter.format(document, text, offset);
		}
		if (doTrim)
		{
			text = text.trim();
		}
		return text.replaceAll("\n", FileHelper.getLineDelimiter(document));
	}

	protected void setFirstInGroup(boolean first)
	{
		firstEditInGroup = first;
	}

	protected void setLastInGroup(boolean last)
	{
		lastEditInGroup = last;
	}

	protected boolean isNextLineEmpty(int offset, String document)
	{
		if (document.length() <= offset + 1)
			return false; // no more characters, so no next line
		int firstNL = getNextNLPosition(offset, document);
		if (firstNL == -1)
			return false; // no next line
		int secondNL = getNextNLPosition(firstNL + 1, document);
		if (secondNL == -1)
			secondNL = document.length();
		String nextLine = document.substring(firstNL + 1, secondNL);
		return nextLine.trim().length() == 0;
	}

	private int getNextNLPosition(int offset, String document)
	{
		return document.indexOf(FileHelper.getLineDelimiter(document), offset);
	}

	protected ISourcePosition getExtendedPosition(Node node)
	{
		if (node instanceof NewlineNode)
		{
			node = ((NewlineNode) node).getNextNode();
		}
		ISourcePosition extendedPosition = node.getPositionIncludingComments();

		for (Node currentChild : node.childNodes())
		{
			if (currentChild.isInvisible())
				continue;
			extendedPosition = NodeFactory.unionPositions(extendedPosition, getExtendedPosition(currentChild));
		}

		return extendedPosition;
	}
}
