package org.rubypeople.rdt.internal.ui;

import junit.framework.TestCase;

import org.rubypeople.eclipse.shams.resources.ShamFile;

public class TC_RubyFileMatcher extends TestCase {

	private RubyFileMatcher matcher;

	protected void setUp() throws Exception {
		super.setUp();
		matcher = new RubyFileMatcher();
	}

	public void testHasRubyEditorAssociationIfContainsRubyShebang() {
		ShamFile file = new ShamFile("fake", false);
		file.setContents("#! /usr/local/bin/ruby\nputs 'Hello World!'\n");
		assertTrue(matcher.hasRubyEditorAssociation(file));
	}

	public void testRakefileHasRubyEditorAssociation() {
		ShamFile file = new ShamFile("Rakefile", false);
		assertTrue(matcher.hasRubyEditorAssociation(file));
	}

	public void testGemHasRubyEditorAssociation() {
		ShamFile file = new ShamFile("syntax.gem", false);
		assertTrue(matcher.hasRubyEditorAssociation(file));
	}

	public void testGemspecHasRubyEditorAssociation() {
		ShamFile file = new ShamFile("syntax.gemspec", false);
		assertTrue(matcher.hasRubyEditorAssociation(file));
	}

	public void testYAMLHasRubyEditorAssociation() {
		ShamFile file = new ShamFile("database.yml", false);
		assertTrue(matcher.hasRubyEditorAssociation(file));
	}

	public void testRHTMLHasRubyEditorAssociation() {
		ShamFile file = new ShamFile("index.rhtml", false);
		assertTrue(matcher.hasRubyEditorAssociation(file));
	}

	// TODO Test that a user can add new files which are associated

}
