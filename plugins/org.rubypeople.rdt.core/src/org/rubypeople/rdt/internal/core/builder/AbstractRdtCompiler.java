package org.rubypeople.rdt.internal.core.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.internal.core.RubyModelManager;

public abstract class AbstractRdtCompiler
{

	protected final IProject project;
	protected final IMarkerManager markerManager;
	protected CompilationParticipant[] fParticipants;
	private IRubyProject fRubyProject;

	public AbstractRdtCompiler(IProject project, IMarkerManager markerManager)
	{
		this.project = project;
		this.markerManager = markerManager;
		this.fRubyProject = getRubyProject();
		fParticipants = RubyModelManager.getRubyModelManager().compilationParticipants
				.getCompilationParticipants(fRubyProject);
	}

	protected abstract void removeMarkers(IMarkerManager markerManager, IProgressMonitor monitor);

	public void compile(IProgressMonitor monitor) throws CoreException
	{
		SubMonitor sub = SubMonitor.convert(monitor, "Building " + project.getName() + "...", 100);
		notifyParticipants(sub.newChild(5));
		// traverse the delta
		BuildContext[] files = getBuildContexts();
		sub.worked(5);

		int workUnitsPerTask = 90 / (fParticipants.length + 1);

		if (monitor.isCanceled())
			throw new OperationCanceledException();

		removeMarkers(markerManager, sub.newChild(workUnitsPerTask));

		if (sub.isCanceled())
			throw new OperationCanceledException();

		compileFiles(files, sub.newChild(workUnitsPerTask * fParticipants.length));
		monitor.done();
	}

	private void notifyParticipants(IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, "Notifying build participants", fParticipants.length);
		for (int i = 0; i < fParticipants.length; i++)
		{
			fParticipants[i].aboutToBuild(fRubyProject);
			sub.worked(1);
		}
		sub.done();
	}

	private void compileFiles(BuildContext[] contexts, IProgressMonitor monitor) throws CoreException
	{
		SubMonitor sub = SubMonitor.convert(monitor, "Analyzing Files...", fParticipants.length + contexts.length);
		if (fParticipants != null)
		{
			for (int i = 0; i < fParticipants.length; i++)
			{
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				try
				{
					long start = System.currentTimeMillis();
					fParticipants[i].buildStarting(contexts, true, sub.newChild(1));
					if (RubyBuilder.DEBUG)
						System.out.println(fParticipants[i].getClass().getSimpleName() + " took "
								+ (System.currentTimeMillis() - start) + "ms");
				}
				catch (Exception e)
				{
					RubyCore.log(e);
				}
			}
		}
		for (int i = 0; i < contexts.length; i++)
		{
			CategorizedProblem[] problems = contexts[i].getProblems();
			if (problems == null || problems.length == 0)
			{
				sub.worked(1);
				continue;
			}
			for (int j = 0; j < problems.length; j++)
			{
				markerManager.addProblem(contexts[i].getFile(), problems[j]);
			}
			sub.worked(1);
		}
		sub.done();
	}

	abstract protected BuildContext[] getBuildContexts() throws CoreException;

	private IRubyProject getRubyProject()
	{
		return RubyCore.create(project);
	}

	public void cleanStarting()
	{
		for (int i = 0; i < fParticipants.length; i++)
		{
			fParticipants[i].cleanStarting(fRubyProject);
		}
	}
}
