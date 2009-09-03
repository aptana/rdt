package org.rubypeople.rdt.internal.debug.ui.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class RubyApplicationTabGroup
	extends AbstractLaunchConfigurationTabGroup {

	public RubyApplicationTabGroup() {
		super();
	}

	/**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog, String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new RubyEntryPointTab(),
			new RubyArgumentsTab(),
			new RubyEnvironmentTab(),
			new EnvironmentTab(),
			new CommonTab()
		};
		setTabs(tabs);
	}

}
