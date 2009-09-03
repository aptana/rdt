package org.rubypeople.rdt.internal.debug.ui.launcher;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.internal.ui.util.ProjectFileSelector;
import org.rubypeople.rdt.internal.ui.util.RubyProjectSelector;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

public class RubyEntryPointTab extends AbstractLaunchConfigurationTab
{
	protected String originalFileName, originalProjectName;
	protected RubyProjectSelector projectSelector;
	protected ProjectFileSelector fileSelector;
	protected Composite composite;

	public RubyEntryPointTab()
	{
		super();
	}

	public void createControl(Composite parent)
	{
		composite = createPageRoot(parent);

		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_projectLabel);
		projectSelector = new RubyProjectSelector(composite);
		projectSelector
				.setBrowseDialogMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_projectSelectorMessage);
		projectSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectSelector.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent evt)
			{
				updateLaunchConfigurationDialog();
			}
		});

		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_fileLabel);
		fileSelector = new ProjectFileSelector(composite, projectSelector);
		fileSelector
				.setBrowseDialogMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_fileSelectorMessage);
		fileSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSelector.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent evt)
			{
				updateLaunchConfigurationDialog();
			}
		});
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		IResource selectedResource = RubyPlugin.getDefault().getSelectedResource();
		if (!RubyPlugin.getDefault().isRubyFile(selectedResource))
		{
			return;
		}
		IProject project = selectedResource.getProject();
		if (project == null || !RubyCore.isRubyProject(project))
		{
			return;
		}
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		configuration.setAttribute(getFileToLaunchAttribute(), modifyFileToLaunch(selectedResource
				.getProjectRelativePath().toString()));
	}

	public void initializeFrom(ILaunchConfiguration configuration)
	{
		try
		{
			originalProjectName = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		}
		catch (CoreException e)
		{
			log(e);
		}
		projectSelector.setSelectionText(originalProjectName);

		try
		{
			originalFileName = handleFileName(configuration.getAttribute(getFileToLaunchAttribute(), ""));
		}
		catch (CoreException e)
		{
			log(e);
		}
		if (originalFileName.length() != 0)
		{
			fileSelector.setSelectionText(new Path(originalFileName).toOSString());
		}
	}

	protected String handleFileName(String filename)
	{
		return filename;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectSelector
				.getSelectionText());
		String text = fileSelector.getSelectionText();

		String workingDirectory = null;
		try
		{
			workingDirectory = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
		}
		catch (CoreException e)
		{
			RdtDebugUiPlugin.log(e);
		}

		if (fileExists(text, workingDirectory))
		{
			configuration.setAttribute(getFileToLaunchAttribute(), modifyFileToLaunch(text));
		}
		else
		{
			configuration.setAttribute(getFileToLaunchAttribute(), modifyFileToLaunch(""));
		}
	}

	protected String modifyFileToLaunch(String text)
	{
		// it's a path, keep it that way
		return text;
	}

	protected Composite createPageRoot(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		composite.setLayout(layout);

		setControl(composite);
		return composite;
	}

	public String getName()
	{
		return RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_name;
	}

	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		try
		{
			if (!super.isValid(launchConfig))
				return false;

			String projectName = launchConfig.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			if (projectName.length() == 0)
			{
				setErrorMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidProjectSelectionMessage);
				return false;
			}

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null)
			{
				setErrorMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidProjectSelectionMessage);
				return false;
			}
			if (!project.exists())
			{
				setErrorMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidProjectSelectionMessage);
				return false;
			}

			String fileName = handleFileName(launchConfig.getAttribute(getFileToLaunchAttribute(), ""));
			if (fileName.length() == 0)
			{
				setErrorMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidFileSelectionMessage);
				return false;
			}

			String workingDirectory = null;
			try
			{
				workingDirectory = launchConfig.getAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
						(String) null);
			}
			catch (CoreException e)
			{
				RdtDebugUiPlugin.log(e);
			}
			if (!fileExists(fileName, workingDirectory))
			{
				setErrorMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidFileSelectionMessage);
				return false;
			}

			setErrorMessage(null);
			return true;
		}
		catch (CoreException e)
		{
			setErrorMessage(e.getMessage());
			RdtDebugUiPlugin.log(e);
		}
		return false;
	}

	protected String getFileToLaunchAttribute()
	{
		return IRubyLaunchConfigurationConstants.ATTR_FILE_NAME;
	}

	private boolean fileExists(String text, String workingDirectory)
	{
		File test = new File(text);
		if (test.exists())
			return true;
		// try relative to working directory
		if (workingDirectory != null && !workingDirectory.trim().equals(""))
		{
			if (new File(workingDirectory + text).exists())
				return true;
		}
		// try relative to project
		IProject project = getProject();
		if (project == null || project.getLocation() == null)
			return false;
		IPath path = project.getLocation().append(text);
		if (path == null || path.toFile() == null)
			return false;
		return path.toFile().exists();
	}

	protected IProject getProject()
	{
		String projectName = projectSelector.getSelectionText();
		if (projectName == null || projectName.trim().length() == 0)
		{
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	protected void log(Throwable t)
	{
		RdtDebugUiPlugin.log(t);
	}

	public boolean canSave()
	{
		return getErrorMessage() == null;
	}

	public Image getImage()
	{
		return RubyPluginImages.get(RubyPluginImages.IMG_CTOOLS_RUBY_PAGE);
	}

}