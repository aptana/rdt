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
import java.util.Observable;

import org.rubypeople.rdt.refactoring.classnodeprovider.ClassNodeProvider;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ArgsNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.VisibilityNodeWrapper.METHOD_VISIBILITY;
import org.rubypeople.rdt.refactoring.util.NameHelper;

public class MoveMethodConfig extends Observable implements IRefactoringConfig {

	private IDocumentProvider docProvider;
	private int caretPosition;
	private MethodNodeWrapper methodNode;
	private ClassNodeWrapper sourceClassNode;
	private ClassNodeProvider allClassesNodeProvider;
	private ClassNodeWrapper destinationClassNode;
	private String fieldInSourceClassOfTypeDestinationClass;
	private String fieldInDestinationClassOfTypeSourceClass;
	private METHOD_VISIBILITY movedMethodVisibility;
	private METHOD_VISIBILITY methodVisibility;
	private Collection<String> targetClassNames;
	private Collection<String> fieldInSourceClassOfTypeDestinationClassNames;
	private boolean leaveDelegateMethodInSoruce;
	private boolean newMethodHasReferenceToSourceClass;
	private boolean sourceClassHasCallsToMovingMethod;
	private ArgsNodeWrapper movedMethodArgs;
	private String movedMethodName;
	private Collection<String> warnings;
	
	public MoveMethodConfig(IDocumentProvider docProvider, int caretPosition) {
		this.docProvider = docProvider;
		this.caretPosition = caretPosition;
		warnings = new ArrayList<String>();
	}

	public boolean doesNewMethodNeedsReferenceToSourceClass() {
		return newMethodHasReferenceToSourceClass;
	}

	public void setNewMethodNeedsReferenceToSourceClass(boolean newMethodNeedsReferenceToSourceClass) {
		this.newMethodHasReferenceToSourceClass = newMethodNeedsReferenceToSourceClass;
	}

	public String getFieldInSourceClassOfTypeDestinationClass() {
		return fieldInSourceClassOfTypeDestinationClass;
	}

	public void setFieldInSourceClassOfTypeDestinationClass(String fieldOfDestinationClassType) {
		this.fieldInSourceClassOfTypeDestinationClass = fieldOfDestinationClassType;
		this.setChanged();
		this.notifyObservers();
	}

	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}
	
	public boolean isClassMethod() {
		return methodNode.isClassMethod();
	
	}

	public ClassNodeProvider getAllClassesNodeProvider() {
		return allClassesNodeProvider;
	}

	public void setAllClassesNodeProvider(ClassNodeProvider allClassesNodeProvider) {
		this.allClassesNodeProvider = allClassesNodeProvider;
	}

	public ClassNodeWrapper getDestinationClassNode() {
		return destinationClassNode;
	}

	public void setDestinationClassNode(String aktClassName) {
		destinationClassNode = allClassesNodeProvider.getClassNode(aktClassName);
		movedMethodName = initMovedMethodName();
		this.setChanged();
		this.notifyObservers();
	}
	
	private String initMovedMethodName() {
		String name = getMethodNode().getName();
		while(NameHelper.methodnameExistsInClass(name, destinationClassNode)) {
			name = NameHelper.createName(name);
		}
		return name;
	}

	public MethodNodeWrapper getMethodNode() {
		return methodNode;
	}

	public void setMethodNode(MethodNodeWrapper methodNode) {
		this.methodNode = methodNode;
		if(methodNode != null) {
			movedMethodArgs = methodNode.getArgsNode();
			movedMethodName = methodNode.getName();
		}
	}

	public ClassNodeWrapper getSourceClassNode() {
		return sourceClassNode;
	}

	public void setSourceClassNode(String sourceClassName) {
		sourceClassNode = allClassesNodeProvider.getClassNode(sourceClassName);
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public String getFieldInDestinationClassOfTypeSourceClass() {
		return fieldInDestinationClassOfTypeSourceClass;
	}

	public void setFieldInDestinationClassOfTypeSourceClass(String fieldInDestinationClassOfTypeSourceClass) {
		this.fieldInDestinationClassOfTypeSourceClass = fieldInDestinationClassOfTypeSourceClass;
	}

	public boolean leaveDelegateMethodInSource() {
		return leaveDelegateMethodInSoruce;
	}

	public void setLeaveDelegateMethodInSource(boolean leaveDelegateMethodInSoruce) {
		this.leaveDelegateMethodInSoruce = leaveDelegateMethodInSoruce;
		setChanged();
		notifyObservers();
	}

	public METHOD_VISIBILITY getMovedMethodVisibility() {
		return movedMethodVisibility;
	}

	public void setMovedMethodVisibility(METHOD_VISIBILITY neededMethodVisibility) {
		this.movedMethodVisibility = neededMethodVisibility;
	}

	public METHOD_VISIBILITY getMethodVisibility() {
		return methodVisibility;
	}

	public void setMethodVisibility(METHOD_VISIBILITY methodVisibility) {
		this.methodVisibility = methodVisibility;
	}

	public Collection<String> getTargetClassNames() {
		return targetClassNames;
	}

	public void setTargetClassNames(Collection<String> targetClassNames) {
		this.targetClassNames = targetClassNames;
	}

	public Collection<String> getFieldInSourceClassOfTypeDestinationClassNames() {
		return fieldInSourceClassOfTypeDestinationClassNames;
	}

	public void setFieldInSourceClassOfTypeDestinationClassNames(Collection<String> fieldInSourceClassOfTypeDestinationClassNames) {
		this.fieldInSourceClassOfTypeDestinationClassNames = fieldInSourceClassOfTypeDestinationClassNames;
	}

	public boolean doesSourceClassHasCallsToMovingMethod() {
		return sourceClassHasCallsToMovingMethod;
	}
	
	public void setSourceClassHasCallsToMovingMethod(boolean value) {
		sourceClassHasCallsToMovingMethod = value;
	}

	public ArgsNodeWrapper getMovedMethodArgs() {
		return movedMethodArgs;
	}

	public String getMovedMethodName() {
		return movedMethodName;
	}
	
	public void setMovedMethodName(String name) {
		movedMethodName = name;
	}

	public void setMovedMethodArgs(ArgsNodeWrapper movedMethodArgs) {
		this.movedMethodArgs = movedMethodArgs;
	}

	public boolean needsSecondPage() {
		boolean isPrivate = sourceClassNode.getMethodVisibility(methodNode).equals(METHOD_VISIBILITY.PRIVATE);
		boolean isClassMethod = methodNode.isClassMethod();
		return !isClassMethod && (!isPrivate || sourceClassHasCallsToMovingMethod);
	}

	public boolean canCreateDelegateMethod() {
		return needsSecondPage() || methodNode.isClassMethod();
	}

	public Collection<String> getWarnings() {
		return warnings;
	}
	
	public void addWarning(String warning) {
		warnings.add(warning);
	}

	public void resetWarnings() {
		warnings.clear();
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.docProvider = doc;
	}
}
