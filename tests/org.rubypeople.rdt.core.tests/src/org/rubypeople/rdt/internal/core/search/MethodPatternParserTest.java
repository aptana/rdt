package org.rubypeople.rdt.internal.core.search;

import org.rubypeople.rdt.internal.core.util.CharOperation;

import junit.framework.TestCase;

public class MethodPatternParserTest extends TestCase {

	public void testSelectorOnly() {
		MethodPatternParser parser = new MethodPatternParser();
		parser.parse("run");
		assertStringEqualsCharArray("run", parser.getSelector());
		assertNull(parser.getTypeSimpleName());
		assertNull(parser.getQualifiedTypeName());
		assertNull(parser.getParameterNames());
	}
	
	public void testSimpleTypeNameWithSelector() {
		MethodPatternParser parser = new MethodPatternParser();
		parser.parse("MyClass.run");
		assertStringEqualsCharArray("run", parser.getSelector());		
		assertStringEqualsCharArray("MyClass", parser.getTypeSimpleName());	
		assertStringEqualsCharArray("MyClass", parser.getQualifiedTypeName());	
		assertNull(parser.getParameterNames());
	}

	public void testQualifiedTypeNameWithSelector() {
		MethodPatternParser parser = new MethodPatternParser();
		parser.parse("ActiveRecord::Base.run");
		assertStringEqualsCharArray("run", parser.getSelector());		
		assertStringEqualsCharArray("Base", parser.getTypeSimpleName());	
		assertStringEqualsCharArray("ActiveRecord::Base", parser.getQualifiedTypeName());	
		assertNull(parser.getParameterNames());
	}
	
	public void testQualifiedTypeNameWithSelectorAndParameters() {
		MethodPatternParser parser = new MethodPatternParser();
		parser.parse("ActiveRecord::Base.run(arg1, arg2)");
		assertStringEqualsCharArray("run", parser.getSelector());		
		assertStringEqualsCharArray("Base", parser.getTypeSimpleName());	
		assertStringEqualsCharArray("ActiveRecord::Base", parser.getQualifiedTypeName());
		assertTrue(CharOperation.equals(new char[][] {"arg1".toCharArray(), "arg2".toCharArray()}, parser.getParameterNames()));
	}
	
	private void assertStringEqualsCharArray(String string, char[] selector) {
		assertTrue(CharOperation.equals(string.toCharArray(), selector));		
	}
	
}
