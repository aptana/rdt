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
package org.rubypeople.rdt.internal.corext.refactoring.nls.changes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.rubypeople.rdt.core.IRubyModelStatusConstants;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.corext.refactoring.base.RDTChange;
import org.rubypeople.rdt.refactoring.core.Messages;

public class CreateFileChange extends RDTChange {

	private String fChangeName;

	private IPath fPath;
	private String fSource;
	private String fEncoding;
	private boolean fExplicitEncoding;
	private long fStampToRestore;

	public CreateFileChange(IPath path, String source, String encoding) {
		this(path, source, encoding, IResource.NULL_STAMP);
	}

	public CreateFileChange(IPath path, String source, String encoding, long stampToRestore) {
		Assert.isNotNull(path, "path"); //$NON-NLS-1$
		Assert.isNotNull(source, "source"); //$NON-NLS-1$
		fPath= path;
		fSource= source;
		fEncoding= encoding;
		fExplicitEncoding= fEncoding != null;
		fStampToRestore= stampToRestore;
	}

	/*
	private CreateFileChange(IPath path, String source, String encoding, long stampToRestore, boolean explicit) {
		Assert.isNotNull(path, "path"); //$NON-NLS-1$
		Assert.isNotNull(source, "source"); //$NON-NLS-1$
		Assert.isNotNull(encoding, "encoding"); //$NON-NLS-1$
		fPath= path;
		fSource= source;
		fEncoding= encoding;
		fStampToRestore= stampToRestore;
		fExplicitEncoding= explicit;
	}
	*/

	protected void setEncoding(String encoding, boolean explicit) {
		Assert.isNotNull(encoding, "encoding"); //$NON-NLS-1$
		fEncoding= encoding;
		fExplicitEncoding= explicit;
	}

	public String getName() {
		if (fChangeName == null)
			return Messages.format(Messages.createFile_Create_file, fPath.toOSString());
		else
			return fChangeName;
	}

	public void setName(String name) {
		fChangeName= name;
	}
	
	protected void setSource(String source) {
		fSource= source;
	}

	protected String getSource() {
		return fSource;
	}

	protected void setPath(IPath path) {
		fPath= path;
	}

	protected IPath getPath() {
		return fPath;
	}

	public Object getModifiedElement() {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
	}

	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		RefactoringStatus result= new RefactoringStatus();
		IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
		
		URI location= file.getLocationURI();
		if (location == null) {
			result.addFatalError(Messages.format(
					Messages.CreateFileChange_error_unknownLocation, 
				file.getFullPath().toString()));
			return result;
		}
		
		IFileInfo jFile= EFS.getStore(location).fetchInfo();
		if (jFile.exists()) {
			result.addFatalError(Messages.format(
					Messages.CreateFileChange_error_exists, 
				file.getFullPath().toString()));
			return result;
		}
		return result;
	}

	public Change perform(IProgressMonitor pm) throws CoreException {

		InputStream is= null;
		try {
			pm.beginTask(Messages.createFile_creating_resource, 3); 

			initializeEncoding();
			IFile file= getOldFile(new SubProgressMonitor(pm, 1));
			/*
			if (file.exists()) {
				CompositeChange composite= new CompositeChange(getName());
				composite.add(new DeleteFileChange(file));
				composite.add(new CreateFileChange(fPath, fSource, fEncoding, fStampToRestore, fExplicitEncoding));
				pm.worked(1);
				return composite.perform(new SubProgressMonitor(pm, 1));
			} else { */
			try {
				is= new ByteArrayInputStream(fSource.getBytes(fEncoding));
				file.create(is, false, new SubProgressMonitor(pm, 1));
				if (fStampToRestore != IResource.NULL_STAMP) {
					file.revertModificationStamp(fStampToRestore);
				}
				if (fExplicitEncoding) {
					file.setCharset(fEncoding, new SubProgressMonitor(pm, 1));
				} else {
					pm.worked(1);
				}
				return new DeleteFileChange(file);
			} catch (UnsupportedEncodingException e) {
				throw new RubyModelException(e, IRubyModelStatusConstants.IO_EXCEPTION);
			}
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
				throw new RubyModelException(ioe, IRubyModelStatusConstants.IO_EXCEPTION);
			} finally {
				pm.done();
			}
		}
	}

	protected IFile getOldFile(IProgressMonitor pm) {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
		} finally {
			pm.done();
		}
	}

	private void initializeEncoding() {
		if (fEncoding == null) {
			fExplicitEncoding= false;
			IFile file= ResourcesPlugin.getWorkspace().getRoot().getFile(fPath);
			if (file != null) {
				try {
					if (file.exists()) {
						fEncoding= file.getCharset(false);
						if (fEncoding == null) {
							fEncoding= file.getCharset(true);
						} else {
							fExplicitEncoding= true;
						}
					} else {
						fEncoding= file.getCharset(true);
					}
				} catch (CoreException e) {
					fEncoding= ResourcesPlugin.getEncoding();
					fExplicitEncoding= true;
				}
			} else {
				fEncoding= ResourcesPlugin.getEncoding();
				fExplicitEncoding= true;
			}
		}
		Assert.isNotNull(fEncoding);
	}
}
