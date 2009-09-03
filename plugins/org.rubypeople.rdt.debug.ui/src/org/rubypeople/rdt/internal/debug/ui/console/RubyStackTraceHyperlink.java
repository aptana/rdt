/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2004 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 * This source code is based on org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink
 */
/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.RubySourceLocator;
import org.rubypeople.rdt.internal.ui.util.StackTraceLine;

/**
 * A hyperlink from a stack trace line of the form "*:*:"
 */
public class RubyStackTraceHyperlink implements IHyperlink {

	private IConsole fConsole;
	private StackTraceLine fTraceLine;

	public RubyStackTraceHyperlink(IConsole console, StackTraceLine line) {
		fConsole = console;
		fTraceLine = line;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IHyperlink#linkEntered()
	 */
	public void linkEntered() {}

	/**
	 * @see org.eclipse.debug.ui.console.IHyperlink#linkExited()
	 */
	public void linkExited() {}

	private void setEditorToLine(IEditorPart pEditorPart, IEditorInput pInput) throws CoreException {
		if (!(pEditorPart instanceof ITextEditor)) { return; }
		int lineNumber = this.getLineNumber();
		// documents start at 0
		if (lineNumber > 0) {
			lineNumber--;
		}
		if (lineNumber == 0) { return; }
		ITextEditor textEditor = (ITextEditor) pEditorPart;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		provider.connect(pInput);
		IDocument document = provider.getDocument(pInput);
		try {
			IRegion line = document.getLineInformation(lineNumber);
			textEditor.selectAndReveal(line.getOffset(), line.getLength());
		} catch (BadLocationException e) {
			if (RdtDebugUiPlugin.getDefault().isDebugging()) {
				System.out.println("Could not set editor to line: " + lineNumber);
			}
		}
		provider.disconnect(pInput);
	}

	/**
	 * @see org.eclipse.debug.ui.console.IHyperlink#linkActivated()
	 */
	public void linkActivated() {
		RubySourceLocator rubySourceLocator = null;
		ILaunch launch = getConsole().getProcess().getLaunch();
		if (launch == null) { return; }
		ISourceLocator sourceLocator = launch.getSourceLocator();
		if (!(sourceLocator instanceof RubySourceLocator)) { return; }
		rubySourceLocator = (RubySourceLocator) sourceLocator;
		String filename = this.getFilename();
		try {
			Object sourceElement = rubySourceLocator.getSourceElement(filename);
			IEditorInput input = rubySourceLocator.getEditorInput(sourceElement);
			if (input == null) {
				if (RdtDebugUiPlugin.getDefault().isDebugging()) {
					System.out.println("Could not create editor input for stack trace: " + filename);
				}
				// wrongly detected stack trace
				return;
			}
			IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, rubySourceLocator.getEditorId(input, sourceElement));
			this.setEditorToLine(editorPart, input);
		} catch (CoreException e) {
			RdtDebugUiPlugin.log(new Status(IStatus.ERROR, RdtDebugUiPlugin.PLUGIN_ID, 0, "Could not open editor or set line in editor." + filename, e));
		}
	}

	
	/**
	 * Returns the line number associated with the stack trace
	 * 
	 * @exception CoreException if unable to parse the number
	 */
	public int getLineNumber() {
		return fTraceLine.getLineNumber();
	}

	public String getFilename() {
		return fTraceLine.getFilename();
	}

	/**
	 * Returns the console this link is contained in.
	 *  
	 * @return console
	 */
	protected IConsole getConsole() {
		return fConsole;
	}

	/**
	 * Returns this link's text
	 * 
	 * @exception CoreException if unable to retrieve the text
	 */
	protected String getLinkText() throws BadLocationException {
		IRegion region = getConsole().getRegion(this);
		return getConsole().getDocument().get(region.getOffset(), region.getLength());
	}
}