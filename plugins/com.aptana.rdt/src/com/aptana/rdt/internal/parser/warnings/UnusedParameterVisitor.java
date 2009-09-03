package com.aptana.rdt.internal.parser.warnings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.compiler.IProblem;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;

public class UnusedParameterVisitor extends RubyLintVisitor {

	private Map<String, Node> declared;
	private boolean inArgsNode = false;
	
	public UnusedParameterVisitor(String contents) {
		super(AptanaRDTPlugin.getDefault().getOptions(), contents);
		declared = new HashMap<String, Node>();
	}

	@Override
	protected String getOptionKey() {
		return AptanaRDTPlugin.COMPILER_PB_UNUSED_PARAMETER;
	}
	
	public Object visitDefnNode(DefnNode iVisited) {
		List<Node> args = getArgs(iVisited.getArgsNode());
		for (Node arg : args) {
			declared.put(ASTUtil.getNameReflectively(arg), arg);
		}
		return null;
	}
	
	@Override
	public Object visitArgsNode(ArgsNode visited) {
		inArgsNode = true;
		return super.visitArgsNode(visited);
	}
	
	@Override
	public void exitArgsNode(ArgsNode visited) {
		inArgsNode = false;
		super.exitArgsNode(visited);
	}
	
	@Override
	public Object visitLocalAsgnNode(LocalAsgnNode iVisited) {
		if (inArgsNode) {
			declared.put(iVisited.getName(), iVisited);
		}
		return super.visitLocalAsgnNode(iVisited);
	}
	
	public void exitDefnNode(DefnNode iVisited) {
		for (Node unused : declared.values()) {
			String name = ASTUtil.getNameReflectively(unused);
			ISourcePosition original = unused.getPosition();
			ISourcePosition pos = new IDESourcePosition(original.getFile(), original.getStartLine(), original.getEndLine(), original.getStartOffset(), original.getStartOffset() + name.length());
			createProblem(pos, "Unused Method parameter " + name);
		}
		declared.clear();
	}
	
	private void usedParameter(String name) {
		declared.remove(name);
	}

	private List<Node> getArgs(ArgsNode argsNode) {
		List<Node> arguments = new ArrayList<Node>();
		if (argsNode == null) return arguments;
		ArgsNode args = (ArgsNode) argsNode;
		ListNode argList = args.getPre();
		if (argList == null) return arguments;
		for (Iterator<Node> iter = argList.childNodes().iterator(); iter.hasNext();) {
			Node node = iter.next();
			arguments.add(node);
		}
		return arguments;
	}
	
	public Object visitLocalVarNode(LocalVarNode iVisited) {
		usedParameter(iVisited.getName());
		return super.visitLocalVarNode(iVisited);
	}

	@Override
	protected int getProblemID() {
		return IProblem.ArgumentIsNeverUsed;
	}
}
