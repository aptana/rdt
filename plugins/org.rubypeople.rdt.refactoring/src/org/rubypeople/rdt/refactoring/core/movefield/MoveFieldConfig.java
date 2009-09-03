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

import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;

public class MoveFieldConfig implements IRefactoringConfig {

	private IDocumentProvider doc;
	private final int pos;
	private Collection<String> targetClassCandidates;
	private Collection<String> referenceCandidates;
	private String targetClass;
	private String targetReference;
	private FieldNodeWrapper selectedField;
	
	public MoveFieldConfig(IDocumentProvider doc, int pos) {
		this.doc = doc;
		this.pos = pos;
	}

	public Collection<String> getTargetClassCandidates() {
		return targetClassCandidates;
	}

	public void setTargetClassCandidates(Collection<String> targetCandidates) {
		this.targetClassCandidates = targetCandidates;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public String getTargetReference() {
		return targetReference;
	}

	public void setTargetReference(String targetReference) {
		this.targetReference = targetReference;
	}

	public String getSelectedFieldName() {
		return selectedField.getNameWithoutAts();
	}
	
	public IDocumentProvider getDocumentProvider() {
		return doc;
	}

	public int getPos() {
		return pos;
	}

	public Collection<String> getReferenceCandidates() {
		return referenceCandidates;
	}

	public void setReferenceCandidates(Collection<String> targetReferenceCandidates) {
		this.referenceCandidates = targetReferenceCandidates;
	}

	public void setSelectedField(FieldNodeWrapper selectedField) {
		this.selectedField = selectedField;
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.doc = doc;
	}
}
