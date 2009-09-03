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

package org.rubypeople.rdt.refactoring.core.generateconstructor;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.rubypeople.rdt.refactoring.core.RubyRefactoring;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.pages.ConstructorSelectionPage;
import org.rubypeople.rdt.refactoring.util.Constants;

public class GenerateConstructorRefactoring extends RubyRefactoring {

	public static final String NAME = Messages.GenerateConstructorRefactoring_Name;

	public GenerateConstructorRefactoring() {
		super(NAME);
		ConstructorsGenerator constructorsGenerator = new ConstructorsGenerator(getDocumentProvider());
		setEditProvider(constructorsGenerator);
		ConstructorSelectionPage page = new ConstructorSelectionPage(constructorsGenerator);
		pages.add(page);
		setInfoMessage(page);
	}

	private void setInfoMessage(ConstructorSelectionPage page) {
		if (getDocumentProvider().getClassNodeProvider() == null)
			return;
		StringBuilder classNames = new StringBuilder();
		for (ClassNodeWrapper classNode : getDocumentProvider().getClassNodeProvider().getAllClassNodes()) {
			if (classNode.hasConstructor())
				classNames.append(classNode.getName() + ", "); //$NON-NLS-1$
		}
		if (classNames.length() != 0)
			page.setMessage(Messages.GenerateConstructorRefactoring_TheClass + ((classNames.length() != 1) ? Messages.GenerateConstructorRefactoring_ClassesPluralForm : "") //$NON-NLS-1$
					+ ' ' + classNames.substring(0, classNames.length() - 2)
					+ Messages.GenerateConstructorRefactoring_AlreadyContainsConstructors + "\n", RefactoringStatus.WARNING);
	}
}