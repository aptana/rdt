package org.rubypeople.rdt.internal.core.search;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyElementDelta;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.RubyModel;
import org.rubypeople.rdt.internal.core.RubyModelManager;
import org.rubypeople.rdt.internal.core.RubyProject;
import org.rubypeople.rdt.internal.core.util.Util;

public class RubyWorkspaceScope extends RubySearchScope
{

	private IPath[] enclosingPaths = null;

	public RubyWorkspaceScope()
	{
		// As nothing is stored in the RubyWorkspaceScope now, no initialization is longer needed
	}

	public boolean encloses(IRubyElement element)
	{
		/*
		 * A workspace scope encloses all ruby elements (this assumes that the index selector and thus
		 * enclosingProjectAndJars() returns indexes on the loadpath only and that these indexes are consistent.) NOTE:
		 * Returning true gains 20% of a hierarchy build on Object
		 */
		return true;
	}

	public boolean encloses(String resourcePathString)
	{
		/*
		 * A workspace scope encloses all resources (this assumes that the index selector and thus
		 * enclosingProjectAndJars() returns indexes on the loadpath only and that these indexes are consistent.) NOTE:
		 * Returning true gains 20% of a hierarchy build on Object
		 */
		return true;
	}

	/*
	 * (non-Rubydoc)
	 * @see org.rubypeople.rdt.core.search.IRubySearchScope#enclosingProjectsAndJars()
	 */
	public IPath[] enclosingProjectsAndJars()
	{
		IPath[] result = this.enclosingPaths;
		if (result != null)
		{
			return result;
		}
		long start = BasicSearchEngine.VERBOSE ? System.currentTimeMillis() : -1;
		try
		{
			IRubyProject[] projects = RubyModelManager.getRubyModelManager().getRubyModel().getRubyProjects();
			Set<IPath> paths = new HashSet<IPath>(projects.length * 2);
			for (int i = 0, length = projects.length; i < length; i++)
			{
				RubyProject rubyProject = (RubyProject) projects[i];

				// Add project full path
				IPath projectPath = rubyProject.getProject().getFullPath();
				paths.add(projectPath);

				// Add project libraries paths
				ILoadpathEntry[] entries = rubyProject.getResolvedLoadpath(true);
				for (int j = 0, eLength = entries.length; j < eLength; j++)
				{
					ILoadpathEntry entry = entries[j];
					if (entry.getEntryKind() == ILoadpathEntry.CPE_LIBRARY)
					{
						IPath path = entry.getPath();
						Object target = RubyModel.getTarget(path, false/* don't check existence */);
						if (target instanceof IFolder) // case of an external folder
							path = ((IFolder) target).getFullPath();
						paths.add(entry.getPath());
					}
				}
			}
			result = new IPath[paths.size()];
			paths.toArray(result);
			return this.enclosingPaths = result;
		}
		catch (RubyModelException e)
		{
			Util.log(e, "Exception while computing workspace scope's enclosing projects and jars"); //$NON-NLS-1$
			return new IPath[0];
		}
		finally
		{
			if (BasicSearchEngine.VERBOSE)
			{
				long time = System.currentTimeMillis() - start;
				int length = result == null ? 0 : result.length;
				Util
						.verbose("RubyWorkspaceScope.enclosingProjectsAndJars: " + length + " paths computed in " + time + "ms."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}

	public boolean equals(Object o)
	{
		return o == this; // use the singleton pattern
	}

	public int hashCode()
	{
		return RubyWorkspaceScope.class.hashCode();
	}

	public void processDelta(IRubyElementDelta delta, int eventType)
	{
		if (this.enclosingPaths == null)
			return;
		IRubyElement element = delta.getElement();
		switch (element.getElementType())
		{
			case IRubyElement.RUBY_MODEL:
				IRubyElementDelta[] children = delta.getAffectedChildren();
				for (int i = 0, length = children.length; i < length; i++)
				{
					IRubyElementDelta child = children[i];
					this.processDelta(child, eventType);
				}
				break;
			case IRubyElement.RUBY_PROJECT:
				int kind = delta.getKind();
				switch (kind)
				{
					case IRubyElementDelta.ADDED:
					case IRubyElementDelta.REMOVED:
						this.enclosingPaths = null;
						break;
					case IRubyElementDelta.CHANGED:
						int flags = delta.getFlags();
						if ((flags & IRubyElementDelta.F_CLOSED) != 0 || (flags & IRubyElementDelta.F_OPENED) != 0)
						{
							this.enclosingPaths = null;
						}
						else
						{
							children = delta.getAffectedChildren();
							for (int i = 0, length = children.length; i < length; i++)
							{
								IRubyElementDelta child = children[i];
								this.processDelta(child, eventType);
							}
						}
						break;
				}
				break;
			case IRubyElement.SOURCE_FOLDER_ROOT:
				kind = delta.getKind();
				switch (kind)
				{
					case IRubyElementDelta.ADDED:
					case IRubyElementDelta.REMOVED:
						this.enclosingPaths = null;
						break;
					case IRubyElementDelta.CHANGED:
						int flags = delta.getFlags();
						if ((flags & IRubyElementDelta.F_ADDED_TO_CLASSPATH) > 0
								|| (flags & IRubyElementDelta.F_REMOVED_FROM_CLASSPATH) > 0)
						{
							this.enclosingPaths = null;
						}
						break;
				}
				break;
		}
	}

	public String toString()
	{
		StringBuffer result = new StringBuffer("RubyWorkspaceScope on "); //$NON-NLS-1$
		IPath[] paths = enclosingProjectsAndJars();
		int length = paths == null ? 0 : paths.length;
		if (length == 0)
		{
			result.append("[empty scope]"); //$NON-NLS-1$
		}
		else
		{
			result.append("["); //$NON-NLS-1$
			for (int i = 0; i < length; i++)
			{
				result.append("\n\t"); //$NON-NLS-1$
				result.append(paths[i]);
			}
			result.append("\n]"); //$NON-NLS-1$
		}
		return result.toString();
	}
}
