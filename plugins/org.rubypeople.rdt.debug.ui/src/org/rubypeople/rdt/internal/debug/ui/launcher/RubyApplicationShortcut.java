package org.rubypeople.rdt.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.debug.ui.InstallDeveloperToolsDialog;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.ContributedGemRegistry;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.Version;

public class RubyApplicationShortcut implements ILaunchShortcut
{

	private static final String RUBY_PROFILING_GEM_NAME = "ruby-prof";
	private static final String RUBY_DEBUG_IDE_GEM_NAME = "ruby-debug-ide";
	private static final String MINIMUM_RUBY_DEBUG_IDE_VERSION = "0.4.5";

	public void launch(ISelection selection, String mode)
	{

		Object firstSelection = null;
		if (selection instanceof IStructuredSelection)
		{
			firstSelection = ((IStructuredSelection) selection).getFirstElement();

		}
		if (firstSelection == null)
		{
			log("Could not find selection.");
			return;
		}
		IRubyElement rubyElement = null;
		if (firstSelection instanceof IAdaptable)
		{
			rubyElement = (IRubyElement) ((IAdaptable) firstSelection).getAdapter(IRubyElement.class);
		}
		if (rubyElement == null)
		{
			log("Selection is not a ruby element.");
			return;
		}
		doLaunchWithErrorHandling(rubyElement, mode);
	}

	private void doLaunchWithErrorHandling(IRubyElement rubyElement, String mode)
	{
		if (shouldInstallGemsFirst(mode))
			return;
		try
		{
			doLaunch(rubyElement, mode);
		}
		catch (CoreException e)
		{
			log(e);
			IStatus status = e.getStatus();
			String title = RdtDebugUiMessages.Dialog_launchErrorTitle;
			String message = RdtDebugUiMessages.Dialog_launchErrorMessage;
			if (status != null)
			{
				ErrorDialog.openError(RdtDebugUiPlugin.getActiveWorkbenchWindow().getShell(), title, message, status);
			}
		}
	}

	/**
	 * A check to run before launching. If we're trying to debug or profile, let's make sure the user has installed the
	 * necessary gems.
	 * 
	 * @param mode
	 * @return
	 */
	private static boolean shouldInstallGemsFirst(String mode)
	{
		// XXX All these prompts need to have their strings externalized for i18n!
		if (mode.equals(ILaunchManager.RUN_MODE))
			return false;
		// Install ruby-debug and ruby-prof on demand when user first tries to use those launch modes
		if (mode.equals(ILaunchManager.DEBUG_MODE))
		{
			if (!AptanaRDTPlugin.getDefault().getGemManager().gemInstalled(RUBY_DEBUG_IDE_GEM_NAME))
			{
				installNecessaryDebuggingGems();
				return true;
			}
			else if (updatingRubyDebugIDEGem())
			{
				return true;
			}
		}
		else if (mode.equals(ILaunchManager.PROFILE_MODE))
		{
			if (RubyRuntime.currentVMIsJRuby())
			{
				MessageDialog
						.openError(
								Display.getDefault().getActiveShell(),
								"Profiling not yet supported on JRuby",
								"Profiling is not yet available for the JRuby interpreter. We rely on the ruby-prof gem, which requires native code, and there is not yet a Java based version of the gem.");
				return true;
			}
			if (!AptanaRDTPlugin.getDefault().getGemManager().gemInstalled(RUBY_PROFILING_GEM_NAME))
			{
				installNecessaryProfilingGems();
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the ruby-debug-ide gem if necessary
	 * 
	 * @return false if no update necessary, true if we scheduled an update
	 */
	private static boolean updatingRubyDebugIDEGem()
	{
		List<Version> versions = AptanaRDTPlugin.getDefault().getGemManager().getVersions(RUBY_DEBUG_IDE_GEM_NAME);
		for (Version version : versions)
		{
			if (version.isGreaterThanOrEqualTo(MINIMUM_RUBY_DEBUG_IDE_VERSION))
				return false;
		}
		// Prompt user to let us update ruby-debug-ide
		if (!MessageDialog
				.openQuestion(
						Display.getDefault().getActiveShell(),
						"ruby-debug-ide out of date",
						"To debug, it is required that you use the ruby-debug-ide gem, with version >= "
								+ MINIMUM_RUBY_DEBUG_IDE_VERSION
								+ ". Would you like us to update that gem for you? (You will need to relaunch once it's been updated)."))
			return true;
		Job job = new Job("Updating installed ruby-debug-ide gem...")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				return AptanaRDTPlugin.getDefault().getGemManager().update(
						new Gem(RUBY_DEBUG_IDE_GEM_NAME, Gem.ANY_VERSION, null), monitor);
			}

		};
		job.setUser(true);
		job.schedule();
		return true;
	}

	private static void installNecessaryProfilingGems()
	{
		if (!MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "ruby-prof not installed",
				"To profile, it is required that you use the ruby-prof gem. Would you like us to install that gem?"))
			return;

		if (InstallDeveloperToolsDialog.shouldShow())
		{
			InstallDeveloperToolsDialog dialog = new InstallDeveloperToolsDialog(Display.getDefault().getActiveShell());
			dialog.open();
			return;
		}

		List<Gem> finalGems = new ArrayList<Gem>();
		Gem gem = ContributedGemRegistry.getGem(RUBY_PROFILING_GEM_NAME);
		if (gem != null)
			finalGems.add(gem);
		Job job = new InstallGemsJob(finalGems);
		job.setSystem(true);
		job.schedule();
	}

	private static void installNecessaryDebuggingGems()
	{
		if (!MessageDialog
				.openQuestion(Display.getDefault().getActiveShell(), "ruby-debug not installed",
						"To debug, it is recommended that you use the ruby-debug based debugger. Would you like us to install that gem?"))
			return;

		if (InstallDeveloperToolsDialog.shouldShow())
		{
			InstallDeveloperToolsDialog dialog = new InstallDeveloperToolsDialog(Display.getDefault().getActiveShell());
			dialog.open();
			return;
		}

		List<Gem> finalGems = new ArrayList<Gem>();
		Gem gem = ContributedGemRegistry.getGem("ruby-debug-base");
		if (gem != null)
			finalGems.add(gem);
		gem = ContributedGemRegistry.getGem(RUBY_DEBUG_IDE_GEM_NAME);
		if (gem != null)
			finalGems.add(gem);
		Job job = new InstallGemsJob(finalGems);
		job.setSystem(true);
		job.schedule();
	}

	protected void doLaunch(IRubyElement rubyElement, String mode) throws CoreException
	{
		ILaunchConfiguration config = findOrCreateLaunchConfiguration(rubyElement, mode);
		if (config != null)
		{
			DebugUITools.launch(config, mode);
		}
	}

	public void launch(IEditorPart editor, String mode)
	{
		IEditorInput input = editor.getEditorInput();
		if (input == null)
		{
			log("Could not retrieve input from editor: " + editor.getTitle());
			return;
		}
		IRubyElement rubyElement = (IRubyElement) input.getAdapter(IRubyElement.class);
		if (rubyElement == null)
		{
			log("Editor input is not a ruby file or external ruby file.");
			return;
		}
		doLaunchWithErrorHandling(rubyElement, mode);

	}

	protected ILaunchConfiguration findOrCreateLaunchConfiguration(IRubyElement rubyElement, String mode)
			throws CoreException
	{
		IFile rubyFile = (IFile) rubyElement.getUnderlyingResource();
		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		List candidateConfigs = null;

		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
		candidateConfigs = new ArrayList(configs.length);
		for (int i = 0; i < configs.length; i++)
		{
			ILaunchConfiguration config = configs[i];
			boolean projectsEqual = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "")
					.equals(rubyFile.getProject().getName());
			if (projectsEqual)
			{
				boolean projectRelativeFileNamesEqual = config.getAttribute(
						IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, "").equals(
						rubyFile.getProjectRelativePath().toString());
				if (projectRelativeFileNamesEqual)
				{
					candidateConfigs.add(config);
				}
			}
		}

		switch (candidateConfigs.size())
		{
			case 0:
				return createConfiguration(rubyFile);
			case 1:
				return (ILaunchConfiguration) candidateConfigs.get(0);
			default:
				Status status = new Status(Status.WARNING, RdtDebugUiPlugin.PLUGIN_ID, 0,
						RdtDebugUiMessages.LaunchConfigurationShortcut_Ruby_multipleConfigurationsError, null);
				throw new CoreException(status);
		}
	}

	protected ILaunchConfiguration createConfiguration(IFile rubyFile)
	{
		if (RubyRuntime.getDefaultVMInstall() == null)
		{
			this.showNoInterpreterDialog();
			return null;
		}
		ILaunchConfiguration config = null;
		try
		{
			ILaunchConfigurationType configType = getRubyLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager()
					.generateUniqueLaunchConfigurationNameFrom(rubyFile.getName()));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, rubyFile.getProject().getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, rubyFile.getProjectRelativePath()
					.toString());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, RubyApplicationShortcut
					.getDefaultWorkingDirectory(rubyFile.getProject()));
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, RubyRuntime.getDefaultVMInstall()
					.getName());
			wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, RubyRuntime.getDefaultVMInstall()
					.getVMInstallType().getId());
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
		return getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
	}

	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected void log(String message)
	{
		RdtDebugUiPlugin.log(new Status(Status.INFO, RdtDebugUiPlugin.PLUGIN_ID, Status.INFO, message, null));
	}

	protected void log(Throwable t)
	{
		RdtDebugUiPlugin.log(t);
	}

	protected void showNoInterpreterDialog()
	{
		MessageDialog.openInformation(RubyPlugin.getActiveWorkbenchShell(),
				RdtDebugUiMessages.Dialog_launchWithoutSelectedInterpreter_title,
				RdtDebugUiMessages.Dialog_launchWithoutSelectedInterpreter);
	}

	protected static String getDefaultWorkingDirectory(IProject project)
	{
		if (project != null && project.exists())
		{
			return project.getLocation().toOSString();
		}
		// might have been deleted
		return RdtDebugUiPlugin.getWorkspace().getRoot().getLocation().toOSString();
	}
}