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

package org.rubypeople.rdt.refactoring.classnodeprovider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.StrNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;

public class IncludedClassesProvider extends ClassNodeProvider {
	private String includingFileName;

	private ArrayList<IPath> includeFilePaths;

	public IncludedClassesProvider(IDocumentProvider documentProvider) {
		super(documentProvider);
		this.includingFileName = documentProvider.getActiveFileName();
		prepareIncludedFileNames();
		addIncludedFiles();
	}

	public String getIncludingFileName() {
		return includingFileName;
	}

	public String getIncludingFileDocument() {
		return documentProvider.getActiveFileContent();
	}

	private void prepareIncludedFileNames() {

		includeFilePaths = new ArrayList<IPath>();
		Node rootNode = documentProvider.getActiveFileRootNode();
		Collection<FCallNode> loadAndRequireNodes = NodeProvider.getLoadAndRequireNodes(rootNode);

		for (FCallNode fCallNode : loadAndRequireNodes) {
			addToIncludeFiles(fCallNode.getArgsNode());
		}
	}

	private void addToIncludeFiles(Node node) {
		if (node instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) node;
			for (Object o : arrayNode.childNodes()) {
				if (o instanceof StrNode) {
					StrNode strNode = (StrNode) o;
					appendPath(strNode.getValue().toString());
				}
			}
		}
	}

	private void appendPath(String pathName) {
		IPath path = new Path(pathName);
		if (path.getFileExtension() == null)
			path = path.addFileExtension("rb"); //$NON-NLS-1$
		if (!path.getFileExtension().equalsIgnoreCase("rb")) //$NON-NLS-1$
			path = path.addFileExtension("rb"); //$NON-NLS-1$

		includeFilePaths.add(path);
	}

	private void addIncludedFiles() {
		for (IPath currentPath : includeFilePaths) {
			String currentFileName = currentPath.toOSString();
			super.addSource(currentFileName);
		}

	}

	public IDocumentProvider getDocumentProvider() {
		return documentProvider;
	}
}
