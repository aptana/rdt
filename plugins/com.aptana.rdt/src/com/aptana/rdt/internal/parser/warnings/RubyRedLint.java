package com.aptana.rdt.internal.parser.warnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.IRubyModelMarker;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.core.compiler.ReconcileContext;
import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;

import com.aptana.rdt.AptanaRDTPlugin;

public class RubyRedLint extends CompilationParticipant
{

	@Override
	public void reconcile(ReconcileContext context)
	{
		try
		{
			List<CategorizedProblem> problems = handleFile(context.getWorkingCopy().getElementName(), context
					.getWorkingCopy().getSource(), context.getAST());
			addProblems(context, IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER, problems);
		}
		catch (RubyModelException e)
		{
			AptanaRDTPlugin.log(e);
		}
	}

	private List<CategorizedProblem> handleFile(String name, String contents, Node ast)
	{
		if (ast == null)
			return Collections.emptyList();

		List<RubyLintVisitor> visitors = createLintVisitors(contents);
		List<CategorizedProblem> problems = new ArrayList<CategorizedProblem>();
		for (RubyLintVisitor visitor : visitors)
		{
			ast.accept(visitor);
			problems.addAll(visitor.getProblems());
		}
		return problems;
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, files.length);
		for (BuildContext context : files)
		{
			sub.subTask("Parsing and analyzing " + context.getFile().getLocation().toPortableString());
			String contents = new String(context.getContents());
			List<CategorizedProblem> problems = handleFile(context.getFile().getName(), contents, context.getAST());
			context.recordNewProblems(problems.toArray(new CategorizedProblem[problems.size()]));
			sub.worked(1);
		}
		sub.done();
	}

	@Override
	public boolean isActive(IRubyProject project)
	{
		return true;
	}

	private List<RubyLintVisitor> createLintVisitors(String contents)
	{
		List<RubyLintVisitor> visitors = new ArrayList<RubyLintVisitor>();
		visitors.add(new AccidentalBooleanAssignmentVisitor(contents));
		visitors.add(new UnusedPrivateMethodVisitor(contents));
		visitors.add(new MisspelledConstructorVisitor(contents));
		visitors.add(new LocalsMaskingMethodsVisitor(contents));
		visitors.add(new UnusedParameterVisitor(contents));
		visitors.add(new UnecessaryElseVisitor(contents));
		visitors.add(new TooManyLocalsVisitor(contents));
		visitors.add(new TooManyLinesVisitor(contents));
		visitors.add(new TooManyBranchesVisitor(contents));
		visitors.add(new TooManyArgumentsVisitor(contents));
		visitors.add(new TooManyReturnsVisitor(contents));
		visitors.add(new SimilarVariableNameVisitor(contents));
		visitors.add(new SubclassCallsSuper(contents));
		visitors.add(new ComparableInclusionVisitor(contents));
		visitors.add(new EnumerableInclusionVisitor(contents));
		visitors.add(new AndOrUsedOnRighthandAssignment(contents));
		visitors.add(new ConstantNamingConvention(contents));
		visitors.add(new MethodMissingWithoutRespondTo(contents));
		visitors.add(new DynamicVariableAliasesLocal(contents));
		visitors.add(new LocalVariablePossibleAttributeAccess(contents));
		visitors.add(new LocalAndMethodNamingConvention(contents));
		visitors.add(new UnusedLocalVariable(contents));
		visitors.add(new RequireGemChecker(contents));
		visitors.add(new RetryOutsideRescueBodyChecker(contents));
		visitors.add(new DuplicateHashKeyVisitor(contents));
		visitors.add(new ControlCouple(contents));
		visitors.add(new FeatureEnvy(contents));
		List<RubyLintVisitor> filtered = new ArrayList<RubyLintVisitor>();
		for (RubyLintVisitor visitor : visitors)
		{
			if (visitor.isIgnored())
				continue;
			filtered.add(visitor);
		}
		return filtered;
	}
}
