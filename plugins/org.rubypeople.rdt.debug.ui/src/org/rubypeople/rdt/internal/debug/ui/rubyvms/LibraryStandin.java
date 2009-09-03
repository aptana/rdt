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
package org.rubypeople.rdt.internal.debug.ui.rubyvms;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;


/**
 * Wrapper for an original library location, to support editing.
 * 
 */
public final class LibraryStandin {
	private IPath fSystemLibrary;
	
	/**
	 * Creates a new library standin on the given library location.
	 */	
	public LibraryStandin(IPath path) {
		fSystemLibrary= path;
	}		
		
	/**
	 * Returns the JRE library jar location.
	 * 
	 * @return The JRE library jar location.
	 */
	public IPath getSystemLibraryPath() {
		return fSystemLibrary;
	}
	

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof LibraryStandin) {
			LibraryStandin lib = (LibraryStandin)obj;
			return getSystemLibraryPath().equals(lib.getSystemLibraryPath());
		} 
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getSystemLibraryPath().hashCode();
	}
	
	/**
	 * Returns whether the given paths are equal - either may be <code>null</code>.
	 * @param path1 path to be compared
	 * @param path2 path to be compared
	 * @return whether the given paths are equal
	 */
	protected boolean equals(IPath path1, IPath path2) {
		return equalsOrNull(path1, path2);
	}
	
	/**
	 * Returns whether the given objects are equal - either may be <code>null</code>.
	 * @param o1 object to be compared
	 * @param o2 object to be compared
	 * @return whether the given objects are equal or both null
	 * @since 3.1
	 */	
	private boolean equalsOrNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		if (o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	/**
	 * Returns an equivalent library location.
	 * 
	 * @return library location
	 */
	IPath toLibraryLocation() {
		return getSystemLibraryPath();
	}
	
	/**
	 * Returns a status for this library describing any error states
	 * 
	 * @return
	 */
	IStatus validate() {
		if (!getSystemLibraryPath().toFile().exists()) {
			return new Status(IStatus.ERROR, RdtDebugUiPlugin.getUniqueIdentifier(), RdtDebugUiConstants.INTERNAL_ERROR, 
					MessageFormat.format(RubyVMMessages.LibraryStandin_0, getSystemLibraryPath().toOSString()), null);
		}
		return Status.OK_STATUS;
	}
	
}
