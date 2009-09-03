package org.rubypeople.rdt.internal.ui.wizards;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

class InstallRubyWizardPage extends WizardPage implements IWizardPage, IPageChangedListener
{

	protected IWizardPage fNextPage;
	private Button installRubyButton;
	private Button useJrubyButton;
	private Button browseButton;

	protected InstallRubyWizardPage()
	{
		super(""); //$NON-NLS-1$
		setTitle(NewWizardMessages.InstallRubyWizardPage_TTL);
		setDescription(NewWizardMessages.InstallRubyWizardPage_MSG_Description);
	}

	public void createControl(Composite parent)
	{
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		main.setLayout(layout);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group group = new Group(main, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setText(NewWizardMessages.InstallRubyWizardPage_LBL_Options);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 100;
		group.setLayoutData(gridData);

		// Install Ruby
		installRubyButton = new Button(group, SWT.RADIO);
		installRubyButton.setText(NewWizardMessages.InstallRubyWizardPage_LBL_Install_Ruby);
		installRubyButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (Platform.getOS().equals(Platform.OS_WIN32))
				{
					setNextPage(new DownloadRubyWizardPage());
				}
				else
				{
					setNextPage(null);
				}
				super.widgetSelected(e);
			}
		});

		// Browse to installed ruby
		browseButton = new Button(group, SWT.RADIO);
		browseButton.setText(NewWizardMessages.InstallRubyWizardPage_LBL_Browse_to_installed_ruby);
		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setNextPage(new BrowseToInstalledRubyWizardPage());
				setPageComplete(false);
				super.widgetSelected(e);
			}
		});

		// Use JRuby
		useJrubyButton = new Button(group, SWT.RADIO);
		useJrubyButton.setText(NewWizardMessages.InstallRubyWizardPage_LBL_Use_JRuby);
		useJrubyButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setNextPage(new UseJRubyWizardPage());
				super.widgetSelected(e);
			}
		});
		setControl(main);
		setPageComplete(false);
		(getWizardDialog()).addPageChangedListener(this);
	}

	@Override
	public void dispose()
	{
		if (getWizardDialog() != null)
			(getWizardDialog()).removePageChangedListener(this);
		super.dispose();
	}

	private WizardDialog getWizardDialog()
	{
		return (WizardDialog) getContainer();
	}

	protected void setNextPage(IWizardPage nextPage)
	{
		fNextPage = nextPage;
		if (fNextPage != null)
			fNextPage.setWizard(getWizard());
		else
			((WizardPage) getWizard().getStartingPage()).setPageComplete(true);
		getContainer().updateButtons();
	}

	@Override
	public IWizardPage getNextPage()
	{
		return fNextPage;
	}

	@Override
	public boolean canFlipToNextPage()
	{
		return !downloadSelected() || Platform.getOS().equals(Platform.OS_WIN32);
	}

	public boolean downloadSelected()
	{
		return installRubyButton.getSelection();
	}

	public void pageChanged(PageChangedEvent event)
	{
		Object page = event.getSelectedPage();
		if (page.equals(this))
		{
			setPageComplete(false);
		}
	}

}
