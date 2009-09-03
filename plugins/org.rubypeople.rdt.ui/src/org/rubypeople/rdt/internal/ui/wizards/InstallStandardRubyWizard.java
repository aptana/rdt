package org.rubypeople.rdt.internal.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rubypeople.rdt.internal.launching.LaunchingPlugin;
import org.rubypeople.rdt.internal.ui.RubyInstalledDetector;
import org.rubypeople.rdt.internal.ui.RubyPlugin;

public class InstallStandardRubyWizard extends Wizard implements INewWizard
{

	private static final String WINDOWS_INSTALL_URL = "http://www.aptana.com/ruby/install/windows"; //$NON-NLS-1$
	private static final String LINUX_INSTALL_URL = "http://www.aptana.com/ruby/install/linux"; //$NON-NLS-1$
	private static final String MACOSX_INSTALL_URL = "http://www.aptana.com/ruby/install/macosx"; //$NON-NLS-1$

	private static final String RUBY_BROWSER_ID = RubyPlugin.getPluginId() + ".ruby.download.browser"; //$NON-NLS-1$

	public InstallStandardRubyWizard()
	{
		setWindowTitle(NewWizardMessages.InstallStandardRubyWizard_TTL_Window);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages()
	{
		addPage(new InstallRubyWizardPage());
	}

	@Override
	public boolean performFinish()
	{
		IWizardPage page = getContainer().getCurrentPage();
		// If user finished choosing to install (and isn't on Windows), pop up a browser telling them how they can
		// install
		if (page instanceof InstallRubyWizardPage && ((InstallRubyWizardPage) page).downloadSelected())
		{
			// TODO Set some special pref telling us to change to std ruby if we detect it...
			openBrowser(getURL());
		}
		else if (page instanceof BrowseToInstalledRubyWizardPage)
		{
			// Add the vm!
			((BrowseToInstalledRubyWizardPage) page).addVM();
		}
		else if (page instanceof UseJRubyWizardPage)
		{
			// Store value that user is explicitly using JRuby so we don't bug them again.
			Preferences store = LaunchingPlugin.getDefault().getPluginPreferences();
			if (store != null)
				store.setValue(LaunchingPlugin.USING_INCLUDED_JRUBY, true);
		}
		return true;
	}

	@Override
	public void dispose()
	{
		RubyInstalledDetector.markFinished();
		super.dispose();
	}

	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
	}

	private String getURL()
	{
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return WINDOWS_INSTALL_URL;

		if (Platform.getOS().equals(Platform.OS_MACOSX))
			return MACOSX_INSTALL_URL;

		return LINUX_INSTALL_URL;
	}

	private void openBrowser(String url)
	{
		try
		{
			IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
			IWebBrowser browser = support.createBrowser(RUBY_BROWSER_ID);
			browser.openURL(new URL(url));
		}
		catch (PartInitException e)
		{
			RubyPlugin.log(e);
		}
		catch (MalformedURLException e)
		{
			RubyPlugin.log(e);
		}
	}

	@Override
	public boolean needsPreviousAndNextButtons()
	{
		return true;
	}
}
