/**
 * 
 */
package org.rubypeople.rdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jruby.ast.RootNode;
import org.jruby.common.IRubyWarnings;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.parser.RubyParserResult;
import org.rubypeople.rdt.core.IRubyModelMarker;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.internal.core.builder.SyntaxExceptionHandler;
import org.rubypeople.rdt.internal.core.parser.RdtWarnings;
import org.rubypeople.rdt.internal.core.parser.RubyParser;

/**
 * @author Chris
 */
public class RubyScriptProblemFinder
{
	// DSC convert to ImmediateWarnings
	public static RootNode process(RubyScript script, char[] charContents, HashMap<String, CategorizedProblem[]> problems, IProgressMonitor pm)
	{
		RdtWarnings warnings = new RdtWarnings(script.getElementName());
		String contents = new String(charContents);

		List<CategorizedProblem> generatedProblems = new ArrayList<CategorizedProblem>();
		RubyParserResult parserResult = null;
		try
		{
			parserResult = parse(script, contents, warnings);
		}
		catch (SyntaxException e)
		{
			generatedProblems.add(SyntaxExceptionHandler.handle(e, contents));
		}

		generatedProblems.addAll(warnings.getWarnings());
		problems.put(IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER, generatedProblems
				.toArray(new CategorizedProblem[generatedProblems.size()]));

		if (parserResult == null)
			return null;
		return (RootNode) parserResult.getAST();
	}

	private static RubyParserResult parse(RubyScript script, String contents, IRubyWarnings warnings)
	{
		try
		{
			RubyParser parser = new RubyParser(warnings);
			return parser.parse((IFile) script.getUnderlyingResource(), contents);
		}
		catch (CoreException e)
		{
			RubyCore.log(e);
		}
		return null;
	}
}
