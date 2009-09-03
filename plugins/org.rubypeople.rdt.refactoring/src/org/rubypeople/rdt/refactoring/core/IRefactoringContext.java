package org.rubypeople.rdt.refactoring.core;

import org.eclipse.core.resources.IFile;

public interface IRefactoringContext {

	public abstract int getCaretPosition();

	public abstract int getStartOffset();

	public abstract int getEndOffset();
	
	public abstract String getSource();
	
	public IFile getActiveFile();

}