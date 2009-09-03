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

package org.rubypeople.rdt.refactoring.core.extractmethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

import org.jruby.ast.BlockNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.LocalNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class ExtractedMethodHelper extends Observable {

	public static final VisibilityNodeWrapper.METHOD_VISIBILITY DEFAULT_VISIBILITY = VisibilityNodeWrapper.METHOD_VISIBILITY.PRIVATE;

	private VisibilityNodeWrapper.METHOD_VISIBILITY visibility;

	private Node selectedNodes;

	private Collection<LocalNodeWrapper> localNodesNeededAsReturnValues;

	private Map<Integer, LocalNodeWrapper> afterSelectionNodes;

	private ArrayList<ExtractedArgument> argsOrdered;

	private String methodName = ""; //$NON-NLS-1$

	private final boolean isStaticMethod;

	
	public ExtractedMethodHelper(ExtractMethodConfig config) {
		selectedNodes = config.getSelectedNodes();
		visibility = initVisibility(config.hasEnclosingClassNode());
		isStaticMethod = config.getEnclosingMethodNode() instanceof DefsNode;
		initAfterSelectionNodes(config.getEnclosingScopeNode());
		initNeededLocalNodes();
	}

	public Node getSelectedNodes() {
		return selectedNodes;
	}

	private VisibilityNodeWrapper.METHOD_VISIBILITY initVisibility(boolean isDefnNodeInClassNode) {
		if (isDefnNodeInClassNode) {
			return DEFAULT_VISIBILITY;
		}
		return VisibilityNodeWrapper.METHOD_VISIBILITY.NONE;
	}

	private void initAfterSelectionNodes(Node enclosingScopeNode) {
		Collection<Node> allNodes = NodeProvider.getAllNodes(enclosingScopeNode);
		afterSelectionNodes = new LinkedHashMap<Integer, LocalNodeWrapper>();
		int endPostOfLastSelectedNode = selectedNodes.getPosition().getEndOffset();
		boolean isWrongScopeNode = false;
		int endOfOtherScope = 0;

		for (Node aktNode : allNodes) {
			if (NodeUtil.hasScope(aktNode)) {
				if (aktNode.getPosition().getEndOffset() > endOfOtherScope) {
					isWrongScopeNode = false;
					endOfOtherScope = 0;
				}
				if (!containsSameNodes(selectedNodes, aktNode)) {
					isWrongScopeNode = true;
					int endOfAktNode = aktNode.getPosition().getEndOffset();
					if (endOfAktNode > endOfOtherScope) {
						endOfOtherScope = endOfAktNode;
					}
				}
			}
			if (isLocalNodeOfEnclosingScope(isWrongScopeNode, aktNode)) {
				int aktStartOffset = aktNode.getPosition().getStartOffset();
				if (aktStartOffset > endPostOfLastSelectedNode) {
					LocalNodeWrapper localNode = new LocalNodeWrapper(aktNode);
					afterSelectionNodes.put(Integer.valueOf(localNode.getId()), localNode);
				}
			}
		}
	}

	private boolean containsSameNodes(Node selectionScopeNode, Node aktScopeNode) {
		if(selectionScopeNode.childNodes().isEmpty()) {
			return false;
		}
		Object nodeToFind = selectionScopeNode.childNodes().toArray()[0];
		for (Object aktNode : NodeProvider.getAllNodes(aktScopeNode)) {
			if (aktNode.equals(nodeToFind)) {
				return true;
			}
		}
		return false;
	}

	private boolean isLocalNodeOfEnclosingScope(boolean isWrongScopeNode, Node aktNode) {
		return !isWrongScopeNode && (NodeUtil.nodeAssignableFrom(aktNode, LocalNodeWrapper.LOCAL_NODES_CLASSES));
	}

	private void initNeededLocalNodes() {
		Collection<LocalNodeWrapper> allLocalNodes = LocalNodeWrapper.gatherLocalNodes(selectedNodes);
		Map<String, LocalNodeWrapper> firstOccurrenceIsNotDefinitionLocalNodes = new LinkedHashMap<String, LocalNodeWrapper>();
		Map<String, LocalNodeWrapper> localNodesNeededAsReturnValues = new LinkedHashMap<String, LocalNodeWrapper>();
		Map<String, LocalNodeWrapper> firstOccurrenceIsDefinitionLocalNodes = new LinkedHashMap<String, LocalNodeWrapper>();

		for (LocalNodeWrapper aktLocalNode : allLocalNodes) {
			String nodeName = getLocalNodeName(aktLocalNode);
			if (aktLocalNode.isAsgnNode()) {
				if (!firstOccurrenceIsNotDefinitionLocalNodes.containsKey(nodeName) && !containsOccurrencesOfItself(aktLocalNode)) {
					firstOccurrenceIsDefinitionLocalNodes.put(nodeName, aktLocalNode);
				}
				if (localNodeNeededAfterSelectedNodes(aktLocalNode)) {
					localNodesNeededAsReturnValues.put(nodeName, aktLocalNode);
				}
			} else {
				if (!firstOccurrenceIsDefinitionLocalNodes.containsKey(nodeName)) {
					firstOccurrenceIsNotDefinitionLocalNodes.put(nodeName, aktLocalNode);
				}
			}
		}
		this.localNodesNeededAsReturnValues = localNodesNeededAsReturnValues.values();
		argsOrdered = new ArrayList<ExtractedArgument>();
		for (LocalNodeWrapper aktArgNode : firstOccurrenceIsNotDefinitionLocalNodes.values()) {
			argsOrdered.add(new ExtractedArgument(aktArgNode.getId(), getLocalNodeName(aktArgNode)));
		}
	}

	private boolean containsOccurrencesOfItself(LocalNodeWrapper localNode) {
		String name = getLocalNodeName(localNode);
		Collection<LocalNodeWrapper> subNodes = LocalNodeWrapper.gatherLocalNodes(localNode.getWrappedNode());
		for (LocalNodeWrapper aktSubNode : subNodes) {
			if (!aktSubNode.equals(localNode) && getLocalNodeName(aktSubNode).equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean localNodeNeededAfterSelectedNodes(LocalNodeWrapper localNode) {
		return afterSelectionNodes.containsKey(Integer.valueOf(localNode.getId()));
	}
	
	public Node getMethodNode(boolean needsNewLineAtBeginOfBlock, boolean needsNewLineAtEndOfBlock) {
		updateLocalNamesInNamedNodes(selectedNodes);
		
		BlockNode blockNode = (BlockNode) (selectedNodes instanceof BlockNode ? selectedNodes : NodeFactory.createBlockNode(NodeFactory.createNewLineNode(selectedNodes)));
		
		if (localNodesNeededAsReturnValues.size() > 0) {
			blockNode.add(getReturnNode());
		}
		Node methodDefinitionNode = null;
		
		if(isStaticMethod) {
			methodDefinitionNode = NodeFactory.createStaticMethodNode(methodName, getInMethodStringMethodArgs(), null, blockNode);
		} else {
			methodDefinitionNode = NodeFactory.createMethodNodeWithoutNewline(methodName, NodeFactory.createArgsNode(getInMethodStringMethodArgs()), blockNode);
		}
		
		methodDefinitionNode = NodeFactory.createNewLineNode(methodDefinitionNode);
		
		if (visibility.equals(VisibilityNodeWrapper.METHOD_VISIBILITY.NONE)) {
			return NodeFactory.createBlockNode(needsNewLineAtBeginOfBlock, needsNewLineAtEndOfBlock, methodDefinitionNode);
		}
		
		Node visibilityNode = NodeFactory.createVisibilityNode(visibility, methodName);
		return NodeFactory.createBlockNode(needsNewLineAtBeginOfBlock, needsNewLineAtEndOfBlock, methodDefinitionNode, visibilityNode);
	}

	private void updateLocalNamesInNamedNodes(Node scopeNode) {
		Collection<LocalNodeWrapper> allLocalNodes = LocalNodeWrapper.gatherLocalNodes(scopeNode);
		for (LocalNodeWrapper aktLocalNode : allLocalNodes) {
			updateLocalNameInNamedNode(aktLocalNode);
		}
		updateArgsOrderedNames();
	}

	private void updateArgsOrderedNames() {
		for (ExtractedArgument aktArg : argsOrdered) {
			aktArg.setOldInExtractedMethodArgName(aktArg.getNewInExtractedMethodArgName());
		}
	}

	private void updateLocalNameInNamedNode(LocalNodeWrapper aktNode) {
		String oldName = aktNode.getName();
		for (ExtractedArgument aktArg : argsOrdered) {
			if (aktArg.getOldInExtractedMethodArgName().equals(oldName)) {
				String newName = aktArg.getNewInExtractedMethodArgName();
				aktNode.setName(newName);
			}
		}
	}

	public ArrayList<String> getLocalOnlyVariables() {

		ArrayList<String> arguments = new ArrayList<String>();

		for (ExtractedArgument arg : argsOrdered) {
			arguments.add(arg.getOriginalName());
		}

		ArrayList<String> local = new ArrayList<String>();

		for(LocalNodeWrapper varNode : LocalNodeWrapper.gatherLocalNodes(getMethodCallNode())) {
			if (!arguments.contains(varNode.getName())) {
				local.add(varNode.getName());
			}
		}

		for (DAsgnNode n : NodeProvider.gatherLocalDAsgnNodes(selectedNodes)) {
			local.add(n.getName());
		}

		return local;
	}

	private Node getReturnNode() {
		if (localNodesNeededAsReturnValues.size() == 1) {
			LocalNodeWrapper node = localNodesNeededAsReturnValues.toArray(new LocalNodeWrapper[localNodesNeededAsReturnValues.size()])[0];
			DVarNode localNode = NodeFactory.createDVarNode(getLocalNodeName(node));
			return NodeFactory.createNewLineNode(localNode);
		}
		Collection<Node> localVarNodes = getLocalVarNodes(localNodesNeededAsReturnValues);
		return NodeFactory.createNewLineNode(NodeFactory.createArrayNode(localVarNodes));
	}

	private Collection<String> getInMethodStringMethodArgs() {
		Collection<String> args = new ArrayList<String>();
		for (ExtractedArgument arg : argsOrdered) {
			args.add(arg.getNewInExtractedMethodArgName());
		}
		return args;
	}

	private Collection<Node> getCallArgs() {
		Collection<Node> args = new ArrayList<Node>();
		for (ExtractedArgument aktArg : argsOrdered) {
			args.add(NodeFactory.createDVarNode(aktArg.getOriginalName()));
		}
		return args;
	}

	private Collection<Node> getLocalVarNodes(Collection<LocalNodeWrapper> localNodes) {
		Collection<Node> arguments = new ArrayList<Node>();
		for (LocalNodeWrapper aktLocalNode : localNodes) {
			if (aktLocalNode.isDVarNode()) {
				arguments.add(NodeFactory.createDVarNode(aktLocalNode.getName()));
			} else {
				arguments.add(NodeFactory.createLocalVarNode(aktLocalNode.getName()));
			}
		}
		return arguments;
	}

	public Node getMethodCallNode() {
		Node methodCallNode = NodeFactory.createMethodCallNode(methodName, getCallArgs());

		if (localNodesNeededAsReturnValues.size() > 0) {
			return getAsgnNode(methodCallNode);
		}
		return methodCallNode;
	}

	private Node getAsgnNode(Node methodCallNode) {
		if (localNodesNeededAsReturnValues.size() == 1) {
			LocalNodeWrapper firstReturnNode = localNodesNeededAsReturnValues.toArray(new LocalNodeWrapper[localNodesNeededAsReturnValues.size()])[0];
			return getLocalAsgnNode(firstReturnNode, methodCallNode);
		}
		Collection<Node> localAsgnNodes = getLocalAsgnNodesForMultipleAsgnNode();
		return NodeFactory.createMultipleAsgnNode(localAsgnNodes, methodCallNode);
	}

	private LocalAsgnNode getLocalAsgnNode(LocalNodeWrapper localNode, Node valueNode) {
		String name = getLocalNodeName(localNode);
		return NodeFactory.createLocalAsgnNode(name, localNode.getId(), valueNode);
	}

	private Collection<Node> getLocalAsgnNodesForMultipleAsgnNode() {
		Collection<Node> result = new ArrayList<Node>();
		for (LocalNodeWrapper aktNode : localNodesNeededAsReturnValues) {
			result.add(NodeFactory.createLocalAsgnNode(getLocalNodeName(aktNode), aktNode.getId(), null));
		}
		return result;
	}

	private String getLocalNodeName(LocalNodeWrapper node) {
		return LocalNodeWrapper.getLocalNodeName(node);
	}

	public Collection<String> getArguments() {
		return getInMethodStringMethodArgs();
	}

	public boolean hasArguments() {
		return !argsOrdered.isEmpty();
	}

	public void changeParameter(int fromId, int toId) {
		ExtractedArgument aktArg = argsOrdered.remove(fromId);
		argsOrdered.add(toId, aktArg);
		setChanged();
		notifyObservers();
	}

	public void changeParameter(int id, String name) {
		ExtractedArgument aktArg = argsOrdered.get(id);
		aktArg.setNewInExtractedMethodArgName(name);
		setChanged();
		notifyObservers();
	}

	public void setVisibility(VisibilityNodeWrapper.METHOD_VISIBILITY v) {
		visibility = v;
	}

	public VisibilityNodeWrapper.METHOD_VISIBILITY getVisibility() {
		return visibility;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
		setChanged();
		notifyObservers();
	}

}
