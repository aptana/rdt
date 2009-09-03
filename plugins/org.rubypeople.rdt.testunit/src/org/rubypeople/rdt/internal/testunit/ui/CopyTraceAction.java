/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;

/**
 * Copies a test failure stack trace to the clipboard.
 */
public class CopyTraceAction extends Action {
	private FailureTrace fView;
	
	private final Clipboard fClipboard;

	/**
	 * Constructor for CopyTraceAction.
	 */
	public CopyTraceAction(FailureTrace view, Clipboard clipboard) {
		super(TestUnitMessages.CopyTrace_action_label);
		Assert.isNotNull(clipboard);
		// TODO Show help!
		//WorkbenchHelp.setHelp(this, ITestUnitHelpContextIds.COPYTRACE_ACTION);
		fView= view;
		fClipboard= clipboard;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		String trace= fView.getTrace();
		if (trace == null)
			trace= ""; //$NON-NLS-1$
		
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		try{
			fClipboard.setContents(
				new String[]{ convertLineTerminators(trace) }, 
				new Transfer[]{ plainTextTransfer });
		}  catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) 
				throw e;
			if (MessageDialog.openQuestion(fView.getComposite().getShell(), TestUnitMessages.CopyTraceAction_problem, TestUnitMessages.CopyTraceAction_clipboard_busy))  
				run();
		}
	}
	
	private String convertLineTerminators(String in) {
		StringWriter stringWriter= new StringWriter();
		PrintWriter printWriter= new PrintWriter(stringWriter);
		StringReader stringReader= new StringReader(in);
		BufferedReader bufferedReader= new BufferedReader(stringReader);		
		String line;
		try {
			while ((line= bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}
		} catch (IOException e) {
			return in; // return the trace unfiltered
		}
		return stringWriter.toString();
	}
}
