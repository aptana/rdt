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

package org.rubypeople.rdt.refactoring.core.movemethod;

import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.editprovider.ReplaceEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;

public class ReplaceVisibilityEditProvider extends ReplaceEditProvider {

	private VisibilityNodeWrapper visbilityNode;
	private METHOD_VISIBILITY newVisibility;

	public ReplaceVisibilityEditProvider(VisibilityNodeWrapper visibilityNode, METHOD_VISIBILITY newVisibility) {
		super(true);
		this.visbilityNode = visibilityNode;
		this.newVisibility = newVisibility;
	}

	@Override
	protected int getOffsetLength() {
		return visbilityNode.getPosition().getEndOffset() - visbilityNode.getPosition().getStartOffset();
	}

	@Override
	protected Node getEditNode(int offset, String document) {
		String methodName = visbilityNode.getMethodNames().toArray(new String[visbilityNode.getMethodNames().size()])[0];
		return NodeFactory.createVisibilityNode(newVisibility, methodName);
	}

	@Override
	protected int getOffset(String document) {
		return visbilityNode.getPosition().getStartOffset();
	}

}
