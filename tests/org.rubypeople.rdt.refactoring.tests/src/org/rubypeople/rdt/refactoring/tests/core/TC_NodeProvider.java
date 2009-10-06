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

package org.rubypeople.rdt.refactoring.tests.core;

import java.util.Collection;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.CommentNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.SymbolNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.parser.LocalStaticScope;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.internal.core.parser.ClosestNodeLocator;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class TC_NodeProvider extends FileTestCase
{

	public TC_NodeProvider()
	{
		super("TC_NodeProvider");
	}

	public void testGetAllNodes()
	{
		Node rootNode = getRootNode("testGetAllNodes.rb");
		Node[] allNodes = NodeProvider.getAllNodes(rootNode).toArray(new Node[] {});

		assertEquals(21, allNodes.length);

		assertTrue(NodeUtil.nodeAssignableFrom(allNodes[1], BlockNode.class));
		assertTrue(NodeUtil.nodeAssignableFrom(allNodes[2], NewlineNode.class));
		assertTrue(NodeUtil.nodeAssignableFrom(allNodes[3], ClassNode.class));
		assertTrue(NodeUtil.nodeAssignableFrom(allNodes[4], Colon2Node.class));
		assertTrue(NodeUtil.nodeAssignableFrom(allNodes[20], ConstNode.class));
	}

	public void testGetSelectedNodeOfType()
	{
		Node rootNode = getRootNode("testGetSelectedNodeOfType.rb");

		Node defnNode = SelectionNodeProvider.getSelectedNodeOfType(rootNode, 2, DefnNode.class);
		assertEquals(DefnNode.class, defnNode.getClass());

		Node argumentNode = SelectionNodeProvider.getSelectedNodeOfType(rootNode, 17, ArgumentNode.class);
		assertEquals(ArgumentNode.class, argumentNode.getClass());

		Node nullNode = SelectionNodeProvider.getSelectedNodeOfType(rootNode, 0, ArgumentNode.class);
		assertNull(nullNode);

		Node localAsgnNode = SelectionNodeProvider.getSelectedNodeOfType(rootNode, 39, LocalAsgnNode.class);
		assertEquals(LocalAsgnNode.class, localAsgnNode.getClass());
	}

	public void testNodeAssignableFrom()
	{
		ArgsNode args = new ArgsNode(null, null, null, null, null, null);
		assertTrue(NodeUtil.nodeAssignableFrom(new DefnNode(null, null, args, new LocalStaticScope(null), null), DefnNode.class));
		assertTrue(NodeUtil.nodeAssignableFrom(new LocalVarNode(null, 0, ""), LocalVarNode.class));
	}

	public void testNodeContainsCaretPosition()
	{
		assertTrue(SelectionNodeProvider.nodeContainsPosition(new ArgumentNode(new IDESourcePosition("", 0, 1, 0, 5),
				""), 0));
		assertTrue(SelectionNodeProvider.nodeContainsPosition(new ArgumentNode(new IDESourcePosition("", 0, 1, 0, 5),
				""), 5));
		assertFalse(SelectionNodeProvider.nodeContainsPosition(new ArgumentNode(new IDESourcePosition("", 0, 1, 0, 5),
				""), 6));
		assertFalse(SelectionNodeProvider.nodeContainsPosition(new ArgumentNode(new IDESourcePosition("", 0, 1, 0, 5),
				""), 7));
	}

	public void testGatherLocalAsgnNodes()
	{
		RootNode rootNode = getRootNode("testGatherLocalAsgnNodes.rb");
		LocalAsgnNode[] nodes = NodeProvider.gatherLocalAsgnNodes(rootNode.getBodyNode()).toArray(
				new LocalAsgnNode[] {});

		assertEquals(3, nodes.length);

		assertEquals("var0", nodes[0].getName());
		assertEquals(0, nodes[0].getIndex());

		assertEquals("var1", nodes[1].getName());
		assertEquals(1, nodes[1].getIndex());

		assertEquals("var4", nodes[2].getName());
		assertEquals(2, nodes[2].getIndex());
	}

	public void testCommentAssociation() throws Exception
	{
		String fileName = "test_comments_1.rb";
		String source = new String(Util.getInputStreamAsCharArray(getClass().getResourceAsStream(fileName), -1, null));
		RootNode root = NodeProvider.getRootNode(fileName, source);
		assertNodeHasTypeAndComments(root, 0, ClassNode.class, 0); // class
		assertNodeHasTypeAndComments(root, 34, FCallNode.class, 2); // attr_accessor
		assertNodeHasTypeAndComments(root, 48, SymbolNode.class, 0); // :a
	}

	private void assertNodeHasTypeAndComments(Node ast, int offset, Class<? extends Node> expectedClass,
			int commentCount)
	{
		Node node = getNode(ast, offset);
		assertNotNull(node);
		assertTrue(expectedClass.isAssignableFrom(node.getClass()));
		Collection<CommentNode> comments = node.getComments();
		assertNotNull(comments);
		assertEquals(commentCount, comments.size());
	}

	private Node getNode(Node root, int offset)
	{
		return new ClosestNodeLocator().getClosestNodeAtOffset(root, offset);
	}
}
