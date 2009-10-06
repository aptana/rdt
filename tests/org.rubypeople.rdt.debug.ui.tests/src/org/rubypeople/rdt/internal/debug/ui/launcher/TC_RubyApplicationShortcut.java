package org.rubypeople.rdt.internal.debug.ui.launcher;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.RubySourceLocator;
import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.launching.VMStandin;
import org.rubypeople.rdt.ui.RubyUI;

public class TC_RubyApplicationShortcut extends ModifyingResourceTest
{

	private static final String VM_TYPE_ID = "org.rubypeople.rdt.launching.StandardVMType";

	protected ShamRubyApplicationShortcut shortcut;
	protected IFile rubyFile, nonRubyFile;
	private static String SHAM_LAUNCH_CONFIG_TYPE = "org.rubypeople.rdt.debug.ui.tests.launching.LaunchConfigurationTypeSham";
	private Set configurations = new HashSet();

	public TC_RubyApplicationShortcut(String name)
	{
		super(name);
	}

	protected ILaunchConfiguration createConfiguration(IFile pFile)
	{
		ILaunchConfiguration config = null;
		try
		{
			ILaunchConfigurationType configType = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurationType(SHAM_LAUNCH_CONFIG_TYPE);
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, pFile.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, pFile.getProject().getName());
			wc
					.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, pFile.getProjectRelativePath()
							.toString());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "");
			wc.setAttribute(RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, RubyRuntime
					.getCompositeIdFromVM(RubyRuntime.getDefaultVMInstall()));
			wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, RdtDebugUiConstants.RUBY_SOURCE_LOCATOR);
			config = wc.doSave();
		}
		catch (CoreException ce)
		{
			// ignore
		}
		return config;
	}

	protected ILaunchConfiguration[] getLaunchConfigurations() throws CoreException
	{
		return (ILaunchConfiguration[]) configurations.toArray(new ILaunchConfiguration[configurations.size()]);
	}

	private IVMInstallType vmType;
	private IVMInstall vm;

	protected void setUp() throws Exception
	{
		super.setUp();
		shortcut = new ShamRubyApplicationShortcut();

		createRubyProject("/project1");
		createFolder("/project1/folderOne");
		nonRubyFile = createFile("/project1/folderOne/myFile.java", "");
		rubyFile = createFile("/project1/folderOne/myFile.rb", "");

		ILaunchConfiguration[] configs = this.getLaunchConfigurations();
		for (int i = 0; i < configs.length; i++)
		{
			configs[i].delete();
		}
		Assert.assertEquals("All configurations deleted.", 0, this.getLaunchConfigurations().length);

		ShamApplicationLaunchConfigurationDelegate.resetLaunches();

		// TODO Refcator out this common code which is in a few tests now - setting up a fake default vm install
		vmType = RubyRuntime.getVMInstallType(VM_TYPE_ID);
		VMStandin standin = new VMStandin(vmType, "fake");
		IFolder location = createFolder("/project1/interpreterOne");
		createFolder("/project1/interpreterOne/lib");
		createFolder("/project1/interpreterOne/bin");
		createFile("/project1/interpreterOne/bin/ruby", "");
		standin.setInstallLocation(location.getLocation().toFile());
		standin.setName("InterpreterOne");
		vm = standin.convertToRealVM();
		RubyRuntime.setDefaultVMInstall(vm, null, true);
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		vmType.disposeVMInstall(vm.getId());
		deleteProject("/project1");
		configurations.clear();
	}

	public void testNoInterpreterInstalled() throws Exception
	{
		vmType.disposeVMInstall(vm.getId());
		RubyRuntime.setDefaultVMInstall(null, null, true);

		ISelection selection = new StructuredSelection(rubyFile);
		shortcut.launch(selection, ILaunchManager.RUN_MODE);

		assertTrue("The 'no interpreter selected' dialog should have been shown.", shortcut.didShowDialog);
	}

	public void testLaunchWithSelectedRubyFile() throws Exception
	{
		ISelection selection = new StructuredSelection(rubyFile);

		shortcut.launch(selection, ILaunchManager.RUN_MODE);

		assertEquals("One configuration should have been created.", 1, getLaunchConfigurations().length);
		assertEquals("One launch should have taken place.", 1, shortcut.launchCount());
		assertTrue("The shortcut should not log a message when asked to launch the correct file type.", !shortcut
				.didLog());
	}

	public void testLaunchWithSelectedNonRubyFile() throws Exception
	{
		ISelection selection = new StructuredSelection(nonRubyFile);

		shortcut.launch(selection, ILaunchManager.RUN_MODE);

		assertEquals("There is no configuration.", 0, this.getLaunchConfigurations().length);
		assertEquals("There was no launch.", 0, shortcut.launchCount());
		assertTrue("The shortcut should log a message when asked to launch the wrong file type.", shortcut.didLog());
	}

	public void testLaunchWithSelectionMultipleConfigurationsExist() throws Exception
	{
		createConfiguration(rubyFile);
		createConfiguration(rubyFile);
		ISelection selection = new StructuredSelection(rubyFile);

		shortcut.launch(selection, ILaunchManager.RUN_MODE);

		assertEquals(
				"A new configuration for myFile.rb should not be created when one ore more configurations already exist.",
				1, this.getLaunchConfigurations().length);
		assertEquals("The configuration for myFile.rb should have be launched.", 1, shortcut.launchCount());
	}

	public void testLaunchWithSelectionMultipleSelections() throws Exception
	{
		ISelection selection = new StructuredSelection(new Object[] { rubyFile,
				createFile("project1/folderOne/yourFile.rb", "") });
		shortcut.launch(selection, ILaunchManager.RUN_MODE);
		ILaunchConfiguration[] configurations = this.getLaunchConfigurations();
		assertEquals("A configuration has been created", 1, configurations.length);
		assertEquals("A launch took place.", 1, shortcut.launchCount());
		assertTrue("The shortcut should not log a message when asked to launch the correct file type.", !shortcut
				.didLog());

		String launchedFileName = configurations[0].getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, "");
		assertEquals("folderOne/myFile.rb", launchedFileName);
	}

	public void testLaunchWithSelectionTwice() throws Exception
	{
		ISelection selection = new StructuredSelection(rubyFile);
		shortcut.launch(selection, ILaunchManager.RUN_MODE);
		shortcut.launch(selection, ILaunchManager.RUN_MODE);
		assertEquals("Only one configuration has been created", 1, this.getLaunchConfigurations().length);
		assertEquals("Two launches took place.", 2, shortcut.launchCount());
		assertTrue("The shortcut should not log a message when asked to launch the correct file type.", !shortcut
				.didLog());
	}

	public void testLaunchWithSelectionWhenFileNamesSameInDifferentDirectory() throws Exception
	{
		IFile anotherRubyFileWithSameNameInDifferentFolder = createFile("project1/myFile.rb", "");
		ISelection selection = new StructuredSelection(rubyFile);

		shortcut.launch(selection, ILaunchManager.RUN_MODE);

		ILaunchConfiguration[] configurations = this.getLaunchConfigurations();
		assertEquals("A configuration has been created", 1, configurations.length);
		assertEquals("A launch took place.", 1, shortcut.launchCount());
		assertTrue("The shortcut should not log a message when asked to launch the correct file type.", !shortcut
				.didLog());

		String launchedFileName = configurations[0].getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, "");
		assertEquals("folderOne/myFile.rb", launchedFileName);
	}

	public void testLaunchFromEditorWithRubyFile() throws Exception
	{
		IFile file = createFile("project1/test.rb", "");
		RubySourceLocator sourceLocator = new RubySourceLocator();
		String fullPath = RdtDebugUiPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator
				+ file.getFullPath().toOSString();
		Object sourceElement = sourceLocator.getSourceElement(fullPath);
		IEditorInput input = sourceLocator.getEditorInput(sourceElement);
		IEditorPart rubyEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
				RubyUI.ID_RUBY_EDITOR);

		shortcut.launch(rubyEditor, ILaunchManager.RUN_MODE);

		assertEquals("A configuration has been created", 1, this.getLaunchConfigurations().length);
		assertEquals("A launch took place.", 1, shortcut.launchCount());
		assertTrue("The shortcut should not log a message when asked to launch the correct file type.", !shortcut
				.didLog());
	}

	public void testLaunchFromEditorWithTxtFile() throws Exception
	{
		IFile file = createFile("project1/test.txt", "");
		IEditorInput input = new FileEditorInput(file);
		IEditorPart txtEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
				"org.eclipse.ui.DefaultTextEditor");

		shortcut.launch(txtEditor, ILaunchManager.RUN_MODE);

		assertEquals("No configuration has been created", 0, this.getLaunchConfigurations().length);
		assertEquals("No launch took place.", 0, shortcut.launchCount());
		assertTrue("The shortcut should must have logged a message.", shortcut.didLog());
	}

	public void testLaunchFromExternalRubyFileEditor() throws Exception
	{
		File tmpFile = File.createTempFile("rubyfile.rb", null); //$NON-NLS-1$
		RubySourceLocator sourceLocator = new RubySourceLocator();
		Object sourceElement = sourceLocator.getSourceElement(tmpFile.getAbsolutePath());
		IEditorInput input = sourceLocator.getEditorInput(sourceElement);

		IEditorPart rubyExternalEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.openEditor(input, RubyUI.ID_EXTERNAL_EDITOR);

		shortcut.launch(rubyExternalEditor, ILaunchManager.RUN_MODE);

		assertEquals("No configuration has been created", 0, this.getLaunchConfigurations().length);
		assertEquals("No launch took place.", 0, shortcut.launchCount());
		assertTrue("The shortcut should must have logged a message.", shortcut.didLog());
	}

	protected class ShamRubyApplicationShortcut extends RubyApplicationShortcut
	{

		protected boolean didLog;
		protected boolean didShowDialog = false;
		private boolean expectingException;
		private int launches = 0;

		protected void log(String message)
		{
			didLog = true;
		}

		public void expectException()
		{
			expectingException = true;
		}

		protected void log(Throwable t)
		{
			if (!expectingException)
				throw new RuntimeException("Unexpected throwable: " + t.getMessage(), t);
			didLog = true;
		}

		protected boolean didLog()
		{
			return didLog;
		}

		protected void doLaunch(IRubyElement rubyElement, String mode) throws CoreException
		{
			ILaunchConfiguration config = findOrCreateLaunchConfiguration(rubyElement, mode);
			if (config != null)
			{
				configurations.add(config);
				launches++;
			}
		}

		public int launchCount()
		{
			return launches;
		}

		protected ILaunchConfigurationType getRubyLaunchConfigType()
		{
			return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(SHAM_LAUNCH_CONFIG_TYPE);
		}

		protected void showNoInterpreterDialog()
		{
			didShowDialog = true;
		}
	}

}