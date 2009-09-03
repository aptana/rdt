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

package org.rubypeople.rdt.refactoring.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jruby.ast.Colon2Node;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.ConstNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.refactoring.core.NodeProvider;
import org.rubypeople.rdt.refactoring.nodewrapper.ClassNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.FieldNodeWrapper;
import org.rubypeople.rdt.refactoring.nodewrapper.MethodNodeWrapper;

public class NameHelper {

	public static String createName(String string) {
		Matcher matcher = Pattern.compile("([@]{0,2}\\w*[a-zA-Z_])(\\d+)").matcher(string); //$NON-NLS-1$
		if (matcher.matches()) {
			return matcher.group(1) + String.valueOf(Integer.valueOf(matcher.group(2)).intValue() + 1);
		}
		return string + 1;
	}

	public static ArrayList<String> findDuplicates(String[] myNames, String[] oldNames) {
		ArrayList<String> found = new ArrayList<String>();
		for (int i = 0; i < oldNames.length; i++) {
			if(namesContainName(myNames, oldNames[i])) {
				found.add(oldNames[i]);
			}
		}
		return found;
	}

	public static boolean namesContainName(String[] names, String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static boolean fieldnameExistsInClass(String name, ClassNodeWrapper klass) {
		for (FieldNodeWrapper currentTargetField : klass.getFields()) {
			if (name.equals(currentTargetField.getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean methodnameExistsInClassPart(String methodName, ClassNodeWrapper klass) {

		return methodsContainMethod(klass.getMethods(), methodName);
	}

	private static boolean methodsContainMethod(Collection<MethodNodeWrapper> methods, String methodName) {
		for (MethodNodeWrapper currentTargetMethod : methods) {
			if (methodName.equals(currentTargetMethod.getName())) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean fieldsContainField(Collection<FieldNodeWrapper> fields, String fieldName) {
		for (FieldNodeWrapper currentField : fields) {
			if (fieldName.equals(currentField.getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean methodnameExistsInClass(String methodName, ClassNodeWrapper classNode) {

		return methodsContainMethod(classNode.getMethods(), methodName);
	}

	public static String createMethodName(MethodNodeWrapper methodWrapper, ClassNodeWrapper klass) {
		String newName = methodWrapper.getName();
		while(methodsContainMethod(klass.getMethods(), newName)){
			newName = createName(newName);
		}		
		return newName;
	}
	
	public static String createFieldName(FieldNodeWrapper fieldWrapper, ClassNodeWrapper klass) {
		String newName = fieldWrapper.getName();
		while(fieldsContainField(klass.getFields(), newName)){
			newName = createName(newName);
		}
		return newName;
	}
	
	public static String getEncosingModulePrefix(Node rootNode, Node node) {
		Vector<String> nameParts = new Vector<String>();
		
		while(true) {
			Node parent = NodeProvider.findParentNode(rootNode, node, ModuleNode.class);
			if(parent == null) {
				break;
			}
			Colon3Node path = ((ModuleNode) parent).getCPath();
			if(path != node) {
				nameParts.insertElementAt(getFullyQualifiedName(path), 0);
			}
			node = parent;
		}
		
		StringBuilder prefix = new StringBuilder();
		Iterator<String> it = nameParts.iterator();
		while (it.hasNext()) {
			String name = it.next();

			prefix.append(name);	
			
			if(it.hasNext()) {
				prefix.append("::");
			}
		}
		
		return prefix.toString();
	}

	public static String getFullyQualifiedName(Node n) {
		assert n instanceof ConstNode || n instanceof Colon2Node;
		
		if (n instanceof ConstNode) {
			ConstNode constNode = (ConstNode) n;
			return constNode.getName();
		}
		StringBuilder name = new StringBuilder();
	
		ArrayList<Node> subNodes = new ArrayList<Node>(NodeProvider.getSubNodes(((Colon2Node) n).getLeftNode(), Colon2Node.class, ConstNode.class));
		Collections.reverse(subNodes);
		
		for (Node node : subNodes) {
			name.append(((INameNode) node).getName());
			name.append("::"); //$NON-NLS-1$
		}
		name.append(((INameNode) n).getName());
		return name.toString();
	}
}

