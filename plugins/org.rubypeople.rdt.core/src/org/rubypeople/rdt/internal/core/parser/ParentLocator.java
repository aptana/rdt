package org.rubypeople.rdt.internal.core.parser;

import org.jruby.ast.Node;
import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;

public class ParentLocator extends InOrderVisitor
{

	private Node root;
	private Node child;
	private Node parent;
	private boolean parentFound;
	private INodeAcceptor acceptor;

	public ParentLocator(Node root, Node lastBeforeOnSameLine)
	{
		this.root = root;
		this.child = lastBeforeOnSameLine;
	}

	@Override
	protected Object handleNode(Node visited)
	{
		if (parentFound)
			return null;
		if (visited.equals(child))
		{
			parentFound = true;
			throw new NodeFoundException(parent);
		}
		else
		{
			if (acceptor.doesAccept(visited))
				parent = visited;
		}
		return null;
	}

	public Node findParent(INodeAcceptor nodeAcceptor)
	{
		this.acceptor = nodeAcceptor;
		this.parent = null;
		try
		{
			root.accept(this);
		}
		catch (NodeFoundException e)
		{
			return e.getNode();
		}
		finally
		{
			this.acceptor = null;
		}
		return null;
	}
}
