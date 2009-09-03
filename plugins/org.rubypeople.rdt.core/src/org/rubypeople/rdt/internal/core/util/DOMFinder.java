package org.rubypeople.rdt.internal.core.util;

import java.util.Iterator;

import org.jruby.ast.AliasNode;
import org.jruby.ast.AndNode;
import org.jruby.ast.ArgsCatNode;
import org.jruby.ast.ArgsNode;
import org.jruby.ast.ArgsPushNode;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.AttrAssignNode;
import org.jruby.ast.BackRefNode;
import org.jruby.ast.BeginNode;
import org.jruby.ast.BignumNode;
import org.jruby.ast.BlockArgNode;
import org.jruby.ast.BlockNode;
import org.jruby.ast.BlockPassNode;
import org.jruby.ast.BreakNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.CaseNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.Colon3Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DRegexpNode;
import org.jruby.ast.DStrNode;
import org.jruby.ast.DSymbolNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.DXStrNode;
import org.jruby.ast.DefinedNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.DotNode;
import org.jruby.ast.EnsureNode;
import org.jruby.ast.EvStrNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.FalseNode;
import org.jruby.ast.FixnumNode;
import org.jruby.ast.FlipNode;
import org.jruby.ast.FloatNode;
import org.jruby.ast.ForNode;
import org.jruby.ast.GlobalAsgnNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.HashNode;
import org.jruby.ast.IfNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.IterNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.Match2Node;
import org.jruby.ast.Match3Node;
import org.jruby.ast.MatchNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.MultipleAsgn19Node;
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.NextNode;
import org.jruby.ast.NilNode;
import org.jruby.ast.Node;
import org.jruby.ast.NotNode;
import org.jruby.ast.NthRefNode;
import org.jruby.ast.OpAsgnAndNode;
import org.jruby.ast.OpAsgnNode;
import org.jruby.ast.OpAsgnOrNode;
import org.jruby.ast.OpElementAsgnNode;
import org.jruby.ast.OrNode;
import org.jruby.ast.PostExeNode;
import org.jruby.ast.PreExeNode;
import org.jruby.ast.RedoNode;
import org.jruby.ast.RegexpNode;
import org.jruby.ast.RescueBodyNode;
import org.jruby.ast.RescueNode;
import org.jruby.ast.RestArgNode;
import org.jruby.ast.RetryNode;
import org.jruby.ast.ReturnNode;
import org.jruby.ast.RootNode;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SValueNode;
import org.jruby.ast.SelfNode;
import org.jruby.ast.SplatNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.SuperNode;
import org.jruby.ast.SymbolNode;
import org.jruby.ast.ToAryNode;
import org.jruby.ast.TrueNode;
import org.jruby.ast.UndefNode;
import org.jruby.ast.UntilNode;
import org.jruby.ast.VAliasNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.WhenNode;
import org.jruby.ast.WhileNode;
import org.jruby.ast.XStrNode;
import org.jruby.ast.YieldNode;
import org.jruby.ast.ZArrayNode;
import org.jruby.ast.ZSuperNode;
import org.jruby.ast.visitor.NodeVisitor;
import org.jruby.lexer.yacc.ISourcePosition;
import org.rubypeople.rdt.core.IMember;
import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.SourceRefElement;

public class DOMFinder implements NodeVisitor {
	// XXX Refactor out all sorts of common code between this and RubyScriptStructureBuilder
	// XXX Think about abstracting to higher level AST (and visitor) that just deals with class/method/module/etc declarations like the JDT
	public Node foundNode = null;
	
	private Node ast;
	private SourceRefElement element;
	private int rangeStart = -1, rangeLength = 0;
	
	public DOMFinder(Node ast, SourceRefElement element) {
		this.ast = ast;
		this.element = element;
	}

	public Object visitAliasNode(AliasNode arg0) {
		return null;
	}

	public Object visitAndNode(AndNode iVisited) {
		visitNode(iVisited.getFirstNode());
		visitNode(iVisited.getSecondNode());
		return null;
	}

	public Object visitArgsCatNode(ArgsCatNode iVisited) {
		visitNode(iVisited.getFirstNode());
		visitNode(iVisited.getSecondNode());
		return null;
	}

	public Object visitArgsNode(ArgsNode iVisited) {
		visitNode(iVisited.getBlock());
		if (iVisited.getOptArgs() != null) {
			visitIter(iVisited.getOptArgs().childNodes().iterator());
		}
		return null;
	}
	
	private Object visitIter(Iterator<Node> iterator) {
		while (iterator.hasNext()) {
			visitNode(iterator.next());
		}
		return null;
	}

	public Object visitArrayNode(ArrayNode iVisited) {
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	public Object visitBackRefNode(BackRefNode arg0) {
		return null;
	}

	public Object visitBeginNode(BeginNode iVisited) {
		visitNode(iVisited.getBodyNode());
		return null;
	}

	public Object visitBignumNode(BignumNode arg0) {
		return null;
	}

	public Object visitBlockArgNode(BlockArgNode arg0) {
		return null;
	}

	public Object visitBlockNode(BlockNode iVisited) {
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	public Object visitBlockPassNode(BlockPassNode iVisited) {
		visitNode(iVisited.getArgsNode());
		visitNode(iVisited.getBodyNode());
		return null;
	}

	public Object visitBreakNode(BreakNode iVisited) {
		visitNode(iVisited.getValueNode());
		return null;
	}

	public Object visitCallNode(CallNode iVisited) {
		visitNode(iVisited.getReceiverNode());
		visitNode(iVisited.getArgsNode());
		visitNode(iVisited.getIterNode());
		return null;
	}

	public Object visitCaseNode(CaseNode iVisited) {
		visitNode(iVisited.getCaseNode());
		visitNode(iVisited.getCases());
		return null;
	}

	public Object visitClassNode(ClassNode node) {
		String name = getFullyQualifiedName(node.getCPath());
		ISourcePosition pos = node.getPosition();
		int nameStart = pos.getStartOffset() + "class".length() + 1;
		if (!found(node, nameStart, name.length())) {
			visitNode(node.getSuperNode());
			visitNode(node.getBodyNode());
		}
		return null;
	}
	
	private Object visitNode(Node iVisited) {
		if (iVisited != null)
			iVisited.accept(this);
		return null;
	}
	
	private boolean found(Node node, int start, int length) {
		if (start == this.rangeStart && length == this.rangeLength) {
			this.foundNode = node;
		}
		return false;
	}

	private String getFullyQualifiedName(Node node) {
		if (node == null)
			return "";
		if (node instanceof ConstNode) {
			ConstNode constNode = (ConstNode) node;
			return constNode.getName();
		}
		if (node instanceof Colon2Node) {
			Colon2Node colonNode = (Colon2Node) node;
			String prefix = getFullyQualifiedName(colonNode.getLeftNode());
			if (prefix.length() > 0)
				prefix = prefix + "::";
			return prefix + colonNode.getName();
		}
		return "";
	}
	
	public Object visitClassVarAsgnNode(ClassVarAsgnNode arg0) {
		
		return null;
	}

	public Object visitClassVarDeclNode(ClassVarDeclNode arg0) {
		
		return null;
	}

	public Object visitClassVarNode(ClassVarNode arg0) {
		
		return null;
	}

	public Object visitColon2Node(Colon2Node arg0) {
		
		return null;
	}

	public Object visitColon3Node(Colon3Node arg0) {
		
		return null;
	}

	public Object visitConstDeclNode(ConstDeclNode arg0) {
		
		return null;
	}

	public Object visitConstNode(ConstNode arg0) {
		
		return null;
	}

	public Object visitDAsgnNode(DAsgnNode arg0) {
		
		return null;
	}

	public Object visitDRegxNode(DRegexpNode arg0) {
		
		return null;
	}

	public Object visitDStrNode(DStrNode arg0) {
		
		return null;
	}

	public Object visitDSymbolNode(DSymbolNode arg0) {
		
		return null;
	}

	public Object visitDVarNode(DVarNode arg0) {
		
		return null;
	}

	public Object visitDXStrNode(DXStrNode arg0) {
		
		return null;
	}

	public Object visitDefinedNode(DefinedNode arg0) {
		
		return null;
	}

	public Object visitDefnNode(DefnNode iVisited) {
		String name = iVisited.getName();
		ISourcePosition pos = iVisited.getPosition();
		int nameStart = pos.getStartOffset() + "def".length() + 1; 
		if (!found(iVisited, nameStart, name.length()) ) {
			visitNode(iVisited.getArgsNode());
			visitNode(iVisited.getBodyNode());
		}
		return null;
	}

	public Object visitDefsNode(DefsNode iVisited) {
		String name;
		String receiver = ASTUtil.stringRepresentation(iVisited.getReceiverNode());
		if (receiver != null && receiver.trim().length() > 0) {
			name = receiver + "." + iVisited.getName();
		} else {
			name = iVisited.getName();
		}
		ISourcePosition pos = iVisited.getPosition();
		int nameStart = pos.getStartOffset() + "def".length() + 1; 
		if (!found(iVisited, nameStart, name.length())) {
		  visitNode(iVisited.getReceiverNode());
		  visitNode(iVisited.getArgsNode());
		  visitNode(iVisited.getBodyNode());
		}		
		return null;
	}

	public Object visitDotNode(DotNode arg0) {
		
		return null;
	}

	public Object visitEnsureNode(EnsureNode arg0) {
		
		return null;
	}

	public Object visitEvStrNode(EvStrNode arg0) {
		
		return null;
	}

	public Object visitFCallNode(FCallNode arg0) {
		
		return null;
	}

	public Object visitFalseNode(FalseNode arg0) {
		
		return null;
	}

	public Object visitFixnumNode(FixnumNode arg0) {
		
		return null;
	}

	public Object visitFlipNode(FlipNode arg0) {
		
		return null;
	}

	public Object visitFloatNode(FloatNode arg0) {
		
		return null;
	}

	public Object visitForNode(ForNode arg0) {
		
		return null;
	}

	public Object visitGlobalAsgnNode(GlobalAsgnNode arg0) {
		
		return null;
	}

	public Object visitGlobalVarNode(GlobalVarNode arg0) {
		
		return null;
	}

	public Object visitHashNode(HashNode arg0) {
		
		return null;
	}

	public Object visitIfNode(IfNode arg0) {
		
		return null;
	}

	public Object visitInstAsgnNode(InstAsgnNode arg0) {
		
		return null;
	}

	public Object visitInstVarNode(InstVarNode arg0) {
		
		return null;
	}

	public Object visitIterNode(IterNode arg0) {
		
		return null;
	}

	public Object visitLocalAsgnNode(LocalAsgnNode arg0) {
		
		return null;
	}

	public Object visitLocalVarNode(LocalVarNode arg0) {
		
		return null;
	}

	public Object visitMatch2Node(Match2Node arg0) {
		
		return null;
	}

	public Object visitMatch3Node(Match3Node arg0) {
		
		return null;
	}

	public Object visitMatchNode(MatchNode arg0) {
		
		return null;
	}

	public Object visitModuleNode(ModuleNode arg0) {
		
		return null;
	}

	public Object visitMultipleAsgnNode(MultipleAsgnNode arg0) {
		
		return null;
	}

	public Object visitNewlineNode(NewlineNode iVisited) {
		visitNode(iVisited.getNextNode());
		return null;
	}

	public Object visitNextNode(NextNode iVisited) {
		visitNode(iVisited.getValueNode());
		return null;
	}

	public Object visitNilNode(NilNode arg0) {
		
		return null;
	}

	public Object visitNotNode(NotNode arg0) {
		
		return null;
	}

	public Object visitNthRefNode(NthRefNode arg0) {
		
		return null;
	}

	public Object visitOpAsgnAndNode(OpAsgnAndNode arg0) {
		
		return null;
	}

	public Object visitOpAsgnNode(OpAsgnNode arg0) {
		
		return null;
	}

	public Object visitOpAsgnOrNode(OpAsgnOrNode arg0) {
		
		return null;
	}

	public Object visitOpElementAsgnNode(OpElementAsgnNode arg0) {
		
		return null;
	}

	public Object visitOrNode(OrNode arg0) {
		
		return null;
	}
	
	public Object visitPreExeNode(PreExeNode iVisited) {
		
		return null;
	}

	public Object visitPostExeNode(PostExeNode arg0) {
		
		return null;
	}

	public Object visitRedoNode(RedoNode arg0) {
		
		return null;
	}

	public Object visitRegexpNode(RegexpNode arg0) {
		
		return null;
	}

	public Object visitRescueBodyNode(RescueBodyNode arg0) {
		
		return null;
	}

	public Object visitRescueNode(RescueNode arg0) {
		
		return null;
	}

	public Object visitRetryNode(RetryNode arg0) {
		
		return null;
	}

	public Object visitReturnNode(ReturnNode arg0) {
		
		return null;
	}

	public Object visitSClassNode(SClassNode arg0) {
		
		return null;
	}

	public Object visitSValueNode(SValueNode arg0) {
		
		return null;
	}

	public Object visitSelfNode(SelfNode arg0) {
		
		return null;
	}

	public Object visitSplatNode(SplatNode arg0) {
		
		return null;
	}

	public Object visitStrNode(StrNode arg0) {
		
		return null;
	}

	public Object visitSuperNode(SuperNode arg0) {
		
		return null;
	}

	public Object visitSymbolNode(SymbolNode arg0) {
		
		return null;
	}

	public Object visitToAryNode(ToAryNode arg0) {
		
		return null;
	}

	public Object visitTrueNode(TrueNode arg0) {
		
		return null;
	}

	public Object visitUndefNode(UndefNode arg0) {
		
		return null;
	}

	public Object visitUntilNode(UntilNode arg0) {
		
		return null;
	}

	public Object visitVAliasNode(VAliasNode arg0) {
		
		return null;
	}

	public Object visitVCallNode(VCallNode arg0) {
		
		return null;
	}

	public Object visitWhenNode(WhenNode arg0) {
		
		return null;
	}

	public Object visitWhileNode(WhileNode arg0) {
		
		return null;
	}

	public Object visitXStrNode(XStrNode arg0) {
		
		return null;
	}

	public Object visitYieldNode(YieldNode arg0) {
		
		return null;
	}

	public Object visitZArrayNode(ZArrayNode arg0) {
		
		return null;
	}

	public Object visitZSuperNode(ZSuperNode node) {
		return null;
	}

	public Node search() throws RubyModelException {		
		ISourceRange range = null;
		if (this.element instanceof IMember)
			range = ((IMember) this.element).getNameRange();
		else
			range = this.element.getSourceRange();
		this.rangeStart = range.getOffset();
		this.rangeLength = range.getLength();
		this.ast.accept(this);
		return this.foundNode;
	}

	public Object visitArgsPushNode(ArgsPushNode node) {
		
		return null;
	}

	public Object visitAttrAssignNode(AttrAssignNode iVisited) {
		
		return null;
	}

	public Object visitRootNode(RootNode iVisited) {
		visitNode(iVisited.getBodyNode());
		return null;
	}
	
	public Object visitRestArgNode(RestArgNode visited)
	{
		
		return null;
	}
	
	public Object visitMultipleAsgnNode(MultipleAsgn19Node visited)
	{
		
		return null;
	}

}
