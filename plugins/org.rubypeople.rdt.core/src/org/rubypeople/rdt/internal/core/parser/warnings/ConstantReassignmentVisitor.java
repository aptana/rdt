package org.rubypeople.rdt.internal.core.parser.warnings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jruby.ast.ClassNode;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ModuleNode;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

public class ConstantReassignmentVisitor extends RubyLintVisitor
{

	private Set<String> assignedConstants;
	private List<String> namespace;

	public ConstantReassignmentVisitor(String contents)
	{
		super(contents);
		assignedConstants = new HashSet<String>();
		namespace = new ArrayList<String>();
	}

	@Override
	protected String getOptionKey()
	{
		return RubyCore.COMPILER_PB_CONSTANT_REASSIGNMENT;
	}

	public Object visitConstDeclNode(ConstDeclNode iVisited)
	{
		String name;
		if (iVisited.getConstNode() != null)
		{
			name = ASTUtil.getFullyQualifiedName(iVisited.getConstNode());
		}
		else
		{
			name = getNamespace() + iVisited.getName();
		}
		if (assignedConstants.contains(name))
			createProblem(iVisited.getPosition(), "Reassignment of a constant");
		else
			assignedConstants.add(name);
		return super.visitConstDeclNode(iVisited);
	}

	@Override
	public Object visitClassNode(ClassNode visited)
	{
		namespace.add(ASTUtil.getFullyQualifiedName(visited.getCPath()));
		Object ret = super.visitClassNode(visited);
		namespace.remove(namespace.size() - 1);
		return ret;
	}

	@Override
	public Object visitModuleNode(ModuleNode visited)
	{
		namespace.add(ASTUtil.getFullyQualifiedName(visited.getCPath()));
		Object ret = super.visitModuleNode(visited);
		namespace.remove(namespace.size() - 1);
		return ret;
	}

	private String getNamespace()
	{
		StringBuilder builder = new StringBuilder();
		for (String portion : namespace)
		{
			builder.append(portion).append("::");
		}
		return builder.toString();
	}

}
