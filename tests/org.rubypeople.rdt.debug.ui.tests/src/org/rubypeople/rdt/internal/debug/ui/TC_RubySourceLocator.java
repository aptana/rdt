package org.rubypeople.rdt.internal.debug.ui;

import java.io.ByteArrayInputStream;
import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.core.LaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchConfigurationWorkingCopy;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.rubypeople.rdt.internal.debug.core.model.RubyStackFrame;
import org.rubypeople.rdt.internal.debug.core.model.RubyThread;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

public class TC_RubySourceLocator extends TestCase
{

	public TC_RubySourceLocator(String name)
	{
		super(name);
	}

	protected void setUp()
	{
	}

	public void testWorkspaceInternalFile() throws Exception
	{
		createProject("SourceLocatorTest"); //$NON-NLS-1$
		createEmptyFile("/SourceLocatorTest/test.rb"); //$NON-NLS-1$

		// using slashes for the workspace internal path is platform independent
		String fullPath = getWorkspaceRoot().getLocation().toOSString() + File.separator + "SourceLocatorTest/test.rb"; //$NON-NLS-1$

		RubyStackFrame rubyStackFrame = new RubyStackFrame(new RubyThread(null, 1, ""), fullPath, 5, 1);
		assertCanOpen(rubyStackFrame);
	}

	public void testWorkspaceExternalFile() throws Exception
	{
		// external File
		File tmpFile = File.createTempFile("rubyfile", null); //$NON-NLS-1$
		RubyStackFrame rubyStackFrame = new RubyStackFrame(new RubyThread(null, 1, ""), tmpFile.getAbsolutePath(), 5, 1);

		assertCanOpen(rubyStackFrame);

		// set Working Directory to a project location
		createProject("WorkingDirIsProject"); //$NON-NLS-1$		
		RubySourceLocator sourceLocator = new RubySourceLocator();
		LaunchConfigurationWorkingCopy configuration = new LaunchConfigurationWorkingCopy(null, "name", null)
		{
		};
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, getWorkspaceRoot()
				.getLocation().toOSString()
				+ File.separator + "WorkingDirIsProject");
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "WorkingDirIsProject");

		sourceLocator.initializeDefaults(configuration);
		assertCanOpen(sourceLocator, rubyStackFrame);

		// An External file which is relative to the working directory
		// If an external file is found within an include path, ruby seems always to deliver an
		// absolte file path. But if the file found relative to the working directory, ruby
		// shows a relative path
		String workspacePath = getWorkspaceRoot().getLocation().toOSString();
		File externalFile = new File(workspacePath + File.separator + "externalRelativeRubyFile.rb");
		assertTrue(externalFile.createNewFile());
		// current directory = working dir = workspacePath/WorkingDirIsProject
		rubyStackFrame = new RubyStackFrame(new RubyThread(null, 1, ""), "../externalRelativeRubyFile.rb", 5, 1);
		assertCanOpen(sourceLocator, rubyStackFrame);
	}

	public void testNotExistingFile() throws Exception
	{
		RubyStackFrame rubyStackFrame = new RubyStackFrame(
				new RubyThread(null, 1, ""), "/tmp/nonexistingtestfile", 5, 1); //$NON-NLS-1$
		assertCantOpen(rubyStackFrame);
	}

	/**
	 * http://aptana.com/trac/ticket/5158
	 * 
	 * @throws Exception
	 */
	public void testTracTicket5158() throws Exception
	{
		final String projectName = "BugTest"; //$NON-NLS-1$
		createProject(projectName);
		createFolder("/" + projectName + "/script"); //$NON-NLS-1$ //$NON-NLS-2$
		createFolder("/" + projectName + "/config"); //$NON-NLS-1$ //$NON-NLS-2$
		createFolder("/" + projectName + "/app"); //$NON-NLS-1$ //$NON-NLS-2$
		createFolder("/" + projectName + "/app/models"); //$NON-NLS-1$ //$NON-NLS-2$
		createEmptyFile("/" + projectName + "/app/models/arsupport.rb"); //$NON-NLS-1$ //$NON-NLS-2$

		// using slashes for the workspace internal path is platform independent
		String fullPath = "./script/../config/../app/models/arsupport.rb"; //$NON-NLS-1$

		RubyStackFrame rubyStackFrame = new RubyStackFrame(new RubyThread(null, 1, ""), fullPath, 5, 1);
		RubySourceLocator sourceLocator = new RubySourceLocator();
		LaunchConfigurationWorkingCopy configuration = new LaunchConfigurationWorkingCopy(null, "name", null)
		{
		};
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, getWorkspaceRoot()
				.getLocation().toOSString()
				+ File.separator + projectName);
		configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		sourceLocator.initializeDefaults(configuration);
		assertCanOpen(sourceLocator, rubyStackFrame);
	}

	private void assertCanOpen(RubyStackFrame rubyStackFrame) throws PartInitException
	{
		assertCanOpen(new RubySourceLocator(), rubyStackFrame);
	}

	private void assertCanOpen(RubySourceLocator sourceLocator, RubyStackFrame rubyStackFrame) throws PartInitException
	{
		Object sourceElement = sourceLocator.getSourceElement(rubyStackFrame);
		IEditorInput input = sourceLocator.getEditorInput(sourceElement);
		assertNotNull(input);
		assertTrue(input.exists());
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
				sourceLocator.getEditorId(input, sourceElement));
	}

	private void assertCantOpen(RubyStackFrame rubyStackFrame)
	{
		RubySourceLocator sourceLocator = new RubySourceLocator();
		Object sourceElement = sourceLocator.getSourceElement(rubyStackFrame);
		IEditorInput input = sourceLocator.getEditorInput(sourceElement);
		assertNull(input);
	}

	private IFile createEmptyFile(String path) throws CoreException
	{
		IFile file = getWorkspaceRoot().getFile(new Path(path));
		file.create(new ByteArrayInputStream(new byte[0]), true, null);
		return file;
	}

	private IWorkspaceRoot getWorkspaceRoot()
	{
		return RdtDebugUiPlugin.getWorkspace().getRoot();
	}

	private Project createProject(String name) throws CoreException
	{
		Workspace workspace = (Workspace) RdtDebugUiPlugin.getWorkspace();
		Project p = new TestProject("/" + name, workspace); //$NON-NLS-1$		
		p.create(null);
		p.open(null);
		return p;
	}

	private IFolder createFolder(String path) throws CoreException
	{
		IFolder folder = getWorkspaceRoot().getFolder(new Path(path));
		folder.create(true, true, null);
		return folder;
	}

	public class TestProject extends Project
	{
		public TestProject(String aName, Workspace aWorkspace)
		{
			super(new Path(aName), aWorkspace);
		}
	}
}
