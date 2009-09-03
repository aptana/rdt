package org.rubypeople.rdt.internal.cheatsheets.webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.cheatsheets.RdtPlugin;
import org.rubypeople.rdt.internal.ui.RubyPlugin;


public class CopyContentAction extends Action implements ICheatSheetAction {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetAction#run(java.lang.String[], org.eclipse.ui.cheatsheets.ICheatSheetManager)
	 */
	public void run(String[] params, ICheatSheetManager manager) {		
		File sourceFile = new File(RubyCore.getOSDirectory(RdtPlugin.getDefault()) + params[0]);
		if (!sourceFile.exists()) {
		    this.notifyResult(false);
		    return;
		}
		
		IFile dest = RubyPlugin.getWorkspace().getRoot().getFile(new Path(params[1]));
		if (dest == null || !dest.exists()) {
		    this.notifyResult(false);
		    return;
		}
		try {
            InputStream inputStream = new FileInputStream(sourceFile);
            dest.setContents(inputStream, true, true, null);
            this.notifyResult(true);
        } catch (Exception e) {
		    this.notifyResult(false);
        }
	}

}
