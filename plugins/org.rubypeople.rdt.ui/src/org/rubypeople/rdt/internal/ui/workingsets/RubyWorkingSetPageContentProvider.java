/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.ui.workingsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.rubypeople.rdt.core.IRubyModel;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.ui.StandardRubyElementContentProvider;

class RubyWorkingSetPageContentProvider extends StandardRubyElementContentProvider {
	
	public boolean hasChildren(Object element) {

		if (element instanceof IProject && !((IProject)element).isAccessible())
			return false;

		// if (element instanceof ISourceFolder) {
		// ISourceFolder pkg= (ISourceFolder)element;
		// try {
		// if (pkg.getKind() == ISourceFolderRoot.K_BINARY)
		// return pkg.getChildren().length > 0;
		// } catch (RubyModelException ex) {
		// // use super behavior
		// }
		// }
		return super.hasChildren(element);
	}

	public Object[] getChildren(Object parentElement) {
		try {
			if (parentElement instanceof IRubyModel) 
				return concatenate(super.getChildren(parentElement), getNonRubyProjects((IRubyModel)parentElement));
			
			if (parentElement instanceof IProject) 
				return ((IProject)parentElement).members();

			return super.getChildren(parentElement);
		} catch (CoreException e) {
			return NO_CHILDREN;
		}
	}

	private Object[] getNonRubyProjects(IRubyModel model) throws RubyModelException {
		return model.getNonRubyResources();
	}
}
