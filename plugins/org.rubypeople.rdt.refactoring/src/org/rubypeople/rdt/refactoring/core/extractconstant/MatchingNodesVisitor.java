package org.rubypeople.rdt.refactoring.core.extractconstant;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.jruby.ast.Node;
import org.rubypeople.rdt.core.formatter.EditableFormatHelper;
import org.rubypeople.rdt.core.formatter.FormatHelper;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.core.formatter.ReWriterContext;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;

public class MatchingNodesVisitor extends InOrderVisitor
{

	private Node toMatch;
	private String src;
	private String selectedNodeSrc;
	private List<Node> matches;

	public MatchingNodesVisitor(Node selection, String src)
	{
		this.toMatch = selection;
		this.src = src;
		this.selectedNodeSrc = getSource(selection);
		this.matches = new ArrayList<Node>();
	}

	@Override
	protected Object visitNode(Node iVisited)
	{
		if (iVisited != null && iVisited.getClass().equals(toMatch.getClass()))
		{
			// same type of node
			String currentNodeSrc = getSource(iVisited); // compare src of the nodes
			// FIXME This can be very expensive! We should compare node for node (the attributes we care about - value,
			// args, etc)
			if (currentNodeSrc.equals(selectedNodeSrc))
			{
				matches.add(iVisited);
			}
		}
		return super.visitNode(iVisited);
	}

	private String getSource(Node iVisited)
	{
		StringWriter writer = new StringWriter();
		FormatHelper helper = new EditableFormatHelper();
		ReWriterContext context = new ReWriterContext(writer, src, helper);
		// FIXME Do this in a way that we don't care about difference between single and double quotes for strings!
		ReWriteVisitor visitor = new ReWriteVisitor(context);
		iVisited.accept(visitor);
		return writer.getBuffer().toString();
	}

	public List<Node> getMatches()
	{
		return matches;
	}

}
