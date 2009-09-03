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

package org.rubypeople.rdt.refactoring.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jruby.CompatVersion;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.CommentNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.NilImplicitNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.WhileNode;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.parser.ParserConfiguration;
import org.jruby.util.KCode;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.core.parser.RubyParserWithComments;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

/**
 * This class would make more sense integrated into the Class org.jruby.ast.Node. It is not (yet) implemented there
 * because we like to keep our code seperated from JRuby.
 */
public class NodeProvider
{

	private static final Class[] EMPTY_NODES = { NewlineNode.class, BlockNode.class, ArrayNode.class, ArgsNode.class,
			IterNode.class, WhileNode.class };

	public static Collection<Node> getChildren(Node enclosingNode)
	{
		Iterator<Node> it = enclosingNode.childNodes().iterator();
		Collection<Node> children = new ArrayList<Node>();
		while (it.hasNext())
			children.add(it.next());
		return children;
	}

	public static boolean hasSyntaxErrors(String fileName, String fileContent)
	{
		try
		{
			parseFile(fileName, fileContent);
			return false;
		}
		catch (SyntaxException e)
		{
			return true;
		}
	}

	private static RootNode parseFile(String fileName, String fileContent)
	{
		RubyParser parser = new RubyParserWithComments()
		{
			@Override
			protected ParserConfiguration getParserConfig()
			{
				// Refactoring code expects line numbers to be 1 based, no 0-based
				return new ParserConfiguration(KCode.NIL, 1, true, false, CompatVersion.RUBY1_8);
			}			

		};
		return (RootNode) parser.parse(fileName, fileContent, true /* bypass cache */).getAST();
	}

	public static RootNode getRootNode(String fileName, String fileContent)
	{
		try
		{
			return (fileContent != null) ? parseFile(fileName, fileContent) : null;
		}
		catch (SyntaxException e)
		{
			return null;
		}
	}

	public static Collection<Node> getAttributeNodes(Node parent)
	{
		Collection<Node> attrNodes = getSubNodes(parent, InstAsgnNode.class, InstVarNode.class);
		attrNodes.addAll(getAttrListNodes(parent));
		TreeSet<Node> attrNodesNoDuplicates = new TreeSet<Node>(new Comparator<Node>()
		{
			public int compare(Node node0, Node node1)
			{
				return getName(node0).compareTo(getName(node1));
			}

			private String getName(Node node)
			{
				return ((INameNode) node).getName();
			}
		});
		attrNodesNoDuplicates.addAll(attrNodes);
		return attrNodesNoDuplicates;
	}

	private static Collection<Node> getAttrListNodes(Node parent)
	{
		Collection<Node> result = new ArrayList<Node>();
		Collection<Node> fCallNodes = getSubNodes(parent, FCallNode.class);
		for (Node node : fCallNodes)
		{
			FCallNode fCallNode = (FCallNode) node;
			if (fCallNode.getName().equals("attr")) { //$NON-NLS-1$
				result.addAll(getSubNodes(fCallNode.getArgsNode(), SymbolNode.class));
			}
		}
		return result;
	}

	public static Collection<AttrAccessorNodeWrapper> getAccessorNodes(Node parent)
	{
		Collection<Node> callNodes = getSubNodes(parent, FCallNode.class);
		Collection<AttrAccessorNodeWrapper> accessorNodes = new ArrayList<AttrAccessorNodeWrapper>();

		for (Node node : callNodes)
		{
			FCallNode fCallNode = (FCallNode) node;
			if (isAccessorNode(fCallNode))
			{
				addAccessorNodes(accessorNodes, fCallNode);
			}
		}
		return accessorNodes;
	}

	private static void addAccessorNodes(Collection<AttrAccessorNodeWrapper> accessorNodes, FCallNode callNode)
	{
		if (NodeUtil.nodeAssignableFrom(callNode.getArgsNode(), ArrayNode.class))
		{
			for (Object o : callNode.getArgsNode().childNodes())
			{
				Node aktNode = (Node) o;
				if (NodeUtil.nodeAssignableFrom(aktNode, SymbolNode.class))
				{
					SymbolNode symbolNode = ((SymbolNode) aktNode);
					accessorNodes.add(new AttrAccessorNodeWrapper(callNode, symbolNode));
				}
			}
		}
	}

	public static boolean isAccessorNode(FCallNode fCallNode)
	{
		if (!hasAccessorName(fCallNode))
		{
			return false;
		}
		if (NodeUtil.nodeAssignableFrom(fCallNode.getArgsNode(), ArrayNode.class))
		{
			ArrayNode arrayNode = (ArrayNode) fCallNode.getArgsNode();
			for (Object o : arrayNode.childNodes())
			{
				Node aktNode = (Node) o;
				if (!NodeUtil.nodeAssignableFrom(aktNode, SymbolNode.class))
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		return true;
	}

	private static boolean hasAccessorName(FCallNode callNode)
	{
		String name = callNode.getName();
		return (name.equals(AttrAccessorNodeWrapper.ATTR_ACCESSOR) || name.equals(AttrAccessorNodeWrapper.ATTR_READER) || name
				.equals(AttrAccessorNodeWrapper.ATTR_WRITER));
	}

	public static Node getLastChildNode(Node parent)
	{
		return getLastChildNode(parent, Node.class);
	}

	public static Node getLastChildNode(Node parent, Class<? extends Node> childClass)
	{

		Node lastMatch = null;
		Collection<Node> childList = getChildren(parent);
		for (Node o : childList)
		{
			Node node = unwrap(o);
			if (childClass.isAssignableFrom(node.getClass()))
				lastMatch = node;
		}
		return lastMatch;
	}

	public static boolean hasChildNode(Node parent, Class<? extends Node> childClass)
	{
		return getFirstChildNode(parent, childClass) != null;
	}

	public static Node getFirstChildNode(Node parent, Class<? extends Node> childClass)
	{
		Collection<Node> childList = getChildren(parent);
		for (Node o : childList)
		{
			Node node = unwrap(o);
			if (childClass.isAssignableFrom(node.getClass()))
				return node;
		}
		return null;
	}

	/**
	 * Returns the parent of a given node.
	 */
	public static Node findParentNode(Node rootNode, Node child)
	{

		if (child == null)
			return null;

		Collection<Node> allNodes = getAllNodes(rootNode);
		for (Node node : allNodes)
		{
			if (containsNode(node.childNodes(), child))
			{
				return node;
			}
		}
		return null;
	}

	public static Node findParentNode(Node rootNode, Node child, Class type)
	{
		while ((child = findParentNode(rootNode, child)) != null && !(child instanceof RootNode))
		{
			if (child.getClass().isAssignableFrom(type))
			{
				return child;
			}
		}
		return null;
	}

	private static boolean containsNode(List<Node> list, Node node)
	{
		for (Node child : list)
		{
			if (child.equals(NilImplicitNode.NIL))
				continue;
			if (child.getPosition().getStartOffset() == node.getPosition().getStartOffset()
					&& child.getPosition().getEndOffset() == node.getPosition().getEndOffset())
			{
				return true;
			}
		}
		return false;
	}

	public static Node getNextNode(Node parentNode, Node node)
	{
		boolean match = false;
		Collection<Node> childList = getChildren(parentNode);
		for (Node o : childList)
		{
			Node aktNode = unwrap(o);
			if (match)
				return aktNode;
			if (node.equals(aktNode))
				match = true;
		}
		return null;
	}

	public static Node getNodeBefore(Node parentNode, Node node)
	{
		Node nodeBefore = null;
		Collection<Node> childList = getChildren(parentNode);
		for (Node o : childList)
		{
			Node aktNode = unwrap(o);
			if (aktNode.equals(node) && !(aktNode instanceof CommentNode))
				return nodeBefore;
			else if (!(aktNode instanceof CommentNode))
				nodeBefore = aktNode;
		}
		return null;
	}

	public static Node unwrap(Node node)
	{
		if (node instanceof NewlineNode)
			node = ((NewlineNode) node).getNextNode();
		return node;
	}

	public static boolean hasNodeBefore(Node parentNode, Node node)
	{
		return getNodeBefore(parentNode, node) != null;
	}

	public static Collection<Node> getAllNodes(Node parentNode)
	{
		Collection<Node> allNodes = new ArrayList<Node>();

		if (parentNode != null)
		{
			allNodes.add(parentNode);
			for (Object o : parentNode.childNodes())
			{
				Node node = (Node) o;
				allNodes.addAll(getAllNodes(node));
			}
		}
		return allNodes;
	}

	public static Collection<MethodDefNode> getMethodNodes(Node parentNode)
	{
		Collection<Node> subNodes = getSubNodes(parentNode, DefnNode.class, DefsNode.class);
		return Arrays.asList(subNodes.toArray(new MethodDefNode[subNodes.size()]));
	}

	public static Collection<FCallNode> getLoadAndRequireNodes(Node rootNode)
	{
		Collection<FCallNode> loadAndRequireNodes = new ArrayList<FCallNode>();
		Collection<Node> fCallNodes = NodeProvider.getSubNodes(rootNode, FCallNode.class);
		for (Node node : fCallNodes)
		{
			FCallNode fCallNode = (FCallNode) node;
			if (isLoadOrRequireNode(fCallNode))
				loadAndRequireNodes.add(fCallNode);
		}
		return loadAndRequireNodes;
	}

	private static boolean isLoadOrRequireNode(FCallNode fCallNode)
	{
		return fCallNode.getName().equalsIgnoreCase("load") || fCallNode.getName().equalsIgnoreCase("require"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static Collection<LocalAsgnNode> gatherLocalAsgnNodes(Node baseNode)
	{
		Collection<Node> nodes = gatherNodesOfTypeInAktScopeNode(baseNode, LocalAsgnNode.class);
		LocalAsgnNode[] asgnNodes = nodes.toArray(new LocalAsgnNode[nodes.size()]);
		Collection<LocalAsgnNode> localAsgnNodes = Arrays.asList(asgnNodes);
		return localAsgnNodes;
	}

	public static Collection<DAsgnNode> gatherLocalDAsgnNodes(Node baseNode)
	{

		Collection<Node> nodes = gatherNodesOfTypeInAktScopeNode(baseNode, DAsgnNode.class);
		Collection<DAsgnNode> dAsgnNodes = Arrays.asList(nodes.toArray(new DAsgnNode[nodes.size()]));
		return dAsgnNodes;
	}

	public static Collection<Node> gatherNodesOfTypeInAktScopeNode(Node baseNode, Class... klasses)
	{
		ArrayList<Node> candidates = new ArrayList<Node>();
		if (NodeUtil.nodeAssignableFrom(baseNode, klasses))
		{
			candidates.add(baseNode);
		}
		if (baseNode != null && !NodeUtil.hasScope(baseNode))
		{
			for (Object o : baseNode.childNodes())
			{
				Node n = (Node) o;
				candidates.addAll(gatherNodesOfTypeInAktScopeNode(n, klasses));
			}
		}

		return candidates;
	}

	public static Collection<Node> getSubNodes(Node baseNode, Class... klasses)
	{
		Collection<Node> allNodes = getAllNodes(baseNode);
		Collection<Node> resultNodes = new ArrayList<Node>();
		for (Node aktNode : allNodes)
		{
			if (NodeUtil.nodeAssignableFrom(aktNode, klasses))
			{
				resultNodes.add(aktNode);
			}
		}
		return resultNodes;
	}

	public static boolean hasSubNodes(Node baseNode, Class... klasses)
	{
		return !getSubNodes(baseNode, klasses).isEmpty();
	}

	public static Node getEnclosingNodeOfType(Node baseNode, Node enclosedNode, Class... klasses)
	{
		return SelectionNodeProvider.getSelectedNodeOfType(baseNode, enclosedNode.getPosition().getStartOffset(),
				klasses);
	}

	public static Collection<MethodDefNode> gatherMethodDefinitionNodes(Node enclosingScopeNode)
	{
		Collection<Node> nodes = gatherNodesOfTypeInAktScopeNode(enclosingScopeNode, DefnNode.class, DefsNode.class);

		return Arrays.asList(nodes.toArray(new MethodDefNode[nodes.size()]));
	}

	public static Collection<Node> getInstFieldOccurences(Node node)
	{
		Collection<Node> allOccurences = getSubNodes(node, InstAsgnNode.class, InstVarNode.class);
		allOccurences.addAll(getAttrListNodes(node));
		return allOccurences;
	}

	public static Collection<Node> getClassFieldOccurences(Node decoratedNode)
	{
		return getSubNodes(decoratedNode, ClassVarAsgnNode.class, ClassVarNode.class);
	}

	public static boolean isEmptyNode(Node node)
	{
		if (node == null)
		{
			return true;
		}
		if (!NodeUtil.nodeAssignableFrom(node, EMPTY_NODES))
		{
			return false;
		}
		for (Object o : node.childNodes())
		{
			Node aktChild = (Node) o;
			if (!isEmptyNode(aktChild))
			{
				return false;
			}
		}
		return true;
	}

	public static Collection<MethodCallNodeWrapper> getMethodCallNodes(Node baseNode)
	{
		Collection<Node> callNodes = getSubNodes(baseNode, MethodCallNodeWrapper.METHOD_CALL_NODE_CLASSES());
		Collection<MethodCallNodeWrapper> callNode = new ArrayList<MethodCallNodeWrapper>();
		for (Node aktCallNode : callNodes)
		{
			callNode.add(new MethodCallNodeWrapper(aktCallNode));
		}
		return callNode;
	}

	public static Collection<FieldNodeWrapper> getFieldNodes(Node baseNode)
	{
		Collection<Node> fieldNodes = getSubNodes(baseNode, FieldNodeWrapper.fieldNodeClasses());
		Collection<FieldNodeWrapper> fields = new ArrayList<FieldNodeWrapper>();
		for (Node aktFieldNode : fieldNodes)
		{
			fields.add(new FieldNodeWrapper(aktFieldNode));
		}
		return fields;
	}
}
