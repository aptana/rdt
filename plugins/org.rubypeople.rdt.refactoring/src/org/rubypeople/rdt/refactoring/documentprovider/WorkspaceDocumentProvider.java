/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.refactoring.documentprovider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.util.Util;

public class WorkspaceDocumentProvider extends DocumentProvider {

	private IFile activeFile;

	private String activeFileContent;

	private IProject activeProject;

	private IWorkspaceRoot workspaceRoot;

	public WorkspaceDocumentProvider(IFile activeFile) {
		this.activeFile = activeFile;
		this.activeProject = activeFile.getProject();
		workspaceRoot = this.activeFile.getWorkspace().getRoot();
		this.activeFileContent = getFileContent(getActiveFileName());

	}

	public String getActiveFileContent() {

		return activeFileContent;
	}

	public String getActiveFileName() {
		return activeFile.getFullPath().toOSString();
	}

	public IFile getIFile(String fileName) {

		fileName = makePathAbsoulte(fileName);
		return workspaceRoot.getFile(new Path(fileName));
	}

	public Collection<String> getFileNames() {
		ArrayList<String> fileNames = new ArrayList<String>();
		fillFileNames(fileNames, activeProject);
		return fileNames;

	}

	private void fillFileNames(ArrayList<String> fileNames, IContainer container) {
		try {
			for (IResource resource : container.members()) {
				if (resource.getType() == IResource.FILE) {
					IFile currentFile = (IFile) resource;
					String fileExtension = currentFile.getFileExtension();
					if(fileExtension != null && fileExtension.equals("rb")) { //$NON-NLS-1$
						fileNames.add(currentFile.getFullPath().toOSString());
					}
				}
				if (resource instanceof IContainer) {
					IContainer subContainer = (IContainer) resource;
					fillFileNames(fileNames, subContainer);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public String getFileContent(String fileName) {
		IFile currentFile = getIFile(fileName);
		try {
			return new String(Util.getResourceContentsAsCharArray(currentFile));
		}
		catch (RubyModelException e) {
			/** Resource does not exist, that happens if a library file is included from an external location.**/
		}
		return null;
	}

	private String makePathAbsoulte(String path) {
		Path absolutePath = new Path(path);
		if (absolutePath.isAbsolute()) {
			return absolutePath.toOSString();
		}
		return activeFile.getFullPath().removeLastSegments(1).append(path).toOSString();
	}
}
