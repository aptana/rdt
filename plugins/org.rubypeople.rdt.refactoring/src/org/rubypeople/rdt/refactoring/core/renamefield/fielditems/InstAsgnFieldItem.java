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

package org.rubypeople.rdt.refactoring.core.renamefield.fielditems;

import java.util.ArrayList;
import java.util.List;

import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.Node;

public class InstAsgnFieldItem extends FieldItem {

	private InstAsgnNode instAsgnNode;
	public InstAsgnFieldItem(InstAsgnNode instAsgnNode) {
		this.instAsgnNode = instAsgnNode;
	}

	@Override
	public String getFieldName() {
		return fieldName(instAsgnNode.getName());
	}

	@Override
	public Node getFieldNode() {
		return instAsgnNode;
	}

	@Override
	public Node getRenamedNode(String newName) {
		Node valueNode = instAsgnNode.getValueNode();
		replaceInstVars(valueNode, setPrefixName(newName, "@")); //$NON-NLS-1$
		return new InstAsgnNode(instAsgnNode.getPosition(), setPrefixName(newName, "@"), valueNode); //$NON-NLS-1$
		
	}
	
	private void replaceInstVars(Node valueNode, String newName){
		if(valueNode instanceof InstVarNode){
			InstVarNode instVarNode = (InstVarNode) valueNode;
			if(instVarNode.getName().equals(instAsgnNode.getName())){
				((InstVarNode)valueNode).setName(newName);
			}
		}
		List childNodes;
		if(valueNode != null){
			childNodes = valueNode.childNodes();
		}
		else{
			childNodes = new ArrayList<Object>();
		}
		for(Object currentChild : childNodes){
			replaceInstVars((Node)currentChild, newName);
		}
	}

	@Override
	public boolean concernsClassField() {
		return false;
	}
}
