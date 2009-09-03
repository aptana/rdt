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
import java.util.Iterator;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.FCallNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.VCallNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.renamemodule.ModuleSpecifierWrapper;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public abstract class PartialClassNodeWrapper implements INodeWrapper {
	
	private Node wrappedNode;

	private Collection<ModuleNode> enclosingModules;

	private Collection<MethodNodeWrapper> methods;

	private Collection<FieldNodeWrapper> fields;

	private Collection<Node> attributes;

	public PartialClassNodeWrapper(Node node) {
		wrappedNode = node;
	}
	
	public Collection<FieldNodeWrapper> getFields() {
		if(fields == null) {
			fields = getFieldsFromNode(wrappedNode);
		}
		return fields;
	}

	public static Collection<FieldNodeWrapper> getFieldsFromNode(Node wrappedNode) {
			Collection<FieldNodeWrapper> fields = new ArrayList<FieldNodeWrapper>();
			Collection<Node> fieldNodes = NodeProvider.getSubNodes(wrappedNode, FieldNodeWrapper.FIELD_NODE_CLASSES_WITHOUT_SYMBOL_NODE);

			for (Node currentField : fieldNodes) {
				fields.add(new FieldNodeWrapper(currentField));
			}
			addSymbolNodeFields(fields, wrappedNode);
			return fields;
	}

	private static void addSymbolNodeFields(Collection<FieldNodeWrapper> fields, Node wrappedNode) {
		Collection<AttrAccessorNodeWrapper> accessors = NodeProvider.getAccessorNodes(wrappedNode);
		for(AttrAccessorNodeWrapper aktAcessor : accessors) {
			fields.add(new FieldNodeWrapper(aktAcessor.getSymbolNode()));
		}
		for(Node aktNode : NodeProvider.getSubNodes(wrappedNode, FCallNode.class)) {
			FCallNode aktFCallNode = (FCallNode) aktNode;
			if(aktFCallNode.getName().equals(FieldNodeWrapper.ATTR_NAME)) {
				addAttrNodeFields(aktFCallNode, fields);
			}
		}
	}

	private static void addAttrNodeFields(FCallNode callNode, Collection<FieldNodeWrapper> fields) {
		if (NodeUtil.nodeAssignableFrom(callNode.getArgsNode(), ArrayNode.class)) {
			for (Object o : callNode.getArgsNode().childNodes()) {
				Node aktNode = (Node) o;
				if (NodeUtil.nodeAssignableFrom(aktNode, SymbolNode.class)) {
					SymbolNode symbolNode = ((SymbolNode) aktNode);
					fields.add(new FieldNodeWrapper(symbolNode));
				}
			}
		}
	}
	

	public Collection<MethodNodeWrapper> getMethods() {
		if(methods == null) {
			methods = new ArrayList<MethodNodeWrapper>();
			Collection<Node> methodNodes = NodeProvider.getSubNodes(wrappedNode, MethodDefNode.class);
			for (Node methodNode : methodNodes) {
				methods.add(new MethodNodeWrapper((MethodDefNode) methodNode, new ClassNodeWrapper(this)));
			}
		}
		return methods;
	}

	public abstract Node getClassBodyNode();

	public abstract String getClassName();

	public Node getWrappedNode() {
		return wrappedNode;
	}

	public abstract Node getDeclarationEndNode();

	public abstract String getSuperClassName();

	public Collection<Node> getAttrNodes() {
		if(attributes == null) {
			Collection<Node> allAttrs = NodeProvider.getAttributeNodes(wrappedNode);
			attributes = new ArrayList<Node>();
			for (Node node : allAttrs) {
				if (!isDirectChild(node))
					attributes.add(node);
			}
		}
		return attributes;
	}

	private boolean isDirectChild(Node child) {
		return isDirectChild(child, wrappedNode);
	}

	private boolean isDirectChild(Node child, Node parent) {
		for (Object aktChild : parent.childNodes()) {
			if (aktChild.equals(child))
				return true;
			if (ignoreInDirectChildLine((Node) aktChild))
				if (isDirectChild(child, (Node) aktChild))
					return true;
		}
		return false;
	}

	private boolean ignoreInDirectChildLine(Node node) {
		return node instanceof NewlineNode || node instanceof BlockNode;
	}

	public static PartialClassNodeWrapper getPartialClassNodeWrapper(Node node, Node rootNode) throws NoClassNodeException {
		if (node instanceof ClassNode)
			return new RealClassNodeWrapper(node);
		if (node instanceof SClassNode)
			return new SClassNodeWrapper(node, rootNode);
		throw new NoClassNodeException();
	}

	public Collection<AttrAccessorNodeWrapper> getAccessorNodes() {
		return NodeProvider.getAccessorNodes(wrappedNode);
	}

	public String getFile() {
		return wrappedNode.getPosition().getFile();
	}

	public void setEnclosingModules(Collection<ModuleNode> enclosingModules) {
		if(enclosingModules.size() > 0) { 
			this.enclosingModules = enclosingModules;
		}
	}

	public String getModulePrefix() {
		if (enclosingModules == null) {
			return ""; //$NON-NLS-1$
		}

		StringBuilder modulePrefix = new StringBuilder();
		Iterator<ModuleNode> it = enclosingModules.iterator();
		while (it.hasNext()) {
			ModuleNode currentModule = it.next();
			Node cPath = currentModule.getCPath();
			if (cPath instanceof Colon2Node) {
				modulePrefix.append(((Colon2Node) cPath).getName());
				if(it.hasNext()) {
					modulePrefix.append("::"); //$NON-NLS-1$
				}
			}
		}

		return modulePrefix.toString();
	}

	public Collection<Node> getInstFieldOccurences() {
		return NodeProvider.getInstFieldOccurences(wrappedNode);
	}
	
	public Collection<Node> getClassFieldOccurences() {
		return NodeProvider.getClassFieldOccurences(wrappedNode);
	}	
	
	public Collection<ModuleSpecifierWrapper> getIncludeCalls() {
		Collection<ModuleSpecifierWrapper> includes = new ArrayList<ModuleSpecifierWrapper>();

		for (Node node : NodeProvider.getSubNodes(wrappedNode, FCallNode.class)) {
			FCallNode call = (FCallNode) node;
			if("include".equals(call.getName())) { //$NON-NLS-1$
				includes.add(ModuleSpecifierWrapper.create(((ArrayNode) call.getArgsNode()).get(0), getModulePrefix()));
			}
		}
		return includes;
	}

	public Collection<MethodCallNodeWrapper> getMethodCalls(MethodDefNode decoratedMethod) {
		ArrayList<MethodCallNodeWrapper> localCalls = new ArrayList<MethodCallNodeWrapper>();
		
		Collection<Node> callNodes = NodeProvider.getSubNodes(this.wrappedNode, VCallNode.class, CallNode.class, FCallNode.class);
		for(Node currentCall : callNodes){
		
			MethodCallNodeWrapper callNode = new MethodCallNodeWrapper(currentCall);
			if(callNode.getName().equals(decoratedMethod.getName())){
				localCalls.add(callNode);
			}
		}
		
		return localCalls;
	}

	public Collection<SymbolNode> getMethodSymbols(MethodDefNode decoratedNode) {
		ArrayList<SymbolNode> localSymbols = new ArrayList<SymbolNode>();
	
		Collection<Node> fCallNodes = NodeProvider.getSubNodes(this.wrappedNode, FCallNode.class);
	
		for(Node currentCall : fCallNodes){
			FCallNode currentFCall = (FCallNode)currentCall;
			String callName = currentFCall.getName();
			
			if(VisibilityNodeWrapper.isVisibilityString(callName) || "alias_method".equals(callName)){			
				Collection<Node> symbolNodes = NodeProvider.getSubNodes(currentFCall, SymbolNode.class);
				for(Node currentItem : symbolNodes){
					SymbolNode currentSymbol = (SymbolNode)currentItem;
					if(currentSymbol.getName().equals(decoratedNode.getName())){
						localSymbols.add(currentSymbol);
					}
				}
			}
		}
		return localSymbols;
	}

	public METHOD_VISIBILITY getPosVisibility(int pos) {
		Collection<Node> vCallNodes = NodeProvider.getSubNodes(wrappedNode, VCallNode.class);
		VCallNode lastMatch = null;
		for(Node aktNode : vCallNodes) {
			VCallNode aktVCallNode = (VCallNode) aktNode;
			if(VisibilityNodeWrapper.isVisibilityString(aktVCallNode.getName())) {
				if(aktVCallNode.getPosition().getEndOffset() <= pos) {
					lastMatch = aktVCallNode;
				} else {
					break;
				}
			}
		}
		if(lastMatch == null) {
			return METHOD_VISIBILITY.PUBLIC;
		}
		return VisibilityNodeWrapper.getVisibility(lastMatch.getName());
	}

	public Collection<MethodCallNodeWrapper> getMethodCallNodes() {
		return NodeProvider.getMethodCallNodes(wrappedNode);
	}

	public Collection<MethodNodeWrapper> getExistingConstructors() {
		Collection<MethodNodeWrapper> methodNodes = getMethods();
		Collection<MethodNodeWrapper> constructors = new ArrayList<MethodNodeWrapper>();
		for(MethodNodeWrapper aktMethod : methodNodes) {
			if(aktMethod.isConstructor()) {
				constructors.add(aktMethod);
			}
		}
		return constructors;
	}
	
	public Collection<VisibilityNodeWrapper> getMethodVisibilityNodes() {
		Collection<Node> fCallNodes = NodeProvider.getSubNodes(wrappedNode, FCallNode.class);
		Collection<VisibilityNodeWrapper> visibilities = new ArrayList<VisibilityNodeWrapper>();
		for(Node aktNode : fCallNodes) { 
			FCallNode aktFCallNode = (FCallNode) aktNode;
			if(VisibilityNodeWrapper.isVisibilityString(aktFCallNode.getName())) {
				visibilities.add(new VisibilityNodeWrapper(aktFCallNode));
			}
		}
		return visibilities;
	}
}