/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.aptana.rdt.internal.rake;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

/**
 * @author mkent
 * @author cwilliams
 */
public class RakeTasksHelper implements IRakeHelper
{

	private Map<String, String> fCachedTasks;
	private IProject fLastProject;
	private static RakeTasksHelper fgInstance;

	private RakeTasksHelper()
	{
	}

	public static IRakeHelper getInstance()
	{
		if (fgInstance == null)
		{
			fgInstance = new RakeTasksHelper();
		}
		return fgInstance;
	}

	public void runRakeTask(IProject project, String task, String parameters, IProgressMonitor monitor)
	{
		if (monitor == null)
			monitor = new NullProgressMonitor();

		if (monitor.isCanceled())
			return;

		try
		{
			ILaunchConfiguration config = run(project, task, parameters);
			if (monitor.isCanceled())
				return;

			config.launch(ILaunchManager.RUN_MODE, monitor);
		}
		catch (CoreException e)
		{
			RakePlugin.log("Error running rake task", e);
		}
	}

	public ILaunchConfiguration run(IProject project, String task, String parameters)
	{
		Map<String, String> envMap = new HashMap<String, String>();
		if (parameters.contains("RAILS_ENV="))
		{
			String value = parameters.substring(parameters.indexOf("RAILS_ENV=") + 10);
			if (value.indexOf(' ') != -1)
			{
				value = value.substring(0, value.indexOf(' '));
			}
			envMap.put("RAILS_ENV", value);
		}
		String command = task + " " + parameters;
		try
		{
			ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch(RakePlugin.getDefault().getRakePath(),
					command, project, getWorkingDirectory(project));
			Map<String, String> map = new HashMap<String, String>();
			map.put(IRubyLaunchConfigurationConstants.ATTR_RUBY_COMMAND, "ruby");
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE_SPECIFIC_ATTRS_MAP, map);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_TERMINAL_COMMAND, "rake " + command);
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_USE_TERMINAL, "org.radrails.rails.shell"); // use
			// rails
			// shell
			// if
			// it's
			// available
			if (envMap != null && !envMap.isEmpty())
			{
				wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, envMap);
			}
			return wc.doSave();
		}
		catch (CoreException e)
		{
			RakePlugin.log("Error running rake task", e);
		}
		return null;
	}

	private static String getWorkingDirectory(IProject project)
	{
		if (project == null)
			return null;
		try
		{
			RakeFileFinder finder = new RakeFileFinder();
			project.accept(finder, IResource.NONE);
			File workingDir = finder.getWorkingDirectory();
			if (workingDir != null)
				return workingDir.getAbsolutePath();
		}
		catch (CoreException e)
		{
			RakePlugin.log(e);
		}
		return project.getLocation().toOSString();
	}

	/**
	 * Gets the rake tasks for the passed in project
	 * 
	 * @param project
	 *            The IProject to gather rake tasks for
	 * @return a Map of rake task names to their descriptions
	 */
	public Map<String, String> getTasks(IProject project, IProgressMonitor monitor)
	{
		return getTasks(project, false, monitor);
	}

	/**
	 * Gets the rake tasks for the passed in project
	 * 
	 * @param project
	 *            The IProject to gather rake tasks for
	 * @param force
	 *            Whether or not to force a refresh (don't grab cached value)
	 * @return a Map of rake task names to their descriptions
	 */
	public Map<String, String> getTasks(IProject project, boolean force, IProgressMonitor monitor)
	{
		if (!force && projectHasntChanged(project) && haveCachedTasks())
		{
			return fCachedTasks;
		}
		if (monitor == null)
			monitor = new NullProgressMonitor();
		
		fLastProject = project;
		fCachedTasks = null;

		try
		{
			if (monitor.isCanceled())
				return Collections.emptyMap();
			
			BufferedReader bufReader = new BufferedReader(new StringReader(getTasksText(project,
					getWorkingDirectory(project))));

			Pattern pat = Pattern.compile("^rake\\s+([\\w:]+)\\s+#\\s+(.+)$");
			String line = null;
			Map<String, String> tasks = new HashMap<String, String>();
			while ((line = bufReader.readLine()) != null)
			{
				Matcher mat = pat.matcher(line);
				if (mat.matches())
				{
					tasks.put(mat.group(1), mat.group(2));
				}
			}
			if (tasks.isEmpty())
				return new HashMap<String, String>();
			fCachedTasks = Collections.unmodifiableMap(tasks);
			return fCachedTasks;
		}
		catch (IOException e)
		{
			RakePlugin.log("Error parsing rake tasks", e);
		}
		return new HashMap<String, String>();
	}

	private boolean haveCachedTasks()
	{
		return (fCachedTasks != null && !fCachedTasks.isEmpty());
	}

	private boolean projectHasntChanged(IProject selected)
	{
		return selected != null && selected.equals(fLastProject);
	}

	private static String getTasksText(IProject project, String workingDirectory)
	{
		try
		{
			String rakePath = RakePlugin.getDefault().getRakePath();
			if (project != null && rakePath != null && rakePath.trim().length() > 0)
			{
				ILaunchConfigurationWorkingCopy wc = RubyRuntime.createBasicLaunch(rakePath, "--tasks", project,
						workingDirectory);
				File file = getRakeTasksFile(project);
				String result = RubyRuntime.launchInBackgroundAndRead(wc.doSave(), file);
				if (result == null)
					return "";
				return result;
			}

		}
		catch (CoreException e)
		{
			RakePlugin.log("Error listing rake tasks", e);
		}
		return "";
	}

	private static File getRakeTasksFile(IProject proj)
	{
		File file = RakePlugin.getDefault().getStateLocation().append("rake").append(proj.getName() + "_tasks.txt")
				.toFile();
		try
		{
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		catch (IOException e)
		{
			// ignore
		}
		return file;
	}
}
