package com.aptana.rdt.internal.core.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.lexer.yacc.SyntaxException;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;

public class GemLoadpathAdder extends CompilationParticipant
{

	private IRubyProject project;

	@Override
	public int aboutToBuild(IRubyProject project)
	{
		this.project = project;
		return super.aboutToBuild(project);
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, 100);
		Collection<String> gems = getReferencedGems(files, sub.newChild(80));
		addReferencedGemsToLoadpath(gems, sub.newChild(20));
		sub.done();
	}

	private void addReferencedGemsToLoadpath(Collection<String> gems, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, gems.size());
		for (String gemName : gems)
		{
			if (gemName.equals("rails"))
				continue; // HACK don't do this for rails
			try
			{
				AptanaRDTPlugin.addGemLoadPath(project, new Gem(gemName, "", ""), sub.newChild(1));
			}
			catch (RubyModelException e)
			{
				AptanaRDTPlugin.log(e);
			}
		}
		sub.done();
	}

	private Collection<String> getReferencedGems(BuildContext[] files, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, files.length);
		RubyParser parser = new RubyParser();
		Collection<String> gems = new HashSet<String>();
		for (BuildContext context : files)
		{
			sub.subTask("Looking for referenced gems in " + context.getFile().getLocation().toPortableString());
			gems.addAll(getGemNames(parser, context));
			sub.worked(1);
		}
		sub.done();
		return gems;
	}

	private Collection<String> getGemNames(RubyParser parser, BuildContext context)
	{
		try
		{
			Node root = context.getAST();
			if (root == null)
				return Collections.emptyList();
			GemVisitor visitor = new GemVisitor();
			root.accept(visitor);
			return visitor.getGems();
		}
		catch (SyntaxException e)
		{
			// ignore
		}
		catch (Exception e)
		{
			RubyCore.log(e);
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isActive(IRubyProject project)
	{
		return true;
	}

	private static class GemVisitor extends InOrderVisitor
	{

		private Set<String> gems;

		public GemVisitor()
		{
			gems = new HashSet<String>();
		}

		@Override
		public Object visitFCallNode(FCallNode iVisited)
		{
			String name = iVisited.getName();
			if (name.equals("gem") || name.equals("require_gem"))
			{
				List<String> args = ASTUtil.getArgumentsFromFunctionCall(iVisited);
				if (args != null && !args.isEmpty())
				{
					String gemName = args.get(0);
					if (gemName.startsWith("\"") || gemName.startsWith("'"))
					{
						gemName = gemName.substring(1, gemName.length() - 1);
					}
					gems.add(gemName);
				}

			}
			return super.visitFCallNode(iVisited);
		}

		public Collection<String> getGems()
		{
			return gems;
		}
	}

}
