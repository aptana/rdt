/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.testunit.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.launcher.RubyApplicationShortcut;
import org.rubypeople.rdt.internal.testunit.ui.TestUnitMessages;
import org.rubypeople.rdt.internal.testunit.ui.TestunitPlugin;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IRuntimeLoadpathEntry;
import org.rubypeople.rdt.launching.RubyRuntime;

public class TestUnitLaunchShortcut extends RubyApplicationShortcut
{

	/**
	 * @param mode
	 * @param rubyElement
	 */
	protected void doLaunch(IRubyElement rubyElement, String mode) throws CoreException
	{
		String container = getContainer(rubyElement);
		ILaunchConfiguration config = findOrCreateLaunchConfiguration(rubyElement, mode, container, "", "");
		if (config != null)
		{
			DebugUITools.launch(config, mode);
		}
	}

	protected ILaunchConfiguration findOrCreateLaunchConfiguration(IRubyElement rubyElement, String mode,
			String container, String testClass, String testName) throws CoreException
	{
		IFile rubyFile = (IFile) rubyElement.getUnderlyingResource();
		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		List<ILaunchConfiguration> candidateConfigs = null;

		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
		candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
		for (int i = 0; i < configs.length; i++)
		{
			ILaunchConfiguration config = configs[i];
			if ((config.getAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, "").equals(container))
					&& (config.getAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, "").equals(""))
					&& (config.getAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, "").equals(testName))
					&& (config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(rubyFile
							.getProject().getName())))
			{
				candidateConfigs.add(config);
			}
		}
		switch (candidateConfigs.size())
		{
			case 0:
				return createConfiguration(rubyFile, container, testName);
			case 1:
				return candidateConfigs.get(0);
			default:
				ILaunchConfiguration config = chooseConfiguration(candidateConfigs, mode);
				if (config != null)
				{
					return config;
				}
				return null;
		}
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the specified launch configurations. Return the
	 * chosen config, or <code>null</code> if the user cancelled the dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List configList, String mode)
	{
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(TestUnitMessages.LaunchTestAction_message_selectConfiguration);
		if (mode.equals(ILaunchManager.DEBUG_MODE))
		{
			dialog.setMessage(TestUnitMessages.LaunchTestAction_message_selectDebugConfiguration);
		}
		else
		{
			dialog.setMessage(TestUnitMessages.LaunchTestAction_message_selectRunConfiguration);
		}
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK)
		{
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Convenience method to get the window that owns this action's Shell.
	 */
	protected Shell getShell()
	{
		return TestunitPlugin.getActiveWorkbenchShell();
	}

	/**
	 * @param rubyElement
	 * @return
	 */
	private String getContainer(IRubyElement rubyElement)
	{
		return rubyElement.getHandleIdentifier();
	}

	protected ILaunchConfiguration createConfiguration(IFile rubyFile, String container, String testName)
	{
		return createConfiguration(rubyFile.getLocation().toOSString(), container, rubyFile.getProject(), testName);
	}

	protected ILaunchConfiguration createConfiguration(String rubyFile, String container, IProject project,
			String testName)
	{
		if (RubyRuntime.getDefaultVMInstall() == null)
		{
			showNoInterpreterDialog();
			return null;
		}

		// Force project's lib and test folders to be added to loadpath for testing runs
		String[] commonLoadPathFolders = new String[] { "lib", "test" };
		List<String> loadpath = new ArrayList<String>();
		try
		{
			for (int i = 0; i < commonLoadPathFolders.length; i++)
			{
				if (project.getFolder(commonLoadPathFolders[i]).exists())
				{
					IRuntimeLoadpathEntry entry = RubyRuntime.newArchiveRuntimeLoadpathEntry(project.getLocation()
							.append(commonLoadPathFolders[i]));
					loadpath.add(entry.getMemento());
				}
			}
		}
		catch (CoreException e)
		{
			log(e);
		}

		ILaunchConfiguration config = null;
		try
		{
			ILaunchConfigurationType configType = getRubyLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, RubyRuntime.generateUniqueLaunchConfigurationNameFrom(rubyFile));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, TestUnitLaunchConfigurationDelegate
					.getTestRunnerPath());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, TestUnitLaunchShortcut
					.getDefaultWorkingDirectory(project));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, RubyRuntime.getDefaultVMInstall()
					.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, RubyRuntime.getDefaultVMInstall()
					.getVMInstallType().getId());
			wc.setAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, container);
			wc.setAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, testName);
			wc.setAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, "");
			if (loadpath != null && !loadpath.isEmpty())
			{
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_DEFAULT_LOADPATH, false);
				wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_LOADPATH, loadpath);
			}
			wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, RdtDebugUiConstants.RUBY_SOURCE_LOCATOR);
			config = wc.doSave();
		}
		catch (CoreException ce)
		{
			log(ce);
		}
		return config;
	}

	protected ILaunchConfigurationType getRubyLaunchConfigType()
	{
		return getLaunchManager().getLaunchConfigurationType(
				TestUnitLaunchConfigurationDelegate.ID_TESTUNIT_APPLICATION);
	}

	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected void log(String message)
	{
		TestunitPlugin.log(new Status(Status.INFO, TestunitPlugin.PLUGIN_ID, Status.INFO, message, null));
	}

	protected void log(Throwable t)
	{
		TestunitPlugin.log(t);
	}

	protected void showNoInterpreterDialog()
	{
		MessageDialog.openInformation(TestunitPlugin.getActiveWorkbenchShell(),
				TestUnitMessages.Dialog_launchWithoutSelectedInterpreter_title,
				TestUnitMessages.Dialog_launchWithoutSelectedInterpreter);
	}

	protected static String getDefaultWorkingDirectory(IProject project)
	{
		if (project != null && project.exists())
		{
			return project.getLocation().toOSString();
		}
		// might have been deleted
		return TestunitPlugin.getWorkspace().getRoot().getLocation().toOSString();
	}
}