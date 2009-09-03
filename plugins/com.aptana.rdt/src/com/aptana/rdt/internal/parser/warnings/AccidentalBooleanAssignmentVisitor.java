package com.aptana.rdt.internal.parser.warnings;

import java.util.HashSet;
import java.util.Set;

import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.WhenNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.IProblem;

public class AccidentalBooleanAssignmentVisitor extends RubyLintVisitor
{

	private Set<String> locals = new HashSet<String>();

	public AccidentalBooleanAssignmentVisitor(String contents)
	{
		super(AptanaRDTPlugin.getDefault().getOptions(), contents);
	}

	@Override
	protected String getOptionKey()
	{
		return AptanaRDTPlugin.COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT;
	}

	@Override
	public Object visitLocalAsgnNode(LocalAsgnNode iVisited)
	{
		locals.add(iVisited.getName());
		return super.visitLocalAsgnNode(iVisited);
	}

	@Override
	public Object visitDefnNode(DefnNode iVisited)
	{
		locals.clear();
		return super.visitDefnNode(iVisited);
	}

	@Override
	public Object visitDefsNode(DefsNode iVisited)
	{
		locals.clear();
		return super.visitDefsNode(iVisited);
	}

	public Object visitIfNode(IfNode iVisited)
	{
		Node condition = iVisited.getCondition();
		checkCondition(condition);
		return super.visitIfNode(iVisited);
	}

	private void checkCondition(Node condition)
	{
		if (containsAssignment(condition))
		{
			// Only create a problem if we've seen the local before (new locals assigned in an if are bad, but very
			// common. If we've seen it before, this is bad news!)
			if (condition instanceof LocalAsgnNode)
			{
				LocalAsgnNode assign = (LocalAsgnNode) condition;
				if (!locals.contains(assign.getName()))
					return;
			}
			ISourcePosition original = condition.getPosition();
			IDESourcePosition position = new IDESourcePosition(original.getFile(), original.getStartLine(), original
					.getEndLine(), original.getStartOffset(), original.getEndOffset() - 1);
			createProblem(position, "Possible accidental boolean assignment");
		}
	}

	private boolean containsAssignment(Node condition)
	{
		// FIXME Dive into this node branch recursively and see if it contains any assignment nodes
		return condition instanceof LocalAsgnNode || condition instanceof GlobalAsgnNode
				|| condition instanceof InstAsgnNode || condition instanceof ClassVarAsgnNode
				|| condition instanceof ConstDeclNode;
	}

	@Override
	public Object visitWhenNode(WhenNode visited)
	{
		Node expressions = visited.getExpressionNodes();
		if (expressions instanceof ListNode)
		{
			ListNode list = (ListNode) expressions;
			for (Node expression : list.childNodes())
			{
				checkCondition(expression);
			}
		}
		else
		{
			checkCondition(expressions);
		}
		return super.visitWhenNode(visited);
	}

	@Override
	protected int getProblemID()
	{
		return IProblem.PossibleAccidentalBooleanAssignment;
	}

}
