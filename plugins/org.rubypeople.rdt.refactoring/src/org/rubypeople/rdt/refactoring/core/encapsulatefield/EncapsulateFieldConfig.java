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

package org.rubypeople.rdt.refactoring.core.encapsulatefield;

import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;

public class EncapsulateFieldConfig implements IRefactoringConfig {

	private IDocumentProvider docProvider;
	private int caretPosition;
	private boolean readerGenerationDisabled;
	private boolean writerGenerationDisabled;
	private ClassNodeWrapper enclosingClassNode;
	private INameNode selectedInstNode;
	private AttrAccessorNodeWrapper selectedAccessor;

	public EncapsulateFieldConfig(IDocumentProvider docProvider, int caretPosition) {
		this.docProvider = docProvider;
		this.caretPosition = caretPosition;
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}

	public boolean isReaderGenerationDisabled() {
		return readerGenerationDisabled;
	}

	public boolean isWriterGenerationDisabled() {
		return writerGenerationDisabled;
	}

	public ClassNodeWrapper getEnclosingClassNode() {
		return enclosingClassNode;
	}

	public INameNode getSelectedInstNode() {
		return selectedInstNode;
	}

	public AttrAccessorNodeWrapper getSelectedAccessor() {
		return selectedAccessor;
	}

	public void setEnclosingClassNode(ClassNodeWrapper enclosingClassNode) {
		this.enclosingClassNode = enclosingClassNode;
	}

	public void setReaderGenerationDisabled(boolean readerGenerationDisabled) {
		this.readerGenerationDisabled = readerGenerationDisabled;
	}

	public void setSelectedAccessor(AttrAccessorNodeWrapper selectedAccessor) {
		this.selectedAccessor = selectedAccessor;
	}

	public void setSelectedInstNode(INameNode selectedInstNode) {
		this.selectedInstNode = selectedInstNode;
	}

	public void setWriterGenerationDisabled(boolean writerGenerationDisabled) {
		this.writerGenerationDisabled = writerGenerationDisabled;
	}

	public boolean hasSelectedInstNode() {
		return selectedInstNode != null;
	}

	public boolean hasSelectedAccessor() {
		return selectedAccessor != null;
	}

	public String getReaderMethodName() {
		String fieldName = getFieldName();
		return fieldName != null ? fieldName : null;
	}

	public String getWriterMethodName() {
		String fieldName = getFieldName();
		return fieldName != null ? fieldName + '=' : null;
	}

	String getFieldName() {
		if (hasSelectedAccessor()) {
			return getSelectedAccessor().getAttrName();
		}
		if (hasSelectedInstNode()) {
			return getSelectedInstNode().getName().substring(1);
		}
		return null;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.docProvider = doc;
	}
}
