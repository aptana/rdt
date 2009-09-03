/*
 * Created on 16.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.rubypeople.rdt.internal.debug.ui.actions;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

/**
 * @author Markus
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AbstractInspectAction {

    protected IWorkbenchPage page;

    public void init(IViewPart view) {
//    	System.out.println("view:" + view) ;
    	page = view.getSite().getPage() ;
    }

    protected void showExpressionView() {
        IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
        if (part == null) {
            try {
                page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
            } catch (PartInitException e) {
                RdtDebugUiPlugin.log(e);
            }
        } else {
            page.bringToTop(part);
        }
    
    }

    protected ISelection selection;

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    
    }

}
