package org.rubypeople.rdt.core.parser;

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

public abstract class AbstractVisitor implements NodeVisitor
{

	protected abstract Object visitNode(Node visited);

	public Object visitNullNode()
	{
		return visitNode(null);
	}

	public Object acceptNode(Node node)
	{
		if (node == null)
		{
			return visitNullNode();
		}
		else
		{
			return node.accept(this);
		}
	}

	public Object visitAliasNode(AliasNode visited)
	{
		return visitNode(visited);
	}

	public Object visitAndNode(AndNode visited)
	{
		return visitNode(visited);
	}

	public Object visitArgsCatNode(ArgsCatNode visited)
	{
		return visitNode(visited);
	}

	public Object visitArgsNode(ArgsNode visited)
	{
		return visitNode(visited);
	}

	public Object visitArgsPushNode(ArgsPushNode visited)
	{
		return visitNode(visited);
	}

	public Object visitArrayNode(ArrayNode visited)
	{
		return visitNode(visited);
	}

	public Object visitAttrAssignNode(AttrAssignNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBackRefNode(BackRefNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBeginNode(BeginNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBignumNode(BignumNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBlockArgNode(BlockArgNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBlockNode(BlockNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBlockPassNode(BlockPassNode visited)
	{
		return visitNode(visited);
	}

	public Object visitBreakNode(BreakNode visited)
	{
		return visitNode(visited);
	}

	public Object visitCallNode(CallNode visited)
	{
		return visitNode(visited);
	}

	public Object visitCaseNode(CaseNode visited)
	{
		return visitNode(visited);
	}

	public Object visitClassNode(ClassNode visited)
	{
		return visitNode(visited);
	}

	public Object visitClassVarAsgnNode(ClassVarAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitClassVarDeclNode(ClassVarDeclNode visited)
	{
		return visitNode(visited);
	}

	public Object visitClassVarNode(ClassVarNode visited)
	{
		return visitNode(visited);
	}

	public Object visitColon2Node(Colon2Node visited)
	{
		return visitNode(visited);
	}

	public Object visitColon3Node(Colon3Node visited)
	{
		return visitNode(visited);
	}

	public Object visitConstDeclNode(ConstDeclNode visited)
	{
		return visitNode(visited);
	}

	public Object visitConstNode(ConstNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDAsgnNode(DAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDRegxNode(DRegexpNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDStrNode(DStrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDSymbolNode(DSymbolNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDVarNode(DVarNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDXStrNode(DXStrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDefinedNode(DefinedNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDefnNode(DefnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDefsNode(DefsNode visited)
	{
		return visitNode(visited);
	}

	public Object visitDotNode(DotNode visited)
	{
		return visitNode(visited);
	}

	public Object visitEnsureNode(EnsureNode visited)
	{
		return visitNode(visited);
	}

	public Object visitEvStrNode(EvStrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitFCallNode(FCallNode visited)
	{
		return visitNode(visited);
	}

	public Object visitFalseNode(FalseNode visited)
	{
		return visitNode(visited);
	}

	public Object visitFixnumNode(FixnumNode visited)
	{
		return visitNode(visited);
	}

	public Object visitFlipNode(FlipNode visited)
	{
		return visitNode(visited);
	}

	public Object visitFloatNode(FloatNode visited)
	{
		return visitNode(visited);
	}

	public Object visitForNode(ForNode visited)
	{
		return visitNode(visited);
	}

	public Object visitGlobalAsgnNode(GlobalAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitGlobalVarNode(GlobalVarNode visited)
	{
		return visitNode(visited);
	}

	public Object visitHashNode(HashNode visited)
	{
		return visitNode(visited);
	}

	public Object visitIfNode(IfNode visited)
	{
		return visitNode(visited);
	}

	public Object visitInstAsgnNode(InstAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitInstVarNode(InstVarNode visited)
	{
		return visitNode(visited);
	}

	public Object visitIterNode(IterNode visited)
	{
		return visitNode(visited);
	}

	public Object visitLocalAsgnNode(LocalAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitLocalVarNode(LocalVarNode visited)
	{
		return visitNode(visited);
	}

	public Object visitMatch2Node(Match2Node visited)
	{
		return visitNode(visited);
	}

	public Object visitMatch3Node(Match3Node visited)
	{
		return visitNode(visited);
	}

	public Object visitMatchNode(MatchNode visited)
	{
		return visitNode(visited);
	}

	public Object visitModuleNode(ModuleNode visited)
	{
		return visitNode(visited);
	}

	public Object visitMultipleAsgnNode(MultipleAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitMultipleAsgnNode(MultipleAsgn19Node visited)
	{
		return visitNode(visited);
	}

	public Object visitNewlineNode(NewlineNode visited)
	{
		return visitNode(visited);
	}

	public Object visitNextNode(NextNode visited)
	{
		return visitNode(visited);
	}

	public Object visitNilNode(NilNode visited)
	{
		return visitNode(visited);
	}

	public Object visitNotNode(NotNode visited)
	{
		return visitNode(visited);
	}

	public Object visitNthRefNode(NthRefNode visited)
	{
		return visitNode(visited);
	}

	public Object visitOpAsgnAndNode(OpAsgnAndNode visited)
	{
		return visitNode(visited);
	}

	public Object visitOpAsgnNode(OpAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitOpAsgnOrNode(OpAsgnOrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitOpElementAsgnNode(OpElementAsgnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitOrNode(OrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitPostExeNode(PostExeNode visited)
	{
		return visitNode(visited);
	}

	public Object visitPreExeNode(PreExeNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRedoNode(RedoNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRegexpNode(RegexpNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRescueBodyNode(RescueBodyNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRescueNode(RescueNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRestArgNode(RestArgNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRetryNode(RetryNode visited)
	{
		return visitNode(visited);
	}

	public Object visitReturnNode(ReturnNode visited)
	{
		return visitNode(visited);
	}

	public Object visitRootNode(RootNode visited)
	{
		return visitNode(visited);
	}

	public Object visitSClassNode(SClassNode visited)
	{
		return visitNode(visited);
	}

	public Object visitSValueNode(SValueNode visited)
	{
		return visitNode(visited);
	}

	public Object visitSelfNode(SelfNode visited)
	{
		return visitNode(visited);
	}

	public Object visitSplatNode(SplatNode visited)
	{
		return visitNode(visited);
	}

	public Object visitStrNode(StrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitSuperNode(SuperNode visited)
	{
		return visitNode(visited);
	}

	public Object visitSymbolNode(SymbolNode visited)
	{
		return visitNode(visited);
	}

	public Object visitToAryNode(ToAryNode visited)
	{
		return visitNode(visited);
	}

	public Object visitTrueNode(TrueNode visited)
	{
		return visitNode(visited);
	}

	public Object visitUndefNode(UndefNode visited)
	{
		return visitNode(visited);
	}

	public Object visitUntilNode(UntilNode visited)
	{
		return visitNode(visited);
	}

	public Object visitVAliasNode(VAliasNode visited)
	{
		return visitNode(visited);
	}

	public Object visitVCallNode(VCallNode visited)
	{
		return visitNode(visited);
	}

	public Object visitWhenNode(WhenNode visited)
	{
		return visitNode(visited);
	}

	public Object visitWhileNode(WhileNode visited)
	{
		return visitNode(visited);
	}

	public Object visitXStrNode(XStrNode visited)
	{
		return visitNode(visited);
	}

	public Object visitYieldNode(YieldNode visited)
	{
		return visitNode(visited);
	}

	public Object visitZArrayNode(ZArrayNode visited)
	{
		return visitNode(visited);
	}

	public Object visitZSuperNode(ZSuperNode visited)
	{
		return visitNode(visited);
	}

}
