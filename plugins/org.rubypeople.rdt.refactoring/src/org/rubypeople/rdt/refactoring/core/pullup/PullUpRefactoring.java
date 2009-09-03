/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.core.pullup;

import org.rubypeople.rdt.refactoring.core.RubyRefactoring;
import org.rubypeople.rdt.refactoring.ui.pages.MethodUpPullerSelectionPage;

public class PullUpRefactoring extends RubyRefactoring {

	public static final String NAME = "Pull up";

	public PullUpRefactoring() {
		super(NAME);
		MethodUpPuller upPuller = new MethodUpPuller(getDocumentProvider());
		setEditProvider(upPuller);
		pages.add(new MethodUpPullerSelectionPage(upPuller));
	}
}
