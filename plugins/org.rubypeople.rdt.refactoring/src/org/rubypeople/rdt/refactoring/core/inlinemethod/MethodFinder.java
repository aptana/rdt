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

package org.rubypeople.rdt.refactoring.core.inlinemethod;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.ti.util.OffsetNodeLocator;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
import org.rubypeople.rdt.refactoring.classnodeprovider.IncludedClassesProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;

public class MethodFinder implements IMethodFinder {

	public MethodDefNode find(String className, String methodName, IDocumentProvider doc) {
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.METHOD, className + '.' + methodName, IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		SearchParticipant[] participants = {SearchEngine.getDefaultSearchParticipant()};
		IRubySearchScope scope = SearchEngine.createWorkspaceScope(); // FIXME Create scope from doc provider?
		CollectingSearchRequestor requestor = new CollectingSearchRequestor();
		try {
			engine.search(pattern, participants, scope, requestor, new NullProgressMonitor());
			List<SearchMatch> matches = requestor.getResults();
			for (SearchMatch match : matches) {
				IRubyElement element = (IRubyElement) match.getElement();
				if (element.isType(IRubyElement.METHOD)) {
					IMethod method = (IMethod) element;
					Node rootNode = ASTProvider.getASTProvider().getAST(method.getRubyScript(), ASTProvider.WAIT_YES, new NullProgressMonitor());
					Node node = OffsetNodeLocator.Instance().getNodeAtOffset(rootNode, method.getSourceRange().getOffset());
					if (node instanceof MethodDefNode) {
						return (MethodDefNode) node;
					}
				}
			}
			
		} catch (CoreException e) {
			RubyPlugin.log(e);
		}
		
		for(MethodNodeWrapper method : new IncludedClassesProvider(doc).getAllMethodsFor(className)) {
			if(method.getName().equals(methodName)) {
				return method.getWrappedNode();
			}
		}
		
		return null;
	}
}
