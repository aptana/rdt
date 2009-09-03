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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.SelfNode;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.EditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiFileEditProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;
import org.rubypeople.rdt.refactoring.util.NameHelper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class MethodMover implements IMultiFileEditProvider, Observer
{

	private MoveMethodConfig config;
	private Collection<String> visibilitiesToDelete;

	public MethodMover(MoveMethodConfig config)
	{
		this.config = config;
		visibilitiesToDelete = new ArrayList<String>();
		config.addObserver(this);
		initConfig();
	}

	private void initConfig()
	{
		boolean methodHasCallsToSourceClass = methodContainsReferencesToSourceClass();
		config.setNewMethodNeedsReferenceToSourceClass(methodHasCallsToSourceClass);
		if (methodHasCallsToSourceClass)
		{
			initMovedMethodArgs();
		}
		config.setSourceClassHasCallsToMovingMethod(sourceClassContainsCallsToMovingMethod());
		METHOD_VISIBILITY aktVisibility = config.getSourceClassNode().getMethodVisibility(config.getMethodNode());
		config.setMethodVisibility(aktVisibility);
		setMovedMethodVisibility();
		config.setLeaveDelegateMethodInSource(config.needsSecondPage());
	}

	private void setMovedMethodVisibility()
	{
		if (config.doesSourceClassHasCallsToMovingMethod() || config.leaveDelegateMethodInSource())
		{
			config.setMovedMethodVisibility(METHOD_VISIBILITY.PUBLIC);
		}
		else
		{
			config.setMovedMethodVisibility(config.getMethodVisibility());
		}
	}

	private void initMovedMethodArgs()
	{
		String className = config.getSourceClassNode().getName();
		String newArgName = className.substring(0, 1).toLowerCase(Locale.ENGLISH) + className.substring(1);
		while (NameHelper.namesContainName(config.getMethodNode().getLocalNames(), newArgName))
		{
			newArgName = NameHelper.createName(newArgName);
		}
		config.setFieldInDestinationClassOfTypeSourceClass(newArgName);
		ArgsNodeWrapper argsNode = config.getMethodNode().getArgsNode();
		config.setMovedMethodArgs(argsNode.cloneWithNewArgName(newArgName));
	}

	private boolean methodContainsReferencesToSourceClass()
	{
		for (MethodCallNodeWrapper aktCall : config.getMethodNode().getMethodCallNodes())
		{
			if (isCallToSourceClass(aktCall))
			{
				return true;
			}
		}
		for (FieldNodeWrapper aktFieldNode : NodeProvider.getFieldNodes(config.getMethodNode().getWrappedNode()))
		{
			if (aktFieldNode.isInstVar())
			{
				return true;
			}
		}
		return false;
	}

	private boolean isCallToSourceClass(MethodCallNodeWrapper callNode)
	{
		if (callNode.isCallToClassMethod())
		{
			return false;
		}
		boolean isReceiverSelf = callNode.isCallNode()
				&& NodeUtil.nodeAssignableFrom(callNode.getReceiverNode(), SelfNode.class);
		boolean isNotCallNode = !callNode.isCallNode();
		boolean hasExistingMethodName = config.getSourceClassNode().containsMethod(callNode.getName());
		return (isReceiverSelf || isNotCallNode) && hasExistingMethodName;
	}

	public Collection<FileMultiEditProvider> getFileEditProviders()
	{
		MultiFileEditProvider multiFileEditProvider = new MultiFileEditProvider();
		multiFileEditProvider.addEditProvider(getInsertMethodInTargetClassProvider());
		if (config.leaveDelegateMethodInSource())
		{
			multiFileEditProvider.addEditProvider(getDelegateMethodEditProvider());
		}
		else
		{
			multiFileEditProvider.addEditProvider(getDeleteSelectedMethodEditProvider());
			addDeleteVisibilityNodesOfMovingMethod(multiFileEditProvider);
			addUpdateReferencesInSourceClassEditProviders(multiFileEditProvider);
		}

		if (config.doesNewMethodNeedsReferenceToSourceClass())
		{
			addMethodVisibilityModifierEditProviders(multiFileEditProvider);
			addGenerateAccessorsEditProviders(multiFileEditProvider);
		}
		addDeleteMethodVisibilitiesEditProvider(multiFileEditProvider);

		return multiFileEditProvider.getFileEditProviders();
	}

	private void addDeleteMethodVisibilitiesEditProvider(MultiFileEditProvider multiFileEditProvider)
	{
		for (VisibilityNodeWrapper aktVisibilityNode : config.getSourceClassNode().getMethodVisibilityNodes())
		{
			String fileName = aktVisibilityNode.getPosition().getFile();
			RemovePartOfVisibilityNodeProvider editProvider = new RemovePartOfVisibilityNodeProvider(aktVisibilityNode,
					visibilitiesToDelete);
			if (editProvider.shouldRemoveAll())
			{
				multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, new DeleteEditProvider(
						aktVisibilityNode.getWrappedNode())));
			}
			else if (editProvider.hasChange())
			{
				multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, editProvider));
			}
		}
	}

	private void addDeleteVisibilityNodesOfMovingMethod(MultiFileEditProvider multiFileEditProvider)
	{
		VisibilityNodeWrapper visibilityNode = config.getSourceClassNode().getMethodVisibilityNode(
				config.getMethodNode());
		String fileName = config.getMethodNode().getPosition().getFile();
		if (visibilityNode == null)
		{
			return;
		}
		if (visibilityNode.getMethodNames().size() == 1)
		{
			multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, new DeleteEditProvider(visibilityNode
					.getWrappedNode())));
		}
		else
		{
			visibilitiesToDelete.add(config.getMethodNode().getName());
		}
	}

	private void addGenerateAccessorsEditProviders(MultiFileEditProvider multiFileEditProvider)
	{
		Collection<AttrAccessorNodeWrapper> accessorsToCreate = getMissingAccessors();
		String fileName = config.getDocumentProvider().getActiveFileName();
		for (AttrAccessorNodeWrapper aktAccessorNode : accessorsToCreate)
		{
			EditProvider editProvider = new InsertAccessorEditProvider(aktAccessorNode, config.getSourceClassNode());
			multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, editProvider));
		}
	}

	private Collection<AttrAccessorNodeWrapper> getMissingAccessors()
	{
		Collection<FieldNodeWrapper> fieldNodes = NodeProvider.getFieldNodes(config.getMethodNode().getWrappedNode());
		Collection<AttrAccessorNodeWrapper> existingAccessors = config.getSourceClassNode().getAccessorNodes();
		Map<String, AttrAccessorNodeWrapper> accessorsToCreate = new LinkedHashMap<String, AttrAccessorNodeWrapper>();
		String destClassField = config.getFieldInSourceClassOfTypeDestinationClass();
		for (FieldNodeWrapper aktFieldNode : fieldNodes)
		{
			if (!aktFieldNode.getName().equals(destClassField))
			{
				addAccessorForField(existingAccessors, accessorsToCreate, aktFieldNode);
			}
		}
		return accessorsToCreate.values();
	}

	private void addAccessorForField(Collection<AttrAccessorNodeWrapper> existingAccessors,
			Map<String, AttrAccessorNodeWrapper> accessorsToCreate, FieldNodeWrapper fieldNode)
	{
		AttrAccessorNodeWrapper accessorToInsert = getAccessor(fieldNode);
		if (fieldNode.isInstVar() && !existsAccessor(accessorToInsert, existingAccessors))
		{
			if (accessorsToCreate.containsKey(fieldNode.getName()))
			{
				AttrAccessorNodeWrapper accessor = accessorsToCreate.get(fieldNode.getName());
				accessor.addAccessorType(getAccessor(fieldNode));
			}
			else
			{
				accessorsToCreate.put(fieldNode.getName(), getAccessor(fieldNode));
			}
		}
	}

	private boolean existsAccessor(AttrAccessorNodeWrapper accessorToInsert,
			Collection<AttrAccessorNodeWrapper> existingAccessors)
	{
		for (AttrAccessorNodeWrapper accessorNode : existingAccessors)
		{
			if (accessorNode.containsAccessor(accessorToInsert))
			{
				return true;
			}
		}
		return false;
	}

	private AttrAccessorNodeWrapper getAccessor(final FieldNodeWrapper aktFieldNode)
	{
		String accessorName;
		if (aktFieldNode.isAsgnNode())
		{
			accessorName = AttrAccessorNodeWrapper.ATTR_WRITER;
		}
		else
		{
			accessorName = AttrAccessorNodeWrapper.ATTR_READER;
		}
		FCallNode fCallNode = NodeFactory.createFCallNode(accessorName, new ArrayList<Node>());
		return new AttrAccessorNodeWrapper(fCallNode, NodeFactory.createSymboleNode(aktFieldNode.getName()));
	}

	private void addUpdateReferencesInSourceClassEditProviders(MultiFileEditProvider multiFileEditProvider)
	{
		Collection<MethodCallNodeWrapper> methodCallsToMovingMethod = getMethodCallsToMovingMethodFromSourceClass();
		for (MethodCallNodeWrapper aktCall : methodCallsToMovingMethod)
		{
			addReplaceMethodCallEditProvider(aktCall, multiFileEditProvider);
		}
	}

	private void addReplaceMethodCallEditProvider(MethodCallNodeWrapper methodCall,
			MultiFileEditProvider multiFileEditProvider)
	{
		String fileName = methodCall.getPosition().getFile();
		EditProvider replaceEdit = new ReplaceMethodCallEditProvider(methodCall, config);
		multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, replaceEdit));
	}

	private void addMethodVisibilityModifierEditProviders(MultiFileEditProvider multiFileEditProvider)
	{
		Collection<MethodNodeWrapper> referencedMethod = getSourceClassMethodsReferencedInMovingMethod();
		for (MethodNodeWrapper aktMethod : referencedMethod)
		{
			if (!config.getSourceClassNode().getMethodVisibility(aktMethod).equals(METHOD_VISIBILITY.PUBLIC))
			{
				addMethodVisibilityModifierEditProvider(aktMethod, multiFileEditProvider);
			}
		}
	}

	private void addMethodVisibilityModifierEditProvider(MethodNodeWrapper methodNode,
			MultiFileEditProvider multiFileEditProvider)
	{
		VisibilityNodeWrapper visibilityNode = config.getSourceClassNode().getMethodVisibilityNode(methodNode);
		String fileName = methodNode.getWrappedNode().getPosition().getFile();
		if (visibilityNode == null)
		{
			EditProvider insertEdit = new InsertVisibilityEditProvider(methodNode, METHOD_VISIBILITY.PUBLIC);
			multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, insertEdit));
		}
		else
		{
			if (visibilityNode.getVisibility().equals(METHOD_VISIBILITY.PUBLIC))
			{
				return;
			}
			if (visibilityNode.getMethodNames().size() == 1)
			{
				EditProvider editProvider = new ReplaceVisibilityEditProvider(visibilityNode, METHOD_VISIBILITY.PUBLIC);
				multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, editProvider));
			}
			else
			{
				EditProvider insertEdit = new InsertVisibilityEditProvider(methodNode, METHOD_VISIBILITY.PUBLIC);
				multiFileEditProvider.addEditProvider(new FileEditProvider(fileName, insertEdit));
				visibilitiesToDelete.add(methodNode.getName());
			}
		}
	}

	private Collection<MethodNodeWrapper> getSourceClassMethodsReferencedInMovingMethod()
	{
		Collection<MethodCallNodeWrapper> calledMethods = config.getMethodNode().getMethodCallNodes();
		Collection<MethodNodeWrapper> methods = new ArrayList<MethodNodeWrapper>();
		for (MethodCallNodeWrapper aktCall : calledMethods)
		{
			if (!aktCall.isCallToClassMethod() && !aktCall.isCallNode()
					&& config.getSourceClassNode().containsMethod(aktCall.getName()))
			{
				methods.add(config.getSourceClassNode().getMethod(aktCall.getName()));
			}
		}
		return methods;
	}

	private FileEditProvider getDeleteSelectedMethodEditProvider()
	{
		EditProvider deleteEditProvider = new DeleteEditProvider(config.getMethodNode().getWrappedNode());
		return new FileEditProvider(config.getDocumentProvider().getActiveFileName(), deleteEditProvider);
	}

	private FileEditProvider getDelegateMethodEditProvider()
	{
		EditProvider editProvider = new DelegateMethodEditProvider(config);
		return new FileEditProvider(config.getDocumentProvider().getActiveFileName(), editProvider);
	}

	private FileEditProvider getInsertMethodInTargetClassProvider()
	{
		PartialClassNodeWrapper insertClassPart = config.getDestinationClassNode().getPartialClassNodeForFileName(
				config.getMethodNode().getPosition().getFile());
		if (insertClassPart == null)
		{
			insertClassPart = config.getDestinationClassNode().getFirstPartialClassNode();
		}
		InsertEditProvider insertEdit = new InsertMethodEditProvider(config, insertClassPart);
		return new FileEditProvider(insertClassPart.getFile(), insertEdit);
	}

	private Collection<MethodCallNodeWrapper> getMethodCallsToMovingMethodFromSourceClass()
	{
		Collection<MethodCallNodeWrapper> allMethodCalls = config.getSourceClassNode().getMethodCallNodes();
		Collection<MethodCallNodeWrapper> methodCallsToMovingMethod = new ArrayList<MethodCallNodeWrapper>();
		for (MethodCallNodeWrapper aktCall : allMethodCalls)
		{
			if (isCallToMovingMethod(aktCall))
			{
				methodCallsToMovingMethod.add(aktCall);
			}
		}
		return methodCallsToMovingMethod;
	}

	private boolean sourceClassContainsCallsToMovingMethod()
	{
		Collection<MethodCallNodeWrapper> allMethodCalls = config.getSourceClassNode().getMethodCallNodes();
		for (MethodCallNodeWrapper aktCall : allMethodCalls)
		{
			if (isCallToMovingMethod(aktCall) && !aktCall.isCallToClassMethod())
			{
				return true;
			}
		}
		return false;
	}

	private boolean isCallToMovingMethod(MethodCallNodeWrapper methodCall)
	{
		String selectedMethodName = config.getMethodNode().getName();
		boolean sameName = methodCall.getName().equals(selectedMethodName);
		boolean notInMovingMethod = !SelectionNodeProvider.isNodeContainedInNode(methodCall.getWrappedNode(), config
				.getMethodNode().getWrappedNode());
		boolean isNotCallNode = !methodCall.isCallNode();
		boolean isSelfNode = methodCall.isCallNode()
				&& NodeUtil.nodeAssignableFrom(methodCall.getReceiverNode(), SelfNode.class);
		boolean sameType = config.getMethodNode().isClassMethod() == methodCall.isCallToClassMethod();
		return sameName && sameType && notInMovingMethod
				&& (isNotCallNode || isSelfNode || methodCall.isCallToClassMethod());
	}

	public void update(Observable arg0, Object arg1)
	{
		setMovedMethodVisibility();
		initWarnings();
	}

	private void initWarnings()
	{
		config.resetWarnings();
		if (config.doesNewMethodNeedsReferenceToSourceClass())
		{
			for (AttrAccessorNodeWrapper aktAccessorNode : getMissingAccessors())
			{
				config.addWarning(Messages.MethodMover_An + aktAccessorNode.getAccessorTypeName()
						+ Messages.MethodMover_ForField + aktAccessorNode.getAttrName()
						+ Messages.MethodMover_WillBeGenerated);
			}
		}
		if (config.doesNewMethodNeedsReferenceToSourceClass())
		{
			for (MethodNodeWrapper aktMethod : getSourceClassMethodsReferencedInMovingMethod())
			{
				if (!config.getSourceClassNode().getMethodVisibility(aktMethod).equals(METHOD_VISIBILITY.PUBLIC))
				{
					config.addWarning(Messages.MethodMover_TheVisibilityOfMethod + aktMethod.getName()
							+ Messages.MethodMover_WillBeChangedToPublic);
				}
			}
		}
		METHOD_VISIBILITY newVisibility = config.getMethodVisibility();
		METHOD_VISIBILITY oldVisibility = config.getMethodVisibility();
		if (!newVisibility.equals(oldVisibility))
		{
			String oldVisibilityName = VisibilityNodeWrapper.getVisibilityName(oldVisibility);
			String newVisibilityName = VisibilityNodeWrapper.getVisibilityName(newVisibility);
			config.addWarning(Messages.MethodMover_TheVisibilityOfTheMovingMethod + config.getMethodNode().getName()
					+ Messages.MethodMover_IsChangedFrom + oldVisibilityName + Messages.MethodMover_To
					+ newVisibilityName + '.');
		}
		if (!config.getMethodNode().getName().equals(config.getMovedMethodName()))
		{
			config.addWarning(Messages.MethodMover_NameWillBeChangedTo + config.getMovedMethodName()
					+ Messages.MethodMover_DuToNameConflicts);
		}
	}
}
