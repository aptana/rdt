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
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
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

package org.rubypeople.rdt.refactoring.core.renameclass;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.CallNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.util.NameHelper;

public class ClassInstanciationFinder implements IClassInstanciationFinder {

	private String modulePrefix;

	public Collection<ConstructorCall> findAll(IDocumentProvider doc, String name, String modulePrefix) {
		
		this.modulePrefix = modulePrefix;
		Collection<ConstructorCall> found = new ArrayList<ConstructorCall>();
		
		for(String fileName : doc.getFileNames()) {
			if(! fileName.equals(doc.getActiveFileName())) {
				addIfCreatesInstance(name, found, new StringDocumentProvider(fileName, doc.getFileContent(fileName)));
			}
		}
		addIfCreatesInstance(name, found, new StringDocumentProvider(doc.getActiveFileName(), doc.getActiveFileContent()));

		return found;
	}

	private void addIfCreatesInstance(String name, Collection<ConstructorCall> found, DocumentProvider file) {
		for(Node node : NodeProvider.getSubNodes(file.getActiveFileRootNode(), CallNode.class)) {
			CallNode call = (CallNode) node;
			if(isConstructorFor(name, call)) {
				found.add(new ConstructorCall(call));
			}
		}
	}

	private boolean isConstructorFor(String name, CallNode call) {
		return isCallToNew(call) && ((isNotInModule() && createsAnInstance(name, call)) || createsAnInstanceWithFullModulePath(name, call));
	}

	private boolean isCallToNew(CallNode call) {
		return call.getName().equals("new");//$NON-NLS-1$
	}

	private boolean createsAnInstanceWithFullModulePath(String name, CallNode call) {
		return (call.getReceiverNode() instanceof Colon2Node && NameHelper.getFullyQualifiedName(call.getReceiverNode()).equals(modulePrefix + name	));
	}

	private boolean isNotInModule() {
		return modulePrefix == null || "".equals(modulePrefix);//$NON-NLS-1$
	}
	
	private boolean createsAnInstance(String name, CallNode call) {
		return call.getReceiverNode() instanceof ConstNode && ((ConstNode) call.getReceiverNode()).getName().equals(name);
	}
}
