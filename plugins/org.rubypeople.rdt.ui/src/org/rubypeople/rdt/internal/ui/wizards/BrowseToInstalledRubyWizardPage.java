package org.rubypeople.rdt.internal.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.launching.VMStandin;

class BrowseToInstalledRubyWizardPage extends WizardPage implements IWizardPage, IPageChangedListener
{

	private Button fBrowseButton;
	private Text fText;
	private String defaultValue;

	protected BrowseToInstalledRubyWizardPage()
	{
		this(null);
	}

	protected BrowseToInstalledRubyWizardPage(String defaultValue)
	{
		super(""); //$NON-NLS-1$
		setTitle(NewWizardMessages.BrowseToInstalledRubyWizardPage_TTL);
		setDescription(NewWizardMessages.BrowseToInstalledRubyWizardPage_MSG_Description);
		this.defaultValue = defaultValue;
	}

	public void createControl(Composite parent)
	{
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		main.setLayout(layout);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(main, SWT.WRAP);
		label.setText(NewWizardMessages.BrowseToInstalledRubyWizardPage_MSG_Explanation_text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.widthHint = parent.getSize().x;
		label.setLayoutData(data);

		fText = new Text(main, SWT.SINGLE | SWT.BORDER);
		GridData textData = new GridData();
		textData.widthHint = 400;
		fText.setLayoutData(textData);
		fText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				validateVMLocation();
			}
		});

		fBrowseButton = new Button(main, SWT.PUSH | SWT.LEFT);
		fBrowseButton.setText(NewWizardMessages.BrowseToInstalledRubyWizardPage_LBL_Browse_button);
		fBrowseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setFilterPath(fText.getText());
				dialog.setMessage(NewWizardMessages.BrowseToInstalledRubyWizardPage_MSG_Browse_dialog);
				String newPath = dialog.open();
				fText.setText(newPath);
				validateVMLocation();
				super.widgetSelected(e);
			}
		});

		setControl(main);
		((WizardPage) getWizard().getStartingPage()).setPageComplete(false);
		getWizardDialog().addPageChangedListener(this);

		if (defaultValue != null)
		{
			fText.setText(defaultValue);
			validateVMLocation();
		}
	}

	protected void validateVMLocation()
	{
		final IVMInstallType type = getStandardVMType();
		if (type == null)
		{
			setErrorMessage(NewWizardMessages.BrowseToInstalledRubyWizardPage_ERR_MSG_Unable_find_standard_vm_metadata);
			((WizardPage) getWizard().getStartingPage()).setPageComplete(false);
			getContainer().updateButtons();
			return;
		}
		String location = fText.getText();
		if (location == null || location.trim().length() == 0)
		{
			setErrorMessage(NewWizardMessages.BrowseToInstalledRubyWizardPage_ERR_MSG_Location_empty);
			((WizardPage) getWizard().getStartingPage()).setPageComplete(false);
			getContainer().updateButtons();
			return;
		}
		final IStatus[] temp = new IStatus[1];
		final File tempFile = new File(location);
		Runnable r = new Runnable()
		{
			/**
			 * @see java.lang.Runnable#run()
			 */
			public void run()
			{
				temp[0] = type.validateInstallLocation(tempFile);
			}
		};
		BusyIndicator.showWhile(getShell().getDisplay(), r);
		if (temp[0].getSeverity() == IStatus.ERROR)
		{
			setErrorMessage(temp[0].getMessage());
			((WizardPage) getWizard().getStartingPage()).setPageComplete(false);
			getContainer().updateButtons();
		}
		else if (temp[0].getSeverity() == IStatus.WARNING)
		{
			setMessage(temp[0].getMessage(), IMessageProvider.WARNING);
		}
		else
		{
			setErrorMessage(null);
			setMessage(null);
			((WizardPage) getWizard().getStartingPage()).setPageComplete(true);
			getContainer().updateButtons();
		}
	}

	private IVMInstallType getStandardVMType()
	{
		return RubyRuntime.getVMInstallType(IRubyLaunchConfigurationConstants.ID_STANDARD_VM_TYPE);
	}

	public void addVM()
	{
		VMStandin standin = new VMStandin(getStandardVMType(), String.valueOf(System.currentTimeMillis()));
		standin.setName(NewWizardMessages.BrowseToInstalledRubyWizardPage_LBL_Standard_ruby_entry_name);
		standin.setInstallLocation(new File(fText.getText()));
		IVMInstall vm = standin.convertToRealVM();
		try
		{
			RubyRuntime.setDefaultVMInstall(vm, new NullProgressMonitor(), true);
		}
		catch (CoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private WizardDialog getWizardDialog()
	{
		return (WizardDialog) getContainer();
	}

	public void dispose()
	{
		if (getWizardDialog() != null)
			(getWizardDialog()).removePageChangedListener(this);
		super.dispose();
	}

	public void pageChanged(PageChangedEvent event)
	{
		Object page = event.getSelectedPage();
		if (page.equals(this))
			validateVMLocation();
	}

}
