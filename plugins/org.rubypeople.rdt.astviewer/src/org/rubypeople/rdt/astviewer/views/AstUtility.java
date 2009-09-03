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

package org.rubypeople.rdt.astviewer.views;

import java.util.ArrayList;
import java.util.Iterator;

import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;

public class AstUtility {

	public static ArrayList<Node> findAllNodes(Node rootNode) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		if(rootNode == null)
			return nodes;
		nodes.add(rootNode);
		for(Object o : rootNode.childNodes()) {
			nodes.addAll(findAllNodes((Node) o));
		}
		return nodes;
	}

	public static String nodeList(ArrayList<Node> nodes) {
		StringBuilder str = new StringBuilder();
		for (Node node : nodes) {
			String name = node.getClass().getName();
			str.append(name.substring(name.lastIndexOf(".") + 1, name.length()));
			str.append(", ");
		} 
		str.deleteCharAt(str.length() - 1);
		str.deleteCharAt(str.length() - 1);
		return str.toString();
	}
	
	public static String formatedPosition(Node n) {
		if(n == null || n.getPosition() == null)
			return "";
		
		StringBuilder posString = new StringBuilder();
		posString.append("Lines [");
		posString.append(n.getPosition().getStartLine());
		posString.append(":");
		posString.append(n.getPosition().getEndLine());
		posString.append("], Offset [");
		posString.append(n.getPosition().getStartOffset());
		posString.append(":");
		posString.append(n.getPosition().getEndOffset());
		posString.append("]");
		return posString.toString();
	}

	public static String nodeListJRubyFormat(ArrayList<Node> nodes) {
		StringBuilder builder = new StringBuilder();
		builder.append("list = [\n");
		Iterator nodeIter = nodes.iterator();
		
		while(nodeIter.hasNext()) {
			
			Node node = (Node) nodeIter.next();
			
			if(node instanceof NewlineNode || node instanceof RootNode) {
				builder.append("nil,\n");
				continue;
			}
			
			builder.append("['");
			builder.append(node.getClass().getSimpleName());
			builder.append("', ");
			builder.append(node.getPosition().getStartLine());
			builder.append(", ");
			builder.append(node.getPosition().getEndLine());
			builder.append(", ");
			builder.append(node.getPosition().getStartOffset());
			builder.append(", ");
			builder.append(node.getPosition().getEndOffset());
			builder.append("]");
		
			if(nodeIter.hasNext()) {
				builder.append(",\n");
			}
		}

		builder.append("\n]");
		return builder.toString();
	}

}
