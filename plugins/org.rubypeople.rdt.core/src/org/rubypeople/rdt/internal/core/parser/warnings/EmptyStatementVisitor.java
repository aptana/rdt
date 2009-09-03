package org.rubypeople.rdt.internal.core.parser.warnings;

import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.NilImplicitNode;
import org.jruby.ast.Node;
import org.jruby.ast.WhenNode;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

public class EmptyStatementVisitor extends RubyLintVisitor
{

	public EmptyStatementVisitor(String contents)
	{
		super(contents);
	}

	@Override
	protected String getOptionKey()
	{
		return RubyCore.COMPILER_PB_EMPTY_STATEMENT;
	}

	public Object visitIfNode(IfNode iVisited)
	{
		if (iVisited.getThenBody() == null && iVisited.getElseBody() == null)
		{
			createProblem(iVisited.getPosition(), "Empty Conditional Body");
			return super.visitIfNode(iVisited);
		}
		String source = getSourceOfKeywordForIf(iVisited);
		Node body = null;
		if (source != null && source.trim().startsWith("unless"))
		{
			body = iVisited.getElseBody();
		}
		else if (source != null && source.trim().startsWith("if"))
		{
			body = iVisited.getThenBody();
		}
		if (body == null)
		{
			createProblem(iVisited.getPosition(), "Empty Conditional Body");
		}
		return super.visitIfNode(iVisited);
	}

	private String getSourceOfKeywordForIf(IfNode iVisited)
	{
		Node conditionNode = iVisited.getCondition();
		Node elseBody = iVisited.getElseBody();
		if (elseBody == null)
		{
			Node thenBody = iVisited.getThenBody();
			if (thenBody == null)
				return null; // FIXME
			if (thenBody.getPosition().getEndOffset() > conditionNode.getPosition().getStartOffset())
			{  // condition
				return getSource(iVisited.getPosition().getStartOffset(), conditionNode.getPosition().getStartOffset());
			}
			else
			{ // modifier
				return getSource(thenBody.getPosition().getEndOffset(), conditionNode.getPosition().getStartOffset());
			}
		}
		else
		{
			if (elseBody.getPosition().getEndOffset() > conditionNode.getPosition().getStartOffset())
			{ // unless condition
				return getSource(iVisited.getPosition().getStartOffset(), conditionNode.getPosition().getStartOffset());
			}
			else
			{ // unless modifier
				return getSource(elseBody.getPosition().getEndOffset(), conditionNode.getPosition().getStartOffset());
			}
		}
	}

	public Object visitDefnNode(DefnNode iVisited)
	{
		if (iVisited.getBodyNode() == null)
		{
			createProblem(iVisited.getPosition(), "Empty Method Definition");
		}
		return super.visitDefnNode(iVisited);
	}

	public Object visitDefsNode(DefsNode iVisited)
	{
		if (iVisited.getBodyNode() == null)
		{
			createProblem(iVisited.getPosition(), "Empty Method Definition");
		}
		return super.visitDefsNode(iVisited);
	}

	public Object visitWhenNode(WhenNode iVisited)
	{
		if (iVisited.getBodyNode() == null || iVisited.getBodyNode().equals(NilImplicitNode.NIL))
		{
			createProblem(iVisited.getPosition(), "Empty When Body");
		}
		return super.visitWhenNode(iVisited);
	}

	public Object visitIterNode(IterNode iVisited)
	{
		if (iVisited.getBodyNode() == null)
		{
			createProblem(iVisited.getPosition(), "Empty Block");
		}
		return super.visitIterNode(iVisited);
	}

}
