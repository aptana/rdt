package org.rubypeople.rdt.debug.ui.tests;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class RdtDebugUiTestsPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static RdtDebugUiTestsPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public RdtDebugUiTestsPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Plugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
}
