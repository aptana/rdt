package org.rubypeople.rdt.internal.core.parser;

import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;

public class ClosestNodeLocator extends InOrderVisitor
{

	private int startOffset;
	private int endOffset;
	private int smallestDiff = Integer.MAX_VALUE;
	private Node locatedNode;
	private INodeAcceptor acceptor;

	public Object handleNode(Node iVisited)
	{
		ISourcePosition position = iVisited.getPosition();
		int diff = Integer.MAX_VALUE;
		// if node is before we want to compare the end of the node's position to beginning of given position
		if (position.getEndOffset() < startOffset)
		{
			diff = Math.abs(startOffset - position.getEndOffset());
		}
		else if (position.getStartOffset() > endOffset)
		{
			diff = Math.abs(position.getStartOffset() - endOffset);
		}
		else
		{
			// wraps the given position. So we compare starts to each other and ends to each other and take smallest
			diff = Math.abs(position.getStartOffset() - startOffset);
			diff = Math.min(diff, Math.abs(position.getEndOffset() - endOffset));
		}
		if (diff <= smallestDiff && acceptor.doesAccept(iVisited))
		{
			locatedNode = iVisited;
			smallestDiff = diff;
		}
		// TODO If both start and end offset are past the start and end offsets we have, then we can probably shortcut
		// FIXME Since we're going in order we can probably shortcut big time here once we see the diffs getting
		// progressively bigger again
		return super.handleNode(iVisited);
	}

	public Node getClosestNodeAtOffset(Node ast, int startOffset)
	{
		return getClosestNodeAtOffset(ast, startOffset, new INodeAcceptor()
		{
			public boolean doesAccept(Node node)
			{
				return !(node instanceof NewlineNode);
			}
		});
	}

	public Node getClosestNodeAtOffset(Node ast, int startOffset, INodeAcceptor nodeAcceptor)
	{
		return getClosestNode(ast, startOffset, startOffset, nodeAcceptor);
	}

	public Node getClosestNode(Node ast, ISourcePosition pos)
	{
		return getClosestNode(ast, pos, new INodeAcceptor()
		{
			public boolean doesAccept(Node node)
			{
				return !(node instanceof NewlineNode);
			}
		});
	}

	public Node getClosestNode(Node ast, ISourcePosition pos, INodeAcceptor nodeAcceptor)
	{
		return getClosestNode(ast, pos.getStartOffset(), pos.getEndOffset(), nodeAcceptor);
	}

	private Node getClosestNode(Node ast, int startOffset, int endOffset, INodeAcceptor nodeAcceptor)
	{
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.acceptor = nodeAcceptor;
		ast.accept(this);
		this.acceptor = null;
		return locatedNode;
	}

}
