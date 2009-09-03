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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.ArgsCatNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.Node;
import org.jruby.ast.SplatNode;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class CallArgsNodeWrapper implements INodeWrapper
{

	private SplatNode splatNode;
	private ArrayNode arrayNode;
	private Node wrappedNode;

	public CallArgsNodeWrapper(Node node)
	{

		wrappedNode = node;
		if (NodeUtil.nodeAssignableFrom(node, ArrayNode.class))
		{
			arrayNode = (ArrayNode) node;

		}
		else if (NodeUtil.nodeAssignableFrom(node, SplatNode.class))
		{
			splatNode = (SplatNode) node;

		}
		else if (NodeUtil.nodeAssignableFrom(node, ArgsCatNode.class))
		{
			ArgsCatNode argsCatNode = (ArgsCatNode) node;
			arrayNode = (ArrayNode) argsCatNode.getFirstNode();
			splatNode = (SplatNode) argsCatNode.getSecondNode();

		}
	}

	public boolean hasSplatNode()
	{
		return splatNode != null;
	}

	public boolean hasArrayNode()
	{
		return arrayNode != null;
	}

	public Node cloneWithAddedArg(Node addedArg)
	{
		ArrayNode newArrayNode = getNewArrayNode(addedArg);
		if (hasSplatNode())
		{
			return NodeFactory.createArgsCatNode(newArrayNode, splatNode);
		}
		return newArrayNode;
	}

	private ArrayNode getNewArrayNode(Node addedArg)
	{
		Collection<Node> newArrayChilds = new ArrayList<Node>();
		if (hasArrayNode())
		{
			for (Node obj : arrayNode.childNodes())
			{
				newArrayChilds.add(obj);
			}
		}
		newArrayChilds.add(addedArg);
		return NodeFactory.createArrayNode(newArrayChilds);
	}

	public Node getWrappedNode()
	{
		return wrappedNode;
	}
}
