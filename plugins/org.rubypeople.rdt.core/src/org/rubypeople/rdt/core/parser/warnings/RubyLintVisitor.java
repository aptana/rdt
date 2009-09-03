package org.rubypeople.rdt.core.parser.warnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.RescueBodyNode;
import org.jruby.ast.RootNode;
import org.jruby.ast.SClassNode;
import org.jruby.ast.WhenNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.IProblem;
import org.rubypeople.rdt.internal.core.parser.Error;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.parser.Warning;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

public abstract class RubyLintVisitor extends InOrderVisitor
{

	private String contents;
	protected Map<String, String> fOptions;
	private List<CategorizedProblem> problems;

	public RubyLintVisitor(String contents)
	{
		this(RubyCore.getOptions(), contents);
	}

	public RubyLintVisitor(Map<String, String> options, String contents)
	{
		this.problems = new ArrayList<CategorizedProblem>();
		this.contents = contents;
		this.fOptions = options;
	}

	protected String getSource(Node node)
	{
		return ASTUtil.getSource(contents, node);
	}

	protected String getSource(int start, int end)
	{
		if (start > end)
		{
			int temp = end;
			end = start;
			start = temp;
		}
		if (contents.length() < end)
			end = contents.length();
		if (start < 0)
			start = 0;
		return contents.substring(start, end);
	}

	public List<CategorizedProblem> getProblems()
	{
		return problems;
	}

	public boolean isIgnored()
	{
		String value = getSeverity();
		if (value != null && value.equals(RubyCore.IGNORE))
			return true;
		return false;
	}

	protected void createProblem(ISourcePosition position, String message)
	{
		String value = getSeverity();
		if (value != null && value.equals(RubyCore.IGNORE))
			return;
		CategorizedProblem problem;
		if (value != null && value.equals(RubyCore.ERROR))
			problem = new Error(position, message, getProblemID());
		else
			problem = new Warning(position, message, getProblemID());
		problems.add(problem);
	}

	protected String getSeverity()
	{
		return (String) fOptions.get(getOptionKey());
	}

	@Override
	protected Object visitNode(Node iVisited)
	{
		return null;
	}

	/**
	 * The key used to store the error/warning severity option.
	 * 
	 * @return a String key
	 */
	abstract protected String getOptionKey();

	/**
	 * Meant to be overridden by classes needing to perform some action when a class definition was exited.
	 * 
	 * @param iVisited
	 */
	public void exitClassNode(ClassNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a method definition.
	 * 
	 * @param iVisited
	 */
	public void exitDefnNode(DefnNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a singleton method
	 * definition.
	 * 
	 * @param iVisited
	 */
	public void exitIfNode(IfNode iVisited)
	{
	}

	/**
	 * To be overriden by subclasses who need to run particular behavior/code when exiting a singleton method
	 * definition.
	 * 
	 * @param iVisited
	 */
	public void exitBlockNode(BlockNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a singleton method
	 * definition.
	 * 
	 * @param iVisited
	 */
	public void exitDefsNode(DefsNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a singleton method
	 * definition.
	 * 
	 * @param iVisited
	 */
	public void exitModuleNode(ModuleNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a singleton method
	 * definition.
	 * 
	 * @param iVisited
	 */
	public void exitWhenNode(WhenNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a singleton method
	 * definition.
	 * 
	 * @param iVisited
	 */
	public void exitSClassNode(SClassNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a method signature.
	 * 
	 * @param iVisited
	 */
	public void exitArgsNode(ArgsNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting rescue body.
	 * 
	 * @param iVisited
	 */
	public void exitRescueBodyNode(RescueBodyNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting a hash node.
	 * 
	 * @param iVisited
	 */
	public void exitHashNode(HashNode iVisited)
	{
	}

	/**
	 * To be overridden by subclasses who need to run particular behavior/code when exiting the AST.
	 * 
	 * @param iVisited
	 */
	public void exitRootNode(RootNode iVisited)
	{
	}

	protected int getProblemID()
	{
		return IProblem.Uncategorized;
	}

	@Override
	public Object visitArgsNode(ArgsNode iVisited)
	{
		Object ins = super.visitArgsNode(iVisited);
		exitArgsNode(iVisited);
		return ins;
	}

	@Override
	public Object visitBlockNode(BlockNode iVisited)
	{
		Object ins = super.visitBlockNode(iVisited);
		exitBlockNode(iVisited);
		return ins;
	}

	@Override
	public Object visitClassNode(ClassNode iVisited)
	{
		Object ins = super.visitClassNode(iVisited);
		exitClassNode(iVisited);
		return ins;
	}

	@Override
	public Object visitDefnNode(DefnNode iVisited)
	{
		Object ins = super.visitDefnNode(iVisited);
		exitDefnNode(iVisited);
		return ins;
	}

	@Override
	public Object visitDefsNode(DefsNode iVisited)
	{
		Object ins = super.visitDefsNode(iVisited);
		exitDefsNode(iVisited);
		return ins;
	}

	@Override
	public Object visitHashNode(HashNode iVisited)
	{
		Object ins = super.visitHashNode(iVisited);
		exitHashNode(iVisited);
		return ins;
	}

	@Override
	public Object visitIfNode(IfNode iVisited)
	{
		Object ins = super.visitIfNode(iVisited);
		exitIfNode(iVisited);
		return ins;
	}

	@Override
	public Object visitModuleNode(ModuleNode iVisited)
	{
		Object ins = super.visitModuleNode(iVisited);
		exitModuleNode(iVisited);
		return ins;
	}

	@Override
	public Object visitRescueBodyNode(RescueBodyNode iVisited)
	{
		Object ins = super.visitRescueBodyNode(iVisited);
		exitRescueBodyNode(iVisited);
		return ins;
	}

	@Override
	public Object visitRootNode(RootNode iVisited)
	{
		problems.clear();
		Object ret = super.visitRootNode(iVisited);
		exitRootNode(iVisited);
		return ret;
	}

	@Override
	public Object visitSClassNode(SClassNode iVisited)
	{
		Object ins = super.visitSClassNode(iVisited);
		exitSClassNode(iVisited);
		return ins;
	}

	@Override
	public Object visitWhenNode(WhenNode iVisited)
	{
		Object ins = super.visitWhenNode(iVisited);
		exitWhenNode(iVisited);
		return ins;
	}

}
