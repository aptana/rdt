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

package org.rubypeople.rdt.refactoring.core.renamelocal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;

public class LocalVariableRenamer {
	private final DocumentProvider doc;
	private final String from;
	private final String to;

	public LocalVariableRenamer(DocumentProvider doc, String from, String to) {
		this.doc = doc;
		this.from = from;
		this.to = to;
	}
	
	public TextEdit getEdit() {
		RenameLocalConfig config = new RenameLocalConfig(doc, 0);
		new RenameLocalConditionChecker(config);
		RenameLocalEditProvider editProvider = new RenameLocalEditProvider(config);
		
		editProvider.setSelectedVariableName(from);
		editProvider.setNewVariableName(to);
		
		return editProvider.getEdit(doc.getActiveFileContent());
	}
	
	public DocumentProvider rename() {
		return applyEdit(doc, getEdit());
	}
	
	public static StringDocumentProvider applyEdit(DocumentProvider doc, TextEdit edit) {
		
		Document result = new Document(doc.getActiveFileContent());
		try {
			edit.apply(result);
		} catch (MalformedTreeException e) {
			assert false;
		} catch (BadLocationException e) {
			assert false;
		}
		return new StringDocumentProvider(Messages.LocalVariableRenamer_Modified + doc.getActiveFileName(), result.get());
	}
}
