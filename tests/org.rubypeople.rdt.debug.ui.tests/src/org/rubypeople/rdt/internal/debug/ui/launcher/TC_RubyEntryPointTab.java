package org.rubypeople.rdt.internal.debug.ui.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.rubypeople.eclipse.shams.debug.core.ShamLaunchConfigurationWorkingCopy;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;

public class TC_RubyEntryPointTab extends ModifyingResourceTest {

	private static final String PROJECT_NAME = "myProjectName";

	private RubyEntryPointTab fTab;

	private ILaunchConfigurationWorkingCopy fConfiguration;

	public TC_RubyEntryPointTab(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fTab = createTab();
		fConfiguration = new ShamLaunchConfigurationWorkingCopy();
	}

	public void testEmptyConfigurationIsInvalid() {
		String errorMessage = fTab.getErrorMessage();
		assertNull("There should be no error message.", errorMessage);
		assertTrue(
				"The tab is not valid when the configuration is completely empty.",
				!fTab.isValid(fConfiguration));
		assertEquals(
				"The tab should set the error message for no project.",
				RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidProjectSelectionMessage,
				fTab.getErrorMessage());
	}

	public void testNonexistantProjectIsInvalid() {
		fConfiguration.setAttribute(
				IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				PROJECT_NAME);
		assertTrue(
				"The tab is not valid when the configuration has a projectname whose project doesn't exist.",
				!fTab.isValid(fConfiguration));
		assertEquals(
				"The tab should set the error message for non-existant project.",
				RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidProjectSelectionMessage,
				fTab.getErrorMessage());
	}

	public void testNoFilenameIsInvalid() throws CoreException {
		try {
			createRubyProject(PROJECT_NAME);
			fConfiguration.setAttribute(
					IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					PROJECT_NAME);
			assertTrue(
					"The tab is not valid when the configuration has only a projectname.",
					!fTab.isValid(fConfiguration));
			assertEquals(
					"The tab should set the error message for no file.",
					RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidFileSelectionMessage,
					fTab.getErrorMessage());
		} finally {
			deleteProject(PROJECT_NAME);
		}
	}

	public void testValidConfiguration() throws CoreException {
		IFile file;
		String path = null;
		try {
			IRubyProject project = createRubyProject(PROJECT_NAME);
			path = new Path(PROJECT_NAME).append("myFileName").toPortableString();
			createFile(path, "");
			fConfiguration.setAttribute(
					IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					PROJECT_NAME);
			fConfiguration.setAttribute(
					IRubyLaunchConfigurationConstants.ATTR_FILE_NAME,
					"myFileName");
			assertTrue(
					"The tab is valid when the configuration has a filename and projectName.",
					fTab.isValid(fConfiguration));
			assertNull(
					"The tab should set the error message to null when there is a filename and projectname.",
					fTab.getErrorMessage());
		} finally {
			deleteProject(PROJECT_NAME);
			if (path != null) deleteFile(path);
		}
	}

	public void testNonexistantFileIsInvalid() throws CoreException {
		try {
			createRubyProject(PROJECT_NAME);
			fConfiguration.setAttribute(
					IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					PROJECT_NAME);
			fConfiguration.setAttribute(
					IRubyLaunchConfigurationConstants.ATTR_FILE_NAME,
					"myFileName2");
			assertTrue(
					"The tab is invalid when the configuration has a valid projectName, but a nonexistant filename.",
					!fTab.isValid(fConfiguration));
			assertEquals(
					"The tab should set the error message for non-existant file.",
					RdtDebugUiMessages.LaunchConfigurationTab_RubyEntryPoint_invalidFileSelectionMessage,
					fTab.getErrorMessage());
		} finally {
			deleteProject(PROJECT_NAME);
		}
	}
	
	// TODO Add tests for relative vs absolute filenames
	// TODO Add tests for filenames relative to project and relative to working directory
	
	private RubyEntryPointTab createTab() {
		RubyEntryPointTab tab = new RubyEntryPointTab();
		tab.createControl(new Composite(Display.getDefault().getActiveShell(),
				SWT.NULL));
		return tab;
	}
}
