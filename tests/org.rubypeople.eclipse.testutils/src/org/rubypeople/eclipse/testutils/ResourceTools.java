package org.rubypeople.eclipse.testutils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ResourceTools {
	public static IProject createProject(String name) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(name);
		if (!project.exists()) {
			IProjectDescription desc = workspace.newProjectDescription(project.getName());
			project.create(desc, null);
		}
		if (!project.isOpen())
			project.open(null);

		return project;
	}
}
