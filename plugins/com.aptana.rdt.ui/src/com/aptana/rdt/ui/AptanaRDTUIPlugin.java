package com.aptana.rdt.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.rubypeople.rdt.core.RubyModelException;

import com.aptana.rdt.internal.ui.GemAutoInstallDialogJob;

/**
 * The activator class controls the plug-in life cycle
 */
public class AptanaRDTUIPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.rdt.ui";

	// The shared instance
	private static AptanaRDTUIPlugin plugin;

	/**
	 * The constructor
	 */
	public AptanaRDTUIPlugin()
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
		scheduleGemAutoInstall();
	}

	private void scheduleGemAutoInstall()
	{
		new GemAutoInstallDialogJob().schedule(1000);
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
	public static AptanaRDTUIPlugin getDefault()
	{
		return plugin;
	}

	public static void log(RubyModelException e)
	{
		getDefault().getLog().log(e.getStatus());
	}
}
