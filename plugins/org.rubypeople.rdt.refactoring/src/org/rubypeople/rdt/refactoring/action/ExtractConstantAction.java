/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.action;

import org.rubypeople.rdt.internal.refactoring.RefactoringMessages;
import org.rubypeople.rdt.refactoring.core.extractconstant.ExtractConstantRefactoring;

public class ExtractConstantAction extends WorkbenchWindowActionDelegate {

	@Override
	public void run() {
		run(ExtractConstantRefactoring.class, RefactoringMessages.ExtractConstantAction_label);
	}
}
