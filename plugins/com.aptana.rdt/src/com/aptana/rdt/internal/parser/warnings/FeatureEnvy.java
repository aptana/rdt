package com.aptana.rdt.internal.parser.warnings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.ast.CallNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.SelfNode;
import org.jruby.ast.VCallNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.IProblem;

public class FeatureEnvy extends RubyLintVisitor
{

	private static final int DEFAULT_MIN_REFERENCES_FOR_REPORT = 2;
	private static final String SELF = "self";

	private HashMap<String, List<ISourcePosition>> references = new HashMap<String, List<ISourcePosition>>();
	private boolean recordReferences = false;
	private int minReferences;

	public FeatureEnvy(String src)
	{
		this(AptanaRDTPlugin.getDefault().getOptions(), src);
	}

	public FeatureEnvy(Map<String, String> options, String src)
	{
		super(options, src);
		minReferences = getInt(AptanaRDTPlugin.COMPILER_PB_MIN_REFERENCES_FOR_ENVY, DEFAULT_MIN_REFERENCES_FOR_REPORT);
	}

	private int getInt(String key, int defaultValue)
	{
		try
		{
			return Integer.parseInt((String) fOptions.get(key));
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	@Override
	protected int getProblemID()
	{
		return IProblem.FeatureEnvy;
	}

	@Override
	protected String getOptionKey()
	{
		return AptanaRDTPlugin.COMPILER_PB_FEATURE_ENVY;
	}

	@Override
	public Object visitDefnNode(DefnNode visited)
	{
		enterMethod();
		return super.visitDefnNode(visited);
	}

	private void enterMethod()
	{
		recordReferences = true;
	}

	@Override
	public Object visitDefsNode(DefsNode visited)
	{
		enterMethod();
		return super.visitDefsNode(visited);
	}

	@Override
	public void exitDefnNode(DefnNode visited)
	{
		exitMethod();
		super.exitDefnNode(visited);
	}

	@Override
	public void exitDefsNode(DefsNode visited)
	{
		exitMethod();
		super.exitDefsNode(visited);
	}

	private void exitMethod()
	{
		List<ISourcePosition> enviousReferences = getEnviousReferences();
		for (ISourcePosition pos : enviousReferences)
		{
			createProblem(pos,
					"Feature envy: More method calls made to object than self. Consider moving method to object.");
		}
		recordReferences = false;
		references.clear();
	}

	private List<ISourcePosition> getEnviousReferences()
	{
		if (references.isEmpty()) // no references
			return Collections.emptyList();
		if (references.size() == 1 && references.containsKey(SELF)) // only references to self
			return Collections.emptyList();
		Collection<List<ISourcePosition>> listOfPositions = references.values();
		int max = Math.max(0, minReferences);
		for (List<ISourcePosition> list : listOfPositions)
		{
			if (list.size() > max)
				max = list.size();
		}
		List<ISourcePosition> envious = new ArrayList<ISourcePosition>();
		for (Map.Entry<String, List<ISourcePosition>> entry : references.entrySet())
		{
			if (entry.getValue().size() == max)
			{
				if (entry.getKey().equals(SELF))
				{
					envious.clear();
					break;
				}
				else
				{
					envious.add(entry.getValue().get(0));
				}
			}
		}
		return envious;
	}

	@Override
	public Object visitCallNode(CallNode visited)
	{
		if (!visited.getName().equals("new"))
		{
			Node receiver = visited.getReceiverNode();
			if (receiver instanceof SelfNode)
			{
				recordReference(SELF, receiver.getPosition());
			}
			else if (receiver instanceof DVarNode)
			{
				return super.visitCallNode(visited); // don't count calls made on dynamic vars because blocks are
				// special!
			}
			// else if (receiver instanceof ConstNode)
			// {
			// // Calling a method on a constant usually means we're calling a type singleton method
			// }
			else
			{
				String expr = ASTUtil.getNameReflectively(receiver);
				if (expr == null)
					expr = ASTUtil.stringRepresentation(receiver);
				recordReference(expr, receiver.getPosition());
			}
		}
		return super.visitCallNode(visited);
	}

	@Override
	public Object visitFCallNode(FCallNode visited)
	{
		recordReference(SELF, visited.getPosition());
		return super.visitFCallNode(visited);
	}

	@Override
	public Object visitVCallNode(VCallNode visited)
	{
		recordReference(SELF, visited.getPosition());
		return super.visitVCallNode(visited);
	}

	private void recordReference(String name, ISourcePosition pos)
	{
		if (!recordReferences)
			return;
		List<ISourcePosition> value = references.get(name);
		if (value == null)
			value = new ArrayList<ISourcePosition>();
		value.add(pos);
		references.put(name, value);
	}

}
