/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.refactoring.action;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.Action;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.RubyRefactoring;
import org.rubypeople.rdt.refactoring.ui.RubyRefactoringWizard;

public class RefactoringAction extends Action {


	private Class<? extends RubyRefactoring> refactoringClass;
	private IRefactoringContext selectionProvider;
	private RubyRefactoring fRefactoring;

	public RefactoringAction(Class<? extends RubyRefactoring> refactoringClass, String refactoringName, IRefactoringContext selectionProvider) {
		setText(refactoringName + "..."); //$NON-NLS-1$
		this.refactoringClass = refactoringClass;
		this.selectionProvider = selectionProvider;
	}

	public void run() {		
		try {
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true)) {
				RubyRefactoring refactoring = getRefactoring();
				RubyRefactoringWizard wizard = new RubyRefactoringWizard(refactoring);
				wizard.setWindowTitle(refactoring.getName());
				RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				op.run(shell, refactoring.getName());
			}
		} catch (Exception e) {
			RubyCore.log(e);
		}
	}
	
	// XXX Temporarily comment this out to see if it's causing http://support.aptana.com/asap/browse/ROR-188
//	@Override
//	public boolean isEnabled() {
//		try {
//			final RubyRefactoring refactoring = getRefactoring();
//			IRefactoringConditionChecker checker = refactoring.getConditionChecker();
//			
//			if (checker == null) {
//				return true;
//			}
//			return checker.shouldPerform();
//		} catch (Exception e) {
//			RubyCore.log(e); // TODO Return false?
//		}
//		return true;
//	}

	private RubyRefactoring getRefactoring() throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (fRefactoring != null) return fRefactoring;
		Constructor constructor = refactoringClass.getConstructors()[0];
		Object[] args;
		if(constructor.getParameterTypes().length == 1) {
			args = new Object[] {selectionProvider};
		} else {
			args = new Object[0];
		}
		fRefactoring = (RubyRefactoring) constructor.newInstance(args);
		return fRefactoring;
	}
	
	
}
