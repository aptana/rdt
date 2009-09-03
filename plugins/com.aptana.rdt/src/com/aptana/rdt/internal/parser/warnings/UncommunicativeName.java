package com.aptana.rdt.internal.parser.warnings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.IProblem;

public class UncommunicativeName extends RubyLintVisitor
{

	private static List<Pattern> reject = new ArrayList<Pattern>();
	private static List<String> accept = new ArrayList<String>();
	static
	{
		reject.add(Pattern.compile("^.[0-9]*$"));
		accept.add("Inline::C");
	}

	private List<Node> scopes; // stack of nodes that represent scopes for variables
	private Map<Node, Map<String, Node>> scopedVariables; // Map from scopes to the variable names and nodes in them

	public UncommunicativeName(String src)
	{
		super(AptanaRDTPlugin.getDefault().getOptions(), src);
	}

	@Override
	protected String getOptionKey()
	{
		return AptanaRDTPlugin.COMPILER_PB_UNCOMMUNICATIVE_NAME;
	}

	@Override
	protected int getProblemID()
	{
		return IProblem.UncommunicativeName;
	}

	@Override
	public Object visitRootNode(RootNode visited)
	{
		// init
		scopes = new ArrayList<Node>();
		scopedVariables = new HashMap<Node, Map<String, Node>>();

		pushScope(visited);
		return super.visitRootNode(visited);
	}

	@Override
	public void exitRootNode(RootNode visited)
	{
		popScope();
		// clean up
		scopes = null;
		scopedVariables = null;
		super.exitRootNode(visited);
	}

	private void popScope()
	{
		Node scopingNode = scopes.remove(0);
		Map<String, Node> varsInScope = scopedVariables.get(scopingNode);
		for (Map.Entry<String, Node> entry : varsInScope.entrySet())
		{
			considerVariable(entry.getKey(), entry.getValue());
		}
	}

	private void pushScope(Node visited)
	{
		scopes.add(visited);
		scopedVariables.put(visited, new HashMap<String, Node>());
	}

	@Override
	public Object visitModuleNode(ModuleNode visited)
	{
		considerName(visited);
		pushScope(visited);
		return super.visitModuleNode(visited);
	}

	@Override
	public Object visitClassNode(ClassNode visited)
	{
		considerName(visited);
		pushScope(visited);
		return super.visitClassNode(visited);
	}

	@Override
	public Object visitDefnNode(DefnNode visited)
	{
		considerName(visited);
		return super.visitDefnNode(visited);
	}

	@Override
	public Object visitDefsNode(DefsNode visited)
	{
		considerName(visited);
		return super.visitDefsNode(visited);
	}

	private void considerName(Node node)
	{
		String fullName = fullyQualifiedName(node);
		if (accept.contains(fullName))
			return;
		String shortName = ASTUtil.getNameReflectively(node);
		if (isBadName(shortName))
		{
			createProblem(node.getPosition(), "Uncommunicative name: " + fullName);
		}
	}

	private String fullyQualifiedName(Node node)
	{
		if (node instanceof MethodDefNode)
			return ((MethodDefNode) node).getName();
		return ASTUtil.getFullyQualifiedTypeName(getRootNode(), node);
	}

	private Node getRootNode()
	{
		if (scopes == null || scopes.isEmpty())
			return null;
		return scopes.get(0);
	}

	private boolean isBadName(String var)
	{
		if (var.equals("*") || accept.contains(var))
			return false;
		for (Pattern p : reject)
		{
			if (p.matcher(var).find())
				return true;
		}
		return false;
	}

	@Override
	public Object visitLocalAsgnNode(LocalAsgnNode visited)
	{
		addVariable(visited.getName(), visited);
		return super.visitLocalAsgnNode(visited);
	}

	@Override
	public Object visitConstDeclNode(ConstDeclNode visited)
	{
		addVariable(visited.getName(), visited);
		return super.visitConstDeclNode(visited);
	}

	@Override
	public Object visitClassVarDeclNode(ClassVarDeclNode visited)
	{
		addVariable(visited.getName(), visited);
		return super.visitClassVarDeclNode(visited);
	}

	@Override
	public Object visitClassVarAsgnNode(ClassVarAsgnNode visited)
	{
		addVariable(visited.getName(), visited);
		return super.visitClassVarAsgnNode(visited);
	}

	private void addVariable(String name, Node visited)
	{
		Node enclosingScope = null;
		// FIXME Handle pushing instance, constants and class vars up to type level scope!
		if (visited instanceof ClassVarNode || visited instanceof ClassVarDeclNode
				|| visited instanceof ClassVarAsgnNode || visited instanceof InstVarNode
				|| visited instanceof InstAsgnNode || visited instanceof ConstDeclNode)
		{
			for (int i = scopes.size() - 1; i >= 0; i--)
			{
				enclosingScope = scopes.get(i);
				if (enclosingScope instanceof ClassNode || enclosingScope instanceof RootNode
						|| enclosingScope instanceof ModuleNode)
					break;
			}
		}
		else
		{
			enclosingScope = scopes.get(scopes.size() - 1);
		}
		Map<String, Node> vars = scopedVariables.get(enclosingScope);
		if (vars == null)
		{
			vars = new HashMap<String, Node>();
			scopedVariables.put(enclosingScope, vars);
		}
		if (vars.containsKey(name))
			return;
		vars.put(name, visited);
	}

	@Override
	public Object visitInstAsgnNode(InstAsgnNode visited)
	{
		addVariable(visited.getName(), visited);
		return super.visitInstAsgnNode(visited);
	}

	@Override
	public Object visitDVarNode(DVarNode visited)
	{
		considerVariable(visited.getName(), visited);
		return super.visitDVarNode(visited);
	}

	private void considerVariable(String name, Node node)
	{
		name = effectiveName(name);
		if (isBadName(name))
		{
			createProblem(node.getPosition(), "Variable has an uncommunicative name: " + name);
		}
	}

	@Override
	public void exitClassNode(ClassNode visited)
	{
		popScope();
		super.exitClassNode(visited);
	}

	private String effectiveName(String name)
	{
		if (name.startsWith("@@"))
			return name.substring(2);
		if (name.startsWith("@"))
			return name.substring(1);
		return name;
	}

}
