/**
 * Copyright (c) 2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl -v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.rubypeople.rdt.refactoring.classnodeprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jruby.ast.ClassNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.SClassNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;

public class ClassNodeProvider {

	private Map<String, ClassNodeWrapper> classNodeWrappers;

	protected IDocumentProvider documentProvider;

	public ClassNodeProvider(IDocumentProvider docProvider) {
		this(docProvider, true);
	}

	public ClassNodeProvider(IDocumentProvider docProvider, boolean addActiveFile) {
		classNodeWrappers = new LinkedHashMap<String, ClassNodeWrapper>();
		this.documentProvider = docProvider;
		if(addActiveFile) {
			addSource(docProvider.getActiveFileName());
		}
	}

	public void addSource(String sourceName) {
		Node rootNode = documentProvider.getRootNode(sourceName);
		createClassNodes(rootNode);
	}

	private void createClassNodes(Node rootNode) {
		if (rootNode == null) {
			return;
		}
		Collection<Node> classNodes = NodeProvider.getSubNodes(rootNode, ClassNode.class);
		Collection<Node> moduleNodes = NodeProvider.getSubNodes(rootNode, ModuleNode.class);
		classNodes.addAll(NodeProvider.getSubNodes(rootNode, SClassNode.class));
		for (Node node : classNodes) {
			try {
				PartialClassNodeWrapper partialClassNode = PartialClassNodeWrapper.getPartialClassNodeWrapper(node, rootNode);
				addEnclosingModules(partialClassNode, moduleNodes);
				addPartialClassNode(partialClassNode, classNodeWrappers);
			} catch (NoClassNodeException e) {
				e.printStackTrace();
			}
		}
	}

	private void addEnclosingModules(PartialClassNodeWrapper partialClassNode, Collection<Node> moduleNodes) {
		ISourcePosition nodePosition = partialClassNode.getWrappedNode().getPosition();
		ArrayList<ModuleNode> enclosingModules = new ArrayList<ModuleNode>();
		for (Node currentModule : moduleNodes) {
			ISourcePosition modulePosition = currentModule.getPosition();
			if (modulePosition.getStartOffset() < nodePosition.getStartOffset() && modulePosition.getEndOffset() > nodePosition.getEndOffset()) {

				enclosingModules.add((ModuleNode) currentModule);
			}
		}

		partialClassNode.setEnclosingModules(enclosingModules);
	}

	private void addPartialClassNode(PartialClassNodeWrapper partialClassNode, Map<String, ClassNodeWrapper> classes) {
		String className = partialClassNode.getClassName();
		if (classes.containsKey(className)) {
			ClassNodeWrapper classNode = classes.get(className);
			classNode.addPartialClassNode(partialClassNode);
		} else {
			ClassNodeWrapper classNode = new ClassNodeWrapper(partialClassNode);
			classes.put(className, classNode);
		}
	}

	public void addClassNodeProvider(ClassNodeProvider provider) {
		if (provider != null) {
			for (ClassNodeWrapper classNode : provider.getAllClassNodes()) {
				if (!hasClassNode(classNode.getName())) {
					classNodeWrappers.put(classNode.getName(), classNode);
				}
			}
		}
	}

	public Collection<ClassNodeWrapper> getAllClassNodes() {
		return classNodeWrappers.values();
	}

	public ClassNodeWrapper getClassNode(String className) {
		return classNodeWrappers.containsKey(className) ? classNodeWrappers.get(className) : null;
	}

	public boolean hasClassNode(String className) {
		return classNodeWrappers.containsKey(className);
	}

	public Collection<ClassNodeWrapper> getSubClassesOf(String className) {
		Collection<ClassNodeWrapper> childs = new ArrayList<ClassNodeWrapper>();
		for (ClassNodeWrapper classNode : getAllClassNodes()) {
			if (classNode.getSuperClassName() != null && classNode.getSuperClassName().equals(className))
				childs.add(classNode);
		}
		return childs;
	}

	public Collection<MethodNodeWrapper> getAllMethodsFor(String className) {
		Collection<MethodNodeWrapper> methodNodes = new ArrayList<MethodNodeWrapper>();

		for (ClassNodeWrapper classNode : getClassAndAllSuperClassesFor(className)) {
			methodNodes.addAll(classNode.getMethods());
		}
		return methodNodes;
	}

	public Collection<ClassNodeWrapper> getClassAndAllSuperClassesFor(String className) {
		return getClassAndAllSuperClasses(getClassNode(className));
	}

	public Collection<ClassNodeWrapper> getClassAndAllSuperClasses(ClassNodeWrapper classNode) {
		ArrayList<ClassNodeWrapper> classNodes = new ArrayList<ClassNodeWrapper>();

		do {
			if (classNode != null) {
				classNodes.add(classNode);
			}
		} while (classNode != null && (classNode = getClassNode(classNode.getSuperClassName())) != null);
		return classNodes;
	}

	public Collection<ClassNodeWrapper> getClassAndAllSubClasses(ClassNodeWrapper classNode) {
		Collection<ClassNodeWrapper> classes = new ArrayList<ClassNodeWrapper>();
		if (classNode == null) {
			return classes;
		}
		classes.add(classNode);
		for (ClassNodeWrapper aktClassNode : getAllClassNodes()) {
			if (classNode.getName().equals(aktClassNode.getSuperClassName())) {
				classes.addAll(getClassAndAllSubClasses(aktClassNode));
			}
		}
		return classes;
	}

	public ClassNodeWrapper getSuperClassOf(String className) {
		for (ClassNodeWrapper classNode : getAllClassNodes()) {
			if (classNode.getName() != null && classNode.getName().equals(className))
				return classNode;
		}
		return null;
	}
}
