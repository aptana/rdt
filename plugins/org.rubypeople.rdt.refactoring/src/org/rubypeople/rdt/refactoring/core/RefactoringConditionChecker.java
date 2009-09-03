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

package org.rubypeople.rdt.refactoring.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jruby.lexer.yacc.SyntaxException;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;

public abstract class RefactoringConditionChecker implements IRefactoringConditionChecker {

	private Map<String, Collection<String>> messages;
	private IDocumentProvider docProvider;
	private final IRefactoringConfig config;

	public RefactoringConditionChecker(IRefactoringConfig config) {
		this.docProvider = config.getDocumentProvider();
		this.config = config;
		initMessages();
		addLocalInitialErrors();
		if(shouldPerform(true)) {
			init(config);
		}
	}
	
	public boolean shouldPerform(boolean onlyInternalErrors) {
		if(!onlyInternalErrors && shouldPerform(true)) {
			checkInitialConditions();
		}
		return messages.get(IRefactoringConditionChecker.ERRORS).isEmpty();
	}
	
	public boolean shouldPerform() {
		return shouldPerform(false);
	}
	
	public Map<String, Collection<String>> getFinalMessages() {
		initMessages();
		checkFinalConditions();
		checkForSyntaxErrors();
		return messages;
	}

	private void checkForSyntaxErrors() {
		boolean syntaxError = false;
		for(String file : docProvider.getFileNames()) {
			if(NodeProvider.hasSyntaxErrors(file, docProvider.getFileContent(file))) {
				syntaxError = true;
			}
		}
		if (syntaxError) {
			addWarning(Messages.RefactoringConditionChecker_SyntaxErrorInProject);
		}
	}

	private void initMessages() {
		messages = new LinkedHashMap<String, Collection<String>>();
		messages.put(IRefactoringConditionChecker.ERRORS, new ArrayList<String>());
		messages.put(IRefactoringConditionChecker.WARNING, new ArrayList<String>());
	}

	public Map<String, Collection<String>> getInitialMessages() {
		initMessages();
		addInitialMessages();
		return messages;
	}

	private void addInitialMessages() {
		addLocalInitialErrors();
		if(shouldPerform(true)) {
			checkInitialConditions();
		}
	}

	private void addLocalInitialErrors() {
		String fileName = null;
		try {
			fileName = docProvider.getActiveFileName();
			if(docProvider.getActiveFileContent().equals("")) { //$NON-NLS-1$
				addError(Messages.RefactoringConditionChecker_EmptyDocument);
			}
			for(String aktFileName : docProvider.getFileNames()) {
				fileName = aktFileName;
				docProvider.getRootNode(aktFileName);
			}
		} catch(SyntaxException se) {
			String activeFileName = docProvider.getActiveFileName();
			if(fileName == null || fileName.equals(activeFileName)) {
				addError(Messages.RefactoringConditionChecker_SyntaxErrorInCurrent);
			}
		}
		
		if(NodeProvider.hasSyntaxErrors(docProvider.getActiveFileName(), docProvider.getActiveFileContent())) {
			addError(Messages.RefactoringConditionChecker_SyntaxErrorInCurrent);
		}
	}
	
	protected void addError(String message) {
		messages.get(IRefactoringConditionChecker.ERRORS).add(message);
	}

	protected void addWarning(String message) {
		messages.get(IRefactoringConditionChecker.WARNING).add(message);
	}
	
	protected boolean hasErrors() {
		return !messages.get(IRefactoringConditionChecker.ERRORS).isEmpty();
	}

	protected abstract void checkInitialConditions();

	protected void checkFinalConditions() {
	}
	
	public abstract void init(IRefactoringConfig configObj);

	public IRefactoringConfig getConfig() {
		return config;
	}
}
