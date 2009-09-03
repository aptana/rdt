package com.aptana.rdt.internal.parser.warnings;

import java.util.List;
import java.util.Map;

import org.jruby.ast.CaseNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.Node;
import org.jruby.ast.WhenNode;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;

public class TooManyBranchesVisitor extends RubyLintVisitor {

	private int maxBranches;
	private int branchCount;

	public TooManyBranchesVisitor(String contents) {
		this(AptanaRDTPlugin.getDefault().getOptions(), contents);		
	}
	
	public TooManyBranchesVisitor(Map<String, String> options, String contents) {
		super(options, contents);
		maxBranches = getInt(AptanaRDTPlugin.COMPILER_PB_MAX_BRANCHES, 5); 
		branchCount = 0;
	}
	private int getInt(String key, int defaultValue) {
		try {
			return Integer.parseInt((String) fOptions.get(key));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	@Override
	protected String getOptionKey() {
		return AptanaRDTPlugin.COMPILER_PB_CODE_COMPLEXITY_BRANCHES;
	}

	@Override
	public Object visitDefsNode(DefsNode iVisited) {
		branchCount = 0;
		return super.visitDefsNode(iVisited);
	}
	
	@Override
	public Object visitDefnNode(DefnNode iVisited) {
		branchCount = 0;
		return super.visitDefnNode(iVisited);
	}

	@Override
	public Object visitIfNode(IfNode iVisited) {
		// TODO Make sure this doesn't count modifiers
		if (iVisited.getThenBody() != null) {
			branchCount++;
		}
		if (iVisited.getElseBody() != null) {
			branchCount++;
		}
		return super.visitIfNode(iVisited);
	}

	@Override
	public Object visitCaseNode(CaseNode iVisited) {
		List<Node> list = iVisited.getCases().childNodes();
		WhenNode when = (WhenNode) list.get(0);
		while (when != null) {
			branchCount++;
			Node thing = when.getNextCase();
			if (thing instanceof WhenNode) {
				when = (WhenNode) when.getNextCase();
			} else {
				when = null;
			}			
		}
		return super.visitCaseNode(iVisited);
	}

	public void exitDefnNode(DefnNode iVisited) {
		if (branchCount > maxBranches) {
			createProblem(iVisited.getNameNode().getPosition(), "Too many branches: " + branchCount);
		}
		branchCount = 0;
	}
	
	@Override
	public void exitDefsNode(DefsNode iVisited) {
		if (branchCount > maxBranches) {
			createProblem(iVisited.getNameNode().getPosition(), "Too many branches: " + branchCount);
		}
		branchCount = 0;
		super.exitDefsNode(iVisited);
	}
}
