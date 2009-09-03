package org.rubypeople.rdt.internal.core.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.IRubyModelMarker;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.core.compiler.ReconcileContext;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.ConstantReassignmentVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.CoreClassReOpening;
import org.rubypeople.rdt.internal.core.parser.warnings.EmptyStatementVisitor;
import org.rubypeople.rdt.internal.core.parser.warnings.Ruby19HashCommaSyntax;
import org.rubypeople.rdt.internal.core.parser.warnings.Ruby19WhenStatements;

public class RubyCodeAnalyzer extends CompilationParticipant
{

	private Map<String, Long> timings;

	@Override
	public boolean isActive(IRubyProject project)
	{
		return true;
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch, IProgressMonitor monitor)
	{
		timings = new HashMap<String, Long>();
		SubMonitor sub = SubMonitor.convert(monitor, files.length);
		for (BuildContext context : files)
		{
			sub.subTask("Parsing and analyzing " + context.getFile().getLocation().toPortableString());
			String contents = new String(context.getContents());
			IRubyScript script = RubyCore.create(context.getFile());
			long start = System.currentTimeMillis();
			Node ast = context.getAST();
			addTiming("AST Generation", System.currentTimeMillis() - start);
			List<CategorizedProblem> problems = parse(script, contents, ast);
			context.recordNewProblems(problems.toArray(new CategorizedProblem[problems.size()]));
			sub.worked(1);
		}
		if (RubyBuilder.DEBUG)
		{
			for (Map.Entry<String, Long> timing : timings.entrySet())
			{
				System.out.println(timing.getKey() + " took " + timing.getValue() + "ms");
			}
			timings.clear();
		}
		sub.done();
	}

	private List<CategorizedProblem> parse(IRubyScript script, String contents, Node ast)
	{
		if (ast == null)
			return Collections.emptyList();
		List<CategorizedProblem> problems = new ArrayList<CategorizedProblem>();
		long lintVTime = System.currentTimeMillis();
		List<RubyLintVisitor> visitors = getLintVisitors(script, contents);
		addTiming("RubyLintVisitor creation", System.currentTimeMillis() - lintVTime);
		for (RubyLintVisitor rubyLintVisitor : visitors)
		{
			long start = System.currentTimeMillis();
			rubyLintVisitor.acceptNode(ast);
			if (RubyBuilder.DEBUG)
			{
				addTiming(rubyLintVisitor.getClass().getSimpleName(), System.currentTimeMillis() - start);
			}
			problems.addAll(rubyLintVisitor.getProblems());
		}
		return problems;
	}

	private void addTiming(String simpleName, long length)
	{
		if (timings == null)
			timings = new HashMap<String, Long>();
		Long existingValue = timings.get(simpleName);
		if (existingValue == null)
			existingValue = 0L;
		timings.put(simpleName, existingValue + length);
	}

	private List<RubyLintVisitor> getLintVisitors(IRubyScript script, String contents)
	{
		List<RubyLintVisitor> visitors = new ArrayList<RubyLintVisitor>();
		visitors.add(new EmptyStatementVisitor(contents));
		visitors.add(new ConstantReassignmentVisitor(contents));
		if (script != null)
		{
			visitors.add(new CoreClassReOpening(script, contents));
		}
		visitors.add(new Ruby19WhenStatements(contents));
		visitors.add(new Ruby19HashCommaSyntax(contents));
		List<RubyLintVisitor> filtered = new ArrayList<RubyLintVisitor>();
		for (RubyLintVisitor visitor : visitors)
		{
			if (visitor.isIgnored())
				continue;
			filtered.add(visitor);
		}
		return filtered;
	}

	@Override
	public void reconcile(ReconcileContext context)
	{
		try
		{
			List<CategorizedProblem> problems = parse(context.getWorkingCopy(), context.getWorkingCopy().getSource(),
					context.getAST());
			addProblems(context, IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER, problems);
		}
		catch (RubyModelException e)
		{
			RubyCore.log(e);
		}
	}
}
