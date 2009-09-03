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
 * Copyright (C) 2007 Thomas Corbat <tcorbat@hsr.ch>
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

package org.rubypeople.rdt.refactoring.core.inlinemethod;

import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.AssignableNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.jruby.lexer.yacc.IDESourcePosition;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.refactoring.core.renamelocal.LocalVariableRenamer;
import org.rubypeople.rdt.refactoring.documentprovider.DocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodCallNodeWrapper;
import org.rubypeople.rdt.refactoring.util.FileHelper;
import org.rubypeople.rdt.refactoring.util.NodeUtil;

public class ParameterReplacer implements IParameterReplacer {

	public DocumentProvider replace(IDocumentProvider doc, MethodCallNodeWrapper call, MethodDefNode definition) {
		
		DocumentProvider strDoc = new StringDocumentProvider("part_of_" + doc.getActiveFileName(), doc.getActiveFileContent().substring(definition.getPosition().getStartOffset(), definition.getPosition().getEndOffset() + 1)); //$NON-NLS-1$
		
		ArrayNode headList = new ArrayNode(new IDESourcePosition());
		ArrayNode tailList = new ArrayNode(new IDESourcePosition());
		
		
		if(definition.getArgsNode().getArgs() != null && call.getArgsNode() != null) {
			Object[] defnArguments = definition.getArgsNode().getArgs().childNodes().toArray();
			Object[] arguments = call.getArgsNode().childNodes().toArray();
			
			for(int i = 0; i < defnArguments.length; i++) {
				strDoc = processArguments(strDoc, headList, tailList, (Node) arguments[i], (ArgumentNode) defnArguments[i]);
			}
			
			if(definition.getArgsNode().getRestArg() >= 0) {
				processRestArg(definition, headList, tailList, defnArguments, arguments);
			}
		}

		if(definition.getArgsNode().getOptArgs() != null) {
			processOptArgs(definition, headList, tailList);
		}
			
		StringBuffer insert = new StringBuffer();
		
		if(headList.size() > 0) {
			String lineDelimiter = FileHelper.getLineDelimiter(strDoc.getActiveFileContent());
			createAssignments(headList, tailList, insert, lineDelimiter);
		}
		
		MethodDefNode newDefinition = (MethodDefNode) ((NewlineNode) strDoc.getActiveFileRootNode().getBodyNode()).getNextNode();
		ISourcePosition bodyPosition = NodeUtil.subPositionUnion(newDefinition.getBodyNode());
		insert.append(strDoc.getActiveFileContent().substring(
				bodyPosition.getStartOffset(), 
				bodyPosition.getEndOffset() + 1).trim());
		
		return new StringDocumentProvider("subpart_of_" + doc.getActiveFileName(), insert.toString()); //$NON-NLS-1$
	}

	private void createAssignments(ArrayNode headList, ArrayNode tailList, StringBuffer insert, String lineDelimiter) {
		MultipleAsgnNode multipleAsgnNode = new MultipleAsgnNode(new IDESourcePosition(), headList, null);
		multipleAsgnNode.setValueNode(tailList);
		insert.append(ReWriteVisitor.createCodeFromNode(multipleAsgnNode, "")); //$NON-NLS-1$
		insert.append(lineDelimiter);
	}

	private DocumentProvider processArguments(DocumentProvider StringDocumentProvider, ArrayNode headList, ArrayNode tailList, Node node, ArgumentNode arg) {
		if (node instanceof LocalVarNode || node instanceof InstAsgnNode) {
			StringDocumentProvider = renameVariable(StringDocumentProvider, (INameNode) node, (INameNode) arg);
		} else {
			headList.add(createAssignment(arg.getName()));
			tailList.add(node);
		}
		return StringDocumentProvider;
	}

	private void processOptArgs(MethodDefNode definition, ArrayNode headList, ArrayNode tailList) {
		for(Object obj : definition.getArgsNode().getOptArgs().childNodes()) {
			assert obj instanceof AssignableNode;
			headList.add((AssignableNode) obj);
			tailList.add(((AssignableNode) obj).getValueNode());
			((AssignableNode) obj).setValueNode(null);
		}
	}

	private void processRestArg(MethodDefNode definition, ArrayNode headList, ArrayNode tailList, Object[] defnArguments, Object[] arguments) {
		String restArgName = definition.getScope().getVariables()[definition.getArgsNode().getRestArg()];
		ArrayNode restParams = new ArrayNode(new IDESourcePosition());
		for(int i = defnArguments.length; i < arguments.length; i++) {
			restParams.add((Node) arguments[i]);
		}
		headList.add(createAssignment(restArgName));
		tailList.add(restParams);
	}

	private LocalAsgnNode createAssignment(String name) {
		return new LocalAsgnNode(new IDESourcePosition(), name, -1, null);
	}
	
	private DocumentProvider renameVariable(DocumentProvider StringDocumentProvider, INameNode varNode, INameNode argumentNode) {
		return new LocalVariableRenamer(StringDocumentProvider, argumentNode.getName(), varNode.getName()).rename();
	}
}
