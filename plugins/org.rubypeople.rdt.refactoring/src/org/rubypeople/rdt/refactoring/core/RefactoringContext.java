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

package org.rubypeople.rdt.refactoring.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.rubypeople.rdt.core.IMember;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor;

public class RefactoringContext implements IRefactoringContext {

	private String source;
	private int start;
	private int end;
	private int caret;
	private IFile file;
	
	public RefactoringContext(int start, int end, String src) {
		this(start, end, start, src);
	}
	
	public RefactoringContext(int start, int end, int caret, String src) {
		this.start = start;
		this.end = end;
		this.caret = caret;
		this.source = src;
	}
	
	public RefactoringContext() {
		initEditor();
	}
	
	public RefactoringContext(IAction action) {		
		if (action == null || action instanceof org.eclipse.ui.internal.EditorPluginAction) {
			initEditor();
		} else {
			initOutline(action);
		}
	}
	
	private void initEditor() {
		RubyEditor editor = (RubyEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		start = selection.getOffset();
		end = start + selection.getLength();
		if (end > start) {
			end--;
		}
		caret = editor.getCaretPosition().getOffset();
		if (editor.getEditorInput() instanceof IFileEditorInput)
			file = ((IFileEditorInput) editor.getEditorInput()).getFile();
	}

	private void initOutline(IAction action) {
		TreeSelection selection = (TreeSelection) ((org.eclipse.ui.internal.PluginAction)action).getSelection();
		IMember member = (IMember) selection.toArray()[0];
		try {
			start = member.getNameRange().getOffset();
			end = start + member.getNameRange().getLength();
			caret = start;
			file = (IFile) member.getRubyScript().getResource();
		} catch (RubyModelException e) {
			e.printStackTrace();
		}
	}
	
	public IFile getActiveFile() {
		return file;
	}
	
	/* (non-Javadoc)
	 * @see org.rubypeople.rdt.refactoring.core.IRefactoringContext#getCarretPosition()
	 */
	public int getCaretPosition() {
		return caret;
	}
	
	/* (non-Javadoc)
	 * @see org.rubypeople.rdt.refactoring.core.IRefactoringContext#getStartOffset()
	 */
	public int getStartOffset() {
		return start;
	}
	
	/* (non-Javadoc)
	 * @see org.rubypeople.rdt.refactoring.core.IRefactoringContext#getEndOffset()
	 */
	public int getEndOffset() {
		return end;
	}

	public String getSource() {
		if (source == null) {
			RubyEditor editor = (RubyEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			source = editor.getViewer().getDocument().get();
		}
		return source;
	}
}
