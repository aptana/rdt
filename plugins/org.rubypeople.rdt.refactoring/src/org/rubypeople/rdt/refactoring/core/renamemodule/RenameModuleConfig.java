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

package org.rubypeople.rdt.refactoring.core.renamemodule;

import java.util.Collection;

import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.renamemethod.NodeSelector;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentWithIncluding;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.INodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ModuleNodeWrapper;
import org.rubypeople.rdt.refactoring.ui.INewNameReceiver;

public class RenameModuleConfig implements IRefactoringConfig, INewNameReceiver, NodeSelector {

	private IDocumentProvider doc;
	private final int carretPosition;
	private ModuleNodeWrapper selectedModule;
	private String newName;
	private Collection<ModuleNodeWrapper> moduleParts;
	private String originalFullName;
	private String originalName;
	private Collection<? extends INodeWrapper> possibleCalls;
	private Collection<? extends INodeWrapper> selectedCalls;
	private Collection<ModuleSpecifierWrapper> includes;
	private Collection<String> allModuleNames;

	public RenameModuleConfig(IDocumentProvider doc, int carretPosition) {
		this.doc = doc;
		this.carretPosition = carretPosition;
	}

	public IDocumentProvider getDocumentProvider() {
		return doc;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.doc = new DocumentWithIncluding(doc);
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public int getCarretPosition() {
		return carretPosition;
	}

	public ModuleNodeWrapper getSelectedModule() {
		return selectedModule;
	}

	public void setSelectedModule(ModuleNodeWrapper selectedModule) {
		this.selectedModule = selectedModule;
		originalFullName = selectedModule.getFullName();
		originalName = selectedModule.getName();
	}

	public String getNewName() {
		return newName;
	}

	public void setModuleParts(Collection<ModuleNodeWrapper> moduleParts) {
		this.moduleParts = moduleParts;
	}

	public Collection<ModuleNodeWrapper> getModuleParts() {
		return moduleParts;
	}

	public String getOriginalFullName() {
		return originalFullName;
	}

	public String getOriginalName() {
		return originalName;
	}

	public Collection<? extends INodeWrapper> getPossibleCalls() {
		return possibleCalls;
	}

	public Collection<ModuleSpecifierWrapper> getSelectedCalls() {
		return (Collection<ModuleSpecifierWrapper>) selectedCalls;
	}

	public void setPossibleCalls(Collection<? extends INodeWrapper> possibleCalls) {
		this.possibleCalls = possibleCalls;
	}

	public void setSelectedCalls(Collection<? extends INodeWrapper> selectedCalls) {
		this.selectedCalls = selectedCalls;
	}

	public void setIncludes(Collection<ModuleSpecifierWrapper> includes) {
		this.includes = includes;
	}

	public Collection<ModuleSpecifierWrapper> getIncludes() {
		return includes;
	}

	public Collection<String> getAllModuleNames() {
		return allModuleNames;
	}

	public void setAllModuleNames(Collection<String> allModuleNames) {
		this.allModuleNames = allModuleNames;
	}
}
