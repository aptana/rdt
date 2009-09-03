package org.rubypeople.rdt.debug.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.RubyRuntime;

/**
 * InstallDeveloperToolsDialog
 */
public class InstallDeveloperToolsDialog extends Dialog
{

	/**
	 * InstallDeveloperToolsDialog
	 * 
	 * @param parentShell
	 */
	public InstallDeveloperToolsDialog(Shell parentShell)
	{
		super(parentShell);
		setShellStyle(getDefaultOrientation() | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
	}

	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(RdtDebugUiMessages.ToolChainNotFound_title);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite control = new Composite(composite, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		control.setLayout(layout);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (Platform.getOS().equals(Platform.OS_MACOSX))
		{
			createMacDialog(control);
		}
		else
		{
			createLinuxDialog(control);
		}
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		control.setLayoutData(data);

		return composite;
	}

	private void createLinuxDialog(Composite parent)
	{
		Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setText(RdtDebugUiMessages.ToolChainNotFound_msg);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.widthHint = 300;
		label.setLayoutData(data);
	}

	private void createMacDialog(Composite parent)
	{
		FontData[] fds = parent.getFont().getFontData();
		for (FontData fd : fds)
		{
			fd.setHeight(fd.getHeight() + 4);
			fd.setStyle(SWT.BOLD);
		}
		final Font font = new Font(parent.getDisplay(), fds);
		fds = parent.getFont().getFontData();
		for (FontData fd : fds)
		{
			fd.setHeight(fd.getHeight() + 2);
			fd.setStyle(SWT.BOLD);
		}
		final Font font2 = new Font(parent.getDisplay(), fds);

		Composite top = new Composite(parent, SWT.NONE);
		top.addDisposeListener(new DisposeListener()
		{

			public void widgetDisposed(DisposeEvent e)
			{
				if (font != null && !font.isDisposed())
				{
					font.dispose();
				}
				if (font2 != null && !font2.isDisposed())
				{
					font2.dispose();
				}
			}

		});
		GridLayout tLayout = new GridLayout(2, false);
		tLayout.marginHeight = 0;
		tLayout.marginWidth = 0;
		tLayout.marginBottom = 10;
		top.setLayout(tLayout);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridData iconData = new GridData(SWT.FILL, SWT.TOP, true, false);
		iconData.verticalIndent = 5;
		Label icon = new Label(top, SWT.LEFT);
		icon.setImage(RdtDebugUiPlugin.getImage("images/radrails32.png"));
		icon.setLayoutData(iconData);
		Composite rightTop = new Composite(top, SWT.NONE);
		GridLayout rtLayout = new GridLayout(1, false);
		rtLayout.marginHeight = 0;
		rtLayout.marginWidth = 0;
		rightTop.setLayout(rtLayout);
		Label title = new Label(rightTop, SWT.LEFT);
		title.setFont(font);
		title.setText("Welcome to the Mac OS X version of RadRails!");
		title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Label line1 = new Label(rightTop, SWT.LEFT);
		line1.setText("Rails for the Mac requires the Mac OS X developer tools be installed.");
		Label line2 = new Label(rightTop, SWT.LEFT);
		line2.setText("These tools came with your Mac but are not yet installed.");
		Label installDiskLabel = new Label(parent, SWT.LEFT);
		installDiskLabel.setImage(RdtDebugUiPlugin.getImage("images/osx_disc.png"));
		installDiskLabel.setLayoutData(new GridData(SWT.CENTER, SWT.END, true, false));
		GridData oData = new GridData(SWT.FILL, SWT.FILL, true, false);
		oData.horizontalIndent = 10;
		Label optionA = new Label(parent, SWT.LEFT);
		optionA.setLayoutData(oData);
		optionA.setFont(font2);
		optionA.setText("Please insert your OS X installation disk");
		GridData iData = new GridData(SWT.FILL, SWT.FILL, true, false);
		iData.horizontalIndent = 25;
		Label instructionA = new Label(parent, SWT.LEFT);
		instructionA.setLayoutData(iData);
		instructionA
				.setText("1) Find the Xcode Tools directory.\n2) Double-click the XcodeTools.mpkg file to begin installation.\n3) Once Xcode is installed, please restart RadRails.\n");
		Label optionB = new Label(parent, SWT.LEFT);
		GridData oData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		oData2.horizontalIndent = 10;
		oData2.verticalIndent = 10;
		optionB.setFont(font2);
		optionB.setLayoutData(oData2);
		optionB.setText("No installation disk?");
		Link instructionsB = new Link(parent, SWT.LEFT);
		instructionsB
				.setText("1) Go to <a>http://developer.apple.com/tools/download/</a>\n2) Download Xcode\n3) Install from the downloaded DMG file.\n4) Once Xcode is installed, please restart RadRails.");
		instructionsB.setLayoutData(iData);
		instructionsB.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					if (support != null)
					{
						support.createBrowser(null).openURL(new URL("http://developer.apple.com/tools/download/"));
					}
				}
				catch (PartInitException e1)
				{
					RdtDebugUiPlugin.log(e1);
				}
				catch (MalformedURLException e1)
				{
					RdtDebugUiPlugin.log(e1);
				}
			}

		});

	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	private static boolean osAndVMNeedCompiling()
	{
		if (RubyRuntime.currentVMIsCygwin())
			return true;
		if (RubyRuntime.currentVMIsJRuby())
			return false;
		if (Platform.getOS().equals(Platform.OS_WIN32))
			return false;
		return true;
	}

	private static boolean hasMake()
	{
		if (RubyRuntime.currentVMIsCygwin())
		{
			IVMInstall install = RubyRuntime.getDefaultVMInstall();
			File location = install.getInstallLocation();
			String[] binDirs = new String[] { "bin", "usr/sbin", "usr/bin" };
			for (int i = 0; i < binDirs.length; i++)
			{
				File exe = new File(location.getAbsolutePath() + File.separator + binDirs[i] + File.separator
						+ "make.exe");
				if (exe.exists() && exe.isFile() /* && exe.canExecute() */)
				{
					return true;
				}
			}
		}
		IPath path = RubyCore.checkSystemPath("make");
		if (path != null && path.toFile().exists())
		{
			return true;
		}
		path = RubyCore.checkCommonBinLocations("make");
		if (path != null && path.toFile().exists())
		{
			return true;
		}
		return false;
	}

	/**
	 * Should show
	 * 
	 * @return - true if dialog should show
	 */
	public static boolean shouldShow()
	{
		return osAndVMNeedCompiling() && !hasMake();
	}

}
