package com.aptana.rdt.internal.rake.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

public class RunRakeAction extends Action
{

	private IProject project;
	private String task;
	private String description;

	public RunRakeAction(IProject project, String task, String description)
	{
		this.project = project;
		this.task = task;
		this.description = description;
	}

	@Override
	public void run()
	{
		Job job = new Job(MessageFormat.format("Running rake task {0}", task))
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				getRakeHelper().runRakeTask(project, task, "", monitor);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	protected IRakeHelper getRakeHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	@Override
	public String getText()
	{
		String[] parts = task.split(":");
		return parts[parts.length - 1];
	}

	@Override
	public String getToolTipText()
	{
		return description;
	}
}
