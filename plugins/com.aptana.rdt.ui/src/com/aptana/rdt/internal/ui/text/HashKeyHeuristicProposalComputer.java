package com.aptana.rdt.internal.ui.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.CompletionProposal;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.search.CollectingSearchRequestor;
import org.rubypeople.rdt.core.search.IRubySearchConstants;
import org.rubypeople.rdt.core.search.IRubySearchScope;
import org.rubypeople.rdt.core.search.SearchEngine;
import org.rubypeople.rdt.core.search.SearchMatch;
import org.rubypeople.rdt.core.search.SearchParticipant;
import org.rubypeople.rdt.core.search.SearchPattern;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;
import org.rubypeople.rdt.internal.ti.util.OffsetNodeLocator;
import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyContentAssistInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.RubyCompletionProposalComputer;

import com.aptana.rdt.AptanaRDTPlugin;

public class HashKeyHeuristicProposalComputer extends RubyCompletionProposalComputer {
	
	public HashKeyHeuristicProposalComputer() {
		super();
	}
	
	@Override
	protected List<CompletionProposal> doComputeCompletionProposals(RubyContentAssistInvocationContext context, IProgressMonitor monitor) {
		return computeHashKeySuggestions();
	}

	private List<CompletionProposal> computeHashKeySuggestions() {	
		List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
		
		String methodCall = getMethodName();		
		String args = getArgumentsToMethodCall();
		int argIndex = calculateArgIndex(args);
		
		List<IRubyElement> methods = search(IRubyElement.METHOD, methodCall, IRubySearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		for (IRubyElement element : methods) {
			IMethod method = (IMethod) element;
			try {
				String[] parameters = method.getParameterNames();
				if (parameters == null || parameters.length == 0) continue;
				if (parameters.length <= argIndex) {
					argIndex = parameters.length - 1;
				}
				String param = parameters[argIndex];
				if (!param.endsWith(" = {}")) continue;
				// Now traverse the method's AST and find out what valid options are!					
				RootNode ast = ASTProvider.getASTProvider().getAST(method.getRubyScript(), ASTProvider.WAIT_YES, new NullProgressMonitor());
				Node methodDefNode = OffsetNodeLocator.Instance().getNodeAtOffset(ast, method.getSourceRange().getOffset());
				String variableName = param.substring(0, param.indexOf(' '));
				ValidOptionVisitor visitor = new ValidOptionVisitor(variableName);
				methodDefNode.accept(visitor);
				Set<String> options = visitor.getValidOptions();
				for (String option : options) {					
					CompletionProposal proposal = new CompletionProposal(CompletionProposal.KEYWORD, option, 201);
					proposal.setName(option);
					int start = fContext.getInvocationOffset();
					proposal.setReplaceRange(start, start + option.length());
					proposals.add(proposal);
				}
			} catch (RubyModelException e) {
				AptanaRDTPlugin.log(e);
			}		
		}
		return proposals;
	}
	
	protected List<IRubyElement> search(int type, String patternString, int limitTo, int matchRule) {
		List<IRubyElement> elements = new ArrayList<IRubyElement>();
		try {			
			SearchEngine engine = new SearchEngine();
			SearchPattern pattern = SearchPattern.createPattern(type, patternString, limitTo, matchRule);
			SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
			IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { fContext.getRubyScript().getRubyProject() } );
			CollectingSearchRequestor requestor = new CollectingSearchRequestor();
			engine.search(pattern, participants, scope, requestor, new NullProgressMonitor());
			List<SearchMatch> matches = requestor.getResults();
			for (SearchMatch match : matches) {
				elements.add((IRubyElement) match.getElement());
			}			
		} catch (CoreException e) {
			AptanaRDTPlugin.log(e);
		}
		return elements;
	}
	
	private String getArgumentsToMethodCall() {
		String prefix = getStatementPrefix();
		String methodCall = getMethodName();
		String args = prefix.trim().substring(methodCall.length());
		if (args.startsWith("(")) args = args.substring(1);
		return args;
	}

	private String getMethodName() {
		String prefix = getStatementPrefix();
		String methodCall = prefix.trim();
		int space = methodCall.indexOf(" ");
		if (space != -1) {
			methodCall = methodCall.substring(0, space);
		}
		space = methodCall.indexOf("(");
		if (space != -1) {
			methodCall = methodCall.substring(0, space);
		}
		return methodCall;
	}
	
	private String getStatementPrefix() {
		try {
			return fContext.computeStatementPrefix().toString();
		} catch (BadLocationException e) {
			AptanaRDTPlugin.log(e);
			return "";
		}
	}
	
	private int calculateArgIndex(String prefix) {
		String[] args = prefix.split(",");
		if (args.length == 1) {
			if (prefix.indexOf(",") == -1) return 0;
			return 1;
		}
		return args.length;
	}
	
	private static class ValidOptionVisitor extends InOrderVisitor {
		
		private Set<String> options = new HashSet<String>();
		private String variableName;
		private boolean stringify = false;
		
		public ValidOptionVisitor(String variableName) {
			this.variableName = variableName;
		}

		public Set<String> getValidOptions() {
			if (stringify) {
				// convert strings to symbols
				Set<String> symbols = new HashSet<String>();
				for (String option : options) {
					if (option.startsWith("'") || option.startsWith("\"")) {
						symbols.add(":" + option.substring(1, option.length() - 5) + " => ");
					}
				}
				return symbols;
			}
			return options;
		}
		
		@Override
		public Object visitCallNode(CallNode iVisited) {
			String methodName = iVisited.getName();
			if (methodName.equals("[]")) {
				Node receiver = iVisited.getReceiverNode();
				if (ASTUtil.getNameReflectively(receiver).equals(variableName)) {
					ArrayNode arguments = (ArrayNode) iVisited.getArgsNode();
					Node arg = arguments.get(0);
					String value = ASTUtil.stringRepresentation(arg);
					if (value != null) {
						options.add(value + " => ");
					}
				}
			} else if (methodName.equals("stringify_keys")) {
				Node receiver = iVisited.getReceiverNode();
				if (ASTUtil.getNameReflectively(receiver).equals(variableName)) {
					stringify = true;
				}
			}
			return super.visitCallNode(iVisited);
		}
	}
}
