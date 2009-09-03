package org.rubypeople.rdt.internal.testunit.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.internal.ui.wizards.NewElementWizard;
import org.rubypeople.rdt.testunit.wizards.RubyNewTestCaseWizardPage;
import org.rubypeople.rdt.testunit.wizards.RubyNewTestCaseWizardPageTwo;

public class NewTestCaseCreationWizard extends NewElementWizard {

	private RubyNewTestCaseWizardPage fPage1;
	private RubyNewTestCaseWizardPageTwo fPage2;

	public NewTestCaseCreationWizard() {
		setDefaultPageImageDescriptor(RubyPluginImages.DESC_WIZBAN_NEWCLASS);
		setDialogSettings(RubyPlugin.getDefault().getDialogSettings());
		setWindowTitle(WizardMessages.Wizard_title_new_testcase);
	}

	/*
	 * @see Wizard#createPages
	 */
	public void addPages() {
		super.addPages();
		fPage2 = new RubyNewTestCaseWizardPageTwo();
		fPage1 = new RubyNewTestCaseWizardPage(fPage2);
		addPage(fPage1);
		fPage1.init(getSelection());
		addPage(fPage2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rubypeople.rdt.internal.ui.wizards.NewElementWizard#finishPage(org
	 * .eclipse .core.runtime.IProgressMonitor)
	 */
	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		fPage1.createType(monitor); // use the full progress monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		boolean res = super.performFinish();
		if (res) {
			IResource resource = fPage1.getModifiedResource();
			if (resource != null) {
				selectAndReveal(resource);
				openResource((IFile) resource);
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rubypeople.rdt.internal.ui.wizards.NewElementWizard#getCreatedElement
	 * ()
	 */
	public IRubyElement getCreatedElement() {
		return fPage1.getCreatedType();
	}
}