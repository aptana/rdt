package org.rubypeople.rdt.internal.core.parser;

import org.jruby.ast.Node;

public class NextNodeFinder extends InOrderVisitor
{

	@Override
	public Object acceptNode(Node node)
	{
		if (node != null && !node.isInvisible())
			throw new NodeFoundException(node);
		return null;
	}

	public Node nextNode(Node current)
	{
		try
		{
			current.accept(this);
		}
		catch (NodeFoundException e)
		{
			return e.getNode();
		}
		return null;
	}
}
