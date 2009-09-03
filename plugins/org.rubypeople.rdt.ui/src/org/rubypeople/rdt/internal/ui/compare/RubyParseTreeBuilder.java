package org.rubypeople.rdt.internal.ui.compare;

import java.util.List;
import java.util.Stack;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.DStrNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.RootNode;
import org.jruby.ast.SClassNode;
import org.jruby.ast.StrNode;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

class RubyParseTreeBuilder extends InOrderVisitor {

	private char[] fBuffer;
	private Stack<RubyNode> fStack = new Stack<RubyNode>();
	private boolean fShowCU;
	private RubyNode fImportContainer;

	public RubyParseTreeBuilder(RubyNode root, char[] buffer, boolean showCU) {
		fBuffer = buffer;
		fShowCU = showCU;
		fStack.clear();
		fStack.push(root);
	}
	
	 /**
     * Closes the current Ruby node by setting its end position and pops it off
     * the stack.
     */
    private void pop() {
        fStack.pop();
    }

    private RubyNode getCurrentContainer() {
        return fStack.peek();
    }
    
    /**
     * Adds a new RubyNode with the given type and name to the current
     * container.
     */
    private void push(int type, String name, int declarationStart, int length) {

        while (declarationStart > 0) {
            char c= fBuffer[declarationStart - 1];
            if (c != ' ' && c != '\t')
                break;
            declarationStart--;
            length++;
        }

        RubyNode node= new RubyNode(getCurrentContainer(), type, name, declarationStart, length);
        if (type == RubyNode.SCRIPT)
            node.setAppendPosition(declarationStart + length + 1);
        else
            node.setAppendPosition(declarationStart + length);

        fStack.push(node);
    }
    
    @Override
    public Object visitClassNode(ClassNode iVisited) {
    	int start = iVisited.getPosition().getStartOffset();
    	int end = iVisited.getPosition().getEndOffset();
    	push(RubyNode.CLASS, ASTUtil.getFullyQualifiedName(iVisited.getCPath()), start, end - start);
    	Object ins = super.visitClassNode(iVisited);
    	pop();
    	return ins;
    }
    
    @Override
    public Object visitSClassNode(SClassNode iVisited) {
    	int start = iVisited.getPosition().getStartOffset();
    	int end = iVisited.getPosition().getEndOffset();
    	push(RubyNode.CLASS, ASTUtil.getNameReflectively(iVisited.getReceiverNode()), start, end - start);
    	Object ins = super.visitSClassNode(iVisited);
    	pop();
    	return ins;
    }
    
    @Override
    public Object visitModuleNode(ModuleNode iVisited) {
    	int start = iVisited.getPosition().getStartOffset();
    	int end = iVisited.getPosition().getEndOffset();
    	push(RubyNode.MODULE, ASTUtil.getFullyQualifiedName(iVisited.getCPath()), start, end - start);
    	Object ins = super.visitModuleNode(iVisited);
    	pop();
    	return ins;
    }
    
    @Override
    public Object visitRootNode(RootNode iVisited) {
    	if (fShowCU)
    		push(RubyNode.SCRIPT, null, iVisited.getPosition().getStartOffset(), iVisited.getPosition().getEndOffset());
    	Object ins = super.visitRootNode(iVisited);
    	if (fShowCU)
    		pop();
    	return ins;
    }
    
    @Override
    public Object visitDefnNode(DefnNode iVisited) {
    	int start = iVisited.getPosition().getStartOffset();
    	int end = iVisited.getPosition().getEndOffset();
    	push(RubyNode.METHOD, iVisited.getName(), start, end - start);
    	Object ins = super.visitDefnNode(iVisited);
    	pop();
    	return ins;
    }
    
    @Override
    public Object visitDefsNode(DefsNode iVisited) {
    	int start = iVisited.getPosition().getStartOffset();
    	int end = iVisited.getPosition().getEndOffset();
    	push(RubyNode.METHOD, iVisited.getName(), start, end - start);
    	Object ins = super.visitDefsNode(iVisited);
    	pop();
    	return ins;
    }
    
    public Object visitFCallNode(FCallNode iVisited) {
		String name = iVisited.getName();
		List<String> arguments = getArgumentsFromFunctionCall(iVisited);
		if (name.equals("require") || name.equals("load")) {
			addImport(iVisited);
		}
		return super.visitFCallNode(iVisited);
	}
    
    private void addImport(FCallNode iVisited) {
		ArrayNode node = (ArrayNode) iVisited.getArgsNode();
		String arg = getString(node);
		if (arg != null) {
			 int s= node.getPosition().getStartOffset();
		     int declarationEnd= node.getPosition().getEndOffset();
		     int l = declarationEnd - s;
		     if (fImportContainer == null)
		         fImportContainer= new RubyNode(getCurrentContainer(), RubyNode.IMPORT_CONTAINER, null, s, l);
		     new RubyNode(fImportContainer, RubyNode.IMPORT, arg, s, l);
		     fImportContainer.setLength(declarationEnd - fImportContainer.getRange().getOffset() + 1);
		     fImportContainer.setAppendPosition(declarationEnd + 2); // FIXME
		}
	}
    
	/**
	 * @param node
	 * @return
	 */
	private String getString(ArrayNode node) {
		Object tmp = node.childNodes().iterator().next();
		if (tmp instanceof DStrNode) {
			DStrNode dstrNode = (DStrNode) tmp;
			tmp = dstrNode.childNodes().iterator().next();
		}
		if (tmp instanceof StrNode) {
			StrNode strNode = (StrNode) tmp;
			return strNode.getValue().toString();
		}
		return null;
	}
	
	@Override
	public Object visitInstAsgnNode(InstAsgnNode iVisited) {
		int start = iVisited.getPosition().getStartOffset();
		int end = iVisited.getPosition().getEndOffset();
		push(RubyNode.FIELD, iVisited.getName(), start, end - start);
		Object ins = super.visitInstAsgnNode(iVisited);
		pop();
		return ins;
	}
	
	@Override
	public Object visitClassVarDeclNode(ClassVarDeclNode iVisited) {
		int start = iVisited.getPosition().getStartOffset();
		int end = iVisited.getPosition().getEndOffset();
		push(RubyNode.FIELD, iVisited.getName(), start, end - start);
		Object ins = super.visitClassVarDeclNode(iVisited);
		pop();
		return ins;
	}
	
	@Override
	public Object visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
		int start = iVisited.getPosition().getStartOffset();
		int end = iVisited.getPosition().getEndOffset();
		push(RubyNode.FIELD, iVisited.getName(), start, end - start);
		Object ins = super.visitClassVarAsgnNode(iVisited);
		pop();
		return ins;
	}
	
	@Override
	public Object visitConstDeclNode(ConstDeclNode iVisited) {
		int start = iVisited.getPosition().getStartOffset();
		int end = iVisited.getPosition().getEndOffset();
		push(RubyNode.FIELD, iVisited.getName(), start, end - start);
		Object ins = super.visitConstDeclNode(iVisited);
		pop();
		return ins;
	}
   
}
