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

package org.rubypeople.rdt.refactoring.core.splitlocal;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.DAsgnNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.renamelocal.DynamicVariableRenamer;
import org.rubypeople.rdt.refactoring.core.renamelocal.IAbortCondition;
import org.rubypeople.rdt.refactoring.core.renamelocal.SingleLocalVariableEdit;
import org.rubypeople.rdt.refactoring.core.renamelocal.VariableRenamer;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class SplittedVariableRenamer implements ISplittedVariableRenamer {
	
	private static final class AbortOnOtherMethod implements IAbortCondition {
		private final LocalVarUsage usage;

		private AbortOnOtherMethod(LocalVarUsage usage) {
			this.usage = usage;
		}

		public boolean abort(Node currentNode) {
			if(currentNode instanceof NewlineNode) {
				currentNode = ((NewlineNode) currentNode).getNextNode();
			}
			if (currentNode.isInvisible())
				return false;
			return currentNode instanceof MethodDefNode 
			|| currentNode.getPosition().getEndOffset() < usage.getFromPosition() 
			|| currentNode.getPosition().getStartOffset() > usage.getToPosition();
		}
	}

	private static final class AbortOnOtherScope implements IAbortCondition {
		private final LocalVarUsage usage;

		private AbortOnOtherScope(LocalVarUsage usage) {
			this.usage = usage;
		}

		public boolean abort(Node currentNode) {
			if(currentNode instanceof NewlineNode) {
				currentNode = ((NewlineNode) currentNode).getNextNode();
			}
			if (currentNode.isInvisible())
				return false;
			return NodeUtil.hasScope(currentNode)
			|| currentNode.getPosition().getEndOffset() < usage.getFromPosition() 
			|| currentNode.getPosition().getStartOffset() > usage.getToPosition();
		}
	}

	private final Node scopeNode;

	public SplittedVariableRenamer(Node scopeNode) {
		this.scopeNode = scopeNode;
	}

	public Collection<EditProvider> rename(final Collection<LocalVarUsage> variables) {
		
		final ArrayList<EditProvider> edits = new ArrayList<EditProvider>();
		
		for (final LocalVarUsage localVarUsage : variables) {
			
			if(localVarUsage.getName().equals(localVarUsage.getNewName())) {
				continue;
			}
			
			VariableRenamer renamer = null; 
			
			assert localVarUsage.getNode() instanceof LocalAsgnNode || localVarUsage.getNode() instanceof DAsgnNode;
			
			if(localVarUsage.getNode() instanceof LocalAsgnNode) {
				renamer = createLocalVariableRenamer(localVarUsage);
			} else if(localVarUsage.getNode() instanceof DAsgnNode) {
				renamer = createDynamicVariableRenamer(localVarUsage);
			} else {
				return null;
			}
			
			final ArrayList<Node> nodes = renamer.replaceVariableNamesInNode(scopeNode, NodeUtil.getScope(scopeNode).getVariables());
			for (Node node : nodes) {
				edits.add(new SingleLocalVariableEdit(node, NodeUtil.getScope(scopeNode).getVariables()));	
			}
		}
		
		return edits;
	}
	
	private VariableRenamer createLocalVariableRenamer(final LocalVarUsage localVarUsage) {
		VariableRenamer renamer;
		renamer = new VariableRenamer(localVarUsage.getName(), localVarUsage.getNewName(), new AbortOnOtherMethod(localVarUsage));
		return renamer;
	}

	private VariableRenamer createDynamicVariableRenamer(final LocalVarUsage localVarUsage) {
		VariableRenamer renamer;
		renamer = new DynamicVariableRenamer(localVarUsage.getName(), localVarUsage.getNewName(), new AbortOnOtherScope(localVarUsage));
		return renamer;
	}

}
