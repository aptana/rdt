package org.rubypeople.rdt.internal.cheatsheets;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class RdtPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.rubypeople.rdt.branding"; //$NON-NLS-1$
	protected static RdtPlugin plugin;
	
	public RdtPlugin() {
		plugin = this;
	}
	
	public static RdtPlugin getDefault() {
		return plugin;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}

