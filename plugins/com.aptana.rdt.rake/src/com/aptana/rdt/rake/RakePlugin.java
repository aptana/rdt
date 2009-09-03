package com.aptana.rdt.rake;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.rake.RakeTasksHelper;

/**
 * The activator class controls the plug-in life cycle
 */
public class RakePlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.rdt.rake";

	private static final String RAKE = "rake";

	// The shared instance
	private static RakePlugin plugin;

	private IRakeHelper rakeHelper;

	/**
	 * The constructor
	 */
	public RakePlugin()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{

		plugin = this;
		super.start(context);
		rakeHelper = RakeTasksHelper.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RakePlugin getDefault()
	{
		return plugin;
	}

	public String getRakePath()
	{
		// if user has already configured, just use what they put in.
		String path = getSavedPath(PreferenceConstants.PREF_RAKE_PATH);
		if (path != null && path.trim().length() > 0)
			return path;
		return buildBinExecutablePath(RAKE);
	}

	private String getSavedPath(String prefKey)
	{
		String path = getPreferenceStore().getString(prefKey);
		if (path == null || path.trim().length() == 0)
			return null;
		if (path.endsWith(".bat") || path.endsWith(".cmd"))
		{
			return path.substring(0, path.length() - 4);
		}
		return path;
	}

	private String buildBinExecutablePath(String command)
	{
		// Check the bin directory where ruby executable is.
		IPath path = RubyRuntime.checkInterpreterBin(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		// try a bin subdir of gem install directory, then try system path
		path = AptanaRDTPlugin.checkBinDir(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		// try system path
		path = RubyCore.checkSystemPath(command);
		if (path != null && path.toFile().exists())
			return path.toOSString();

		return null;
	}

	public static void log(String message, Exception e)
	{
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, -1, message, e));
	}

	public static void log(Exception e)
	{
		log(e.getMessage(), e);
	}

	public IRakeHelper getRakeHelper()
	{
		return rakeHelper;
	}

}
