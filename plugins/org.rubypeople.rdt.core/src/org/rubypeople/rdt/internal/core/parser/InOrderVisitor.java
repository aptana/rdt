/*
 * Author: C.Williams
 * 
 * Copyright (c) 2004 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
 * can get copy of the GPL along with further information about RubyPeople and
 * third party software bundled with RDT in the file
 * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
 * http://www.rubypeople.org/RDT.license.
 * 
 * RDT is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.rubypeople.rdt.internal.core.parser;

import java.util.Iterator;
import java.util.List;

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
import org.jruby.ast.IArgumentNode;
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
import org.jruby.ast.MultipleAsgnNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.NextNode;
import org.jruby.ast.NilImplicitNode;
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
import org.jruby.ast.RedoNode;
import org.jruby.ast.RegexpNode;
import org.jruby.ast.RescueBodyNode;
import org.jruby.ast.RescueNode;
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
import org.rubypeople.rdt.core.parser.AbstractVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

/**
 * @author Chris
 */
public class InOrderVisitor extends AbstractVisitor
{

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitAliasNode(org.jruby.ast.AliasNode)
	 */
	public Object visitAliasNode(AliasNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitAndNode(org.jruby.ast.AndNode)
	 */
	public Object visitAndNode(AndNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		acceptNode(iVisited.getSecondNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsNode(org.jruby.ast.ArgsNode)
	 */
	public Object visitArgsNode(ArgsNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBlock());
		if (iVisited.getOptArgs() != null)
		{
			visitIter(iVisited.getOptArgs().childNodes().iterator());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsCatNode(org.jruby.ast.ArgsCatNode)
	 */
	public Object visitArgsCatNode(ArgsCatNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		acceptNode(iVisited.getSecondNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitArrayNode(org.jruby.ast.ArrayNode)
	 */
	public Object visitArrayNode(ArrayNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/**
	 * @param iterator
	 */
	private Object visitIter(Iterator<Node> iterator)
	{
		while (iterator.hasNext())
		{
			acceptNode(iterator.next());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBackRefNode(org.jruby.ast.BackRefNode)
	 */
	public Object visitBackRefNode(BackRefNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBeginNode(org.jruby.ast.BeginNode)
	 */
	public Object visitBeginNode(BeginNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBignumNode(org.jruby.ast.BignumNode)
	 */
	public Object visitBignumNode(BignumNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockArgNode(org.jruby.ast.BlockArgNode)
	 */
	public Object visitBlockArgNode(BlockArgNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockNode(org.jruby.ast.BlockNode)
	 */
	public Object visitBlockNode(BlockNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockPassNode(org.jruby.ast.BlockPassNode)
	 */
	public Object visitBlockPassNode(BlockPassNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitBreakNode(org.jruby.ast.BreakNode)
	 */
	public Object visitBreakNode(BreakNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitConstDeclNode(org.jruby.ast.ConstDeclNode)
	 */
	public Object visitConstDeclNode(ConstDeclNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarAsgnNode(org.jruby.ast.ClassVarAsgnNode)
	 */
	public Object visitClassVarAsgnNode(ClassVarAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarDeclNode(org.jruby.ast.ClassVarDeclNode)
	 */
	public Object visitClassVarDeclNode(ClassVarDeclNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarNode(org.jruby.ast.ClassVarNode)
	 */
	public Object visitClassVarNode(ClassVarNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitCallNode(org.jruby.ast.CallNode)
	 */
	public Object visitCallNode(CallNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getIterNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitCaseNode(org.jruby.ast.CaseNode)
	 */
	public Object visitCaseNode(CaseNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassNode(org.jruby.ast.ClassNode)
	 */
	public Object visitClassNode(ClassNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getSuperNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitColon2Node(org.jruby.ast.Colon2Node)
	 */
	public Object visitColon2Node(Colon2Node iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getLeftNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitColon3Node(org.jruby.ast.Colon3Node)
	 */
	public Object visitColon3Node(Colon3Node iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitConstNode(org.jruby.ast.ConstNode)
	 */
	public Object visitConstNode(ConstNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDAsgnNode(org.jruby.ast.DAsgnNode)
	 */
	public Object visitDAsgnNode(DAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDRegxNode(org.jruby.ast.DRegexpNode)
	 */
	public Object visitDRegxNode(DRegexpNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDStrNode(org.jruby.ast.DStrNode)
	 */
	public Object visitDStrNode(DStrNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDSymbolNode(org.jruby.ast.DSymbolNode)
	 */
	public Object visitDSymbolNode(DSymbolNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDVarNode(org.jruby.ast.DVarNode)
	 */
	public Object visitDVarNode(DVarNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDXStrNode(org.jruby.ast.DXStrNode)
	 */
	public Object visitDXStrNode(DXStrNode iVisited)
	{
		handleNode(iVisited);
		visitIter(iVisited.childNodes().iterator());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefinedNode(org.jruby.ast.DefinedNode)
	 */
	public Object visitDefinedNode(DefinedNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getExpressionNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefnNode(org.jruby.ast.DefnNode)
	 */
	public Object visitDefnNode(DefnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefsNode(org.jruby.ast.DefsNode)
	 */
	public Object visitDefsNode(DefsNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitDotNode(org.jruby.ast.DotNode)
	 */
	public Object visitDotNode(DotNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBeginNode());
		acceptNode(iVisited.getEndNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitEnsureNode(org.jruby.ast.EnsureNode)
	 */
	public Object visitEnsureNode(EnsureNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getEnsureNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitEvStrNode(org.jruby.ast.EvStrNode)
	 */
	public Object visitEvStrNode(EvStrNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBody());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitFCallNode(org.jruby.ast.FCallNode)
	 */
	public Object visitFCallNode(FCallNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getIterNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitFalseNode(org.jruby.ast.FalseNode)
	 */
	public Object visitFalseNode(FalseNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitFixnumNode(org.jruby.ast.FixnumNode)
	 */
	public Object visitFixnumNode(FixnumNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitFlipNode(org.jruby.ast.FlipNode)
	 */
	public Object visitFlipNode(FlipNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBeginNode());
		acceptNode(iVisited.getEndNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitFloatNode(org.jruby.ast.FloatNode)
	 */
	public Object visitFloatNode(FloatNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitForNode(org.jruby.ast.ForNode)
	 */
	public Object visitForNode(ForNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getVarNode());
		acceptNode(iVisited.getIterNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitGlobalAsgnNode(org.jruby.ast.GlobalAsgnNode)
	 */
	public Object visitGlobalAsgnNode(GlobalAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitGlobalVarNode(org.jruby.ast.GlobalVarNode)
	 */
	public Object visitGlobalVarNode(GlobalVarNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitHashNode(org.jruby.ast.HashNode)
	 */
	public Object visitHashNode(HashNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getListNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitInstAsgnNode(org.jruby.ast.InstAsgnNode)
	 */
	public Object visitInstAsgnNode(InstAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitInstVarNode(org.jruby.ast.InstVarNode)
	 */
	public Object visitInstVarNode(InstVarNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitIfNode(org.jruby.ast.IfNode)
	 */
	public Object visitIfNode(IfNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getCondition());
		acceptNode(iVisited.getThenBody());
		acceptNode(iVisited.getElseBody());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitIterNode(org.jruby.ast.IterNode)
	 */
	public Object visitIterNode(IterNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getVarNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitLocalAsgnNode(org.jruby.ast.LocalAsgnNode)
	 */
	public Object visitLocalAsgnNode(LocalAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitLocalVarNode(org.jruby.ast.LocalVarNode)
	 */
	public Object visitLocalVarNode(LocalVarNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitMultipleAsgnNode(org.jruby.ast.MultipleAsgnNode)
	 */
	public Object visitMultipleAsgnNode(MultipleAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getHeadNode());
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatch2Node(org.jruby.ast.Match2Node)
	 */
	public Object visitMatch2Node(Match2Node iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatch3Node(org.jruby.ast.Match3Node)
	 */
	public Object visitMatch3Node(Match3Node iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatchNode(org.jruby.ast.MatchNode)
	 */
	public Object visitMatchNode(MatchNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getRegexpNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitModuleNode(org.jruby.ast.ModuleNode)
	 */
	public Object visitModuleNode(ModuleNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitNewlineNode(org.jruby.ast.NewlineNode)
	 */
	public Object visitNewlineNode(NewlineNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getNextNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitNextNode(org.jruby.ast.NextNode)
	 */
	public Object visitNextNode(NextNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitNilNode(org.jruby.ast.NilNode)
	 */
	public Object visitNilNode(NilNode iVisited)
	{
		if (!(iVisited instanceof NilImplicitNode))
		{
			handleNode(iVisited);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitNotNode(org.jruby.ast.NotNode)
	 */
	public Object visitNotNode(NotNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getConditionNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitNthRefNode(org.jruby.ast.NthRefNode)
	 */
	public Object visitNthRefNode(NthRefNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpElementAsgnNode(org.jruby.ast.OpElementAsgnNode)
	 */
	public Object visitOpElementAsgnNode(OpElementAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getArgsNode());
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnNode(org.jruby.ast.OpAsgnNode)
	 */
	public Object visitOpAsgnNode(OpAsgnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnAndNode(org.jruby.ast.OpAsgnAndNode)
	 */
	public Object visitOpAsgnAndNode(OpAsgnAndNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		acceptNode(iVisited.getSecondNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnOrNode(org.jruby.ast.OpAsgnOrNode)
	 */
	public Object visitOpAsgnOrNode(OpAsgnOrNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		acceptNode(iVisited.getSecondNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitOrNode(org.jruby.ast.OrNode)
	 */
	public Object visitOrNode(OrNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		acceptNode(iVisited.getSecondNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitPostExeNode(org.jruby.ast.PostExeNode)
	 */
	public Object visitPostExeNode(PostExeNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitRedoNode(org.jruby.ast.RedoNode)
	 */
	public Object visitRedoNode(RedoNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitRegexpNode(org.jruby.ast.RegexpNode)
	 */
	public Object visitRegexpNode(RegexpNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitRescueBodyNode(org.jruby.ast.RescueBodyNode)
	 */
	public Object visitRescueBodyNode(RescueBodyNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getExceptionNodes());
		acceptNode(iVisited.getOptRescueNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitRescueNode(org.jruby.ast.RescueNode)
	 */
	public Object visitRescueNode(RescueNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getRescueNode());
		acceptNode(iVisited.getBodyNode());
		acceptNode(iVisited.getElseNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitRetryNode(org.jruby.ast.RetryNode)
	 */
	public Object visitRetryNode(RetryNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitReturnNode(org.jruby.ast.ReturnNode)
	 */
	public Object visitReturnNode(ReturnNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValueNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitSClassNode(org.jruby.ast.SClassNode)
	 */
	public Object visitSClassNode(SClassNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitSelfNode(org.jruby.ast.SelfNode)
	 */
	public Object visitSelfNode(SelfNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitSplatNode(org.jruby.ast.SplatNode)
	 */
	public Object visitSplatNode(SplatNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValue());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitStrNode(org.jruby.ast.StrNode)
	 */
	public Object visitStrNode(StrNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitSuperNode(org.jruby.ast.SuperNode)
	 */
	public Object visitSuperNode(SuperNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getArgsNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitSValueNode(org.jruby.ast.SValueNode)
	 */
	public Object visitSValueNode(SValueNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValue());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitSymbolNode(org.jruby.ast.SymbolNode)
	 */
	public Object visitSymbolNode(SymbolNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitToAryNode(org.jruby.ast.ToAryNode)
	 */
	public Object visitToAryNode(ToAryNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getValue());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitTrueNode(org.jruby.ast.TrueNode)
	 */
	public Object visitTrueNode(TrueNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitUndefNode(org.jruby.ast.UndefNode)
	 */
	public Object visitUndefNode(UndefNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitUntilNode(org.jruby.ast.UntilNode)
	 */
	public Object visitUntilNode(UntilNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getConditionNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitVAliasNode(org.jruby.ast.VAliasNode)
	 */
	public Object visitVAliasNode(VAliasNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitVCallNode(org.jruby.ast.VCallNode)
	 */
	public Object visitVCallNode(VCallNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitWhenNode(org.jruby.ast.WhenNode)
	 */
	public Object visitWhenNode(WhenNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getExpressionNodes());
		acceptNode(iVisited.getBodyNode());
		acceptNode(iVisited.getNextCase());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitWhileNode(org.jruby.ast.WhileNode)
	 */
	public Object visitWhileNode(WhileNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getConditionNode());
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitXStrNode(org.jruby.ast.XStrNode)
	 */
	public Object visitXStrNode(XStrNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitYieldNode(org.jruby.ast.YieldNode)
	 */
	public Object visitYieldNode(YieldNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getArgsNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitZArrayNode(org.jruby.ast.ZArrayNode)
	 */
	public Object visitZArrayNode(ZArrayNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitZSuperNode(org.jruby.ast.ZSuperNode)
	 */
	public Object visitZSuperNode(ZSuperNode iVisited)
	{
		handleNode(iVisited);
		return null;
	}

	protected Object handleNode(Node visited)
	{
		return visitNode(visited);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.AbstractVisitor#visitRootNode(org.jruby.ast.RootNode)
	 */
	public Object visitRootNode(RootNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getBodyNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsPushNode(org.jruby.ast.ArgsPushNode)
	 */
	public Object visitArgsPushNode(ArgsPushNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getFirstNode());
		acceptNode(iVisited.getSecondNode());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jruby.ast.visitor.NodeVisitor#visitAttrAssignNode(org.jruby.ast.AttrAssignNode)
	 */
	public Object visitAttrAssignNode(AttrAssignNode iVisited)
	{
		handleNode(iVisited);
		acceptNode(iVisited.getReceiverNode());
		acceptNode(iVisited.getArgsNode());
		return null;
	}

	@Override
	protected Object visitNode(Node iVisited)
	{
		return null;
	}

	protected List<String> getArgumentsFromFunctionCall(IArgumentNode iVisited)
	{
		return ASTUtil.getArgumentsFromFunctionCall(iVisited);
	}

}