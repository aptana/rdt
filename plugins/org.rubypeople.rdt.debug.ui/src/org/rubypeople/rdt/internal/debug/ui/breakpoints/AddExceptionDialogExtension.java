/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rubypeople.rdt.internal.debug.ui.breakpoints;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.ITypeHierarchy;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.ui.dialogs.StatusInfo;
import org.rubypeople.rdt.ui.dialogs.TypeSelectionExtension;

/**
 * 
 * This class adds the extensions for the AddExceptionDialog, to use the new Camel cased selection dialog
 * @since 3.2
 *
 */
public class AddExceptionDialogExtension extends TypeSelectionExtension {
	
	/**
	 * Constructor
	 */
	public AddExceptionDialogExtension() {
		super();
	}
    
    /* (non-Rubydoc)
     * @see org.rubypeople.rdt.ui.dialogs.TypeSelectionExtension#getSelectionValidator()
     */
    public ISelectionStatusValidator getSelectionValidator() {
    	ISelectionStatusValidator validator = new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				IType type = null;
				for(int i = 0; i < selection.length; i ++) {
					type = (IType)selection[i];
					if(!isException(type)) {
						return new StatusInfo(IStatus.ERROR, "Selected type is not an exception");
					}
				}
				return new StatusInfo(IStatus.OK, ""); //$NON-NLS-1$
			}
    		
    	};
		return validator;
	}

    /**
     * Returns if the exception is checked or not
     * @param type the type of the exception breakpoint
     * @return true if it is a checked exception, false other wise
     * @since 3.2
     */
    protected boolean isException(IType type) {
        if(type != null) {
	    	try {
	            ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
	            IType curr = type;
	            while (curr != null) {
	                if ("Exception".equals(curr.getFullyQualifiedName())) { //$NON-NLS-1$
	                    return true;
	                }
	                curr = hierarchy.getSuperclass(curr);
	            }
	        } 
	        catch (RubyModelException e) {RdtDebugUiPlugin.log(e);}
        }
        return false;
    }
}
