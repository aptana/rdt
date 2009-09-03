package org.rubypeople.rdt.internal.ui.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

class DownloadRubyWizardPage extends WizardPage implements IWizardPage
{

	private static final String RUBY_INSTALLER_EXE = "ruby-installer.exe"; //$NON-NLS-1$
	private static final String RUBY_INSTALLER_URL = "http://rubyforge.org/frs/download.php/47082/ruby186-27_rc2.exe"; //$NON-NLS-1$

	private static final int BUFFER_SIZE = 64 * 1024; // 64k
	private static final int READ_TIMEOUT = 30000; // 30 seconds timeout on reads
	private static final int CONNECT_TIMEOUT = 15000;
	private static final long SLEEP_TIME = 100; // sleep .1sec between reads

	protected boolean fInstalledProperly;
	private IWizardPage fNextPage;
	private Label downloadButton;
	private Image fEnabledImage;
	private Image fDisabledImage;
	private MouseAdapter downloadListener;

	protected DownloadRubyWizardPage()
	{
		super(""); //$NON-NLS-1$
		setTitle(NewWizardMessages.DownloadRubyWizardPage_TTL);
		setDescription(NewWizardMessages.DownloadRubyWizardPage_MSG_Description);
	}

	public void createControl(Composite parent)
	{
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		main.setLayout(layout);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(main, SWT.WRAP);
		label.setText(NewWizardMessages.DownloadRubyWizardPage_MSG_Explanation_text);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.widthHint = 400;
		label.setLayoutData(data);

		downloadButton = new Label(main, SWT.None);
		downloadButton.setImage(getEnabledButtonImage());
		downloadListener = new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				Display.getCurrent().asyncExec(new Runnable()
				{
					public void run()
					{
						downloadButton.setImage(getDisabledButtonImage());
					}
				});
				downloadRuby();
				downloadButton.removeMouseListener(this);
			}
		};
		downloadButton.addMouseListener(downloadListener);

		GridData downloadButtonData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		downloadButton.setLayoutData(downloadButtonData);

		setControl(main);
	}

	private Image getEnabledButtonImage()
	{
		if (fEnabledImage == null)
		{
			fEnabledImage = RubyPlugin.imageDescriptorFromPlugin(RubyPlugin.PLUGIN_ID, "icons/full/install_ruby.png")
					.createImage();
		}
		return fEnabledImage;
	}

	private Image getDisabledButtonImage()
	{
		if (fDisabledImage == null)
		{
			fDisabledImage = RubyPlugin.imageDescriptorFromPlugin(RubyPlugin.PLUGIN_ID,
					"icons/full/install_ruby_disabled.png").createImage();
		}
		return fDisabledImage;
	}

	@Override
	public void dispose()
	{
		if (fEnabledImage != null)
		{
			fEnabledImage.dispose();
			fEnabledImage = null;
		}
		if (fDisabledImage != null)
		{
			fDisabledImage.dispose();
			fDisabledImage = null;
		}
		super.dispose();
	}

	protected void downloadRuby()
	{
		try
		{
			getContainer().run(true, true, new IRunnableWithProgress()
			{

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					if (monitor == null)
						monitor = new NullProgressMonitor();

					download(monitor);

					if (monitor.isCanceled())
						return;
					try
					{
						monitor.subTask("Running installer...");
						Process p = Runtime.getRuntime().exec(getSaveLocation());
						int installerExit = p.waitFor();
						if (installerExit != 0)
						{
							UIJob job = new UIJob("") //$NON-NLS-1$
							{
								@Override
								public IStatus runInUIThread(IProgressMonitor monitor)
								{
									setErrorMessage(NewWizardMessages.DownloadRubyWizardPage_ERR_Installer_exit_failure);
									return Status.OK_STATUS;
								}
							};
							job.setSystem(true);
							job.schedule();
							return;
						}
						fInstalledProperly = true;
					}
					catch (IOException e)
					{
						setErrorMessage(NewWizardMessages.DownloadRubyWizardPage_ERR_Launching_installer);
					}
				}

				private void download(IProgressMonitor monitor)
				{
					String fileURL = RUBY_INSTALLER_URL;
					InputStream inStream = null;
					OutputStream outStream = null;
					try
					{
						URLConnection connection = new URL(fileURL).openConnection();
						connection.setDoOutput(false);
						connection.setDoInput(true);
						connection.setReadTimeout(READ_TIMEOUT);
						connection.setAllowUserInteraction(false);
						connection.setConnectTimeout(CONNECT_TIMEOUT);
						connection.setUseCaches(false);
						connection.setRequestProperty("Accept", //$NON-NLS-1$
								"application/zip, application/octet-stream, *; q=.2, */*; q=.2"); //$NON-NLS-1$
						connection.connect();

						int length = connection.getContentLength();
						if (length != -1)
							monitor.beginTask(NewWizardMessages.DownloadRubyWizardPage_LBL_Downloading_ruby_installer,
									length);

						inStream = connection.getInputStream();
						outStream = new FileOutputStream(getSaveLocation());
						// R E A D / W R I T E by chunks
						// we know length > 0
						int chunkSize = (int) Math.min(BUFFER_SIZE, length);
						long chunks = length / chunkSize;
						int lastChunkSize = (int) (length % chunkSize);
						// code will work even when lastChunkSize = 0 or chunks = 0;
						byte[] ba = new byte[chunkSize];
						for (long i = 0; i < chunks; i++)
						{
							if (monitor.isCanceled())
							{
								return;
							}
							int bytesRead = readBytesBlocking(inStream, ba, 0, chunkSize, READ_TIMEOUT);
							if (bytesRead != chunkSize)
							{
								throw new IOException();
							}
							outStream.write(ba);
							monitor.worked(bytesRead);
						}

						// R E A D / W R I T E last chunk, if any
						if (lastChunkSize > 0)
						{
							int bytesRead = readBytesBlocking(inStream, ba, 0
							/* offset in ba */, lastChunkSize, READ_TIMEOUT);
							if (bytesRead != lastChunkSize)
							{
								throw new IOException();
							}
							outStream.write(ba, 0/* offset */, lastChunkSize/* length */);
						}
					}
					catch (IOException e)
					{
						UIJob job = new UIJob("") //$NON-NLS-1$
						{
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor)
							{
								setErrorMessage(NewWizardMessages.DownloadRubyWizardPage_ERR_Downloading_ruby_installer);
								return Status.OK_STATUS;
							}
						};
						job.setSystem(true);
						job.schedule();
						return;
					}
					finally
					{
						try
						{
							if (inStream != null)
								inStream.close();
						}
						catch (IOException e)
						{
						}
						try
						{
							if (outStream != null)
								outStream.close();
						}
						catch (IOException e)
						{
						}
					}
				}

				private int readBytesBlocking(InputStream in, byte b[], int off, int len, int timeoutInMillis)
						throws IOException
				{
					int totalBytesRead = 0;
					int bytesRead;
					long whenToGiveUp = System.currentTimeMillis() + timeoutInMillis;
					while (totalBytesRead < len
							&& (bytesRead = in.read(b, off + totalBytesRead, len - totalBytesRead)) >= 0)
					{
						if (bytesRead == 0)
						{
							try
							{
								if (System.currentTimeMillis() >= whenToGiveUp)
								{
									throw new IOException("timeout"); //$NON-NLS-1$
								}
								// don't hammer the system and suck up all the CPU
								// beating a tight loop when there are no chars.
								// If this keeps up we may trigger a java.net.SocketTimeoutException exception.
								Thread.sleep(SLEEP_TIME);
							}
							catch (InterruptedException e)
							{
							}
						}
						else
						{
							totalBytesRead += bytesRead;
							whenToGiveUp = System.currentTimeMillis() + timeoutInMillis;
						}
					}
					return totalBytesRead;
				}
			});
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fInstalledProperly)
		{
			setNextPage(new BrowseToInstalledRubyWizardPage("C:\\ruby")); //$NON-NLS-1$
			setErrorMessage(null);
			getContainer().updateButtons();
			// Automatically advance user to next page
			getContainer().showPage(fNextPage);
		}
	}

	protected void setNextPage(IWizardPage page)
	{
		fNextPage = page;
		if (fNextPage == null)
			return;
		fNextPage.setWizard(getWizard());
		((WizardPage) getWizard().getStartingPage()).setPageComplete(false);
	}

	protected String getSaveLocation()
	{
		String value = System.getProperty("user.home"); //$NON-NLS-1$
		if (value != null && value.trim().length() > 0)
		{
			return value + File.separator + "Desktop" + File.separator + RUBY_INSTALLER_EXE; //$NON-NLS-1$
		}
		return "C:" + File.separator + RUBY_INSTALLER_EXE; //$NON-NLS-1$
	}

	@Override
	public IWizardPage getNextPage()
	{
		return fNextPage;
	}

	@Override
	public boolean isPageComplete()
	{
		return canFlipToNextPage();
	}

	@Override
	public boolean canFlipToNextPage()
	{
		return fNextPage != null;
	}

}
