package org.rubypeople.rdt.internal.cheatsheets.webservice;

import java.lang.reflect.Method;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

public class OpenRunConfigurationAction extends Action implements ICheatSheetAction
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetAction#run(java.lang.String[],
	 * org.eclipse.ui.cheatsheets.ICheatSheetManager)
	 */
	public void run(String[] params, ICheatSheetManager manager)
	{
		LaunchConfigurationManager launchManager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		LaunchGroupExtension group = null;
		try
		{
			Method method = LaunchConfigurationManager.class.getMethod("getDefaultLaunchGroup",
					new Class[] { String.class });
			group = (LaunchGroupExtension) method.invoke(launchManager,
					new Object[] { IDebugUIConstants.ID_RUN_LAUNCH_GROUP });
		}
		catch (Exception e)
		{
			// ignore
		}
		if (group == null)
		{
			group = launchManager.getDefaultLaunchGroup(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
		}
		LaunchConfigurationsDialog dialog = new LaunchConfigurationsDialog(DebugUIPlugin.getShell(), group);
		dialog.setOpenMode(LaunchConfigurationsDialog.LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED);
		dialog.open();

	}

}
