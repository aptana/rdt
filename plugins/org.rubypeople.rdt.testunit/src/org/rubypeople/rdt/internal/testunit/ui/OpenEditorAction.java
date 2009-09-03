/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyModel;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyConventions;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.ui.rubyeditor.EditorUtility;

/**
 * Abstract Action for opening a Ruby editor.
 */
public abstract class OpenEditorAction extends Action {
	protected String fClassName;
	protected TestUnitView fTestRunner;
	private final boolean fActivate;
	
	protected OpenEditorAction(TestUnitView testRunner, String testClassName) {
		this(testRunner, testClassName, true);
	}

	public OpenEditorAction(TestUnitView testRunner, String className, boolean activate) {
		super(TestUnitMessages.OpenEditorAction_action_label); 
		fClassName= className;
		fTestRunner= testRunner;
		fActivate= activate;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		ITextEditor textEditor= null;
		try {
			IRubyElement element= findElement(getLaunchedProject(), fClassName);
			if (element == null) {
				MessageDialog.openError(getShell(), 
						TestUnitMessages.OpenEditorAction_error_cannotopen_title, TestUnitMessages.OpenEditorAction_error_cannotopen_message); 
				return;
			} 
			textEditor= (ITextEditor)EditorUtility.openInEditor(element, fActivate);			
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), TestUnitMessages.OpenEditorAction_error_dialog_title, TestUnitMessages.OpenEditorAction_error_dialog_message, e.getStatus()); 
			return;
		}
		if (textEditor == null) {
			fTestRunner.setInfoMessage(TestUnitMessages.OpenEditorAction_message_cannotopen);
			return;
		}
		reveal(textEditor);
	}
	
	protected Shell getShell() {
		return fTestRunner.getSite().getShell();
	}

	protected IRubyProject getLaunchedProject() {
		return fTestRunner.getLaunchedProject();
	}
	
	protected String getClassName() {
		return fClassName;
	}

	protected abstract IRubyElement findElement(IRubyProject project, String className) throws CoreException;
	
	protected abstract void reveal(ITextEditor editor);

	protected IType findType(IRubyProject project, String className) throws RubyModelException {
		return internalFindType(project, className, new HashSet<IRubyProject>());
	}

	private IType internalFindType(IRubyProject project, String className, Set<IRubyProject> visitedProjects) throws RubyModelException {
		if (visitedProjects.contains(project))
			return null;
		
		IStatus status = RubyConventions.validateRubyTypeName(className);
		if (!status.isOK()) return null;
		
		IType type= project.findType(className, (IProgressMonitor) null);
		if (type != null)
			return type;
		
		//fix for bug 87492: visit required projects explicitly to also find not exported types
		visitedProjects.add(project);
		IRubyModel javaModel= project.getRubyModel();
		String[] requiredProjectNames= project.getRequiredProjectNames();
		for (int i= 0; i < requiredProjectNames.length; i++) {
			IRubyProject requiredProject= javaModel.getRubyProject(requiredProjectNames[i]);
			if (requiredProject.exists()) {
				type= internalFindType(requiredProject, className, visitedProjects);
				if (type != null)
					return type;
			}
		}
		return null;
	}
	
}
