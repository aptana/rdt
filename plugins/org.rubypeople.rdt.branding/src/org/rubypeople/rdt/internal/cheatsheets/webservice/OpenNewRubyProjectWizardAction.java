package org.rubypeople.rdt.internal.cheatsheets.webservice;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.util.PixelConverter;
import org.rubypeople.rdt.internal.ui.wizards.RubyProjectWizard;
import org.rubypeople.rdt.internal.ui.wizards.RubyProjectWizardFirstPage;


/**
 * @author markus
 *
 */
public class OpenNewRubyProjectWizardAction extends Action implements ICheatSheetAction  {
	public OpenNewRubyProjectWizardAction() {
		super("OpenProject"); //$NON-NLS-1$
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run() {
		run(new String [] {}, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetAction#run(java.lang.String[], org.eclipse.ui.cheatsheets.ICheatSheetManager)
	 */
	public void run(String[] params, ICheatSheetManager manager) {
		RubyProjectWizard wizard = new RubyProjectWizard();
	
		Shell shell = RubyPlugin.getActiveWorkbenchShell();
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		
		WizardDialog dialog= new WizardDialog(shell, wizard);
		if (shell != null) {
			PixelConverter converter= new PixelConverter(shell);
			dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));
		}
		dialog.create();
		if (params.length > 0) {
			((RubyProjectWizardFirstPage) wizard.getPages()[0]).setName(params[0]);
		}
		int res= dialog.open();
		
		notifyResult(res == Window.OK);
	}
}
