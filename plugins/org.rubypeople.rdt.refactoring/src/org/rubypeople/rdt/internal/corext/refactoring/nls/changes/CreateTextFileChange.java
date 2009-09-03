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
package org.rubypeople.rdt.internal.corext.refactoring.nls.changes;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rubypeople.rdt.core.IRubyModelStatusConstants;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.corext.refactoring.nls.NLSUtil;

public class CreateTextFileChange extends CreateFileChange {
	
	private final String fTextType;
	
	public CreateTextFileChange(IPath path, String source, String encoding, String textType) {
		super(path, source, encoding);
		fTextType= textType;
	}
	
	public String getTextType() {
		return fTextType;
	}
	
	public String getCurrentContent() throws RubyModelException {
		IFile file= getOldFile(new NullProgressMonitor());
		if (! file.exists())
			return ""; //$NON-NLS-1$
		InputStream stream= null;
		try{
			stream= file.getContents();
			String c= NLSUtil.readString(stream);
			return (c == null) ? "": c; //$NON-NLS-1$
		} catch (CoreException e){
			throw new RubyModelException(e, IRubyModelStatusConstants.CORE_EXCEPTION);
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException x) {
			}
		}
	}
	
	public String getPreview() {
		return getSource();
	}
}

