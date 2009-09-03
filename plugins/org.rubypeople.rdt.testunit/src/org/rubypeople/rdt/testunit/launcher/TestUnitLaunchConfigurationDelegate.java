package org.rubypeople.rdt.testunit.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.SocketUtil;
import org.rubypeople.rdt.internal.testunit.ui.TestUnitMessages;
import org.rubypeople.rdt.internal.testunit.ui.TestunitPlugin;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.RubyLaunchDelegate;

public class TestUnitLaunchConfigurationDelegate extends RubyLaunchDelegate {
	/**
	 * The single test type, or "" iff running a launch container.
	 */
	public static final String TESTTYPE_ATTR = TestunitPlugin.PLUGIN_ID + ".TESTTYPE"; //$NON-NLS-1$
	/**
	 * The test method, or "" iff running the whole test type.
	 */
	public static final String TESTNAME_ATTR = TestunitPlugin.PLUGIN_ID + ".TESTNAME"; //$NON-NLS-1$
	/**
	 * The launch container, or "" iff running a single test type.
	 */
	public static final String LAUNCH_CONTAINER_ATTR = TestunitPlugin.PLUGIN_ID + ".CONTAINER"; //$NON-NLS-1$

	public static final String ID_TESTUNIT_APPLICATION = "org.rubypeople.rdt.testunit.launchconfig"; //$NON-NLS-1$
	
	private static final String TEST_RUNNER_FILE = "RemoteTestRunner.rb";
	
	private int port = -1;

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {	
		IType[] testTypes = findTestTypes(configuration, monitor);
		getTestRunnerPath();
		
//		setDefaultSourceLocator(launch, configuration);
		launch.setAttribute(TestunitPlugin.TESTUNIT_PORT_ATTR, Integer.toString(getPort()));
		if (testTypes != null && testTypes.length > 0 && testTypes[0] != null) launch.setAttribute(TESTTYPE_ATTR, testTypes[0].getHandleIdentifier());

		super.launch(configuration, mode, launch, monitor);
	}
	
	protected IType[] findTestTypes(ILaunchConfiguration configuration, IProgressMonitor pm) throws CoreException {
		IRubyProject javaProject= getRubyProject(configuration);
		if ((javaProject == null) || !javaProject.exists()) {
			informAndAbort(TestUnitMessages.TestUnitBaseLaunchConfiguration_error_invalidproject, null, IRubyLaunchConfigurationConstants.ERR_NOT_A_RUBY_PROJECT); 
		}
//		if (!TestSearchEngine.hasTestCaseType(javaProject)) {
//			informAndAbort(TestUnitMessages.JUnitBaseLaunchConfiguration_error_junitnotonpath, null, ITestUnitStatusConstants.ERR_JUNIT_NOT_ON_PATH);
//		}

		String containerHandle = configuration.getAttribute(LAUNCH_CONTAINER_ATTR, ""); //$NON-NLS-1$
		if (containerHandle.length() > 0) {
			IRubyElement element = RubyCore.create(containerHandle);
			if (element != null) {
				if (element.isType(IRubyElement.TYPE)) {
					return new IType[] { (IType) element };
				}
				IRubyScript script = (IRubyScript) element;
				if (script != null) {
					IType type = script.findPrimaryType();
					if (type != null)
						return new IType[] { type };
				}
			}
		}
		String testTypeName= configuration.getAttribute(TESTTYPE_ATTR, (String) null);
		if (testTypeName != null && testTypeName.length() > 0) {
			return new IType[] {javaProject.findType(testTypeName, pm)};
		}
		return new IType[0];
	}
	
	protected void informAndAbort(String message, Throwable exception, int code) throws CoreException {
		IStatus status= new Status(IStatus.INFO, TestunitPlugin.PLUGIN_ID, code, message, exception);
		if (showStatusMessage(status))
			throw new CoreException(status);
		abort(message, exception, code);
	}
	
	private boolean showStatusMessage(final IStatus status) {
		final boolean[] success= new boolean[] { false };
		getDisplay().syncExec(
				new Runnable() {
					public void run() {
						Shell shell= TestunitPlugin.getActiveWorkbenchShell();
						if (shell == null)
							shell= getDisplay().getActiveShell();
						if (shell != null) {
							MessageDialog.openInformation(shell, TestUnitMessages.JUnitBaseLaunchConfiguration_dialog_title, status.getMessage());
							success[0]= true;
						}
					}
				}
		);
		return success[0];
	}
	
	private Display getDisplay() {
		Display display;
		display= Display.getCurrent();
		if (display == null)
			display= Display.getDefault();
		return display;		
	}

	public static String getTestRunnerPath() {
		// Copy test runner files over to workspace
		RubyCore.copyToStateLocation(TestunitPlugin.getDefault(), new Path("ruby").append(TEST_RUNNER_FILE));
		RubyCore.copyToStateLocation(TestunitPlugin.getDefault(), new Path("ruby").append("RemoteTestRunnerRSpec.rb"));
		
		IPath path = TestunitPlugin.getDefault().getStateLocation().append(new Path("ruby").append(TEST_RUNNER_FILE));	
		if (!path.toFile().exists())
			throw new RuntimeException("Expected directory of ruby/" + TEST_RUNNER_FILE + " does not exist: " + path); 
	
		return path.toPortableString();
	}
	
	private int getPort() {
		if (port == -1) {
			port  = SocketUtil.findFreePort();
		}
		return port;
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getLaunchContainerPath(configuration));
		buffer.append(' ');
		buffer.append(Integer.toString(getPort()));
		buffer.append(' ');
		buffer.append(Boolean.toString(false));
		buffer.append(' ');
		buffer.append(configuration.getAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, ""));
		buffer.append(' ');
		buffer.append(configuration.getAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, ""));
		return buffer.toString();
	}

	private String getLaunchContainerPath(ILaunchConfiguration configuration) throws CoreException {
		String container = configuration.getAttribute(TestUnitLaunchConfigurationDelegate.LAUNCH_CONTAINER_ATTR, "");
		IRubyElement element = (IRubyElement) RubyCore.create(container);
		if (element != null)
		  container = element.getResource().getProjectRelativePath().toOSString();
		if (!container.startsWith("\"") && container.indexOf(' ') != -1) {
			container = '"' + container + '"';
		}
		return container;
	}
}