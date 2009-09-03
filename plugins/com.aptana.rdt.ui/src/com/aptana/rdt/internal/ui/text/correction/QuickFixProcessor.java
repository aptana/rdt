package com.aptana.rdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.jruby.ast.ClassNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.formatter.EditableFormatHelper;
import org.rubypeople.rdt.core.formatter.FormatHelper;
import org.rubypeople.rdt.core.formatter.Indents;
import org.rubypeople.rdt.core.formatter.ReWriteVisitor;
import org.rubypeople.rdt.core.util.Util;
import org.rubypeople.rdt.internal.ti.util.ClosestSpanningNodeLocator;
import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
import org.rubypeople.rdt.internal.ui.rubyeditor.ASTProvider;
import org.rubypeople.rdt.refactoring.core.NodeFactory;
import org.rubypeople.rdt.refactoring.core.renamelocal.RenameLocalRefactoring;
import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;
import org.rubypeople.rdt.ui.text.ruby.IQuickFixProcessor;
import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;

import com.aptana.rdt.IProblem;
import com.aptana.rdt.ui.AptanaRDTUIPlugin;

public class QuickFixProcessor implements IQuickFixProcessor {

	private static final String NEWLINE = "\n";

	public IRubyCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}

		HashSet<Integer> handledProblems = new HashSet<Integer>(locations.length);
		ArrayList<IRubyCompletionProposal> resultingCollections = new ArrayList<IRubyCompletionProposal>();
		for (int i = 0; i < locations.length; i++) {
			IProblemLocation curr = locations[i];
			Integer id = Integer.valueOf(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return (IRubyCompletionProposal[]) resultingCollections.toArray(new IRubyCompletionProposal[resultingCollections.size()]);
	}
	
	public boolean hasCorrections(IRubyScript unit, int problemId) {
		switch (problemId) {
		case IProblem.MisspelledConstructor:
		case IProblem.ConstantNamingConvention:
		case IProblem.MethodMissingWithoutRespondTo:
		case IProblem.LocalAndMethodNamingConvention:
		case IProblem.ComparableInclusionMissingCompareMethod:
		case IProblem.EnumerableInclusionMissingEachMethod:
		case IProblem.PossibleAccidentalBooleanAssignment:
		case IProblem.DeprecatedRequireGem:
		case IProblem.DynamicVariableAliasesLocal:
			return true;
		default:
			return false;
		}
	}

	private void process(IInvocationContext context, final IProblemLocation problem, Collection<IRubyCompletionProposal> proposals) throws CoreException {
		int id = problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		switch (id) {
		case IProblem.DeprecatedRequireGem:
			LocalCorrectionsSubProcessor.addReplacementProposal(problem.getOffset(), "require_gem".length(), "gem", "Replace with call to 'gem'", proposals);
			break;
		case IProblem.MisspelledConstructor:
			LocalCorrectionsSubProcessor.addReplacementProposal("initialize\n", "Rename to 'initialize'", problem, proposals);
			break;
		case IProblem.ConstantNamingConvention:
			String constName = getProblemSource(context, problem);
			String fixed = Util.camelCaseToUnderscores(constName).toUpperCase();
			// FIXME Use the rename refactoring!
			LocalCorrectionsSubProcessor.addReplacementProposal(fixed, "Convert to UPPERCASE_WITH_UNDERSCORES convention", problem, proposals);
			break;
		case IProblem.LocalVariablePossibleAttributeAccess:
			String local = getProblemSource(context, problem);
			fixed = "self." + local;
			LocalCorrectionsSubProcessor.addReplacementProposal(fixed, "change assignment to " + fixed + " to use attribute", problem, proposals);
			// Add a proposal to invoke rename local refactoring			
			RefactoringCorrectionProposal proposal = new RefactoringCorrectionProposal("Rename local variable", RenameLocalRefactoring.class, problem);
			proposals.add(proposal);
			break;
		case IProblem.LocalAndMethodNamingConvention:
			String name = getProblemSource(context, problem);
			fixed = Util.camelCaseToUnderscores(name).toLowerCase();
			// FIXME Use the rename refactoring!
			LocalCorrectionsSubProcessor.addReplacementProposal(fixed, "Convert to lowercase_with_undercores convention", problem, proposals);
			break;
		case IProblem.MethodMissingWithoutRespondTo:
			// FIXME Only do this stuff when we apply the proposal! Don't do all this work just to create the proposal...
			int offset = getOffsetOfFirstLineInsideType(context, problem);
			String text = insertedMethodText(context, offset, "respond_to?", new String[] {"symbol", "include_private = false"});			
			LocalCorrectionsSubProcessor.addReplacementProposal(offset, 0, text, "Add respond_to? method stub", proposals);
			break;
		case IProblem.ComparableInclusionMissingCompareMethod:
			offset = getOffsetOfFirstLineInsideType(context, problem);
			text = insertedMethodText(context, offset, "<=>", new String[] {"other"});	
			LocalCorrectionsSubProcessor.addReplacementProposal(offset, 0, text, "Add <=> method stub", proposals);
			break;
		case IProblem.EnumerableInclusionMissingEachMethod:
			offset = getOffsetOfFirstLineInsideType(context, problem);
			text = insertedMethodText(context, offset, "each", new String[] {});	
			LocalCorrectionsSubProcessor.addReplacementProposal(offset, 0, text, "Add each method stub", proposals);
			break;
		case IProblem.PossibleAccidentalBooleanAssignment:
			name = getProblemSource(context, problem);
			fixed = name.replace("=", "==");
			LocalCorrectionsSubProcessor.addReplacementProposal(fixed, "Convert '=' to '=='", problem, proposals);
			break;
		case IProblem.DynamicVariableAliasesLocal:
			RefactoringCorrectionProposal prop = new RefactoringCorrectionProposal("Rename dynamic variable", RenameLocalRefactoring.class, problem);
			proposals.add(prop);
		default:
		}
	}
	
	private String insertedMethodText(IInvocationContext context, int offset, String methodName, String[] args) {
		IRubyScript script = context.getRubyScript();
		String src = "";
		try {
			src = script.getSource();
		} catch (RubyModelException e) {
			AptanaRDTUIPlugin.log(e);
		}
		
		DefnNode methodNode = NodeFactory.createMethodNode(methodName, args, null);		
		Node insert = NodeFactory.createBlockNode(true, NodeFactory.createNewLineNode(methodNode));			
		String text = ReWriteVisitor.createCodeFromNode(insert, src, getFormatHelper());
		
		StringBuffer buffer = new StringBuffer(text);
		int index = text.indexOf(NEWLINE, 1);
		buffer.insert(index + 1, "  # TODO Auto-generated method stub\n");
		// Figure out indent at offset and apply that to each line of text and at end of text
		String indent = findIndent(offset, script, src);
		buffer.insert(0, indent);
		buffer.append(NEWLINE);
		text = buffer.toString();		
		text = text.replaceAll("\\n", NEWLINE + indent);
		return text;
	}

	private String findIndent(int offset, IRubyScript script, String src) {
		if (src == null || src.length() == 0) return "";
		int index = src.indexOf(NEWLINE, offset);
		if (index < 1 || index > src.length()) return "";
		String line = src.substring(0, index);
		index = line.lastIndexOf(NEWLINE);
		Map options = script.getRubyProject().getOptions(true);
		if (index == -1 || ((index + 1) >= line.length()) ) return Indents.extractIndentString(line, options);
		line = line.substring(index + 1);		
		return Indents.extractIndentString(line, options);
	}

	private int getOffsetOfFirstLineInsideType(IInvocationContext context, IProblemLocation problem) {
		IRubyScript script = context.getRubyScript();
		int offset = -1;
		Node rootNode = ASTProvider.getASTProvider().getAST(script, ASTProvider.WAIT_YES, null);
		Node typeNode = ClosestSpanningNodeLocator.Instance().findClosestSpanner(rootNode, problem.getOffset(), new INodeAcceptor() {
		
			public boolean doesAccept(Node node) {
				return node instanceof ClassNode || node instanceof ModuleNode;
			}
		
		});
		if (typeNode instanceof ClassNode) {
			ClassNode classNode = (ClassNode) typeNode;
			offset = classNode.getBodyNode().getPosition().getStartOffset();
		} else if (typeNode instanceof ModuleNode) {
			ModuleNode classNode = (ModuleNode) typeNode;
			offset = classNode.getBodyNode().getPosition().getStartOffset();
		}
		return offset;
	}

	private String getProblemSource(IInvocationContext context, IProblemLocation problem) throws RubyModelException {
		IRubyScript script = context.getRubyScript();
		String src = script.getSource();
		return src.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
	}
	
	protected FormatHelper getFormatHelper() {
		// FIXME Hooks these settings up to format prefs
		EditableFormatHelper helper = new EditableFormatHelper();
		helper.setAlwaysParanthesizeMethodCalls(true);
		helper.setAlwaysParanthesizeMethodDefs(true);
		return helper;
	}
}
