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

package org.rubypeople.rdt.refactoring.core.inlinelocal;

import java.util.Collection;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;

public class InlineLocalConfig implements IRefactoringConfig {

	private boolean replaceTempWithQuery;

	private String newMethodName = "extractedMethod"; //$NON-NLS-1$

	private IDocumentProvider docProvider;

	private int caretPosition;

	private MethodDefNode enclosingMethod;

	private Node enclosingScopeNode;

	private LocalNodeWrapper selectedItem;

	private String selectedItemName;

	private LocalNodeWrapper definitionNode;

	private Collection<LocalNodeWrapper> localOccurrences;

	public InlineLocalConfig(DocumentProvider docProvider, int caretPosition) {
		this.docProvider = docProvider;
		this.caretPosition = caretPosition;
	}

	public boolean isReplaceTempWithQuery() {
		return replaceTempWithQuery;
	}

	public void setReplaceTempWithQuery(boolean replaceTempWithQuery) {
		this.replaceTempWithQuery = replaceTempWithQuery;
	}

	public String getNewMethodName() {
		return newMethodName;
	}

	public void setNewMethodName(String newMethodName) {
		this.newMethodName = newMethodName;
	}

	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}
	
	public int getCaretPosition() {
		return caretPosition;
	}

	public String getActiveFileName() {

		return docProvider.getActiveFileName();
	}

	public LocalNodeWrapper getDefinitionNode() {
		return definitionNode;
	}

	public MethodDefNode getEnclosingMethod() {
		return enclosingMethod;
	}

	public Node getEnclosingScopeNode() {
		return enclosingScopeNode;
	}

	public Collection<LocalNodeWrapper> getLocalOccurrences() {
		return localOccurrences;
	}

	public LocalNodeWrapper getSelectedItem() {
		return selectedItem;
	}

	public String getSelectedItemName() {
		return selectedItemName;
	}

	public void setLocalOccurences(Collection<LocalNodeWrapper> localOccurrences) {
		this.localOccurrences = localOccurrences;
	}

	public void setDefinitionNode(LocalNodeWrapper definitionNode) {
		this.definitionNode = definitionNode;
	}

	public void setSelectedItemName(String selectedItemName) {
		this.selectedItemName = selectedItemName;
	}

	public void setSelectedItem(LocalNodeWrapper selectedItem) {
		this.selectedItem = selectedItem;
	}

	public void setEnclosingScopeNode(Node enclosingScopeNode) {
		this.enclosingScopeNode = enclosingScopeNode;
	}

	public void setEnclosingMethod(MethodDefNode enclosingMethod) {
		this.enclosingMethod = enclosingMethod;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.docProvider = doc;
	}
}
