package com.aptana.rdt.internal.ui.text.correction;

import org.eclipse.jface.text.IDocument;
import org.rubypeople.rdt.refactoring.action.RefactoringAction;
import org.rubypeople.rdt.refactoring.core.IRefactoringContext;
import org.rubypeople.rdt.refactoring.core.RefactoringContext;
import org.rubypeople.rdt.refactoring.core.RubyRefactoring;
import org.rubypeople.rdt.refactoring.core.renamelocal.RenameLocalRefactoring;
import org.rubypeople.rdt.ui.RubyUI;
import org.rubypeople.rdt.ui.text.correction.ChangeCorrectionProposal;
import org.rubypeople.rdt.ui.text.ruby.IProblemLocation;

public class RefactoringCorrectionProposal extends ChangeCorrectionProposal {

	private IProblemLocation problem;
	private String name;

	public RefactoringCorrectionProposal(String name, Class<? extends RubyRefactoring> refactoringClass, IProblemLocation problem) {
		super(name, null, 100, RubyUI.getSharedImages().getImage(org.rubypeople.rdt.ui.ISharedImages.IMG_OBJS_CORRECTION_CHANGE));
		this.problem = problem;
		this.name = name;
	}

	@Override
	public void apply(IDocument document) {
		IRefactoringContext provider = new RefactoringContext(problem.getOffset(), problem.getOffset() + problem.getLength(), problem.getOffset(), null);
		RefactoringAction action = new RefactoringAction(RenameLocalRefactoring.class, name, provider);
		action.run();
	}
}
