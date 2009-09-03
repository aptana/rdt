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

import org.rubypeople.rdt.refactoring.core.pullup.PullUpRefactoring;

public class PullUpAction extends WorkbenchWindowActionDelegate {

	public void run() {
		run(PullUpRefactoring.class, PullUpRefactoring.NAME);
	}
}
