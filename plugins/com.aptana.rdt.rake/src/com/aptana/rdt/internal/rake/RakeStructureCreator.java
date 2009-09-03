package com.aptana.rdt.internal.rake;

import java.util.ArrayList;
import java.util.List;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.IArgumentNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.StrNode;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

public class RakeStructureCreator extends InOrderVisitor {

	private static final String TASK = "task";
	private static final String NAMESPACE = "namespace";
	private List<Namespace> namespaces = new ArrayList<Namespace>();

	public Object visitFCallNode(FCallNode visited) {
		if (visited.getName().equals(TASK)) { // start of a task
			String name = getFirstArgument(visited);
			Task task = new Task(name, getStart(visited), getLength(visited));
			Namespace curNamespace = namespaces.get(namespaces.size() - 1);
			curNamespace.addChild(task);
		} else if (visited.getName().equals(NAMESPACE)) {
			String namespaceName = getFirstArgument(visited);
			Namespace namespace = new Namespace(namespaceName,
					getStart(visited), getLength(visited));
			Namespace curNamespace = namespaces.get(namespaces.size() - 1);
			curNamespace.addChild(namespace);
			namespaces.add(namespace);
			Object ins = super.visitFCallNode(visited);
			namespaces.remove(namespaces.size() - 1);
			return ins;
		}
		return super.visitFCallNode(visited);
	}

	public Object visitCallNode(CallNode visited) {
		if (visited.getName().equals("new")) {
			String receiver = ASTUtil.stringRepresentation(visited
					.getReceiverNode());
			if (receiver.equals("TestTask") || receiver.equals("RDocTask")) {
				String name = getFirstArgument(visited);
				Task task = new Task(name, getStart(visited),
						getLength(visited));
				Namespace curNamespace = namespaces.get(namespaces.size() - 1);
				curNamespace.addChild(task);
			}
		}
		return super.visitCallNode(visited);
	}

	private String getFirstArgument(IArgumentNode visited) {
		List<String> args = ASTUtil.getArgumentsFromFunctionCall(visited);

		Node arguments = visited.getArgsNode();
		String name = args.get(0);
		if (arguments instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) arguments;
			Node firstArg = array.get(0);
			if (firstArg instanceof HashNode) {
				HashNode hash = (HashNode) firstArg;
				Node firstHashMember = hash.getListNode().get(0);
				name = ASTUtil.getNameReflectively(firstHashMember);
			}
			if (firstArg instanceof StrNode) {
				name = ((StrNode)firstArg).getValue().toString();
			} else {
				String newName = ASTUtil.getNameReflectively(firstArg);
				if (newName != null) {
					name = newName;
				}
			}
		}
		return name;
	}

	public Object visitRootNode(RootNode visited) {
		namespaces.add(new Namespace("ROOT", getStart(visited),
				getLength(visited)));
		return super.visitRootNode(visited);
	}

	private int getLength(Node visited) {
		return getEnd(visited) - getStart(visited) + 1;
	}

	private int getEnd(Node visited) {
		return visited.getPosition().getEndOffset();
	}

	private int getStart(Node visited) {
		return visited.getPosition().getStartOffset();
	}

	public Object[] getTasks() {
		return namespaces.get(namespaces.size() - 1).getChildren();
	}
}
