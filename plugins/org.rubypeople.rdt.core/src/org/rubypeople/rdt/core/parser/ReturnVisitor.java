package org.rubypeople.rdt.core.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jruby.ast.BlockNode;
import org.jruby.ast.CaseNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.OpAsgnAndNode;
import org.jruby.ast.OpAsgnNode;
import org.jruby.ast.OpAsgnOrNode;
import org.jruby.ast.OpElementAsgnNode;
import org.jruby.ast.ReturnNode;
import org.jruby.ast.RootNode;
import org.jruby.ast.WhenNode;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;

/**
 * @author Chris Williams (cwilliams@aptana.com)
 */
public class ReturnVisitor extends InOrderVisitor
{

	private boolean implicit = false;

	private Set<ReturnVisitor> branches = new HashSet<ReturnVisitor>();
	private List<Node> values = new ArrayList<Node>();
	private Node lastNode;

	@Override
	protected Object visitNode(Node iVisited)
	{
		if (iVisited != null && !structuralNode(iVisited) && !branchingNode(iVisited)
				&& !(iVisited instanceof ReturnNode))
		{
			implicit = true;
			lastNode = iVisited;
		}
		return super.visitNode(iVisited);
	}
	
	@Override
	public Object visitOpAsgnAndNode(OpAsgnAndNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		return null;
	}
	
	@Override
	public Object visitOpAsgnNode(OpAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		return null;
	}
	
	@Override
	public Object visitOpAsgnOrNode(OpAsgnOrNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		return null;
	}

	private boolean structuralNode(Node visited)
	{
		return (visited instanceof RootNode) || (visited instanceof NewlineNode);
	}

	private boolean branchingNode(Node visited)
	{
		return (visited instanceof IfNode) || (visited instanceof CaseNode);
	}

	@Override
	public Object visitReturnNode(ReturnNode iVisited)
	{
		implicit = false;
		lastNode = null;
		values.add(iVisited);
		return null;
	}

	@Override
	public Object visitCaseNode(CaseNode iVisited)
	{
		ListNode node = iVisited.getCases();
		List<Node> caseChildren = node.childNodes();
		
		WhenNode whenNode = (WhenNode) caseChildren.get(0);
		while (whenNode != null)
		{
			ReturnVisitor visitor = new ReturnVisitor();
			whenNode.getBodyNode().accept(visitor);
			branches.add(visitor);
			Node nextCase = whenNode.getNextCase();
			if (nextCase instanceof BlockNode)
			{
				ReturnVisitor visitor2 = new ReturnVisitor();
				nextCase.accept(visitor2);
				branches.add(visitor);
				whenNode = null;
				break;
			}
			if (nextCase instanceof NewlineNode)
			{
				NewlineNode newline = (NewlineNode) nextCase;
				nextCase = newline.getNextNode();
			}
			if (nextCase instanceof WhenNode)
			{
				whenNode = (WhenNode) whenNode.getNextCase();
			}
			else
			{
				whenNode = null;
			}
		}
		return null;
	}

	@Override
	public Object visitIfNode(IfNode iVisited)
	{
		if (iVisited.getThenBody() != null)
		{
			ReturnVisitor visitor = new ReturnVisitor();
			iVisited.getThenBody().accept(visitor);
			branches.add(visitor);
		}
		if (iVisited.getElseBody() != null)
		{
			ReturnVisitor visitor = new ReturnVisitor();
			iVisited.getElseBody().accept(visitor);
			branches.add(visitor);
		}
		else
		{
			implicit = true;
		}
		return null;
	}

	public boolean alwaysExplicit()
	{
		for (ReturnVisitor visitor : branches)
		{
			if (!visitor.alwaysExplicit())
				return false;
		}
		return !implicit;
	}

	public List<Node> getReturnValues()
	{
		if (lastNode != null)
		{
			values.add(lastNode);
		}
		for (ReturnVisitor visitor : branches)
		{
			values.addAll(visitor.getReturnValues());
		}
		return values;
	}

}