package org.rubypeople.rdt.internal.testunit.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.testunit.ITestRunListener;
import org.rubypeople.rdt.testunit.launcher.TestUnitLaunchConfigurationDelegate;

/**
 * The main plugin class to be used in the desktop.
 */
public class TestunitPlugin extends AbstractUIPlugin implements ILaunchListener {

	public static final String PLUGIN_ID = "org.rubypeople.rdt.testunit"; //$NON-NLS-1$
	public static final String TESTUNIT_PORT_ATTR = "org.rubypeople.rdt.testunit.port"; //$NON-NLS-1$
	//The shared instance.
	private static TestunitPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * Use to track new launches. We need to do this so that we only attach a
	 * TestRunner once to a launch. Once a test runner is connected it is
	 * removed from the set.
	 */
	private AbstractSet<ILaunch> fTrackedLaunches = new HashSet<ILaunch>(20);
	
	private Set<ITestRunListener> fTestRunListeners = new HashSet<ITestRunListener>();

	private static URL fgIconBaseURL;

	/**
	 * The constructor.
	 */
	public TestunitPlugin() {
		super();
		String pathSuffix = "icons/full/"; //$NON-NLS-1$
		try {
			fgIconBaseURL = new URL(Platform.getBundle(PLUGIN_ID).getEntry("/"), pathSuffix); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			// do nothing
		}
		try {
			resourceBundle = ResourceBundle.getBundle("org.rubypeople.rdt.testunit.TestunitPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public static URL makeIconFileURL(String name) throws MalformedURLException {
		if (TestunitPlugin.fgIconBaseURL == null) throw new MalformedURLException();
		return new URL(TestunitPlugin.fgIconBaseURL, name);
	}

	public static ImageDescriptor getImageDescriptor(String relativePath) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(relativePath));
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		super.start(context);
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static TestunitPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = TestunitPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
		if (workBenchWindow == null) return null;
		return workBenchWindow.getShell();
	}

	/**
	 * Returns the active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (plugin == null) return null;
		IWorkbench workBench = plugin.getWorkbench();
		if (workBench == null) return null;
		return workBench.getActiveWorkbenchWindow();
	}

	public static IWorkspace getWorkspace() {
		return RubyCore.getWorkspace();
	}

	public static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow = getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) return null;
		return activeWorkbenchWindow.getActivePage();
	}

	public void connectTestRunner(ILaunch launch, IType finalType, int port) {
		TestUnitView testRunnerViewPart = showTestUnitViewInActivePage(findTestUnitViewInActivePage());
		if (testRunnerViewPart != null) testRunnerViewPart.startTestRunListening(port, finalType, launch, fTestRunListeners);
	}
	
	public void addTestRunListener(ITestRunListener listener) {
		fTestRunListeners.add(listener);
	}
	
	public void removeTestRunListener(ITestRunListener listener) {
		fTestRunListeners.remove(listener);
	}

	private TestUnitView showTestUnitViewInActivePage(TestUnitView testRunner) {
		IWorkbenchPart activePart = null;
		IWorkbenchPage page = null;
		try {
			// TODO: have to force the creation of view part contents
			// otherwise the UI will not be updated
			if (testRunner != null && testRunner.isCreated()) return testRunner;
			page = getActivePage();
			if (page == null) return null;
			activePart = page.getActivePart();
			//	show the result view if it isn't shown yet
			return (TestUnitView) page.showView(TestUnitView.NAME);
		} catch (PartInitException pie) {
			log(pie);
			return null;
		} finally {
			//restore focus stolen by the creation of the result view
			if (page != null && activePart != null) page.activate(activePart);
		}
	}

	public TestUnitView findTestUnitViewInActivePage() {
		IWorkbenchPage page = getActivePage();
		if (page == null) return null;
		return (TestUnitView) page.findView(TestUnitView.NAME);
	}

	/**
	 * @param string
	 */
	public static void log(String string) {
		log(new Throwable(string));
	}

	/*
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(final ILaunch launch) {
		if (!fTrackedLaunches.remove(launch))
			return;
		getDisplay().asyncExec(new Runnable() {

			public void run() {
				TestUnitView testRunnerViewPart = findTestRunnerViewPartInActivePage();
				if (testRunnerViewPart != null && testRunnerViewPart.isCreated() && launch.equals(testRunnerViewPart.getLastLaunch())) testRunnerViewPart.reset();
			}
		});
	}

	private TestUnitView findTestRunnerViewPartInActivePage() {
		IWorkbenchPage page = getActivePage();
		if (page == null) return null;
		return (TestUnitView) page.findView(TestUnitView.NAME);
	}

	/*
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(final ILaunch launch) {
		if (!fTrackedLaunches.contains(launch)) return;

		ILaunchConfiguration config = launch.getLaunchConfiguration();
		IType launchedType= null;
		if (config != null) {

			String typeStr = launch.getAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR);

			if (typeStr != null && typeStr.trim().length() > 0) {
				IRubyElement element = RubyCore.create(typeStr);
				if (element instanceof IType)
					launchedType = (IType) element;
			}
		}
		fTrackedLaunches.remove(launch);
		
		final IType finalType= launchedType;
		final int finalPort = Integer.parseInt(launch.getAttribute(TESTUNIT_PORT_ATTR));
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				connectTestRunner(launch, finalType, finalPort);
			}
		});

	}

	/*
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		try {
			if (launch == null || launch.getLaunchConfiguration() == null || launch.getLaunchConfiguration().getType() == null) return;
			if (launch.getLaunchConfiguration().getType().getDelegate(launch.getLaunchMode()).getClass() != TestUnitLaunchConfigurationDelegate.class) {
				return;
			}
			fTrackedLaunches.add(launch);
		} catch (Exception ex) {
			log(ex);
		}
	}
}