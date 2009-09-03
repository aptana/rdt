package com.aptana.rdt.internal.parser.warnings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.compiler.BuildContext;
import org.rubypeople.rdt.core.compiler.CategorizedProblem;
import org.rubypeople.rdt.core.compiler.CompilationParticipant;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.parser.Warning;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.IProblem;

/**
 * Traverse ASTs. If they have a large enough mass (# of children), it generates as hash based on the subtree and
 * attaches the node object to the hash.
 * 
 * @author cwilliams
 */
public class FlayClone extends CompilationParticipant
{
	private static final int DEFAULT_THRESHOLD = 20;
	private int massThreshold = DEFAULT_THRESHOLD;
	private HashMap<Integer, Set<Node>> hashes;
	private boolean doFuzzy; // TODO Get from some user pref
	private int total = 0; // total score (lower is better)
	private HashMap<Integer, Boolean> identical;
	private HashMap<Integer, Integer> masses;

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch, IProgressMonitor monitor)
	{
		if (!isBatch)
			return;
		if (files == null || files.length == 0)
			return;
		hashes = new HashMap<Integer, Set<Node>>();
		identical = new HashMap<Integer, Boolean>();
		masses = new HashMap<Integer, Integer>();
		massThreshold = getMassThreshold();
		for (BuildContext buildContext : files)
		{
			if (buildContext == null || buildContext.getAST() == null)
				continue;
			buildContext.getAST().accept(new Visitor());
		}
		if (doFuzzy)
		{
			processFuzzySimilarities();
		}
		analyze(files);
	}

	private int getMassThreshold()
	{
		return Platform.getPreferencesService().getInt(AptanaRDTPlugin.PLUGIN_ID,
				AptanaRDTPlugin.DUPLICATE_CODE_MASS_THRESHOLD, DEFAULT_THRESHOLD, null);
	}

	private void analyze(BuildContext[] files)
	{
		prune();

		for (Map.Entry<Integer, Set<Node>> entry : hashes.entrySet())
		{
			Integer hash = entry.getKey();
			Collection<Node> nodes = entry.getValue();
			Node first = nodes.iterator().next();
			boolean isIdentical = true;
			for (Node node : nodes)
			{
				if (!equals(node, first)) // FIXME Have to check values of things (like names of vars/types/methods,
				// values of literals, etc)
				{
					isIdentical = false;
					break;
				}
			}
			identical.put(hash, isIdentical);
			int mass = mass(first) * nodes.size();
			if (isIdentical)
				mass *= nodes.size();
			masses.put(hash, mass);
			total += masses.get(hash);
		}
		// For any masses above a given threshold loop over masses hashmap, and generate warnings for the nodes
		// related!
		Map<BuildContext, List<CategorizedProblem>> contextsToProblems = new HashMap<BuildContext, List<CategorizedProblem>>();
		for (Map.Entry<Integer, Integer> entry : masses.entrySet())
		{
			if (entry.getValue() <= massThreshold) // FIXME Should probably be a different #!
				continue;
			Set<Node> nodes = hashes.get(entry.getKey());
			for (Node node : nodes)
			{
				CategorizedProblem problem = new Warning(node.getPosition(), "Identical code structure with: "
						+ otherNodesPositions(node, nodes), IProblem.DuplicateCodeStructure);
				BuildContext context = findContext(files, node);
				if (context == null)
					continue;
				List<CategorizedProblem> problems = contextsToProblems.get(context);
				if (problems == null)
					problems = new ArrayList<CategorizedProblem>();
				problems.add(problem);
				contextsToProblems.put(context, problems);
			}
		}
		for (Map.Entry<BuildContext, List<CategorizedProblem>> entry : contextsToProblems.entrySet())
		{
			entry.getKey().recordNewProblems(entry.getValue().toArray(new CategorizedProblem[0]));
		}
	}

	private String otherNodesPositions(Node node, Collection<Node> nodes)
	{
		StringBuilder builder = new StringBuilder();
		for (Node node2 : nodes)
		{
			if (node2.getPosition().equals(node.getPosition()))
				continue;
			builder.append(node2.getPosition().toString()).append(", ");
		}
		if (builder.length() > 0)
		{
			builder.delete(builder.length() - 2, builder.length());
		}
		else
		{
			System.out.println("WTF?!");
		}
		return builder.toString();
	}

	private BuildContext findContext(BuildContext[] files, Node node)
	{
		String fileName = node.getPosition().getFile();
		for (BuildContext buildContext : files)
		{
			IPath path = buildContext.getFile().getFullPath();
			if (path.toPortableString().equals(fileName))
				return buildContext;
		}
		return null;
	}

	private boolean equals(Node node, Node first)
	{
		return generateSexp(node).equals(generateSexp(first));
	}

	private void prune()
	{
		/*
		 * prune trees that aren't duped at all, or are too small
		 */
		List<Integer> toRemove = new ArrayList<Integer>();
		for (Map.Entry<Integer, Set<Node>> entry : hashes.entrySet())
		{
			if (entry.getValue().size() == 1)
				toRemove.add(entry.getKey());
		}
		for (Integer integer : toRemove)
		{
			hashes.remove(integer);
		}
		toRemove.clear();
		// Prune all subhashes so we show largest match
		Map<Integer, Set<Node>> hashesCopy = new HashMap<Integer, Set<Node>>(hashes);
		for (Map.Entry<Integer, Set<Node>> entry : hashesCopy.entrySet())
		{
			if (toRemove.contains(entry.getKey()))
				continue;
			for (Node node : entry.getValue())
			{
				for (Integer h : allSubHashes(node))
				{
					toRemove.add(h); // So we can shortcut our iteration of copy of hashes
					hashes.remove(h);
				}
			}
		}
	}

	private Collection<Integer> allSubHashes(final Node node)
	{
		final Set<Integer> subHashes = new HashSet<Integer>();
		InOrderVisitor visitor = new InOrderVisitor()
		{
			@Override
			protected Object handleNode(Node visited)
			{
				if (!visited.equals(node))
					subHashes.add(fuzzyHash(visited));
				return super.handleNode(visited);
			}
		};
		node.accept(visitor);
		return subHashes;
	}

	private void processFuzzySimilarities()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Number of children who have children.
	 * 
	 * @param node
	 * @return
	 */
	private int mass(Node node)
	{
		final int[] size = new int[] { 0 };
		node.accept(new InOrderVisitor()
		{
			@Override
			protected Object handleNode(Node visited)
			{
				if (visited != null)
					size[0] += 1;
				return super.handleNode(visited);
			}
		});
		return size[0];
	}

	private int fuzzyHash(Node node)
	{
		return generateSexp(node).hashCode();
	}

	private String generateSexp(Node node)
	{
		final StringBuilder builder = new StringBuilder();
		node.accept(new InOrderVisitor()
		{
			@Override
			public Object acceptNode(Node node)
			{
				builder.append("[");
				if (node != null)
				{
					builder.append(node.getClass().getSimpleName());
				}
				Object ret = super.acceptNode(node);
				builder.append("]");
				return ret;
			}
		});
		return builder.toString();
	}

	@Override
	public boolean isActive(IRubyProject project)
	{
		// TODO Allow changing this per-project?
		return Platform.getPreferencesService().getBoolean(AptanaRDTPlugin.PLUGIN_ID,
				AptanaRDTPlugin.DUPLICATE_CODE_CHECK_ENABLED, true, null);
	}

	private class Visitor extends InOrderVisitor
	{

		@Override
		protected Object handleNode(Node visited)
		{
			if (mass(visited) >= massThreshold)
			{
				Integer hash = fuzzyHash(visited);
				Set<Node> nodes = hashes.get(hash);
				if (nodes == null)
					nodes = new HashSet<Node>();
				// Avoid adding duplicates by checking that positions are different
				for (Node node : nodes)
				{
					if (node.getPosition().equals(visited.getPosition()))
					{
						return super.handleNode(visited);
					}
				}
				nodes.add(visited);
				hashes.put(hash, nodes);
			}
			return super.handleNode(visited);
		}
	}
}
