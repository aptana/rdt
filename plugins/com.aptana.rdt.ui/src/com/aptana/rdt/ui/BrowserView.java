package com.aptana.rdt.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.core.RubyCore;

public abstract class BrowserView extends ViewPart
{

	public BrowserView()
	{
		super();
	}

	@Override
	public void createPartControl(Composite parent)
	{
		try
		{
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
			IWebBrowser browser = browserSupport.getExternalBrowser();
			browser.openURL(new URL(getURL()));
		}
		catch (PartInitException e)
		{
			RubyCore.log(e);
		}
		catch (MalformedURLException e)
		{
			RubyCore.log(e);
		}

		final ViewPart self = this;
		Job job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				self.getSite().getPage().hideView(self);
				self.dispose();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	abstract protected String getURL();

	@Override
	public void setFocus()
	{
		// do nothing
	}
}