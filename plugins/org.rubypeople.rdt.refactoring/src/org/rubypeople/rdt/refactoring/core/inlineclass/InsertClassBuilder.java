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

package org.rubypeople.rdt.refactoring.core.inlineclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.core.renamefield.FieldRenamer;
import org.rubypeople.rdt.refactoring.core.renamefield.RenameFieldConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamefield.RenameFieldConfig;
import org.rubypeople.rdt.refactoring.core.renamemethod.MethodRenamer;
import org.rubypeople.rdt.refactoring.core.renamemethod.RenameMethodConditionChecker;
import org.rubypeople.rdt.refactoring.core.renamemethod.RenameMethodConfig;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NameHelper;

public class InsertClassBuilder {

	private InlineClassConfig config;

	public InsertClassBuilder(InlineClassConfig config) {
		this.config = config;
	}

	public StringDocumentProvider getInlinedClass(PartialClassNodeWrapper inlinedClassPart) {

		MultiTextEdit prechanges = new MultiTextEdit();
		StringDocumentProvider inlinedClassDocumentProvider = getDocumentProviderForClassPart(inlinedClassPart);
		ClassNodeWrapper inlinedClass = inlinedClassDocumentProvider.getClassNodeProvider().getAllClassNodes().iterator().next();
		PartialClassNodeWrapper contextfreeClassPart = inlinedClass.getFirstPartialClassNode();

		Map<String, MethodNodeWrapper> conflictingMethods = getMethodsWithNameConflict(contextfreeClassPart);
		Map<String, FieldNodeWrapper> conflictingFields = getFieldsWithNameConflict(contextfreeClassPart);

		prechanges.addChildren(getMethodRenameEdits(conflictingMethods, inlinedClassDocumentProvider));
		prechanges.addChildren(getFieldRenameEdits(conflictingFields, inlinedClassDocumentProvider));
		prechanges.addChildren(getConstructorDeleteEdits(inlinedClassDocumentProvider));

		return new StringDocumentProvider(inlinedClassDocumentProvider.getActiveFileName() + "_with_applied_prechanges", applyPrechanges(prechanges, inlinedClassDocumentProvider)); //$NON-NLS-1$

	}

	private TextEdit[] getMethodRenameEdits(Map<String, MethodNodeWrapper> conflictingMethods, StringDocumentProvider inlinedClassDocumentProvider) {
		ArrayList<TextEdit> edits = new ArrayList<TextEdit>();

		for (String currentMethodName : conflictingMethods.keySet()) {
			MethodNodeWrapper currentMethod = conflictingMethods.get(currentMethodName);
			int methodPos = currentMethod.getWrappedNode().getPosition().getStartOffset();
			
			RenameMethodConfig config = new RenameMethodConfig(inlinedClassDocumentProvider, methodPos);
			
			new RenameMethodConditionChecker(config);
			config.setNewName(currentMethodName);
			MethodRenamer renamer = new MethodRenamer(config);

			fillTextEdits(inlinedClassDocumentProvider, edits, renamer);
		}
		filterEditsInConstructor(edits);
		return edits.toArray(new TextEdit[edits.size()]);
	}


	private void filterEditsInConstructor(Collection<TextEdit> edits) {
		ArrayList<TextEdit> removeList = new ArrayList<TextEdit>();
		Collection<MethodNodeWrapper> constructorNodes =  config.getSourceClass().getExistingConstructors();
		for(TextEdit currentEdit : edits){
			int editOffset = currentEdit.getOffset();
			for(MethodNodeWrapper currentConstructor : constructorNodes){
				if(SelectionNodeProvider.nodeContainsPosition(currentConstructor.getWrappedNode(), editOffset)){
					removeList.add(currentEdit);
				}
			}
		}
		for(TextEdit currentRevomable : removeList){
			edits.remove(currentRevomable);
		}
	}

	private void fillTextEdits(StringDocumentProvider inlinedClassDocumentProvider, Collection<TextEdit> edits, IMultiFileEditProvider renamer) {
		for (FileMultiEditProvider currentMultiProvider : renamer.getFileEditProviders()) {
			for (EditProvider currentEditProvider : currentMultiProvider.getEditProviders()) {
				edits.add(currentEditProvider.getEdit(inlinedClassDocumentProvider.getActiveFileContent()));
			}
		}
	}

	private TextEdit[] getFieldRenameEdits(Map<String, FieldNodeWrapper> conflictingFields, StringDocumentProvider inlinedClassDocumentProvider) {

		TreeSet<TextEdit> edits = new TreeSet<TextEdit>(new TextEditComparator());

		for (String newFieldName : conflictingFields.keySet()) {
			FieldNodeWrapper currentField = conflictingFields.get(newFieldName);
			int fieldPos = currentField.getWrappedNode().getPosition().getStartOffset();
			RenameFieldConfig config = new RenameFieldConfig(inlinedClassDocumentProvider, fieldPos);
			new RenameFieldConditionChecker(config);
			config.setNewName(newFieldName.replaceAll("@", ""));	 //$NON-NLS-1$ //$NON-NLS-2$
			setSelectedCalls(config);
			FieldRenamer renamer = new FieldRenamer(config);

			fillTextEdits(inlinedClassDocumentProvider, edits, renamer);
		}
		filterEditsInConstructor(edits);
		return edits.toArray(new TextEdit[edits.size()]);
	}

	private void setSelectedCalls(RenameFieldConfig config) {
		String fieldName = config.getSelectedName();
		boolean concernsClassField = config.concernsClassField();
		config.setSelectedCalls(config.getFieldProvider().getFieldItems(fieldName, concernsClassField));
	}

	public Map<String, MethodNodeWrapper> getMethodsWithNameConflict(PartialClassNodeWrapper inlinedClassPart) {

		Collection<MethodNodeWrapper> inlinedMethods = inlinedClassPart.getMethods();
		HashMap<String, MethodNodeWrapper> conflictingMethods = new HashMap<String, MethodNodeWrapper>();

		for (MethodNodeWrapper currentInlinedMethod : inlinedMethods) {
			addConflictingMethods(conflictingMethods, currentInlinedMethod);
		}
		return conflictingMethods;
	}

	private void addConflictingMethods(Map<String, MethodNodeWrapper> conflictingMethods, MethodNodeWrapper currentInlinedMethod) {

		if (!currentInlinedMethod.isConstructor() && NameHelper.methodnameExistsInClassPart(currentInlinedMethod.getName(), config.getTargetClass())) {
			String newName = NameHelper.createMethodName(currentInlinedMethod, config.getTargetClass());
			conflictingMethods.put(newName, currentInlinedMethod);
		}
		
	}

	private Map<String, FieldNodeWrapper> getFieldsWithNameConflict(PartialClassNodeWrapper inlinedClassPart) {

		Collection<FieldNodeWrapper> inlinedFields = inlinedClassPart.getFields();
		HashMap<String, FieldNodeWrapper> conflictingFields = new HashMap<String, FieldNodeWrapper>();

		for (FieldNodeWrapper currentInlinedField : inlinedFields) {
			addConflictingFields(conflictingFields, currentInlinedField);
		}
		return conflictingFields;
	}

	private void addConflictingFields(Map<String, FieldNodeWrapper> conflictingFields, FieldNodeWrapper currentInlinedField) {

		if (NameHelper.fieldnameExistsInClass(currentInlinedField.getName(), config.getTargetClass())) {
			String newName = NameHelper.createFieldName(currentInlinedField, config.getTargetClass());
			conflictingFields.put(newName, currentInlinedField);	
		}
	}

	private StringDocumentProvider getDocumentProviderForClassPart(PartialClassNodeWrapper inlinedClassPart) {
		ISourcePosition classPartPosition = inlinedClassPart.getWrappedNode().getPosition();
		String activeFileContent = config.getDocumentProvider().getActiveFileContent();
		String inlinedClassDocument = activeFileContent.substring(classPartPosition.getStartOffset(), classPartPosition.getEndOffset());
		String fileName = "part_of_" + config.getDocumentProvider().getActiveFileName(); //$NON-NLS-1$
		StringDocumentProvider inlinedClassDocumentProvider = new StringDocumentProvider(fileName, inlinedClassDocument);
		return inlinedClassDocumentProvider;
	}

	private TextEdit[] getConstructorDeleteEdits(StringDocumentProvider inlinedClassDocument) {
		ArrayList<TextEdit> constructorDeleters = new ArrayList<TextEdit>();
		Node rootNode = inlinedClassDocument.getActiveFileRootNode();
		ClassNodeWrapper classNode;
		try {
			classNode = SelectionNodeProvider.getSelectedClassNode(rootNode, 1);
		} catch (NoClassNodeException e) {
			e.printStackTrace();
			return constructorDeleters.toArray(new TextEdit[constructorDeleters.size()]);
		}
		Collection<MethodNodeWrapper> constructors = classNode.getExistingConstructors();
		for (MethodNodeWrapper currentConstructor : constructors) {
			DeleteEditProvider deleteEditProvider = new DeleteEditProvider(currentConstructor.getWrappedNode());
			constructorDeleters.add(deleteEditProvider.getEdit(inlinedClassDocument.getActiveFileContent()));
		}
		return constructorDeleters.toArray(new TextEdit[constructorDeleters.size()]);
	}

	private String applyPrechanges(MultiTextEdit prechanges, StringDocumentProvider inlinedClassDocumentProvider) {
		Document inlinedPart = new Document(inlinedClassDocumentProvider.getActiveFileContent());

		try {
			prechanges.apply(inlinedPart);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return inlinedPart.get();
	}
}