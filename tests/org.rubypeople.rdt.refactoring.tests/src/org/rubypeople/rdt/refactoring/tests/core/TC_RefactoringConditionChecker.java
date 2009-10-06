package org.rubypeople.rdt.refactoring.tests.core;

import junit.framework.TestCase;

import org.rubypeople.rdt.refactoring.core.IRefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.core.IRefactoringConfig;
import org.rubypeople.rdt.refactoring.core.RefactoringConditionChecker;
import org.rubypeople.rdt.refactoring.documentprovider.IDocumentProvider;
import org.rubypeople.rdt.refactoring.documentprovider.StringDocumentProvider;

public class TC_RefactoringConditionChecker extends TestCase {
	private final class TestConditionChecker extends RefactoringConditionChecker {
		private TestConditionChecker(final IDocumentProvider provider) {
			super(getDefaultConfig(provider));
		}

		

		@Override
		public void init(IRefactoringConfig configObj) {				
		}

		@Override
		protected void checkInitialConditions() {
		}
	}

	public void testSyntaxErrors() {
		
		RefactoringConditionChecker checker = new TestConditionChecker(new StringDocumentProvider("errorous_dummy_doc.rb", "class Test; en"));
		assertEquals(1, checker.getInitialMessages().get(IRefactoringConditionChecker.ERRORS).size());
		assertEquals(0, checker.getInitialMessages().get(IRefactoringConditionChecker.WARNING).size());
	}
	
	public void testSyntaxErrorsInIncludes() {
		
		StringDocumentProvider stringDocumentProvider = new StringDocumentProvider("dummy_doc_wit_errorous_include.rb", "class Test; end");
		stringDocumentProvider.addFile("other", "class Test; en");
		
		RefactoringConditionChecker checker = new TestConditionChecker(stringDocumentProvider);
		assertEquals(0, checker.getInitialMessages().get(IRefactoringConditionChecker.ERRORS).size());
		assertEquals(0, checker.getInitialMessages().get(IRefactoringConditionChecker.WARNING).size());

		assertEquals(0, checker.getFinalMessages().get(IRefactoringConditionChecker.ERRORS).size());
		assertEquals(1, checker.getFinalMessages().get(IRefactoringConditionChecker.WARNING).size());
	}
	
	private static IRefactoringConfig getDefaultConfig(final IDocumentProvider provider) {
		return new IRefactoringConfig() {

			public IDocumentProvider getDocumentProvider() {
				return provider;
			}

			public void setDocumentProvider(IDocumentProvider doc) {
			}};
	}
}
