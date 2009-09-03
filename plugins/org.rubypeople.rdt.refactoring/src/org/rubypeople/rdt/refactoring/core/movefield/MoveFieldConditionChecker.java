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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.core.movefield;

import java.util.Collection;
import java.util.TreeSet;

import org.rubypeople.rdt.refactoring.classnodeprovider.AllFilesClassNodeProvider;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;

public class MoveFieldConditionChecker extends RefactoringConditionChecker {

	private ClassNodeWrapper selectedClassNode;
	private FieldNodeWrapper selectedField;
	private MoveFieldConfig config;

	public MoveFieldConditionChecker(MoveFieldConfig config) {
		super(config);
	}

	@Override
	public void init(IRefactoringConfig configObj) {
		config = (MoveFieldConfig) configObj;
	
		try {
			selectedClassNode = SelectionNodeProvider.getSelectedClassNode(config.getDocumentProvider().getActiveFileRootNode(), config.getPos());
		} catch (NoClassNodeException e) {
			selectedClassNode = null;
			return;
		}
		
		selectedField = findSelectedField(selectedClassNode.getFields(), config.getPos());
		config.setSelectedField(selectedField);
		if(selectedField == null) {
			return;
		}
		config.setTargetClassCandidates(getPossibleTargetClassNames(selectedClassNode.getName()));
		config.setReferenceCandidates(getPossibleFieldNames(selectedClassNode.getFields(), selectedField.getNameWithoutAts()));
	}

	private static Collection<String> getPossibleFieldNames(Collection<FieldNodeWrapper> fields, String selectedFieldName) {
		Collection<String> names = new TreeSet<String>();
		for (FieldNodeWrapper field : fields) {
			if(field.isInstVar() && !field.getNameWithoutAts().equals(selectedFieldName)) {
				names.add("@" + field.getNameWithoutAts()); //$NON-NLS-1$
			}
		}
		return names;
	}

	private static FieldNodeWrapper findSelectedField(Collection<FieldNodeWrapper> fields, int pos) {
		FieldNodeWrapper selected = null;
		for(FieldNodeWrapper fieldNode : fields) {
			if(isFieldAtCursorPos(fieldNode, pos) && fieldNode.isInstVar()) {
				selected =  fieldNode;
			}
		}
		return selected;
	}

	private static boolean isFieldAtCursorPos(FieldNodeWrapper fieldNode, int pos) {
		return fieldNode.getWrappedNode().getPosition().getStartOffset() <=  pos
			&& fieldNode.getWrappedNode().getPosition().getEndOffset() >= pos;
	}

	private Collection<String> getPossibleTargetClassNames(String selectedClassName) {
		Collection<String> allClasses = new TreeSet<String>();
		for(ClassNodeWrapper classNode : new AllFilesClassNodeProvider(config.getDocumentProvider()).getAllClassNodes()) {
			if(!classNode.getName().equals(selectedClassName))
			allClasses.add(classNode.getName());
		}
		return allClasses;
	}
	
	@Override
	protected void checkInitialConditions() {
		if(selectedClassNode == null) {
			addError(Messages.MoveFieldConditionChecker_NoInstanceInsideClass);
		} else if (selectedField == null) {
			addError(Messages.MoveFieldConditionChecker_NoFieldSelected);
		} else if(config.getReferenceCandidates().isEmpty()) {
			addError(Messages.MoveFieldConditionChecker_NoReference);
		} else if (config.getTargetClassCandidates().isEmpty()) {
			addError(Messages.MoveFieldConditionChecker_NoDestination);
		}
	}

}
