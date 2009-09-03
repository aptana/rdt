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

package org.rubypeople.rdt.refactoring.core.mergeclasspartsinfile;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.classnodeprovider.IncludedClassesProvider;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;

public class MergeClassPartsInFileConditionChecker extends RefactoringConditionChecker {

	private MergeClassPartInFileConfig config;

	public MergeClassPartsInFileConditionChecker(MergeClassPartInFileConfig config) {
		super(config);
	}
	
	public void init(IRefactoringConfig configObj) {
		this.config = (MergeClassPartInFileConfig) configObj;
		config.setClassNodeProvider(new IncludedClassesProvider(config.getDocumentProvider()));
		initSelectableClasses();
	}
	
	private void initSelectableClasses() {
		Collection<ClassNodeWrapper> selectableClasses = new ArrayList<ClassNodeWrapper>();
		for (ClassNodeWrapper currentClassNode : config.getAllClassNodes()) {
			if (currentClassNode.getPartialClassNodesOfFile(config.getDocumentProvider().getActiveFileName()).size() >= 2)
				selectableClasses.add(currentClassNode);
		}
		config.setSelectableClasses(selectableClasses);
	}

	@Override
	protected void checkFinalConditions() {
		if (!isSelectionMergable()) {
			addError(Messages.MergeClassPartsInFileConditionChecker_TooFewParts);
		}
	}

	private boolean isSelectionMergable() {
		Collection<PartialClassNodeWrapper> checkedClassParts = config.getCheckedClassParts();
		if (checkedClassParts.isEmpty()) {
			return false;
		} else if (checkedClassParts.size() == 1) {
			if (checkedClassParts.toArray()[0].equals(config.getSelectedClassPart())) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void checkInitialConditions() {
		if (!config.hasSelectableClasses()) {
			addError(Messages.MergeClassPartsInFileConditionChecker_NotEnoughPartsFound);
		}
	}

}
