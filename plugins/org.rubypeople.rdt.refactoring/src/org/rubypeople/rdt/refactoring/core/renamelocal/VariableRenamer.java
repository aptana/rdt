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

package org.rubypeople.rdt.refactoring.core.renamelocal;

import java.util.ArrayList;

import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;

public class VariableRenamer
{

	protected final String oldName;

	protected final String newName;

	protected final IAbortCondition abort;

	public VariableRenamer(String oldName, String newName, IAbortCondition abort)
	{
		super();
		this.oldName = oldName;
		this.newName = newName;
		this.abort = abort;
	}

	private boolean isRenamedVariableRestArg(ArgsNode args, String[] localNames)
	{
		return args.getRestArg() > 0 && args.getRestArg() < localNames.length
				&& localNames[args.getRestArg()].equals(oldName);
	}

	public ArrayList<Node> replaceVariableNamesInNode(Node n, String[] localNames)
	{

		ArrayList<Node> nodes = new ArrayList<Node>();

		// the name of the MethodDefNode is stored in an argumentnode, which we do
		// not handle, so we have to skip
		if (n instanceof MethodDefNode)
		{
			nodes.addAll(replaceVariableNames(((MethodDefNode) n).getArgsNode()));
			nodes.addAll(replaceVariableNames(((MethodDefNode) n).getBodyNode()));
		}
		else
		{
			nodes.addAll(replaceVariableNames(n));
		}

		// rewrite the argument-list if we are renaming
		if (n instanceof MethodDefNode && isRenamedVariableRestArg(((MethodDefNode) n).getArgsNode(), localNames))
		{
			MethodDefNode defn = ((MethodDefNode) n);
			localNames[defn.getArgsNode().getRestArg()] = newName;
		}

		return nodes;
	}

	private ArrayList<Node> replaceVariableNames(Node n)
	{

		ArrayList<Node> renamedNodes = new ArrayList<Node>();

		if (n == null || abort.abort(n))
		{
			return renamedNodes;
		}

		if (n instanceof INameNode && ((INameNode) n).getName().equals(oldName))
		{

			renamedNodes.add(n);

			if (n instanceof LocalAsgnNode)
			{
				((LocalAsgnNode) n).setName(newName);
				replaceVariableNames(((LocalAsgnNode) n).getValueNode());
				return renamedNodes;
			}
			else if (n instanceof ArgumentNode)
			{
				((ArgumentNode) n).setName(newName);
			}
			else if (n instanceof LocalVarNode)
			{
				((LocalVarNode) n).setName(newName);
			}
			else if (n instanceof BlockArgNode)
			{
				((BlockArgNode) n).setName(newName);
			}
		}

		for (Object node : n.childNodes())
		{
			renamedNodes.addAll(replaceVariableNames((Node) node));
		}

		return renamedNodes;
	}
}
