package com.aptana.rdt.core.rspec;

import java.util.ArrayList;
import java.util.List;

import org.jruby.ast.FCallNode;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

public class RSpecStructureCreator extends InOrderVisitor
{

	private List<Behavior> behaviors = new ArrayList<Behavior>();

	public Object visitFCallNode(FCallNode visited)
	{
		if (visited.getName().equals("describe"))
		{ // start of a behavior
			List<String> args = ASTUtil.getArgumentsFromFunctionCall(visited);
			String className = args.get(0);
			int start = visited.getPosition().getStartOffset();
			push(visited, className, start);
		}
		else if (visited.getName().equals("it"))
		{ // start of example for behavior
			List<String> args = ASTUtil.getArgumentsFromFunctionCall(visited);
			String description = args.get(0);
			if (description.startsWith("'") || description.startsWith("\""))
				description = description.substring(1);
			if (description.endsWith("'") || description.endsWith("\""))
				description = new String(description.substring(0, description.length() - 1));
			int start = visited.getPosition().getStartOffset();
			peek().addExample(new Example(description, start, visited.getPosition().getEndOffset() - start));
		}
		else if (visited.getName().equals("shared_examples_for"))
		{ // start of shared example 
			List<String> args = ASTUtil.getArgumentsFromFunctionCall(visited);
			String description = args.get(0);
			int start = visited.getPosition().getStartOffset();
			push(visited, description, start);
		}
		return super.visitFCallNode(visited);
	}

	private Behavior peek()
	{
		return behaviors.get(behaviors.size() - 1);
	}

	private void push(FCallNode visited, String className, int start)
	{
		behaviors.add(new Behavior(className, start, visited.getPosition().getEndOffset() - start));
	}

	public Object[] getBehaviors()
	{
		return behaviors.toArray(new Object[behaviors.size()]);
	}
}
