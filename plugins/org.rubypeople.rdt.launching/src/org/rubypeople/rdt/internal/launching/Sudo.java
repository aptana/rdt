package org.rubypeople.rdt.internal.launching;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

abstract class Sudo
{

	synchronized static final String getPassword(final String msg)
	{
		final String[] password = new String[1];
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				PasswordDialog dialog = new PasswordDialog(null, "Enter local sudo password", msg, null, null);
				if (dialog.open() == Dialog.OK)
				{
					password[0] = dialog.getValue();
				}
			}
		});
		return password[0];
	}

}
