package org.rubypeople.rdt.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class ShamApplicationLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
	private static int launches ;

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		launches += 1 ;
	}
	
	public static int getLaunches() {
		return launches;
	}
	
	public static void resetLaunches() {
		launches = 0 ;
	}
}
