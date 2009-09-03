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

package org.rubypeople.rdt.refactoring.core.renamemethod.methoditems;

import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;

public class CallCandidateItem extends MethodItem {

	private MethodCallNodeWrapper itemDecorator;
	
	public CallCandidateItem(MethodCallNodeWrapper currentCandidate) {
		this.itemDecorator = currentCandidate;
	}

	@Override
	public String getMethodPartName() {
		return itemDecorator.getName();
	}

	@Override
	public Node getMethodPartNode() {
		return itemDecorator.getWrappedNode();
	}

	@Override
	public Node getRenamedPartNode(String newName) {
		return getReamedNode(newName);
	}

	public Node getReamedNode(String newName) {
		int type = itemDecorator.getType();
		if(type == MethodCallNodeWrapper.CALL_NODE){
			return getRenamedCallNode(newName);
		}
		else if(type == MethodCallNodeWrapper.V_CALL_NODE){
			return getRenamedVCallNode(newName);
		}
		else if(type == MethodCallNodeWrapper.F_CALL_NODE){
			return getRenamedFCallNode(newName);
		}
		
		return null;
	}

	private Node getRenamedFCallNode(String newName) {
		Node arguments = ((FCallNode)itemDecorator.getWrappedNode()).getArgsNode();
		return NodeFactory.createFCallNode(newName, arguments);
	}

	private Node getRenamedVCallNode(String newName) {
		return NodeFactory.createVCallNode(newName);
	}

	private Node getRenamedCallNode(String newName) {
		return NodeFactory.createCallNode(itemDecorator.getReceiverNode(), newName, itemDecorator.getArgsNode());
	}
	
}
