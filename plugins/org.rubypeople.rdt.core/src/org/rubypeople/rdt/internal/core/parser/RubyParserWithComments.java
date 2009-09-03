package org.rubypeople.rdt.internal.core.parser;

import java.util.List;

import org.jruby.ast.BlockNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.CommentNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.parser.RubyParserResult;
import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;

public class RubyParserWithComments extends RubyParser
{
	@Override
	protected void postProcessResult(RubyParserResult result)
	{
		super.postProcessResult(result);
		associateCommentsWithNodes(result.getAST(), result.getCommentNodes());
	}

	private void associateCommentsWithNodes(Node ast, List<CommentNode> commentNodes)
	{
		for (CommentNode commentNode : commentNodes)
		{
			final ISourcePosition pos = commentNode.getPosition();
			// Find the closest node that starts or ends on same line
			Node closestOnSameLine = new ClosestNodeLocator().getClosestNode(ast, pos, new INodeAcceptor()
			{
				public boolean doesAccept(Node node)
				{
					if (node instanceof NewlineNode || node instanceof RootNode)
						return false;
					return (node.getPosition().getEndLine() == pos.getStartLine())
							|| (node.getPosition().getStartLine() == pos.getStartLine());
				}
			});
			if (closestOnSameLine == null) // nothing on same line
			{
				// find next type/method def following our position, or any node on very next line
				Node next = new ClosestNodeLocator().getClosestNode(ast, pos, new INodeAcceptor()
				{
					public boolean doesAccept(Node node)
					{
						if (node instanceof NewlineNode)
							return false;
						return node.getPosition().getStartOffset() > pos.getStartOffset();
					}
				});
				if (next != null)
				{
					Node surroundingScopeOfComment = getSurroundingScopeNode(ast, pos);
					Node surroundingScopeOfNext = getSurroundingScopeNode(ast, next.getPosition());
					if (surroundingScopeOfComment.equals(surroundingScopeOfNext))
					{
						// comment and next node are siblings, associate to next
						next.addComment(commentNode);
					}
					else
					{
						// different scopes, associate to scope surrounding comment
						surroundingScopeOfComment.addComment(commentNode);
					}
					continue;
				}
				else
				{
					// No next node, associate with current scope
					Node surroundingScopeOfComment = getSurroundingScopeNode(ast, pos);
					surroundingScopeOfComment.addComment(commentNode);
				}
			}
			else
			{
				// Then "unwrap" out to largest scope on same line
				Node unwrapped = unwrap(ast, closestOnSameLine, pos.getStartLine());
				if (unwrapped != null)
					unwrapped.addComment(commentNode);
			}
		}
	}

	private Node getSurroundingScopeNode(Node ast, final ISourcePosition pos)
	{
		Node result = new ClosestNodeLocator().getClosestNode(ast, pos, new INodeAcceptor()
		{
			public boolean doesAccept(Node node)
			{
				if (!(node instanceof ClassNode || node instanceof ModuleNode || node instanceof DefnNode
						|| node instanceof DefsNode || node instanceof IterNode || node instanceof RootNode))
					return false;
				return node.getPosition().getEndLine() > pos.getEndLine()
						&& node.getPosition().getStartLine() < pos.getStartLine();
			}
		});
		if (result == null)
			return ast;
		return result;
	}

	private Node unwrap(Node ast, Node closest, final int line)
	{
		while (true)
		{
			Node last = closest;
			closest = new ParentLocator(ast, closest).findParent(new INodeAcceptor()
			{
				public boolean doesAccept(Node node)
				{
					return !(node instanceof NewlineNode) && !(node instanceof BlockNode)
							&& !(node instanceof RootNode)
							&& (node.getPosition().getEndLine() == line || node.getPosition().getStartLine() == line);
				}
			});
			if (closest == null)
			{
				return last;
			}
		}
	}
}
