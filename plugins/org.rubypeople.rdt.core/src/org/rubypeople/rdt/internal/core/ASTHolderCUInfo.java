package org.rubypeople.rdt.internal.core;

import java.util.HashMap;

import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;

public class ASTHolderCUInfo extends RubyScriptElementInfo {

	public HashMap<String, CategorizedProblem[]> problems;
	public RootNode ast;

}
