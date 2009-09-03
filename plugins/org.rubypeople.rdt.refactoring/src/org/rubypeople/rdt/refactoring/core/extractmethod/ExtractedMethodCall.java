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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.core.extractmethod;

import java.util.HashMap;
import java.util.Map;

import org.jruby.ast.CallNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.formatter.EditableFormatHelper;
import org.rubypeople.rdt.core.formatter.FormatHelper;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.ReplaceEditProvider;

public class ExtractedMethodCall extends ReplaceEditProvider
{

	private final Node rootNode;
	private Node selectedNode;
	private Node methodCallNode;

	public ExtractedMethodCall(Node selectedNode, Node methodCallNode, Node rootNode)
	{
		super(false);
		this.selectedNode = selectedNode;
		this.methodCallNode = methodCallNode;
		this.rootNode = rootNode;
	}

	public ExtractedMethodCall(ExtractMethodConfig config)
	{
		this(config.getExtractMethodHelper().getSelectedNodes(), config.getExtractMethodHelper().getMethodCallNode(),
				config.getRootNode());
	}

	@Override
	protected int getOffsetLength()
	{
		return getEndOffset() - getStartOffset();
	}

	private int getStartOffset()
	{
		return getExtendedPosition(selectedNode).getStartOffset();
	}

	private int getEndOffset()
	{
		return getExtendedPosition(selectedNode).getEndOffset();
	}

	@Override
	protected Node getEditNode(int offset, String document)
	{
		return methodCallNode;
	}

	@Override
	protected FormatHelper getFormatHelper()
	{
		Node bodyNode = selectedNode;
		while (bodyNode instanceof NewlineNode)
		{
			bodyNode = ((NewlineNode) bodyNode).getNextNode();
		}

		Map<String, Object> options = new HashMap<String, Object>();
		if (NodeProvider.findParentNode(rootNode, bodyNode) instanceof CallNode)
		{
			options.put(FormatHelper.ALWAYS_SURROUND_METHOD_CALLS_IN_PARENS, true);
		}
		return new EditableFormatHelper(options);
	}

	@Override
	protected int getOffset(String document)
	{
		return getStartOffset();
	}

}
