package org.rubypeople.rdt.internal.codeassist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jruby.ast.AliasNode;
import org.jruby.ast.ArgumentNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarAsgnNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.DAsgnNode;
import org.jruby.ast.DVarNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.VCallNode;
import org.jruby.ast.types.INameNode;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IParent;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceFolder;
import org.rubypeople.rdt.core.ISourceFolderRoot;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.ITypeHierarchy;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.codeassist.CodeResolver;
import org.rubypeople.rdt.core.codeassist.ResolveContext;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.core.RubyScript;
import org.rubypeople.rdt.internal.core.search.BasicSearchEngine;
import org.rubypeople.rdt.internal.core.util.ASTUtil;
import org.rubypeople.rdt.internal.core.util.Util;
import org.rubypeople.rdt.internal.ti.ITypeGuess;
import org.rubypeople.rdt.internal.ti.ITypeInferrer;
import org.rubypeople.rdt.internal.ti.util.ClosestSpanningNodeLocator;
import org.rubypeople.rdt.internal.ti.util.FirstPrecursorNodeLocator;
import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
import org.rubypeople.rdt.internal.ti.util.OffsetNodeLocator;

public class RubyCodeResolver extends CodeResolver
{

	private static final String OBJECT = "Object";
	private static final String NEW = "new";
	private static final String DEFAULT_FILE_EXTENSION = ".rb";
	private static final String LOAD = "load";
	private static final String REQUIRE = "require";
	private static final String INITIALIZE = "initialize";
	private HashSet<IType> fVisitedTypes;

	@Override
	public void select(ResolveContext context) throws RubyModelException
	{
		Node selected = OffsetNodeLocator.Instance().getNodeAtOffset(context.getAST(), context.getStartOffset());
		if (selected instanceof StrNode)
		{ // Go to file in a 'require' or 'load' call
			resolveString(context, selected);
			return;
		}
		if (selected instanceof AliasNode)
		{
			resolveAlias(context, selected);
			return;
		}
		if (selected instanceof Colon2Node)
		{
			resolveColon2Node(context, selected);
			return;
		}
		if (selected instanceof DVarNode)
		{
			resolveDynamicVar(context, selected);
			return;
		}
		if (selected instanceof ConstNode)
		{
			resolveConstant(context, selected);
			return;
		}
		if (isLocalVarRef(selected))
		{
			resolveLocalVar(context, selected);
			return;
		}
		if (isInstanceVarRef(selected))
		{
			resolveInstanceVar(context, selected);
			return;
		}
		if (isClassVarRef(selected))
		{
			resolveClassVarRef(context, selected);
			return;
		}
		if (isDeclaration(selected))
		{
			resolveDeclaration(context);
			return;
		}
		if (isMethodCall(selected))
		{
			resolveMethodCall(context, selected);
			return;
		}
	}

	private boolean isDeclaration(Node selected)
	{
		return (selected instanceof DefnNode) || (selected instanceof DefsNode) || (selected instanceof ConstDeclNode)
				|| (selected instanceof ClassNode) || (selected instanceof ModuleNode)
				|| (selected instanceof ClassVarDeclNode);
	}

	protected void resolveString(ResolveContext context, Node selected) throws RubyModelException
	{
		StrNode string = (StrNode) selected;
		FCallNode fcall = (FCallNode) ClosestSpanningNodeLocator.Instance().findClosestSpanner(context.getAST(),
				string.getPosition().getStartOffset(), new INodeAcceptor()
				{

					public boolean doesAccept(Node node)
					{
						return node instanceof FCallNode;
					}

				});
		if (fcall == null)
			return;

		IRubyScript script = context.getScript();
		if (fcall.getName().equals(REQUIRE) || fcall.getName().equals(LOAD))
		{
			String value = string.getValue().toString();
			if (!value.endsWith(DEFAULT_FILE_EXTENSION))
			{
				value += DEFAULT_FILE_EXTENSION;
			}
			ILoadpathEntry[] entries = script.getRubyProject().getResolvedLoadpath(true);
			for (int i = 0; i < entries.length; i++)
			{
				IPath path = entries[i].getPath().append(value);
				if (path.toFile().exists())
				{
					// If it's in the workspace, it's relatively easy...
					IFile file = null;
					if (path.isAbsolute())
					{
						file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
					}
					else
					{
						file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					}
					if (file != null)
					{
						putResolved(context, new IRubyElement[] { RubyCore.create(file) });
						return;
					}
					// ...Otherwise we need to deal with opening an external ruby script by traversing the model of
					// the project
					ISourceFolderRoot sfRoot = script.getRubyProject().getSourceFolderRoot(
							entries[i].getPath().toPortableString());
					String[] parts = value.split("[\\|/]");
					String[] minusFileName;
					if (parts.length == 1)
					{
						minusFileName = new String[0];
					}
					else
					{
						minusFileName = new String[parts.length - 1];
						System.arraycopy(parts, 0, minusFileName, 0, minusFileName.length);
					}
					ISourceFolder folder = sfRoot.getSourceFolder(minusFileName);
					putResolved(context, new IRubyElement[] { folder.getRubyScript(path.lastSegment()) });
					return;
				}
			}
		}
	}

	protected void putResolved(ResolveContext context, IRubyElement[] resolved)
	{
		if (resolved != null && resolved.length > 0)
			context.putResolved(resolved);
	}

	protected void resolveMethodCall(ResolveContext context, Node selected) throws RubyModelException
	{
		String methodName = getName(selected);
		if (methodName.equals(NEW)) // Special case where new resolves to initialize
			methodName = INITIALIZE;
		Set<IRubyElement> possible = new HashSet<IRubyElement>();
		IType[] types = getReceiver(context, selected);
		for (int i = 0; i < types.length; i++)
		{
			IType type = types[i];
			if (fVisitedTypes == null)
			{
				fVisitedTypes = new HashSet<IType>();
			} // keep track of types so we don't get into infinite loop
			Collection<IMethod> methods = suggestMethods(type);
			fVisitedTypes.clear();
			for (IMethod method : methods)
			{
				if (method.getElementName().equals(methodName))
					possible.add(method);
			}
		}
		if (possible.isEmpty())
		{
			// If trying to resolve new/initialize and we have one type, just resolve to type decl!
			Set<String> uniqueTypeNames = uniqueTypeNames(types);
			if (uniqueTypeNames.size() == 1)
			{
				if (methodName.equals(INITIALIZE))
				{
					putResolved(context, types);
					return;
				}
				else
				{
					// limit search to just this type!
					try
					{
						List<SearchMatch> results = search(IRubyElement.METHOD, uniqueTypeNames.iterator().next() + "."
								+ methodName, IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
						for (SearchMatch match : results)
						{
							IRubyElement element = (IRubyElement) match.getElement();
							possible.add(element);
						}
					}
					catch (CoreException e)
					{
						RubyCore.log(e);
					}
				}
			}
			else
			{
				// do a global search for method declarations matching this name
				try
				{
					List<SearchMatch> results = search(IRubyElement.METHOD, methodName,
							IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
					for (SearchMatch match : results)
					{
						IRubyElement element = (IRubyElement) match.getElement();
						possible.add(element);
					}
				}
				catch (CoreException e)
				{
					RubyCore.log(e);
				}
			}
		}
		putResolved(context, possible.toArray(new IRubyElement[possible.size()]));
	}

	protected void resolveInstanceVar(ResolveContext context, Node selected) throws RubyModelException
	{
		List<IRubyElement> possible = getChildrenWithName(context.getScript().getChildren(), IRubyElement.INSTANCE_VAR,
				getName(selected));
		putResolved(context, possible.toArray(new IRubyElement[possible.size()]));
	}

	protected void resolveLocalVar(ResolveContext context, Node selected) throws RubyModelException
	{
		IRubyScript script = context.getScript();
		IRubyElement spanner = script.getElementAt(selected.getPosition().getStartOffset());
		List<IRubyElement> possible = new ArrayList<IRubyElement>();
		if (spanner instanceof IParent)
		{
			IParent parent = (IParent) spanner;
			possible = getChildrenWithName(parent.getChildren(), IRubyElement.LOCAL_VARIABLE, getName(selected));
		}
		if (possible.isEmpty())
		{
			possible = getChildrenWithName(script.getChildren(), IRubyElement.LOCAL_VARIABLE, getName(selected));
		}
		putResolved(context, possible.toArray(new IRubyElement[possible.size()]));
	}

	protected void resolveConstant(ResolveContext context, Node selected) throws RubyModelException
	{
		ConstNode constNode = (ConstNode) selected;
		String name = constNode.getName();
		IRubyScript script = context.getScript();

		// Try to find a matching constant in this script
		// TODO Use convention of all caps versus camelcase to decided which to search for first?
		try
		{
			// Search script for constant
			IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script });
			List<SearchMatch> matches = search(scope, IRubyElement.CONSTANT, name, IRubySearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);
			if (matches.isEmpty())
			{ // none in script, expand search to project
				scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script.getRubyProject() });
				matches = search(scope, IRubyElement.CONSTANT, name, IRubySearchConstants.DECLARATIONS,
						SearchPattern.R_EXACT_MATCH);
			}
			for (SearchMatch match : matches)
			{
				IRubyElement element = (IRubyElement) match.getElement();
				if (element != null)
				{
					putResolved(context, new IRubyElement[] { element });
					return;
				}
			}
		}
		catch (CoreException e)
		{
			RubyCore.log(e);
		}
		// Now search for a type in this script
		try
		{
			IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script });
			List<SearchMatch> matches = search(scope, IRubyElement.TYPE, name, IRubySearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);
			for (SearchMatch match : matches)
			{
				IRubyElement element = (IRubyElement) match.getElement();
				if (element != null)
				{
					putResolved(context, new IRubyElement[] { element });
					return;
				}
			}
		}
		catch (CoreException e)
		{
			RubyCore.log(e);
		}
		RubyElementRequestor completer = new RubyElementRequestor(script);
		String fullyQualifiedName = getFullyQualifiedName(context.getAST(), constNode.getPosition().getStartOffset(),
				name);
		if (fullyQualifiedName != null)
		{
			IType[] types = completer.findType(fullyQualifiedName);
			if (types != null && types.length > 0)
			{
				putResolved(context, types);
				return;
			}
		}
		putResolved(context, completer.findType(name));
	}

	protected void resolveDynamicVar(ResolveContext context, Node selected) throws RubyModelException
	{
		final String name = ((DVarNode) selected).getName();
		Node assignment = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(context.getAST(),
				context.getStartOffset(), new INodeAcceptor()
				{

					public boolean doesAccept(Node node)
					{
						return (node instanceof DAsgnNode) && ((DAsgnNode) node).getName().equals(name);
					}

				});
		putResolved(context, new IRubyElement[] { context.getScript().getElementAt(
				assignment.getPosition().getStartOffset()) });
	}

	protected void resolveClassVarRef(ResolveContext context, Node selected) throws RubyModelException
	{
		List<IRubyElement> possible = getChildrenWithName(context.getScript().getChildren(), IRubyElement.CLASS_VAR,
				getName(selected));
		putResolved(context, possible.toArray(new IRubyElement[possible.size()]));
	}

	protected void resolveDeclaration(ResolveContext context) throws RubyModelException
	{
		IRubyElement element = ((RubyScript) context.getScript()).getElementAt(context.getStartOffset());
		if (element != null)
			putResolved(context, new IRubyElement[] { element });
	}

	protected void resolveColon2Node(ResolveContext context, Node selected)
	{
		String simpleName = ((Colon2Node) selected).getName();
		String fullyQualifiedName = ASTUtil.getFullyQualifiedName((Colon2Node) selected);
		IRubyScript script = context.getScript();
		IRubyElement element = findChild(simpleName, IRubyElement.TYPE, script);
		if (element != null && Util.parentsMatch((IType) element, fullyQualifiedName))
		{
			putResolved(context, new IRubyElement[] { element });
			return;
		}
		RubyElementRequestor completer = new RubyElementRequestor(script);
		putResolved(context, completer.findType(fullyQualifiedName));
	}

	protected void resolveAlias(ResolveContext context, Node selected)
	{
		// figure out if we're pointing at new name or old name.
		AliasNode aliasNode = (AliasNode) selected;
		int startOffset = aliasNode.getPosition().getStartOffset();
		int diff = context.getStartOffset() - startOffset;
		if (diff < (6 + aliasNode.getNewName().length() + 1))
			return; // if we're not over the old name, don't resolve this to anything! FIXME Resolve it to the new
		// method!
		String methodName = aliasNode.getOldName();
		// FIXME Only search within the current class/module scope!
		// do a global search for method declarations matching this name
		List<IRubyElement> possible = new ArrayList<IRubyElement>();
		try
		{
			List<SearchMatch> results = search(IRubyElement.METHOD, methodName, IRubySearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);
			for (SearchMatch match : results)
			{
				IRubyElement element = (IRubyElement) match.getElement();
				possible.add(element);
			}
		}
		catch (CoreException e)
		{
			RubyCore.log(e);
		}
		putResolved(context, possible.toArray(new IRubyElement[possible.size()]));
		return;
	}

	private Set<String> uniqueTypeNames(IType[] types)
	{
		Set<String> names = new HashSet<String>();
		if (types == null)
			return names;
		for (IType type : types)
		{
			names.add(type.getFullyQualifiedName());
		}
		return names;
	}

	private String getFullyQualifiedName(Node root, int offset, String name)
	{
		String namespace = ASTUtil.getNamespace(root, offset);
		if (namespace == null || namespace.trim().length() == 0)
		{
			return name;
		}
		return namespace + "::" + name;
	}

	protected List<SearchMatch> search(int type, String patternString, int limitTo, int matchRule) throws CoreException
	{
		return search(SearchEngine.createWorkspaceScope(), type, patternString, limitTo, matchRule);
	}

	protected List<SearchMatch> search(IRubySearchScope scope, int type, String patternString, int limitTo,
			int matchRule) throws CoreException
	{
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(type, patternString, limitTo, matchRule);
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		CollectingSearchRequestor requestor = new CollectingSearchRequestor();
		engine.search(pattern, participants, scope, requestor, null);
		return requestor.getResults();
	}

	private IType[] getReceiver(ResolveContext context, Node selected) throws RubyModelException
	{
		List<IType> types = new ArrayList<IType>();
		if ((selected instanceof FCallNode) || (selected instanceof VCallNode))
		{
			types = resolveImplicitReceiver(context, selected);
		}
		else
		{
			int start = context.getStartOffset();
			if (selected instanceof CallNode)
			{
				// The problem here is that we want to infer the type of the receiver, not the method (which would
				// give us its return types). So we need to grab the offset of the receiver and infer on that node
				CallNode call = (CallNode) selected;
				Node receiver = call.getReceiverNode();
				start = receiver.getPosition().getStartOffset();
			}
			IRubyScript script = context.getScript();
			ITypeInferrer inferrer = RubyCore.getTypeInferrer();
			Collection<ITypeGuess> guesses = new ArrayList<ITypeGuess>();
			try
			{
				guesses = inferrer.infer(script.getSource(), start);
			}
			catch (RubyModelException e1)
			{
				RubyCore.log(e1);
			}
			// TODO If guesses are empty, just do a global search for this method?
			if (guesses.isEmpty())
			{
				String methodName = ASTUtil.getNameReflectively(selected);
				IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script
						.getRubyProject() });
				CollectingSearchRequestor requestor = new CollectingSearchRequestor();
				SearchPattern pattern = SearchPattern.createPattern(IRubyElement.METHOD, methodName,
						IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
				SearchParticipant[] participants = { BasicSearchEngine.getDefaultSearchParticipant() };
				try
				{
					new BasicSearchEngine().search(pattern, participants, scope, requestor, null);
				}
				catch (CoreException e)
				{
					RubyCore.log(e);
				}
				List<SearchMatch> matches = requestor.getResults();
				if (matches == null || matches.isEmpty())
					return new IType[0];
				for (SearchMatch match : matches)
				{
					IMethod method = (IMethod) match.getElement();
					types.add(method.getDeclaringType());
				}
			}
			else
			{
				RubyElementRequestor requestor = new RubyElementRequestor(script);
				for (ITypeGuess guess : guesses)
				{
					String name = guess.getType();
					IType[] tmpTypes = requestor.findType(name);
					for (int i = 0; i < tmpTypes.length; i++)
					{
						types.add(tmpTypes[i]);
					}
				}
			}
		}
		return types.toArray(new IType[types.size()]);
	}

	protected List<IType> resolveImplicitReceiver(ResolveContext context, Node selected) throws RubyModelException
	{
		IRubyScript script = context.getScript();
		RootNode root = context.getAST();
		int start = context.getStartOffset();
		List<IType> types = new ArrayList<IType>();
		Node receiver = ClosestSpanningNodeLocator.Instance().findClosestSpanner(root, start, new INodeAcceptor()
		{
			public boolean doesAccept(Node node)
			{
				return (node instanceof ClassNode || node instanceof ModuleNode);
			}
		});
		IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script });
		String typeName = ASTUtil.getNameReflectively(receiver);
		if (typeName == null)
			typeName = OBJECT;
		try
		{
			List<SearchMatch> matches = search(scope, IRubyElement.TYPE, typeName, IRubySearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);
			if (matches == null || matches.isEmpty())
				return Collections.emptyList(); // TODO Check up the type hierarchy!
			for (SearchMatch match : matches)
			{
				types.add((IType) match.getElement());
			}
		}
		catch (CoreException e)
		{
			RubyCore.log(e);
		}
		return types;
	}

	// FIXME Just create the type heirarchy, the add all the types into a search scope and search for an exact match to
	// the method name!
	private Collection<IMethod> suggestMethods(IType type) throws RubyModelException
	{
		if (type == null)
			return Collections.emptyList();
		if (fVisitedTypes == null)
			fVisitedTypes = new HashSet<IType>();
		// FIXME We want to avoid visiting the same types across the guesses too!
		List<IMethod> proposals = new ArrayList<IMethod>();
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		IType[] all = new IType[] { type };
		if (hierarchy != null)
		{
			all = hierarchy.getAllSupertypes(type); // Apparently getAllTypes is returning types that are in files chedk
			// that are related to type hierarchy, but aren't supertypes of
			// focus! So I had to switch to getAllSupertypes(focus);
		}
		for (int j = 0; j < all.length; j++)
		{
			IType currentType = all[j];
			if (fVisitedTypes.contains(currentType))
				continue;
			fVisitedTypes.add(currentType);
			IMethod[] methods = currentType.getMethods();
			if (methods != null)
			{
				for (int k = 0; k < methods.length; k++)
				{
					if (methods[k] == null)
						continue;
					proposals.add(methods[k]);
				}
			}
		}
		fVisitedTypes.clear();
		return proposals;
	}

	private IRubyElement findChild(String name, int type, IParent parent)
	{
		try
		{
			IRubyElement[] children = parent.getChildren();
			for (int j = 0; j < children.length; j++)
			{
				IRubyElement child = children[j];
				if (child.getElementName().equals(name) && child.isType(type))
					return child;
				if (child instanceof IParent)
				{
					IRubyElement found = findChild(name, type, (IParent) child);
					if (found != null)
						return found;
				}
			}
		}
		catch (RubyModelException e)
		{
			RubyCore.log(e);
		}
		return null;
	}

	private boolean isMethodCall(Node selected)
	{
		return (selected instanceof VCallNode) || (selected instanceof FCallNode) || (selected instanceof CallNode);
	}

	private List<IRubyElement> getChildrenWithName(IRubyElement[] children, int type, String name)
			throws RubyModelException
	{
		List<IRubyElement> possible = new ArrayList<IRubyElement>();
		for (int i = 0; i < children.length; i++)
		{
			IRubyElement child = children[i];
			if (child.getElementType() == type)
			{
				if (child.getElementName().equals(name))
					possible.add(child);
			}
			if (child instanceof IParent)
			{
				possible.addAll(getChildrenWithName(((IParent) child).getChildren(), type, name));
			}
		}
		return possible;
	}

	private String getName(Node node)
	{
		if (node instanceof INameNode)
		{
			return ((INameNode) node).getName();
		}
		if (node instanceof ClassVarNode)
		{
			return ((ClassVarNode) node).getName();
		}
		return "";
	}

	private boolean isInstanceVarRef(Node node)
	{
		return ((node instanceof InstAsgnNode) || (node instanceof InstVarNode));
	}

	private boolean isClassVarRef(Node node)
	{
		return ((node instanceof ClassVarAsgnNode) || (node instanceof ClassVarNode));
	}

	private boolean isLocalVarRef(Node node)
	{
		return ((node instanceof LocalAsgnNode) || (node instanceof ArgumentNode) || (node instanceof LocalVarNode));
	}
}
