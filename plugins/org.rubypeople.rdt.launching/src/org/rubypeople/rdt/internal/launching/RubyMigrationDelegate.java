/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.launching;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

/**
 * Delegate for migrating Ruby launch configurations.
 * The migration process involves a resource mapping being created such that launch configurations
 * can be filtered from the launch configuration dialog based on resource availability
 * 
 * @since 3.2
 */
public class RubyMigrationDelegate implements ILaunchConfigurationMigrationDelegate {

	/**
	 * represents the empty string
	 */
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Constructor needed for reflection
	 */
	public RubyMigrationDelegate() {}
	
	/**
	 * Method to get the projects for the specified launch configuration that should be mapped to the launch configuration  
	 * 
	 * @param candidate the launch configuration that the projects will be mapped to.
	 * @return a list of the projects to be mapped or an empty list, never null.
	 * @throws CoreException 
	 */
	protected IProject[] getProjectsForCandidate(ILaunchConfiguration candidate) throws CoreException {
		String pname = candidate.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		return new IProject[] {ResourcesPlugin.getWorkspace().getRoot().getProject(pname)};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#isCandidate()
	 */
	public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException {
		if(candidate.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING).equals(EMPTY_STRING)) {
			return false;
		}
		IResource[] mappedResources = candidate.getMappedResources();
		if(mappedResources != null && mappedResources.length > 0) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationMigrationDelegate#migrate(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void migrate(ILaunchConfiguration candidate) throws CoreException {
		IProject[] projects = getProjectsForCandidate(candidate);
		ArrayList mappings = new ArrayList();
		for(int i = 0; i < projects.length; i++) {
			if(!mappings.contains(projects[i])) {
				mappings.add(projects[i]);
			}
		}
		ILaunchConfigurationWorkingCopy wc = candidate.getWorkingCopy();
		wc.setMappedResources((IResource[])mappings.toArray(new IResource[mappings.size()]));
		wc.doSave();
	}
	
	/**
	 * Updates the resource mapping for the given launch configuration.
	 * 
	 * @param wc working copy
	 * @throws CoreException if an exception occurs updating resource mapping.
	 */
	public static void updateResourceMapping(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		IResource resource = getResource(wc);
		IResource[] resources = null;
		if (resource != null) {
			resources = new IResource[]{resource};
		}
		wc.setMappedResources(resources);
	}
	
	/**
	 * Returns the associated <code>IResource</code> for the specified launch configuration
	 * or <code>null</code> if none.
	 * 
	 * @param candidate the candidate to get the backing resource for
	 * @return associated <code>IResource</code> or <code>null</code>
	 * 
	 * @since 3.3
	 * 
	 * @throws CoreException
	 */
	public static IResource getResource(ILaunchConfiguration candidate) throws CoreException {
		IResource resource = null;
		String pname = candidate.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		if(!EMPTY_STRING.equals(pname)) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pname);
			String tname = candidate.getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, EMPTY_STRING);
			if(!EMPTY_STRING.equals(tname)) {
				if(project != null && project.exists() && project.isOpen()) {
					resource = project.getFile(tname);
				}
			} else {
				return project;
			}
			if (resource == null) {
				resource = project;
			}
		}
		return resource;
	}

}
