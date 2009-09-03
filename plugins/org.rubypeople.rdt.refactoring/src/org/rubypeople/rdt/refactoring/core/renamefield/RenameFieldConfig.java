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

package org.rubypeople.rdt.refactoring.core.renamefield;

import java.util.ArrayList;
import java.util.Collection;

import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.AttrFieldItem;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.FieldItem;
import org.rubypeople.rdt.refactoring.core.renamemethod.NodeSelector;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentWithIncluding;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.INodeWrapper;
import org.rubypeople.rdt.refactoring.ui.ICheckboxListener;
import org.rubypeople.rdt.refactoring.ui.INewNameReceiver;

public class RenameFieldConfig implements INewNameReceiver, ICheckboxListener, NodeSelector, IRefactoringConfig {

	private IDocumentProvider docProvider;

	private int caretPosition;

	private String newName;

	private String selectedName;

	private ClassNodeWrapper wholeClassNode;

	private FieldProvider fieldProvider;
	
	private boolean doRenameAccessorMethods;
	private boolean doRenameAccessors = true;

	private Collection<? extends INodeWrapper> possibleCalls;

	private Collection<? extends INodeWrapper> selectedCalls;

	private FieldItem selectedItem;

	public RenameFieldConfig(IDocumentProvider docProvider, int caretPosition) {
		this.docProvider = docProvider;
		this.caretPosition = caretPosition;

		possibleCalls = new ArrayList<INodeWrapper>();
		selectedCalls = new ArrayList<INodeWrapper>();
	}


	public int getCaretPosition() {
		return caretPosition;
	}

	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getSelectedName() {
		return selectedName;
	}

	public ClassNodeWrapper getWholeClassNode() {
		return wholeClassNode;
	}

	public Collection<String> getFieldNames() {
		return fieldProvider.getFieldNames();
	}

	public boolean hasSelectedName() {
		return selectedName != null;
	}

	public boolean hasWholeClassNode() {
		return wholeClassNode != null;
	}

	public FieldProvider getFieldProvider() {
		return fieldProvider;
	}

	public boolean doRenameAccessorMethods() {
		return doRenameAccessorMethods;
	}
	
	public void setDoRenameAccessorMethods(boolean doRenameAccessorMethods){
		this.doRenameAccessorMethods = doRenameAccessorMethods;
	}

	public void setChecked(boolean checked) {
		setDoRenameAccessorMethods(checked);
	}
	
	public boolean concernsClassField(){
		return selectedItem.concernsClassField();
	}
	
	public boolean hasSelectedItem() {
		return selectedItem != null;
	}
	
	public void setSelectedItem(FieldItem selectedItem) {
		this.selectedItem = selectedItem;
	}

	public FieldItem getSelectedItem() {
		return selectedItem;
	}

	public Collection<? extends INodeWrapper> getPossibleCalls() {
		return possibleCalls;
	}
	

	public Collection<? extends INodeWrapper> getSelectedCalls() {
		if(doRenameAccessors) {
			return selectedCalls;
		}
		Collection<INodeWrapper> nodes = new ArrayList<INodeWrapper>();
		for (INodeWrapper wrapper : selectedCalls) {
			if(wrapper instanceof AttrFieldItem) {
				continue;
			}
			nodes.add(wrapper);
		}
		return nodes;
	}

	public void setPossibleCalls(Collection<? extends INodeWrapper> possibleCalls) {
		this.possibleCalls = possibleCalls;
	}

	public void setSelectedCalls(Collection<? extends INodeWrapper> selectedCalls) {
		this.selectedCalls = selectedCalls;
	}

	public boolean isDoRenameAccessors() {
		return doRenameAccessors;
	}

	public void setDoRenameAccessors(boolean doRenameAccessors) {
		this.doRenameAccessors = doRenameAccessors;
	}


	public void setDocProvider(DocumentWithIncluding docProvider) {
		this.docProvider = docProvider;
	}


	public void setWholeClassNode(ClassNodeWrapper wholeClassNode) {
		this.wholeClassNode = wholeClassNode;
	}


	public void setFieldProvider(FieldProvider fieldProvider) {
		this.fieldProvider = fieldProvider;
	}


	public void setSelectedName(String selectedName) {
		this.selectedName = selectedName;
	}


	public void setDocumentProvider(IDocumentProvider doc) {
		this.docProvider = doc;
	}
}
