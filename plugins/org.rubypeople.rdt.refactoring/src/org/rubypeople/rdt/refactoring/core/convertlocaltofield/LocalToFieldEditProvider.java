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

package org.rubypeople.rdt.refactoring.core.convertlocaltofield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.editprovider.ReplaceEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;

public class LocalToFieldEditProvider extends ReplaceEditProvider {

	private LocalNodeWrapper localNode;
	private boolean initInConstructor;

	public LocalToFieldEditProvider(LocalNodeWrapper localNode, String newName, boolean isClassField, boolean initInConstructor) {
		super(false);
		this.localNode = localNode;
		this.initInConstructor = initInConstructor;
		newName = ((isClassField) ? "@@" : "@") + newName; //$NON-NLS-1$ //$NON-NLS-2$
		Collection<LocalNodeWrapper> allLocalNodes = new ArrayList<LocalNodeWrapper>();
		allLocalNodes.add(localNode);
		allLocalNodes.addAll(LocalNodeWrapper.gatherLocalNodes(localNode.getWrappedNode()));
		replaceAllNames(allLocalNodes, LocalNodeWrapper.getLocalNodeName(localNode), newName);
	}

	private void replaceAllNames(Collection<LocalNodeWrapper> allLocalNodes, String orgName, String newName) {
		for (LocalNodeWrapper aktNode : allLocalNodes) {
			String aktNodeName = LocalNodeWrapper.getLocalNodeName(aktNode);
			if (aktNodeName.equals(orgName)) {
				setNodeName(aktNode, newName);
			}
		}
	}

	private void setNodeName(LocalNodeWrapper localNode, String newName) {
		localNode.setName(newName);
	}

	@Override
	protected int getOffsetLength() {
		if(initInConstructor)
			return localNode.getWrappedNode().getPositionIncludingComments().getEndOffset() - getOffset(null);
		else
			return localNode.getWrappedNode().getPosition().getEndOffset() - getOffset(null);
	}

	@Override
	protected Node getEditNode(int offset, String document) {
		if(initInConstructor)
			return localNode.getWrappedNode();
		else
			return stripComments(localNode.getWrappedNode());
	}

	private Node stripComments(Node wrappedNode) {
		wrappedNode.getComments().clear();
		for (Node child : wrappedNode.childNodes())
		{
			if (child.isInvisible())
				continue;
			stripComments(child);
		}
		return wrappedNode;
	}

	@Override
	protected int getOffset(String document) {
		if(initInConstructor)
			return localNode.getWrappedNode().getPositionIncludingComments().getStartOffset();
		else
			return localNode.getWrappedNode().getPosition().getStartOffset();
	}
}
