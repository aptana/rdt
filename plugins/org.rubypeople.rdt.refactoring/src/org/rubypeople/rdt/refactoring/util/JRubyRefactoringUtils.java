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

package org.rubypeople.rdt.refactoring.util;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;

public abstract class JRubyRefactoringUtils {

	public static boolean isParameter(LocalNodeWrapper selectedItem, MethodDefNode enclosingMethod) {
		return isParameter(enclosingMethod.getScope().getVariables()[selectedItem.getId()], enclosingMethod);
	}

	public static boolean isParameter(String name, MethodDefNode enclosingMethod) {
		ArgsNode argsNode = enclosingMethod.getArgsNode();
		ListNode argumentList = argsNode.getArgs();

		if (argumentList == null) {
			return false;
		}

		for (Object currentArg : argumentList.childNodes()) {
			if (currentArg instanceof ArgumentNode) {
				ArgumentNode arg = (ArgumentNode) currentArg;
				if (arg.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}
	

	public static boolean isMathematicalExpression(Node node) {

		if (!(node instanceof CallNode)) {
			return false;
		}

		String name = ((CallNode) node).getName();

		String[] operators = new String[] { "**", "!", "+", "-", "*", "/", "%", ">>", "<<", "&", "^", "|", "+@", "-@", "<=>", ">", "<", ">=", "<=", "==", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$ //$NON-NLS-20$
				"===", "~", "&&" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		for (String currentOp : operators) {
			if (name.equals(currentOp)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasSamePosition(Node node1, Node node2){
		String file1 = node1.getPosition().getFile();
		String file2 = node2.getPosition().getFile();
		if(!file1.equals(file2)){
			return false;
		}
		
		int start1 = node1.getPosition().getStartOffset();
		int start2 = node2.getPosition().getStartOffset();
		if(start1 != start2){
			return false;
		}
		
		int end1 = node1.getPosition().getEndOffset();
		int end2 = node2.getPosition().getEndOffset();
		if(end1 != end2){
			return false;
		}
		return true;
	}
}
