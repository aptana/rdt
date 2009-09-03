package org.rubypeople.rdt.core.codeassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.lexer.yacc.SyntaxException;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.internal.core.ExternalRubyScript;
import org.rubypeople.rdt.internal.core.RubyScript;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.ti.util.OffsetNodeLocator;

public class ResolveContext
{

	private IRubyScript script;
	private int start;
	private int end;
	private RootNode root;
	private Node selected;
	private IRubyElement[] resolved = new IRubyElement[0];

	public ResolveContext(IRubyScript script, int start, int end)
	{
		this.script = script;
		this.start = start;
		this.end = end;
	}

	public RootNode getAST() throws RubyModelException
	{
		if (root == null)
		{
			try
			{
				RubyParser parser = new RubyParser();
				root = (RootNode) parser.parse((IFile) script.getResource(), script.getSource()).getAST();
			}
			catch (SyntaxException e)
			{
				root = (RootNode) ((RubyScript) script).lastGoodAST;
			}
		}
		return root;
	}

	public Node getSelectedNode() throws RubyModelException
	{
		if (selected == null)
		{
			selected = OffsetNodeLocator.Instance().getNodeAtOffset(getAST(), start);
		}
		return selected;
	}

	public IRubyScript getScript()
	{
		return script;
	}

	public int getStartOffset()
	{
		return start;
	}

	public int getEndOffset()
	{
		return end;
	}

	public IRubyElement[] getResolved()
	{
		// TODO Prioritize the results! If in same IRubyScript, they go first, then if in same project, then everything
		// else.
		return prioritize(resolved);
	}

	private IRubyElement[] prioritize(IRubyElement[] resolved)
	{
		List<IRubyElement> prioritized = Arrays.asList(resolved);
		Collections.sort(prioritized, new Comparator<IRubyElement>()
		{

			public int compare(IRubyElement o1, IRubyElement o2)
			{
				IRubyScript o1Script = (IRubyScript) o1.getAncestor(IRubyElement.SCRIPT);
				if (o1Script != null && o1Script.getPath().equals(script.getPath()))
				{ // o1 is in same script!
					IRubyScript o2Script = (IRubyScript) o2.getAncestor(IRubyElement.SCRIPT);
					if (o2Script != null && o2Script.getPath().equals(script.getPath()))
					{ // so is o2, they're equal
						return 0;
					}
					return -1; // o1 is "closer"
				}
				else
				{
					IRubyScript o2Script = (IRubyScript) o2.getAncestor(IRubyElement.SCRIPT);
					if (o2Script != null && o2Script.getPath().equals(script.getPath()))
					{ // o2 is closer
						return 1;
					}
					// Ok neither script matches. Now check if in same project
					IRubyProject o1Project = o1.getRubyProject();
					if (o1Project != null && o1Project.equals(script.getRubyProject()))
					{
						// o1 has same project
						IRubyProject o2Project = o2.getRubyProject();
						if (o2Project != null && o2Project.equals(script.getRubyProject()))
						{
							// o2 has same project too, consider equal
							return 0;
						}
						return -1;
					}
					else
					{ // o1 project doesn't match!
						IRubyProject o2Project = o2.getRubyProject();
						if (o2Project != null && o2Project.equals(script.getRubyProject()))
						{
							// o2 has same project, it's "closer"
							return 1;
						}
						// neither project matches, check if they're from external lib/gem
						if (o1Script != null && o1Script instanceof ExternalRubyScript)
						{
							// o1 is external gem/library
							if (o2Script != null && o2Script instanceof ExternalRubyScript)
							{
								return 0;
							}
							return -1;
						}
						else if (o2Script != null && o2Script instanceof ExternalRubyScript)
						{
							return 1;
						}
						// not external, projects don't match context, consider equal
						return 0;
					}
				}
			}
		});
		return prioritized.toArray(new IRubyElement[prioritized.size()]);
	}

	public void putResolved(IRubyElement[] resolved)
	{
		this.resolved = resolved;
	}
}
