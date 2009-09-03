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

package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.renamemodule.ModuleSpecifierWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;

public class ClassNodeWrapper implements INodeWrapper {

	private Collection<PartialClassNodeWrapper> partialClassNodes;

	public ClassNodeWrapper(PartialClassNodeWrapper partialClassNode) {
		partialClassNodes = new ArrayList<PartialClassNodeWrapper>();
		addPartialClassNode(partialClassNode);
	}

	public void addPartialClassNode(PartialClassNodeWrapper partialClassNode) {
		partialClassNodes.add(partialClassNode);
	}

	public Collection<FieldNodeWrapper> getFields() {
		ArrayList<FieldNodeWrapper> fields = new ArrayList<FieldNodeWrapper>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			fields.addAll(partialClassNode.getFields());
		}
		return fields;
	}
	
	public Collection<ModuleSpecifierWrapper> getIncludes() {
		ArrayList<ModuleSpecifierWrapper> fields = new ArrayList<ModuleSpecifierWrapper>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			fields.addAll(partialClassNode.getIncludeCalls());
		}
		return fields;
	}

	public Collection<MethodNodeWrapper> getMethods() {
		Collection<MethodNodeWrapper> methodNodes = new ArrayList<MethodNodeWrapper>();
		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			methodNodes.addAll(partialClassNode.getMethods());
		}
		return methodNodes;
	}
	
	public boolean hasMethod(String name) {
		for (MethodNodeWrapper method : getMethods()) {
			if(name.equals(method.getName())) {
				return true;
			}
		}
		return false;
	}

	public PartialClassNodeWrapper getFirstPartialClassNode() {
		return partialClassNodes.iterator().next();
	}

	public String getName() {
		return getFirstPartialClassNode().getClassName();
	}

	public String getSuperClassName() {
		return getFirstPartialClassNode().getSuperClassName();
	}

	public MethodNodeWrapper getConstructorNode() {
		Collection<MethodNodeWrapper> constructors = getExistingConstructors();
		if (constructors.isEmpty()) {
			return new MethodNodeWrapper(NodeFactory.createDefaultConstructor(), this);
		}
		return constructors.toArray(new MethodNodeWrapper[constructors.size()])[constructors.size() - 1];
	}

	public Collection<MethodNodeWrapper> getExistingConstructors() {
		Collection<MethodNodeWrapper> constructors = new ArrayList<MethodNodeWrapper>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			constructors.addAll(partialClassNode.getExistingConstructors());
		}
		return constructors;
	}

	public boolean hasConstructor() {
		return !getExistingConstructors().isEmpty();
	}

	public Collection<Node> getAttrNodes() {
		Collection<Node> attrNodes = new ArrayList<Node>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			attrNodes.addAll(partialClassNode.getAttrNodes());
		}
		return attrNodes;
	}

	public Collection<AttrAccessorNodeWrapper> getAccessorNodes() {
		Collection<AttrAccessorNodeWrapper> accessorNodes = new ArrayList<AttrAccessorNodeWrapper>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			accessorNodes.addAll(partialClassNode.getAccessorNodes());
		}
		return accessorNodes;
	}

	public Collection<PartialClassNodeWrapper> getPartialClassNodes() {
		return partialClassNodes;
	}

	public Collection<PartialClassNodeWrapper> getPartialClassNodesOfFile(String file) {
		ArrayList<PartialClassNodeWrapper> matchingPartialClasses = new ArrayList<PartialClassNodeWrapper>();

		for (PartialClassNodeWrapper currentClassPart : partialClassNodes) {
			String fileOfClassPart = currentClassPart.getFile();
			if (fileOfClassPart.equals(file)) {
				matchingPartialClasses.add(currentClassPart);
			}
		}
		return matchingPartialClasses;
	}

	public Collection<Node> getInstFieldOccurences() {
		Collection<Node> allFieldOccuences = new ArrayList<Node>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
				allFieldOccuences.addAll(partialClassNode.getInstFieldOccurences());
		}
		return allFieldOccuences;
	}

	public Collection<Node> getClassFieldOccurences() {
		Collection<Node> classFieldOccuences = new ArrayList<Node>();

		for (PartialClassNodeWrapper partialClassNode : partialClassNodes) {
			classFieldOccuences.addAll(partialClassNode.getClassFieldOccurences());
		}
		return classFieldOccuences;
	}

	public Collection<MethodCallNodeWrapper> getMethodCalls(MethodDefNode decoratedNode) {
		ArrayList<MethodCallNodeWrapper> calls = new ArrayList<MethodCallNodeWrapper>();
		for(PartialClassNodeWrapper classPart : partialClassNodes){
			calls.addAll(classPart.getMethodCalls(decoratedNode));
		}		
		return calls;
	}
	
	public Collection<MethodCallNodeWrapper> getMethodCallNodes() {
		ArrayList<MethodCallNodeWrapper> methodCalls = new ArrayList<MethodCallNodeWrapper>();
		for(PartialClassNodeWrapper classPart : partialClassNodes){
			methodCalls.addAll(classPart.getMethodCallNodes());
		}
		return methodCalls;
	}

	public Collection<SymbolNode> getMethodSymbols(MethodDefNode decoratedNode) {
		ArrayList<SymbolNode> symbols = new ArrayList<SymbolNode>();
		for(PartialClassNodeWrapper classPart : partialClassNodes){
			symbols.addAll(classPart.getMethodSymbols(decoratedNode));
		}
		
		return symbols;
	}

	public METHOD_VISIBILITY getMethodVisibility(MethodNodeWrapper methodNode) {
		if(methodNode.isClassMethod()) {
			return METHOD_VISIBILITY.PUBLIC;
		}
		VisibilityNodeWrapper methodVisibility = getMethodVisibilityNode(methodNode);
		if(methodVisibility != null) {
			return methodVisibility.getVisibility();
		}
		PartialClassNodeWrapper affectedClassPart = getPartContainingMethod(methodNode);
		return affectedClassPart.getPosVisibility(methodNode.getWrappedNode().getPosition().getStartOffset());
	}
	
	public VisibilityNodeWrapper getMethodVisibilityNode(MethodNodeWrapper methodNode) {
		PartialClassNodeWrapper affectedClassPart = getPartContainingMethod(methodNode);
		Collection<VisibilityNodeWrapper> visibilities = affectedClassPart.getMethodVisibilityNodes();
		for(VisibilityNodeWrapper aktNode : visibilities) {
			if(aktNode.containsMethod(methodNode)) {
				return aktNode;
			}
		}
		return null;
	}

	public Collection<VisibilityNodeWrapper> getMethodVisibilityNodes() {
		Collection<VisibilityNodeWrapper> visibilites = new ArrayList<VisibilityNodeWrapper>();
		for(PartialClassNodeWrapper classPart : partialClassNodes){
			visibilites.addAll(classPart.getMethodVisibilityNodes());
		}
		return visibilites;
	}


	private PartialClassNodeWrapper getPartContainingMethod(MethodNodeWrapper methodNode) {
		for(PartialClassNodeWrapper classPart : partialClassNodes){
			for(MethodNodeWrapper aktMethodNode :classPart.getMethods()) {
				if(aktMethodNode.equals(methodNode)) {
					return classPart;
				}
			}
		}
		return null;
	}

	public MethodNodeWrapper getMethod(String searchedMethodName) {
		Collection<MethodNodeWrapper> methodNodes = getMethods();
		MethodNodeWrapper lastMethod = null;
		for(MethodNodeWrapper aktMethod : methodNodes) {
			if(aktMethod.getName().equals(searchedMethodName)) {
				lastMethod = aktMethod;
			}
		}
		return lastMethod;
	}

	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((partialClassNodes == null) ? 0 : partialClassNodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClassNodeWrapper) {
			ClassNodeWrapper otherClassNode = (ClassNodeWrapper) obj;
			return getName().equals(otherClassNode.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return getName();
	}

	public boolean containsMethod(String searchedMethodName, boolean isClassMethod) {
		Collection<MethodNodeWrapper> methodNodes = getMethods();
		for(MethodNodeWrapper aktMethod : methodNodes) {
			if((!isClassMethod || aktMethod.isClassMethod()) && aktMethod.getName().equals(searchedMethodName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsMethod(String searchedMethodName) {
		return containsMethod(searchedMethodName, false);
	}

	public PartialClassNodeWrapper getPartialClassNodeForFileName(String fileName) {
		for(PartialClassNodeWrapper aktPart : partialClassNodes) {
			if(aktPart.getWrappedNode().getPosition().getFile().equals(fileName)) {
				return aktPart;
			}
		}
		return null;
	}

	public Node getWrappedNode() {
		return partialClassNodes.toArray(new PartialClassNodeWrapper[partialClassNodes.size()])[0].getWrappedNode();
	}

	public boolean containsField(String searchedFieldName) {
		for(FieldNodeWrapper aktFieldNode : getFields()) {
			if(aktFieldNode.getName().equals(searchedFieldName)) {
				return true;
			}
		}
		return false;
	}
}
