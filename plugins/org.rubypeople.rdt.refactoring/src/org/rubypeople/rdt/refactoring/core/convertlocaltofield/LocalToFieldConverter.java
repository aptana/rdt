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
import java.util.LinkedHashMap;
import java.util.Map;

import org.jruby.ast.ClassVarNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.FixnumNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.StrNode;
import org.jruby.ast.VCallNode;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;
import org.rubypeople.rdt.refactoring.util.Constants;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class LocalToFieldConverter extends MultiEditProvider {

	public static final int INIT_IN_METHOD = 1;

	public static final int INIT_IN_CONSTRUCTOR = 2;

	private int initPlace;

	private Collection<LocalNodeWrapper> localNodes;

	private LocalToFieldConfig config;
	
	public LocalToFieldConverter(LocalToFieldConfig config) {
		this.config = config;
		config.setNewName(getLocalVarName());
		localNodes = gatherLocalNodes();
	}

	private Collection<LocalNodeWrapper> gatherLocalNodes() {

		Collection<Node> allNodes = gatherLocalNodes(NodeUtil.getBody(config.getEnclosingMethod()));
		Collection<LocalNodeWrapper> allLocalNodes = LocalNodeWrapper.createLocalNodes(allNodes);

		Collection<LocalNodeWrapper> affectedLocalNodes = new ArrayList<LocalNodeWrapper>();
		String selectedNodeName = LocalNodeWrapper.getLocalNodeName(config.getSelectedNode());
		for (LocalNodeWrapper aktLokalNode : allLocalNodes) {
			String aktLokalNodeName = LocalNodeWrapper.getLocalNodeName(aktLokalNode);
			if (selectedNodeName.equals(aktLokalNodeName)) {
				affectedLocalNodes.add(aktLokalNode);
			}
		}
		return affectedLocalNodes;
	}

	private Collection<Node> gatherLocalNodes(Node baseNode) {
		ArrayList<Node> candidates = new ArrayList<Node>();

		if (baseNode == null || baseNode instanceof MethodDefNode) {
			return candidates;
		}

		for (Object o : baseNode.childNodes()) {
			Node n = (Node) o;
			if (NodeUtil.nodeAssignableFrom(n, LocalNodeWrapper.LOCAL_NODES_CLASSES)) {
				candidates.add(n);
			}
			if (!NodeUtil.nodeAssignableFrom(n, DAsgnNode.class, LocalAsgnNode.class)) {
				candidates.addAll(gatherLocalNodes(n));
			}
		}
		return candidates;
	}

	@Override
	protected Collection<EditProvider> getEditProviders() {
		return getConversions(localNodes);
	}

	private Collection<EditProvider> getConversions(Collection<LocalNodeWrapper> localNodes) {
		Map<LocalNodeWrapper, EditProvider> editProviderMap = new LinkedHashMap<LocalNodeWrapper, EditProvider>();
		LocalNodeWrapper firstLocalNode = localNodes.toArray(new LocalNodeWrapper[localNodes.size()])[0];

		for (LocalNodeWrapper aktLocalNode : localNodes) {
			boolean initInConstructor = (initPlace == INIT_IN_CONSTRUCTOR);
			LocalToFieldEditProvider conversion = new LocalToFieldEditProvider(aktLocalNode, config.getNewName(), config.isClassField(),initInConstructor);
			editProviderMap.put(aktLocalNode, conversion);
		}
		if (initPlace == INIT_IN_CONSTRUCTOR) {
			editProviderMap.remove(firstLocalNode);
		}
		Collection<EditProvider> editProviders = new ArrayList<EditProvider>(editProviderMap.values());

		if (initPlace == INIT_IN_CONSTRUCTOR) {
			editProviders.add(new InitInConstructorEditProvider(firstLocalNode, config));
			editProviders.add(new DeleteEditProvider(firstLocalNode.getWrappedNode()));
		}

		return editProviders;
	}

	public void setNewName(String newName) {
		config.setNewName(newName);
	}

	public void setInitPlace(int initPlace) {
		this.initPlace = initPlace;
	}

	public void setIsClassField(boolean isClassField) {
		config.setClassField(isClassField);
	}

	public String getLocalVarName() {
		return LocalNodeWrapper.getLocalNodeName(config.getSelectedNode());
	}

	private Node findSelectedNode(Class<?>... filterNodes) {
		return SelectionNodeProvider.getSelectedNodeOfType(config.getDocumentProvider().getActiveFileRootNode(), config.getCaretPosition(), filterNodes);
	}

	boolean isInitializationExternalizable() {

		if (config.getEnclosingMethod() == null) {
			return false;
		}

		LocalNodeWrapper firstNodeInAST = getFirstLocalNodeWrapper();
		if (firstNodeInAST == null) {
			return false;
		}

		if (findSelectedNode(MultipleAsgnNode.class) != null) {
			return false;
		}

		if (firstNodeInAST.getWrappedNode() instanceof LocalAsgnNode) {
			LocalAsgnNode firstAssignment = (LocalAsgnNode) firstNodeInAST.getWrappedNode();
			Node assignmentValue = firstAssignment.getValueNode();

			return hasSameClass(assignmentValue, FixnumNode.class, StrNode.class, VCallNode.class, InstVarNode.class, ClassVarNode.class);
		}
		return false;
	}

	private boolean hasSameClass(Node assignmentValue, Class<?>... klasses) {

		for (Class klass : klasses) {
			if (assignmentValue.getClass().equals(klass)) {
				return true;
			}
		}
		return false;
	}

	private LocalNodeWrapper getFirstLocalNodeWrapper() {

		if (localNodes == null || localNodes.isEmpty()) {
			return null;
		}

		return localNodes.toArray(new LocalNodeWrapper[localNodes.size()])[0];
	}

	boolean isVariableInConstructor() {
		if (config.getEnclosingMethod() == null) {
			return false;
		}
		return config.getEnclosingMethod().getName().equals(Constants.CONSTRUCTOR_NAME);
	}
}
