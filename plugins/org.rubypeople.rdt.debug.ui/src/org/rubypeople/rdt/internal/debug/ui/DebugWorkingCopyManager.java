/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui;

import org.eclipse.ui.IEditorInput;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.internal.ui.rubyeditor.WorkingCopyManager;
import org.rubypeople.rdt.ui.RubyUI;

/**
 * Controls access to the ruby working copy.  Isolated in this class as implementation requires
 * use of internal RDT UI code.  See bug 151260 for more information.
 *  
 * @since 1.3
 * @see org.rubypeople.rdt.internal.ui.rubyeditor.WorkingCopyManager
 */
public class DebugWorkingCopyManager {
	
	/**
	 * Returns the working copy remembered for the ruby script encoded in the
	 * given editor input.	 
	 *
	 * @param input the editor input
	 * @param primaryOnly if <code>true</code> only primary working copies will be returned
	 * @return the working copy of the compilation unit, or <code>null</code> if the
	 *   input does not encode an editor input, or if there is no remembered working
	 *   copy for this ruby script
	 */
	public static IRubyScript getWorkingCopy(IEditorInput input, boolean primaryOnly) {
    	//TODO Using RDT UI internal code here, see bug 151260 for more information
		return ((WorkingCopyManager)RubyUI.getWorkingCopyManager()).getWorkingCopy(input, primaryOnly);
	}
	
}
