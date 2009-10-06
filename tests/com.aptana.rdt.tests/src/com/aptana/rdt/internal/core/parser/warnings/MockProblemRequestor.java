package com.aptana.rdt.internal.core.parser.warnings;

import java.util.HashSet;
import java.util.Set;

import org.rubypeople.rdt.core.IProblemRequestor;
import org.rubypeople.rdt.core.compiler.IProblem;

public class MockProblemRequestor implements IProblemRequestor {

	private boolean active;
	private Set<IProblem> problems = new HashSet<IProblem>();

	public void acceptProblem(IProblem problem) {
		problems.add(problem);
	}

	public void beginReporting() {
		active = true;
	}

	public void endReporting() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}

	public int numberOfProblems() {
		return problems.size();
	}

	public IProblem getProblemAtLine(int i) {
		for (IProblem problem : problems) {
			if (problem.getSourceLineNumber() == i) return problem;
		}
		return null;
	}

}
