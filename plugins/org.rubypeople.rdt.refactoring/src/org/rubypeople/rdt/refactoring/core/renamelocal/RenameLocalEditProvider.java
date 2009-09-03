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
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiEditProvider;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class RenameLocalEditProvider extends MultiEditProvider implements Observer
{

	private static final class AbortOnScope implements IAbortCondition
	{
		public boolean abort(Node currentNode)
		{
			return NodeUtil.hasScope(currentNode);
		}
	}

	private static final class AbortOnMethodDef implements IAbortCondition
	{
		public boolean abort(Node currentNode)
		{
			return currentNode instanceof MethodDefNode;
		}
	}

	private String selectedVariableName = ""; //$NON-NLS-1$

	private String newVariableName = ""; //$NON-NLS-1$

	private final RenameLocalConfig config;

	public RenameLocalEditProvider(RenameLocalConfig config)
	{
		this.config = config;
		config.setLocalVariablesEditProvider(this);
	}

	public void setSelectedVariableName(String name)
	{
		selectedVariableName = name;
	}

	public String getSelectedVariableName()
	{
		return selectedVariableName;
	}

	public void setNewVariableName(String name)
	{
		newVariableName = name;
	}

	public String getNewVariableName()
	{
		return newVariableName;
	}

	private ArrayList<Node> renameVariables()
	{
		VariableRenamer renamer = null;
		if (config.getSelectedNode() instanceof DVarNode || config.getSelectedNode() instanceof DAsgnNode)
		{
			renamer = new DynamicVariableRenamer(selectedVariableName, newVariableName, new AbortOnScope());
		}
		else
		{
			renamer = new VariableRenamer(selectedVariableName, newVariableName, new AbortOnMethodDef());
		}
		return renamer.replaceVariableNamesInNode(config.getSelectedMethod(), config.getLocalNames());
	}

	public void update(Observable subject, Object arg1)
	{
		if (subject instanceof VariableNameProvider)
		{
			setSelectedVariableName(((VariableNameProvider) subject).getSelected());
			setNewVariableName(((VariableNameProvider) subject).getName());
		}
	}

	@Override
	protected Collection<EditProvider> getEditProviders()
	{
		Collection<EditProvider> edits = new ArrayList<EditProvider>();
		for (Node n : renameVariables())
		{
			edits.add(new SingleLocalVariableEdit(n, config.getLocalNames()));
		}
		return edits;
	}
}
