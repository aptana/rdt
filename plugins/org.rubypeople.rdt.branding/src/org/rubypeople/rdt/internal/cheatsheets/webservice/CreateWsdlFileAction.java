package org.rubypeople.rdt.internal.cheatsheets.webservice;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.rubypeople.rdt.internal.ui.RubyPlugin;


public class CreateWsdlFileAction extends Action implements ICheatSheetAction {


	public void run(String[] params, ICheatSheetManager manager) {

		BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
		
	
		String projectName = params.length > 0 ? params[0] : "Unbekannt" ; 
		IProject project = RubyPlugin.getWorkspace().getRoot().getProject(projectName) ;
		
		if (project == null) {
			// show error dialog
			return ;
		}

		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(project));
		WizardDialog dialog = new WizardDialog(RubyPlugin.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.getShell().setText(wizard.getWindowTitle());
		int result = dialog.open();
		notifyResult(result==WizardDialog.OK);
	}	
	
}
