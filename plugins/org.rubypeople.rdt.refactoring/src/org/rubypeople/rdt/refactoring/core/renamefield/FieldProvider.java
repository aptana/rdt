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

package org.rubypeople.rdt.refactoring.core.renamefield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.Node;
import org.jruby.ast.SymbolNode;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.AttrFieldItem;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.ClassVarAsgnFieldItem;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.ClassVarFieldItem;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.FieldItem;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.InstAsgnFieldItem;
import org.rubypeople.rdt.refactoring.core.renamefield.fielditems.InstVarFieldItem;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.AttrAccessorNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;

public class FieldProvider {

	private ClassNodeWrapper classNode;
	
	private Collection<ClassNodeWrapper> relatedClasses;

	private LinkedHashMap<String, ArrayList<FieldItem>> fields;

	private IDocumentProvider docProvider;

	public FieldProvider(ClassNodeWrapper classNode, IDocumentProvider docProvider) {
		this.classNode = classNode;
		this.fields = new LinkedHashMap<String, ArrayList<FieldItem>>();
		this.docProvider = docProvider;
		
		initRelatedClasses();
		
		for(ClassNodeWrapper currentClass : relatedClasses){
			initAttrs(currentClass);
			initAccessors(currentClass);
			initClassFields(currentClass);
		}
	}

	private void initRelatedClasses() {
		relatedClasses = new ArrayList<ClassNodeWrapper>();
		Collection<ClassNodeWrapper> itselfAndSuperclasses = docProvider.getProjectClassNodeProvider().getClassAndAllSuperClasses(classNode);
		relatedClasses.addAll(itselfAndSuperclasses);

		Collection<ClassNodeWrapper> subclasses = docProvider.getProjectClassNodeProvider().getSubClassesOf(classNode.getName());
		relatedClasses.addAll(subclasses);
		
	}

	private void initClassFields(ClassNodeWrapper classWrapper) {
		
		Collection<Node> allOccurences = classWrapper
				.getClassFieldOccurences();

		for (Node currentAttr : allOccurences) {
			if (currentAttr instanceof ClassVarNode) {
				ClassVarNode classVar = (ClassVarNode) currentAttr;
				if (!isVarSubNodeOfAsgn(classVar, allOccurences, ClassVarAsgnNode.class)) {
					addClassVar(classVar);
				}
			} else if (currentAttr instanceof ClassVarAsgnNode) {
				addClassVarAsgn((ClassVarAsgnNode) currentAttr);
			} else {
				System.out.println(Messages.FieldProvider_UnexpectedNodeOfType
						+ currentAttr.getClass()
						+ Messages.FieldProvider_RetrievedAsField
						+ currentAttr.toString());
			}
		}
	}

	private void addClassVarAsgn(ClassVarAsgnNode classVarAsgnNode) {
		String name = FieldItem.fieldName(classVarAsgnNode.getName());
		
		initNameList(name);
		ArrayList<FieldItem> fieldList = fields.get(name);
		fieldList.add(new ClassVarAsgnFieldItem(classVarAsgnNode));
	}

	private void addClassVar(ClassVarNode classVarNode) {
		String name = FieldItem.fieldName(classVarNode.getName());

		initNameList(name);
		ArrayList<FieldItem> fieldList = fields.get(name);
		fieldList.add(new ClassVarFieldItem(classVarNode));
	}

	private void initAccessors(ClassNodeWrapper classWrapper) {

		for (AttrAccessorNodeWrapper currentAccessor : classWrapper
				.getAccessorNodes()) {

			String name = FieldItem.fieldName(currentAccessor.getAttrName());
			initNameList(name);
			ArrayList<FieldItem> itemList = fields.get(name);

			for (FCallNode accessorPart : currentAccessor.getAccessorNodes()) {
				ArrayNode arrayNode = (ArrayNode) accessorPart.getArgsNode();
				for(Object actObj : arrayNode.childNodes()) {
					SymbolNode aktSymbol = (SymbolNode) actObj;
					if(name.equals(aktSymbol.getName())) {
						itemList.add(new AttrFieldItem(aktSymbol));
					}
				}
			}
		}
	}

	private void initAttrs(ClassNodeWrapper classWrapper) {

		Collection<Node> allOccurences = classWrapper.getInstFieldOccurences();

		for (Node currentAttr : allOccurences) {
			if (currentAttr instanceof SymbolNode) {
				addAttr((SymbolNode) currentAttr);
			} else if (currentAttr instanceof InstVarNode) {
				InstVarNode instVar = (InstVarNode) currentAttr;
				if (!isVarSubNodeOfAsgn(instVar, allOccurences, InstAsgnNode.class)) {
					addInstVar(instVar);
				}

			} else if (currentAttr instanceof InstAsgnNode) {
				addInstAsgn((InstAsgnNode) currentAttr);
			} else {
				System.out.println(Messages.FieldProvider_UnexpectedNodeOfType
						+ currentAttr.getClass()
						+ Messages.FieldProvider_RetrievedAsAttribute
						+ currentAttr.toString());
			}
		}
	}

	private boolean isVarSubNodeOfAsgn(Node instVar,
			Collection<Node> allNodes, Class kind) {
		for (Node currentNode : allNodes) {
			if (currentNode.getClass().isAssignableFrom(kind)) {
				if (currentNode.getPosition().getFile().equals(instVar.getPosition().getFile())) {
					if (SelectionNodeProvider.nodeContainsPosition(currentNode,
							instVar.getPosition().getStartOffset())) {
						return true;
					}
				}
			}
		}
		return false;
	}


	private void addInstAsgn(InstAsgnNode currentAttr) {

		String name = FieldItem.fieldName(currentAttr.getName());

		initNameList(name);
		ArrayList<FieldItem> fieldList = fields.get(name);
		fieldList.add(new InstAsgnFieldItem(currentAttr));
	}

	private void addInstVar(InstVarNode instVar) {
		String name = FieldItem.fieldName(instVar.getName());

		initNameList(name);
		ArrayList<FieldItem> varList = fields.get(name);
		varList.add(new InstVarFieldItem(instVar));
	}

	private void initNameList(String name) {
		if (!fields.containsKey(name)) {
			fields.put(name, new ArrayList<FieldItem>());
		}
	}

	private void addAttr(SymbolNode symbol) {
		String name = FieldItem.fieldName(symbol.getName());

		initNameList(name);
		ArrayList<FieldItem> attrList = fields.get(name);
		attrList.add(new AttrFieldItem(symbol));
	}

	public ArrayList<FieldItem> getFieldItems(String fieldName, boolean concernsClassField) {
		ArrayList<FieldItem> matchingItems = new ArrayList<FieldItem>();

		for(FieldItem currentItem : fields.get(fieldName)){
			if(currentItem.concernsClassField() == concernsClassField){
				matchingItems.add(currentItem);
			}
		}
		
		return matchingItems;
	}

	public Collection<String> getFieldNames() {
		HashSet<String> names = new HashSet<String>(fields.keySet());
		return names;
	}

	public FieldItem getNameAtPosition(int caretPosition, String fileName) {

		for (String currentName : fields.keySet()) {
			for (FieldItem currentItem : fields.get(currentName)) {
				
				if (currentItem.getFieldNode().getPosition().getFile().equals(fileName) && 
						SelectionNodeProvider.nodeContainsPosition(currentItem.getFieldNode(), caretPosition)) {
					return currentItem;
				}
			}
		}
		return null;
	}
}
