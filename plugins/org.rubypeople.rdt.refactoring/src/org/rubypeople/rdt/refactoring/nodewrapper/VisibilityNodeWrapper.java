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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.SymbolNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class VisibilityNodeWrapper implements INodeWrapper {

	public static enum METHOD_VISIBILITY {
		PRIVATE, PROTECTED, PUBLIC, NONE
	}
	
	public static final String PUBLIC = "public"; //$NON-NLS-1$
	public static final String PROTECTED = "protected"; //$NON-NLS-1$
	public static final String PRIVATE = "private"; //$NON-NLS-1$

	private FCallNode wrappedNode;
	
	public VisibilityNodeWrapper(FCallNode node) {
		wrappedNode = node;
	}
	
	public boolean containsMethod(MethodNodeWrapper methodNode) {
		Collection<String> methodNames = getMethodNames();
		String searchedMethodName = methodNode.getName();
		for(String aktMethodName : methodNames) {
			if(aktMethodName.equals(searchedMethodName)) {
				return true;
			}
		}
		return false;
	}

	public Collection<String> getMethodNames() {

		Collection<String> methods = new ArrayList<String>();
		if(NodeUtil.nodeAssignableFrom(wrappedNode.getArgsNode(), ArrayNode.class)) {
			ArrayNode arrayNode = (ArrayNode) wrappedNode.getArgsNode();
			for(Object aktObj : arrayNode.childNodes()) {
				if (aktObj instanceof SymbolNode) {
					methods.add(((SymbolNode) aktObj).getName());
				}
			}
		}
		return methods;
	}

	public METHOD_VISIBILITY getVisibility() {
		return getVisibility(wrappedNode.getName());
	}
	
	public static METHOD_VISIBILITY getVisibility(String name) {
		if(name.equals(PUBLIC)) {
			return METHOD_VISIBILITY.PUBLIC;
		} else if (name.equals(PROTECTED)) {
			return METHOD_VISIBILITY.PROTECTED;
		} else if (name.equals(PRIVATE)) {
			return METHOD_VISIBILITY.PRIVATE;
		}
		return METHOD_VISIBILITY.NONE;
	}
	
	public ISourcePosition getPosition() {
		return wrappedNode.getPosition();
	}
	
	public static boolean isVisibilityString(String name) {
		return name.equals(PUBLIC) || name.equals(PROTECTED) || name.equals(PRIVATE);
	}
	
	public FCallNode getWrappedNode() {
		return wrappedNode;
	}
	
	public static String getVisibilityName(METHOD_VISIBILITY visibility) {
		if(METHOD_VISIBILITY.PUBLIC.equals(visibility)) {
			return PUBLIC;
		} else if (METHOD_VISIBILITY.PROTECTED.equals(visibility)) {
			return PROTECTED;
		} else if(METHOD_VISIBILITY.PRIVATE.equals((visibility))) {
			return PRIVATE;
		}
		return "none"; //$NON-NLS-1$
	}
}
