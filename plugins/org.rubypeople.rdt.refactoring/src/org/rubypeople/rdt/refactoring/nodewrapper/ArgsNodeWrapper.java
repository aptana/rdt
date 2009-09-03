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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.RestArgNode;
import org.rubypeople.rdt.refactoring.core.NodeFactory;

public class ArgsNodeWrapper implements INodeWrapper {

	private ArgsNode argsNode;

	private Collection<String> argumentNames;

	public ArgsNodeWrapper(ArgsNode argsNode) {
		this.argsNode = argsNode;
		argumentNames = new ArrayList<String>();
		if (hasArgs()) {
			ListNode list = argsNode.getArgs();
			for (Object obj : list.childNodes()) {
				if (obj instanceof ArgumentNode) {
					argumentNames.add(((ArgumentNode) obj).getName());
				}
			}
		}
	}

	public String getArgsListAsString() {
		if (!hasArgs())
			return ""; //$NON-NLS-1$

		StringBuilder argList = new StringBuilder();
		for (String argName : argumentNames) {
			argList.append(argName + ", "); //$NON-NLS-1$
		}
		return ' ' + argList.substring(0, argList.length() - 2);
	}

	public Collection<String> getArgsList() {
		return argumentNames;
	}

	public boolean hasArgs() {
		return argsNode.getRequiredArgsCount() != 0 || argsNode.getOptArgs() != null || argsNode.getBlockArgNode() != null || argsNode.getRestArg() > 0;
	}

	public ListNode getOptArgs() {
		return argsNode.getOptArgs();
	}

	public BlockArgNode getBlockArgNode() {
		return argsNode.getBlockArgNode();
	}

	public int getRestArg() {
		return argsNode.getRestArg();
	}
	
	public ArgsNode getWrappedNode() {
		return argsNode;
	}

	public ArgsNodeWrapper cloneWithNewArgName(String newArgName) {
		Collection<String> newArgNames = new ArrayList<String>(argumentNames);
		newArgNames.add(newArgName);
		ArgsNode tempArgsNode = NodeFactory.createArgsNode(newArgNames.toArray(new String[newArgNames.size()]), argsNode.getOptArgs(), argsNode.getRestArg(), (RestArgNode) argsNode.getRestArgNode(), argsNode.getBlockArgNode());
		return new ArgsNodeWrapper(tempArgsNode);
	}

	public boolean argsCountMatches(MethodCallNodeWrapper callNode) {
		int callArgs = callNode.getArgsCount();
		
		int minArgs = argsNode.getRequiredArgsCount();
		int maxArgs = minArgs + getOptArgsCount();
		if(argsNode.getRestArg() >= 0){
			maxArgs = Integer.MAX_VALUE;
		}
			
		return (callArgs >= minArgs) && (callArgs <= maxArgs);
	}

	private int getOptArgsCount() {
		if(getOptArgs()==null){
			return 0;
		}
		return getOptArgs().size();
	}
	
	
}
