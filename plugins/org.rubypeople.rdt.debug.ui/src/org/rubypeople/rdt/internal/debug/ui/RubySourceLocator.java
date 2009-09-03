package org.rubypeople.rdt.internal.debug.ui;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.rubypeople.rdt.debug.core.RdtDebugCorePlugin;
import org.rubypeople.rdt.debug.core.model.IRubyStackFrame;
import org.rubypeople.rdt.internal.ui.rubyeditor.ExternalRubyFileEditorInput;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.ui.RubyUI;

/**
 * @author Administrator To change this generated comment edit the template variable "typecomment":
 *         Window>Preferences>Java>Templates. To enable and disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class RubySourceLocator implements IPersistableSourceLocator, ISourcePresentation
{

	private String absoluteWorkingDirectory;
	private String projectName;

	public RubySourceLocator()
	{

	}

	public String getAbsoluteWorkingDirectory()
	{
		return absoluteWorkingDirectory;
	}

	/**
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(String)
	 */
	public void initializeFromMemento(String memento) throws CoreException
	{
	}

	/**
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException
	{
		this.absoluteWorkingDirectory = configuration.getAttribute(
				IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
		this.projectName = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
	 */
	public Object getSourceElement(IStackFrame stackFrame)
	{
		return this.getSourceElement(((IRubyStackFrame) stackFrame).getFileName());
	}

	public Object getSourceElement(String pFilename)
	{
		return new SourceElement(pFilename, this);
	}

	/**
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object element)
	{
		SourceElement sourceElement = (SourceElement) element;
		try
		{
			IEditorDescriptor desc = IDE.getEditorDescriptor(sourceElement.getFilename());
			return desc.getId();
		}
		catch (PartInitException e)
		{
			// ignore
		}

		return sourceElement.isExternal() ? RubyUI.ID_EXTERNAL_EDITOR : RubyUI.ID_RUBY_EDITOR;
	}

	/**
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object element)
	{		
		SourceElement sourceElement = (SourceElement) element;
		if (!sourceElement.isExternal())
		{
			return new FileEditorInput(sourceElement.getWorkspaceFile());
		}
		File filesystemFile = new File(sourceElement.getFilename());
		if (filesystemFile.exists())
		{
			return new ExternalRubyFileEditorInput(filesystemFile);
		}
		RdtDebugCorePlugin.log(IStatus.INFO, RdtDebugUiMessages.getFormattedString(
				RdtDebugUiMessages.RdtDebugUiPlugin_couldNotOpenFile, sourceElement.getFilename()));
		return null;

	}

	public class SourceElement
	{
		private String filename;
		private IFile workspaceFile;
		private RubySourceLocator sourceLocator;

		public SourceElement(String aFilename, RubySourceLocator pSourceLocator)
		{
			filename = aFilename;
			this.sourceLocator = pSourceLocator;
			init();
		}

		private void init()
		{
			setFileName();
			grabWorkspaceFile();
		}

		private void setFileName()
		{
			if (filename == null)
				return;
			if (filename.startsWith("./"))
			{
				filename = filename.substring(2);
			}
			// Try relative to working dir
			if (sourceLocator.getAbsoluteWorkingDirectory() != null
					&& sourceLocator.getAbsoluteWorkingDirectory().trim().length() > 0)
			{
				String relativeToWorkingDir = sourceLocator.getAbsoluteWorkingDirectory() + "/" + filename;
				File file = new File(relativeToWorkingDir);
				if (file.exists() && !file.isDirectory())
				{
					filename = relativeToWorkingDir;
					return;
				}
			}
			// Try relative to project root
			if (projectName != null && projectName.trim().length() > 0)
			{
				IProject project = getProject();
				String relativeToProject = project.getLocation().toOSString() + filename.substring(1);
				File file = new File(relativeToProject);
				if (file.exists() && !file.isDirectory())
				{
					filename = relativeToProject;
					return;
				}
			}
			// Assume it's absolute...
		}

		private IProject getProject()
		{
			if (projectName == null)
				return null;
			return RdtDebugCorePlugin.getWorkspace().getRoot().getProject(projectName);
		}

		private void grabWorkspaceFile()
		{
			if (filename == null)
				return;

			// Try absolute
			workspaceFile = RdtDebugCorePlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
			if (workspaceFile != null && workspaceFile.exists())
				return;

			// Try relative to workspace root
			try
			{
				workspaceFile = RdtDebugCorePlugin.getWorkspace().getRoot().getFile(new Path(filename));
				if (workspaceFile != null && workspaceFile.exists())
					return;
			}
			catch (RuntimeException e)
			{
				workspaceFile = null;
			}
			if (getProject() != null)
			{
				workspaceFile = getProject().getFile(new Path(filename));
			}
		}

		public boolean isExternal()
		{
			return workspaceFile == null || !workspaceFile.exists();
		}

		public IFile getWorkspaceFile()
		{
			return workspaceFile;
		}

		public String getFilename()
		{
			return filename;
		}

	}
}
