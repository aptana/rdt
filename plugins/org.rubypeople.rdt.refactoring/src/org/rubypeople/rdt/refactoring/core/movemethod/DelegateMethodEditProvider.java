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

package org.rubypeople.rdt.refactoring.core.movemethod;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.ReplaceEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;

public class DelegateMethodEditProvider extends ReplaceEditProvider
{

	private MoveMethodConfig config;
	private Node scopePos;
	private MethodNodeWrapper oldMethod;

	public DelegateMethodEditProvider(MoveMethodConfig config)
	{
		super(false);
		this.config = config;
		oldMethod = config.getMethodNode();
		scopePos = NodeProvider.unwrap(oldMethod.getBodyNode());
	}

	@Override
	protected int getOffsetLength(String document)
	{
		// HACK For some nodes we're getting a position that is one too far off the end, making us gobble up a newline! So we hack a fix here
		int length = getOffsetLength();
		int offset = getOffset(document);
		String sub = document.substring(offset, offset + length);
		if (sub.endsWith("\n"))
			return length - 1;
		return length;
	}
	
	@Override
	protected int getOffsetLength()
	{
		return getExtendedPosition(scopePos).getEndOffset() - getExtendedPosition(scopePos).getStartOffset();
	}

	@Override
	protected Node getEditNode(int offset, String document)
	{
		return NodeFactory.createNewLineNode(getMethodCallNode());
	}

	private Node getMethodCallNode()
	{
		Node receiverNode;
		if (oldMethod.isClassMethod())
		{
			receiverNode = NodeFactory.createConstNode(config.getDestinationClassNode().getName());
		}
		else
		{
			receiverNode = NodeFactory.createInstVarNode(config.getFieldInSourceClassOfTypeDestinationClass());
		}
		ArgsNodeWrapper argsNode = oldMethod.getArgsNode();
		ArrayNode arrayNode = NodeFactory.createArrayNode();
		for (String argName : argsNode.getArgsList())
		{
			arrayNode.add(NodeFactory.createLocalVarNode(argName));
		}
		if (config.doesNewMethodNeedsReferenceToSourceClass())
		{
			arrayNode.add(NodeFactory.createSelfNode());
		}
		return NodeFactory.createCallNode(receiverNode, config.getMovedMethodName(), (arrayNode.size() == 0) ? null
				: arrayNode);
	}

	@Override
	protected int getOffset(String document)
	{
		return getExtendedPosition(scopePos).getStartOffset();
	}

}
