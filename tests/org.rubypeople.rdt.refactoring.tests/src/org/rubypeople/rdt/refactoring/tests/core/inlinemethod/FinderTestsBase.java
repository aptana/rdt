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

package org.rubypeople.rdt.refactoring.tests.core.inlinemethod;

import org.jruby.ast.MethodDefNode;
import org.rubypeople.rdt.refactoring.core.inlinemethod.MethodFinder;
import org.rubypeople.rdt.refactoring.core.inlinemethod.SelectedCallFinder;
import org.rubypeople.rdt.refactoring.core.inlinemethod.TargetClassFinder;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.tests.FileTestCase;
import org.rubypeople.rdt.refactoring.tests.core.MultipleDocumentsInOneProvider;

public abstract class FinderTestsBase extends FileTestCase {

	protected MultipleDocumentsInOneProvider doc;

	public FinderTestsBase() {
		super("");
	}

	public FinderTestsBase(String fileName) {
		super(fileName);
	}

	protected MethodCallNodeWrapper findSelected(int pos, String file) {
		SelectedCallFinder finder = new SelectedCallFinder();
		doc.setActive(file);
		return finder.findSelectedCall(pos, doc);
	}

	protected MethodDefNode findDefinition(int pos, String file) {
		MethodCallNodeWrapper methodCallNode = findSelected(pos, file);
		return findDefinition(methodCallNode);
	}

	protected MethodDefNode findDefinition(MethodCallNodeWrapper methodCallNode) {
		String className = new TargetClassFinder().findTargetClass(methodCallNode, doc);
		return new MethodFinder().find(className, methodCallNode.getName(), doc);
	}
}