package org.rubypeople.rdt.refactoring.nodewrapper;

import java.util.ArrayList;
import java.util.Collection;

import org.jruby.ast.ConstNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.refactoring.core.NodeProvider;

public class ModuleNodeWrapper implements INodeWrapper {
	private final ModuleNode moduleNode;
	private ModuleNodeWrapper parentModule;
	private ArrayList<ConstNode> moduleMethodNodes = new ArrayList<ConstNode>();;

	public ModuleNodeWrapper(ModuleNode moduleNode, ModuleNodeWrapper parentModule) {
		this.moduleNode = moduleNode;
		this.parentModule = parentModule;	
		initModuleMethodConstNodes();
	}

	public ModuleNode getWrappedNode() {
		return moduleNode;
	}

	public ModuleNodeWrapper getParentModule() {
		return parentModule;
	}

	public void setParentModule(ModuleNodeWrapper parentModule) {
		this.parentModule = parentModule;
	}

	public String getName() {
		return moduleNode.getCPath().getName();
	}

	public String getFullName() {
		return (parentModule != null ? parentModule.getFullName() + "::" : "") + getName();
	}
	
	public Collection<ConstNode> getModuleMethodConstNodes() {

		return moduleMethodNodes;
	}

	private void initModuleMethodConstNodes() {
		for (Node node : NodeProvider.getSubNodes(getWrappedNode(), DefsNode.class)) {
			DefsNode defsNode = (DefsNode) node;
			if(defsNode.getReceiverNode() instanceof ConstNode) {
				ConstNode constNode = (ConstNode) defsNode.getReceiverNode();
				if(constNode.getName().equals(getName())) {
					moduleMethodNodes.add(constNode);
				}
			}
		}
	}
}
