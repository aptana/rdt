package com.aptana.rdt.internal.ui.text.correction;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.jruby.ast.IfNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.ui.text.ruby.IInvocationContext;
import org.rubypeople.rdt.ui.text.ruby.IRubyCompletionProposal;

import com.aptana.rdt.AptanaRDTPlugin;

public class StatementModifierAssist implements IRubyCompletionProposal {

	private static final String UNLESS = "unless";
	private static final String IF = "if";
	
	private IInvocationContext context;
	
	public StatementModifierAssist(IInvocationContext context) {
		this.context = context;
	}

	public int getRelevance() {
		return 100;
	}

	public void apply(IDocument document) {
		try {
			IfNode ifNode = (IfNode) context.getCoveredNode();		
			String conditionText = getConditionText(document, ifNode.getCondition());
			Node statement = getStatement();
			String statementText = document.get(statement.getPosition().getStartOffset(), getLength(statement)).trim();
					                                           
			String replacement = statementText + getModifierText() + conditionText;
			int start = ifNode.getPosition().getStartOffset();
			int length = ifNode.getPosition().getEndOffset() - start;

			document.replace(start, length, replacement);
		} catch (BadLocationException e) {
			AptanaRDTPlugin.log(e);
		}
	}

	private Node getStatement() {
		IfNode ifNode = (IfNode) context.getCoveredNode();
		if (isUnless(context)) return ifNode.getElseBody();
		return ifNode.getThenBody();
	}

	private String getModifierText() {
		String modifier = " ";
		if (isUnless(context))  modifier += UNLESS;
		else modifier += IF;
		modifier += " ";
		return modifier;
	}

	private String getConditionText(IDocument document, Node node) throws BadLocationException {
		return document.get(node.getPosition().getStartOffset(), getLength(node)).trim();
	}

	private int getLength(Node node) {
		int start = node.getPosition().getStartOffset();
		return node.getPosition().getEndOffset() - start;
	}

	public String getAdditionalProposalInfo() {
		return null;
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	public String getDisplayString() {
		return "Change conditional statement into statement with modifier";
	}

	public Image getImage() {
		return null;
	}

	public Point getSelection(IDocument document) {
		return null;
	}

	public static boolean enabled(IInvocationContext context) {
		Node covered = context.getCoveredNode();
		if (!(covered instanceof IfNode)) return false;
		
		IfNode ifNode = (IfNode) covered;		
		if (isUnless(context)) return ifNode.getThenBody() == null;			
		return ifNode.getElseBody() == null;
	}
	
	private static boolean isUnless(IInvocationContext context) {
		IfNode ifNode = (IfNode) context.getCoveredNode();
		
		try {
			String src = context.getRubyScript().getSource().substring(ifNode.getPosition().getStartOffset(), ifNode.getPosition().getEndOffset());
			return src.startsWith(UNLESS);			
 		} catch (Exception e) {
			// ignore
		}		
 		return false;
	}

}
