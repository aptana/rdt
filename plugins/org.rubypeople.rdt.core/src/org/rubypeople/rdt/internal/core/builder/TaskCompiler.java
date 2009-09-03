package org.rubypeople.rdt.internal.core.builder;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jruby.ast.CommentNode;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.internal.core.parser.ASTTaskParser;
import org.rubypeople.rdt.internal.core.parser.TaskParser;
import org.rubypeople.rdt.internal.core.parser.TaskTag;

public class TaskCompiler extends CompilationParticipant
{

	private TaskParser taskParser;
	private ASTTaskParser astTaskParser;

	@Override
	public int aboutToBuild(IRubyProject project)
	{
		taskParser = new TaskParser(project.getOptions(true));
		astTaskParser = new ASTTaskParser(project.getOptions(true));
		return super.aboutToBuild(project);
	}

	@Override
	public boolean isActive(IRubyProject project)
	{
		return true;
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch, IProgressMonitor monitor)
	{
		SubMonitor sub = SubMonitor.convert(monitor, files.length);
		for (BuildContext context : files)
		{
			sub.subTask("Recording tasks for " + context.getFile().getLocation().toPortableString());
			Collection<CommentNode> comments = context.getComments();
			if (comments == null)
			{
				// resort to slower string/src based parser
				List<TaskTag> tasks = taskParser.getTasks(new String(context.getContents()));
				context.recordNewProblems(tasks.toArray(new CategorizedProblem[tasks.size()]));
			}
			else
			{
				// just go through the comment nodes, MUCH quicker
				List<TaskTag> tasks = astTaskParser.getTasks(comments);
				context.recordNewProblems(tasks.toArray(new CategorizedProblem[tasks.size()]));
			}
			sub.worked(1);
		}
		sub.done();
	}
}
