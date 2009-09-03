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
 * Copyright (C) 2007 Lukas Felber <lfelber@hsr.ch>
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.TextEdit;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.util.Constants;
import org.rubypeople.rdt.refactoring.util.FileHelper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class DeleteEditProvider extends EditProvider {

	public final static int DELETE_LINEBREAK_AFTER = 0;

	public final static int DELETE_LINEBREAK_BEFORE = 1;

	public final static int DEFAULT_DELETE_TYPE = DELETE_LINEBREAK_BEFORE;

	private Node fromNode;

	private Node toNode;

	private int type;

	public DeleteEditProvider(Node fromNode, Node toNode) {
		super(true, false);
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.type = DEFAULT_DELETE_TYPE;
	}

	public DeleteEditProvider(Node node) {
		this(node, node);
	}

	public TextEdit getEdit(String document) {
		int startOffset = getStartOffset(document);
		int endOffset = getEndOffset(document);
		int length = endOffset - startOffset;
		return new DeleteEdit(startOffset, length);
	}

	private int getStartOffset(String document) {
		int startLine = NodeUtil.subPositionUnion(fromNode).getStartLine() - 1;
		int pos = -1;
		String lineDelimiter = FileHelper.getLineDelimiter(document);
		for (int i = 0; i < startLine; i++) {
			pos = document.indexOf(lineDelimiter, pos + 1);
		}
		if (pos < 0) {
			pos = 0;
		}
		String leadingStr = document.substring(pos, NodeUtil.subPositionUnion(fromNode).getStartOffset());

		if (leadingStr.trim().equals("")) { //$NON-NLS-1$
			return pos;
		}
		return NodeUtil.subPositionUnion(fromNode).getStartOffset();
	}

	private int getEndOffset(String document) {
		int offset = NodeUtil.subPositionUnion(toNode).getEndOffset();
		int aktPos = offset;
		Matcher matcher = Pattern.compile("\\r\\n|[\\s;]").matcher(document);  //$NON-NLS-1$
		String lineDelimiter = FileHelper.getLineDelimiter(document); 
		while (matcher.find(aktPos)) {
			if (matcher.start() == aktPos) {
				aktPos += matcher.group().length(); 
			} else {
				offset = aktPos;
				break;
			}
			if (foundTerminator(matcher.group()))
				break;
		}

		if (type != DELETE_LINEBREAK_AFTER) {
			if (matcher.find(offset) && matcher.group().equals(lineDelimiter)) {
				aktPos -= lineDelimiter.length(); 
			}
		}
		return aktPos;
	}

	private boolean foundTerminator(String terminator) {
		return (terminator.equals(Character.toString(Constants.NL)) || terminator.equals(";")); //$NON-NLS-1$
	}

	@Override
	protected Node getEditNode(int offset, String document) {
		return null;
	}

	@Override
	protected int getOffset(String document) {
		return Integer.MAX_VALUE;
	}

	public void setDeleteType(int deleteType) {
		type = deleteType;
	}
}
