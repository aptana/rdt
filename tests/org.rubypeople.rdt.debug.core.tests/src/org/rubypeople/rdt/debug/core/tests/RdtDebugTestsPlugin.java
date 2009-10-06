/**
 * 
 */
package org.rubypeople.rdt.debug.core.tests;

import org.eclipse.core.runtime.Plugin;

/**
 * @author Chris
 * 
 */
public class RdtDebugTestsPlugin extends Plugin {

	// The shared instance.
	private static RdtDebugTestsPlugin plugin;

	/**
	 * The constructor.
	 */
	public RdtDebugTestsPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Plugin getDefault() {
		return plugin;
	}

}
