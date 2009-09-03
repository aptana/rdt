package org.rubypeople.rdt.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.rubypeople.rdt.core.RubyCore;

/**
 * A Job which replaces what an old install handler did: set the binary scripts in JRuby to be executable (on all
 * non-win32 platforms).
 * 
 * @author cwilliams
 */
public class SetExecutableBits extends Job
{
	public SetExecutableBits()
	{
		super("Setting JRuby binaries to be executable");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return Status.OK_STATUS; // don't need to set an executable flag on windows
		// FIXME Add a shortcut that doesn't run all this stuff if the files have already had the flag set!
		try
		{
			Properties props = new Properties();
			InputStream inStream = FileLocator.openStream(Platform.getBundle("org.jruby"), new Path(
					"permissions.properties"), false);
			props.load(inStream);
			String raw = props.getProperty("permissions.executable");
			String[] paths = raw.split(",");
			for (int i = 0; i < paths.length; i++)
			{
				URL bundleURL = FileLocator.find(Platform.getBundle("org.jruby"), new Path(paths[i]), null);
				if (bundleURL == null)
					continue;
				URL fileURL = FileLocator.toFileURL(bundleURL);
				if (fileURL == null)
					continue;
				setExecutableBit(fileURL.getPath());
			}
		}
		catch (IOException e)
		{
			return new Status(IStatus.ERROR, RubyCore.PLUGIN_ID, 1, e.getMessage(), e);
		}

		return Status.OK_STATUS;
	}

	private void setExecutableBit(String filePath)
	{
		if (filePath == null)
			return;
		try
		{
			Process pr = Runtime.getRuntime().exec(new String[] { "chmod", "a+x", filePath }); //$NON-NLS-1$ //$NON-NLS-2$
			Thread chmodOutput = new StreamConsumer(pr.getInputStream());
			chmodOutput.setName("chmod output reader"); //$NON-NLS-1$
			chmodOutput.start();
			Thread chmodError = new StreamConsumer(pr.getErrorStream());
			chmodError.setName("chmod error reader"); //$NON-NLS-1$
			chmodError.start();
		}
		catch (IOException ioe)
		{
			RubyCore.log(ioe);
		}
	}

	public static class StreamConsumer extends Thread
	{
		InputStream is;
		byte[] buf;

		public StreamConsumer(InputStream inputStream)
		{
			super();
			this.setDaemon(true);
			this.is = inputStream;
			buf = new byte[512];
		}

		public void run()
		{
			try
			{
				int n = 0;
				while (n >= 0)
					n = is.read(buf);
			}
			catch (IOException ioe)
			{
			}
		}
	}
}
