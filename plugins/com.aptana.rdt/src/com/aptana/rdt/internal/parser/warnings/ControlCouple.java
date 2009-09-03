package com.aptana.rdt.internal.parser.warnings;

import java.util.List;

import org.jruby.ast.DefnNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.NilImplicitNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.IProblem;

public class ControlCouple extends RubyLintVisitor
{

	private Node condition;
	private List<String> args;
	private boolean problem;

	public ControlCouple(String src)
	{
		super(AptanaRDTPlugin.getDefault().getOptions(), src);
	}

	@Override
	protected String getOptionKey()
	{
		return AptanaRDTPlugin.COMPILER_PB_CONTROL_COUPLE;
	}

	@Override
	protected int getProblemID()
	{
		return IProblem.ControlCouple;
	}

	@Override
	public Object visitIfNode(IfNode visited)
	{
		Node elseBody = visited.getElseBody();
		if (elseBody != null && !elseBody.equals(NilImplicitNode.NIL))
			condition = visited.getCondition();
		return super.visitIfNode(visited);
	}

	@Override
	public Object visitDefnNode(DefnNode visited)
	{
		args = ASTUtil.getArguments(visited.getArgsNode().getPre());
		return super.visitDefnNode(visited);
	}

	@Override
	public void exitDefnNode(DefnNode visited)
	{
		args = null;
		if (problem)
		{
			// FIXME Create the problem on the arg node
			createProblem(visited.getPosition(), "Control Couple: Two code paths based on an argument to the method");
		}
		problem = false;
		super.exitDefnNode(visited);
	}

	@Override
	public Object visitLocalVarNode(LocalVarNode visited)
	{
		if (args != null && visited.equals(condition))
		{
			// check against method args
			String name = visited.getName();
			if (args.contains(name))
			{
				problem = true;
			}
		}
		return super.visitLocalVarNode(visited);
	}
}
