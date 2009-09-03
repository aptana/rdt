package org.rubypeople.rdt.internal.ui.wizards;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

class UseJRubyWizardPage extends WizardPage implements IWizardPage, IPageChangedListener
{

	protected UseJRubyWizardPage()
	{
		super(""); //$NON-NLS-1$
		setTitle(NewWizardMessages.UseJRubyWizardPage_TTL);
		setDescription(NewWizardMessages.UseJRubyWizardPage_MSG_Description);
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
		label.setText(NewWizardMessages.UseJRubyWizardPage_MSG_Explanation_text);
		GridData data = new GridData();
		data.widthHint = 400;
		label.setLayoutData(data);
		setControl(main);
		((WizardPage) getWizard().getStartingPage()).setPageComplete(true);
		(getWizardDialog()).addPageChangedListener(this);
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
			((WizardPage) getWizard().getStartingPage()).setPageComplete(true);
	}

}
