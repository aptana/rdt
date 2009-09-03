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
package org.rubypeople.rdt.internal.corext.refactoring.changes;

import org.eclipse.core.resources.IResource;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.internal.corext.refactoring.nls.changes.CreateTextFileChange;
import org.rubypeople.rdt.refactoring.core.Messages;


public class CreateRubyScriptChange extends CreateTextFileChange {

	private final IRubyScript fUnit;

	public CreateRubyScriptChange(IRubyScript unit, String source, String encoding) {
		super(unit.getResource().getFullPath(), source, encoding, "ruby"); //$NON-NLS-1$
		fUnit= unit;
	}

	public String getName() {
		return Messages.format(Messages.RubyScriptChange_label, new String[] { fUnit.getElementName(), getPath(fUnit.getResource()) });
	}

	private String getPath(IResource resource) {
		final StringBuffer buffer= new StringBuffer(resource.getProject().getName());
		final String path= resource.getParent().getProjectRelativePath().toString();
		if (path.length() > 0) {
			buffer.append('/');
			buffer.append(path);
		}
		return buffer.toString();
	}
}
