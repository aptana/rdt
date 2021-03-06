package org.rubypeople.rdt.internal.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.internal.core.util.Util;

public class IncrementalRdtCompiler extends AbstractRdtCompiler
{

	private List<BuildContext> contexts;
	private List<IFile> filesToClear;
	private final IResourceDelta rootDelta;

	public IncrementalRdtCompiler(IProject project, IResourceDelta delta, IMarkerManager markerManager)
	{
		super(project, markerManager);
		this.rootDelta = delta;
	}

	public IncrementalRdtCompiler(IProject project, IResourceDelta delta)
	{
		this(project, delta, new MarkerManager());
	}

	protected void removeMarkers(IMarkerManager markerManager, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, "Removing Markers...", fParticipants.length);
		for (IFile file : filesToClear)
		{
			markerManager.removeProblemsAndTasksFor(file);
			sub.worked(1);
		}
		sub.done();
	}

	private void analyzeFiles() throws CoreException
	{
		filesToClear = new ArrayList<IFile>();
		contexts = new ArrayList<BuildContext>();

		rootDelta.accept(new IResourceDeltaVisitor()
		{
			public boolean visit(IResourceDelta delta) throws CoreException
			{
				IResource resource = delta.getResource();
				if (isRubyFile(resource) || isERBFile(resource))
				{
					if (delta.getKind() == IResourceDelta.REMOVED)
					{
						filesToClear.add((IFile) resource);
					}
					else if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)
					{
						filesToClear.add((IFile) resource);
						if (isERBFile(resource))
						{
							contexts.add(new ERBBuildContext((IFile) resource));
						}
						else
						{
							contexts.add(new BuildContext((IFile) resource));
						}
					}
				}
				if (IResource.FOLDER == resource.getType())
				{
					// HACK Don't create problems in vendor folders of ruby projects
					if (resource.getProjectRelativePath().equals(new Path("vendor")))
					{
						return false;
					}
				}
				return true;
			}

			private boolean isERBFile(IResource resource)
			{
				if (!(resource instanceof IFile))
					return false;
				String name = resource.getName();
				return BuildContextCollector.isERB(name);
			}

			private boolean isRubyFile(IResource resource)
			{
				return resource instanceof IFile && Util.isRubyLikeFileName(resource.getName());
			}
		});
	}

	protected BuildContext[] getBuildContexts() throws CoreException
	{
		if (contexts == null)
		{
			analyzeFiles();
		}
		return contexts.toArray(new BuildContext[contexts.size()]);
	}

}
