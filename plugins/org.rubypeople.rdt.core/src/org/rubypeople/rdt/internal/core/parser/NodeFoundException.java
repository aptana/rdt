package org.rubypeople.rdt.internal.core.parser;

import org.jruby.ast.Node;

public class NodeFoundException extends RuntimeException
{
	private static final long serialVersionUID = -1087131531164628936L;

	private Node node;

	public NodeFoundException(Node node)
	{
		super();
		this.node = node;
	}

	public Node getNode()
	{
		return node;
	}
}