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

package org.rubypeople.rdt.refactoring.core.inlineclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.jruby.ast.AssignableNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.core.SelectionNodeProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.editprovider.DeleteEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.InsertEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.MultiFileEditProvider;
import org.rubypeople.rdt.refactoring.editprovider.ReplaceEditProvider;
import org.rubypeople.rdt.refactoring.exception.NoClassNodeException;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.PartialClassNodeWrapper;
import org.rubypeople.rdt.refactoring.offsetprovider.AfterLastMethodInClassOffsetProvider;
import org.rubypeople.rdt.refactoring.util.Constants;


public class ClassInliner implements IMultiFileEditProvider {

	private final class ClassInsertEditProvider extends InsertEditProvider {
		private final StringDocumentProvider provider;

		private ClassInsertEditProvider(boolean format, StringDocumentProvider provider) {
			super(format);
			this.provider = provider;
		}

		@Override
		protected Node getInsertNode(int offset, String document) {
			try {
				Node rootNode = provider.getActiveFileRootNode();
				return SelectionNodeProvider.getSelectedClassNode(rootNode, 1).getFirstPartialClassNode().getClassBodyNode();
			} catch (NoClassNodeException e) {
		
				return provider.getActiveFileRootNode();
			}
		}

		@Override
		protected int getOffset(String document) {
			AfterLastMethodInClassOffsetProvider offsetProvider = new AfterLastMethodInClassOffsetProvider(config.getTargetClassPart(), document );
			return offsetProvider.getOffset();
		}
	}


	private static final class ConstructorlessSelfAssginmentReplacer extends ReplaceEditProvider {
		private final AssignableNode assignment;

		private ConstructorlessSelfAssginmentReplacer(AssignableNode assignment) {
			this.assignment = assignment;
		}

		@Override
		protected int getOffsetLength() {
			return assignment.getPosition().getEndOffset() - assignment.getPosition().getStartOffset();
		}

		@Override
		protected Node getEditNode(int offset, String document) {
			Node selfAsgnNode = NodeFactory.createInstAsgnNode(((INameNode)assignment).getName(), NodeFactory.createSelfNode());
			return selfAsgnNode;
		}

		@Override
		protected int getOffset(String document) {
			return assignment.getPosition().getStartOffset();
		}
	}


	private final class ConstructorAndSelfAsgnReplacer extends ReplaceEditProvider {
		private final AssignableNode assignment;

		private ConstructorAndSelfAsgnReplacer(boolean format, boolean trim, AssignableNode assignment) {
			super(format, trim);
			this.assignment = assignment;
		}

		@Override
		protected int getOffsetLength() {
			ISourcePosition pos = assignment.getPosition();						
			return pos.getEndOffset() - pos.getStartOffset();
		}

		@Override
		protected Node getEditNode(int offset, String document) {
			MethodCallNodeWrapper valueWrapper = new MethodCallNodeWrapper(assignment.getValueNode());
			Node constrReplaceNode = NodeFactory.createMethodCallNode(createNewConstructorName(),  valueWrapper.getArgsNode());
			Node selfAsgnNode = NodeFactory.createInstAsgnNode(((INameNode)assignment).getName(), NodeFactory.createSelfNode());
			return NodeFactory.createBlockNode(false, false, true, constrReplaceNode, selfAsgnNode);
		}

		@Override
		protected int getOffset(String document) {
			return assignment.getPosition().getStartOffset();
		}
	}


	private static final class CallReplaceEditProvider extends ReplaceEditProvider {
		private final MethodCallNodeWrapper wrapper;

		private final String name;

		private CallReplaceEditProvider(MethodCallNodeWrapper wrapper, String name) {
			this.wrapper = wrapper;
			this.name = name;
		}

		@Override
		protected int getOffsetLength() {
			ISourcePosition pos = wrapper.getPosition();
			return pos.getEndOffset() - pos.getStartOffset();
		}

		@Override
		protected Node getEditNode(int offset, String document) {
			
			return NodeFactory.createCallNode(wrapper.getReceiverNode(), name, wrapper.getArgsNode());
		}

		@Override
		protected int getOffset(String document) {
			return wrapper.getPosition().getStartOffset();
		}
	}


	private InlineClassConfig config;
	private PartialClassNodeWrapper inlinedClassPart;
	
	public ClassInliner(InlineClassConfig config) {
		this.config = config;
		inlinedClassPart = getInlinedClassPart();
	}
	

	public Collection<FileMultiEditProvider> getFileEditProviders() {
		
		MultiFileEditProvider editProvider = new MultiFileEditProvider();

		addClassDeleteProvider(editProvider);
		addConstructorInsertProvider(editProvider);
		Collection<AssignableNode> concerningAssignments = addContructorCallReplacer(editProvider);

		InsertClassBuilder inlinedClassProvider = new InsertClassBuilder(config);
		
		addCallReplaceProvider(editProvider, inlinedClassProvider, concerningAssignments);
		
		addClassInsertProvider(editProvider, inlinedClassProvider.getInlinedClass(inlinedClassPart));
		return editProvider.getFileEditProviders();
	}

	private void addCallReplaceProvider(MultiFileEditProvider editProvider, InsertClassBuilder inlinedClassProvider, Collection<AssignableNode> concerningAssignments) {

		Map<String, MethodNodeWrapper> concerningMethods = inlinedClassProvider.getMethodsWithNameConflict(inlinedClassPart);
		for(String currentKey : concerningMethods.keySet()){
			MethodNodeWrapper currentMethod = concerningMethods.get(currentKey);
			for(AssignableNode currentAssignment : concerningAssignments){
				String concerningVarName = ((INameNode)currentAssignment).getName();
				if(currentAssignment instanceof InstAsgnNode){
					Collection<MethodNodeWrapper> targetClassMethods = config.getTargetClass().getMethods();
					for(MethodNodeWrapper currentTargetMethod : targetClassMethods){
						addCallReplacerForMethod(editProvider, currentKey, currentMethod, currentTargetMethod, concerningVarName);
					}
				}
				else if(currentAssignment instanceof LocalAsgnNode){
					MethodNodeWrapper targetConstructor = config.getTargetClass().getConstructorNode();					
					addCallReplacerForMethod(editProvider, currentKey, currentMethod, targetConstructor, concerningVarName);
				}
			}
		}
	}


	private void addCallReplacerForMethod(MultiFileEditProvider editProvider, String newMethodName, MethodNodeWrapper renamedMethod, MethodNodeWrapper targetMethod, String concerningVarName) {
		Collection<Node> calls = NodeProvider.getSubNodes(targetMethod.getWrappedNode(), CallNode.class);
		for(Node currentCall : calls){
			MethodCallNodeWrapper callWrapper = new MethodCallNodeWrapper(currentCall);
			if(concerningVarName.equals(callWrapper.getReceiverName()) && callWrapper.getName().equals(renamedMethod.getName())){
				editProvider.addEditProvider(new FileEditProvider(callWrapper.getFileName(), createCallReplaceEditProvider(callWrapper, newMethodName)));
			}
		}
	}


	private ReplaceEditProvider createCallReplaceEditProvider(final MethodCallNodeWrapper callWrapper, final String newName) {
		return new CallReplaceEditProvider(callWrapper, newName);
	}


	private Collection<AssignableNode> addContructorCallReplacer(MultiFileEditProvider editProvider) {
		MethodNodeWrapper constructorNode = config.getTargetClass().getConstructorNode();
		ArrayList<AssignableNode> concerningAssignables = new ArrayList<AssignableNode>();

		for(AssignableNode currentAssignment : config.findFieldAsgnsOfSource(constructorNode)){
			String file = currentAssignment.getPosition().getFile();
			concerningAssignables.add(currentAssignment);
			if(inlinedClassPart.getExistingConstructors().isEmpty()){
				editProvider.addEditProvider(new FileEditProvider(file, createConstructorlessSelfAsignment(currentAssignment)));
			} else {
				editProvider.addEditProvider(new FileEditProvider(file, createConstructorMethodAndSelfAsgnReplacer(currentAssignment)));
			}
		}
		return concerningAssignables;
	}


	private ReplaceEditProvider createConstructorMethodAndSelfAsgnReplacer(final AssignableNode assignment) {
		return new ConstructorAndSelfAsgnReplacer(true, true, assignment);
	}


	private ReplaceEditProvider createConstructorlessSelfAsignment(final AssignableNode currentAssignment) {
		return new ConstructorlessSelfAssginmentReplacer(currentAssignment);
	}


	private void addConstructorInsertProvider(MultiFileEditProvider editProvider) {
		Collection<MethodNodeWrapper> constructors = inlinedClassPart.getExistingConstructors();
		PartialClassNodeWrapper targetClassPart = config.getTargetClassPart();
		for(MethodNodeWrapper currentConstructor : constructors){
			ConstructorInliner constructorInliner = new ConstructorInliner(currentConstructor, targetClassPart, createNewConstructorName());

			editProvider.addEditProvider(new FileEditProvider(targetClassPart.getFile(),constructorInliner));
		}
	}


	private String createNewConstructorName() {
		String className = inlinedClassPart.getClassName().toLowerCase(Locale.ENGLISH);
		return className + "_" + Constants.CONSTRUCTOR_NAME; //$NON-NLS-1$
	}


	private void addClassInsertProvider(MultiFileEditProvider editProvider, final StringDocumentProvider inlinedClassDocumentProvider) {
		
		InsertEditProvider classEditProvider = new ClassInsertEditProvider(true, inlinedClassDocumentProvider);
		
		String file = config.getTargetClassPart().getWrappedNode().getPosition().getFile();
		editProvider.addEditProvider(new FileEditProvider(file, classEditProvider));
	}


	private void addClassDeleteProvider(MultiFileEditProvider editProvider) {

		DeleteEditProvider classPartDeleter = new DeleteEditProvider(inlinedClassPart.getWrappedNode());
		editProvider.addEditProvider(new FileEditProvider(config.getDocumentProvider().getActiveFileName(), classPartDeleter));
	}


	public PartialClassNodeWrapper getInlinedClassPart(){
		IDocumentProvider docProvider = config.getDocumentProvider();
		Node rootNode = docProvider.getActiveFileRootNode();
		try {
			return SelectionNodeProvider.getSelectedClassNode(rootNode, config.getCaretPosition()).getFirstPartialClassNode();

		} catch (NoClassNodeException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
