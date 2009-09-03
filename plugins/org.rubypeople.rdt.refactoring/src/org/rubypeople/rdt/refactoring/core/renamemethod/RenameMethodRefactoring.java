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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
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

package org.rubypeople.rdt.refactoring.core.renamemethod;

import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.IValidator;
import org.rubypeople.rdt.refactoring.core.RubyRefactoring;
import org.rubypeople.rdt.refactoring.ui.NewNameListener;
import org.rubypeople.rdt.refactoring.ui.pages.OccurenceReplaceSelectionPage;
import org.rubypeople.rdt.refactoring.ui.pages.RenamePage;
import org.rubypeople.rdt.refactoring.util.NameValidator;

public class RenameMethodRefactoring extends RubyRefactoring {

	private static final class MethodNameValidator implements IValidator {
		public boolean isValid(String test) {
			return NameValidator.isValidMethodName(test);
		}
	}

	public static final String NAME = Messages.RenameMethodRefactoring_Name;

	public RenameMethodRefactoring(IRefactoringContext selectionProvider) {
		super(NAME, selectionProvider);
		
		RenameMethodConfig config = new RenameMethodConfig(getDocumentProvider(), selectionProvider.getCaretPosition());
		RenameMethodConditionChecker checker = new RenameMethodConditionChecker(config);
		setRefactoringConditionChecker(checker);
		
		if(checker.shouldPerform()){
			MethodRenamer methodRenamer = new MethodRenamer(config);
			setEditProvider(methodRenamer);
						
			NewNameListener nameListener = new NewNameListener(config, new MethodNameValidator(), checker.getAlreadyUsedNames());
			
			RenamePage page = new RenamePage(NAME + "...", config.getTargetMethod().getName(), nameListener); //$NON-NLS-1$
			pages.add(page);
			if(!config.getPossibleCalls().isEmpty()){
				pages.add(new OccurenceReplaceSelectionPage(config, config.getDocumentProvider()));
			}
		}
	}
}
