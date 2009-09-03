package com.aptana.rdt.internal.parser.warnings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jruby.ast.ClassNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.IProblem;

public class LocalsMaskingMethodsVisitor extends RubyLintVisitor
{

	private Map<Node, Collection<LocalAsgnNode>> locals;
	private HashSet<String> methods;
	private List<Node> scopes;

	public LocalsMaskingMethodsVisitor(String contents)
	{
		super(AptanaRDTPlugin.getDefault().getOptions(), contents);
		init();
	}

	private void init()
	{
		locals = new HashMap<Node, Collection<LocalAsgnNode>>();
		methods = new HashSet<String>();
		scopes = new ArrayList<Node>();
	}

	@Override
	public Object visitRootNode(RootNode visited)
	{
		init();
		enterScope(visited);
		Object ret = super.visitRootNode(visited);
		exitScope();
		return ret;

	}

	private void enterScope(Node scopingNode)
	{
		scopes.add(scopingNode);
	}

	private void exitScope()
	{
		scopes.remove(scopes.size() - 1);
	}

	@Override
	protected String getOptionKey()
	{
		return AptanaRDTPlugin.COMPILER_PB_LOCAL_MASKS_METHOD;
	}

	@Override
	protected int getProblemID()
	{
		return IProblem.LocalMaskingMethod;
	}

	public Object visitClassNode(ClassNode iVisited)
	{
		methods.clear();
		locals.clear();
		enterScope(iVisited);
		Object ret = super.visitClassNode(iVisited);
		findMaskingLocals();
		exitScope();
		return ret;
	}

	@Override
	public Object visitFCallNode(FCallNode iVisited)
	{
		String name = iVisited.getName();
		if (name.equals("attr_accessor") || name.equals("attr"))
		{
			List<String> args = ASTUtil.getArgumentsFromFunctionCall(iVisited);
			if (name.equals("attr"))
			{
				methods.add(args.get(0));
			}
			else
				methods.addAll(convertSymbolsToStrings(args));
		}
		return super.visitFCallNode(iVisited);
	}

	private Collection<? extends String> convertSymbolsToStrings(List<String> args)
	{
		Collection<String> converted = new ArrayList<String>();
		for (String arg : args)
		{
			if (arg.startsWith(":"))
			{
				converted.add(arg.substring(1));
			}
			else
			{
				converted.add(arg);
			}
		}
		return converted;
	}

	public Object visitDefnNode(DefnNode iVisited)
	{
		methods.add(iVisited.getName());
		enterScope(iVisited);
		Object ret = super.visitDefnNode(iVisited);
		exitScope();
		return ret;
	}

	public Object visitLocalAsgnNode(LocalAsgnNode iVisited)
	{
		Collection<LocalAsgnNode> alreadyMarked = locals.get(currentScope());
		if (alreadyMarked == null)
			alreadyMarked = new ArrayList<LocalAsgnNode>();
		for (LocalAsgnNode localAsgnNode : alreadyMarked)
		{
			if (localAsgnNode.getName().equals(iVisited.getName()))
				return super.visitLocalAsgnNode(iVisited);
		}
		alreadyMarked.add(iVisited);
		locals.put(currentScope(), alreadyMarked);
		return super.visitLocalAsgnNode(iVisited);
	}

	private Node currentScope()
	{
		return scopes.get(scopes.size() - 1);
	}

	private void findMaskingLocals()
	{
		for (Collection<LocalAsgnNode> localAsgnNodes : locals.values())
		{
			for (LocalAsgnNode local : localAsgnNodes)
			{
				if (methods.contains(local.getName()))
				{
					createProblem(local.getPosition(), "Local variable hides method");
				}
			}
		}
	}
}
