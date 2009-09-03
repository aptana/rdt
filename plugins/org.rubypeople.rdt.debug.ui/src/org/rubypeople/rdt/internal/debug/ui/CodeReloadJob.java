/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT
 * is subject to the "Common Public License (CPL) v 1.0". You may not use RDT
 * except in compliance with the License. For further information see
 * org.rubypeople.rdt/rdt.license.
 */
package org.rubypeople.rdt.internal.debug.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.rubypeople.rdt.debug.core.RdtDebugCorePlugin;
import org.rubypeople.rdt.internal.debug.core.model.IRubyDebugTarget;

public class CodeReloadJob extends Job
{

	private String filename;
	private IRubyDebugTarget debugTarget;

	public CodeReloadJob(IRubyDebugTarget debugTarget, String filename)
	{
		super("Loading " + filename);
		this.filename = filename;
		this.debugTarget = debugTarget;
	}

	public IStatus run(IProgressMonitor monitor)
	{
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		final IStatus status = debugTarget.load(filename);
		if (status == null)
			return new Status(Status.WARNING, RdtDebugCorePlugin.getPluginIdentifier(), -1,
					"Did not receive a status from the code reload operation for " + filename, null);
		if (!status.isOK())
		{
			DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable()
			{
				public void run()
				{
					MessageDialog.openInformation(DebugUIPlugin.getStandardDisplay().getActiveShell(), "Error loading "
							+ filename, status.getMessage());
				}
			});
		}
		return status;
	}
}