package org.rubypeople.rdt.internal.core.util;

import junit.framework.TestCase;

import org.jruby.ast.Node;
import org.rubypeople.rdt.internal.core.parser.ClosestNodeLocator;
import org.rubypeople.rdt.internal.core.parser.RubyParser;

public class ASTUtilTest extends TestCase {
	public void testNamespace() {
		RubyParser parser = new RubyParser();
		
		String src = "module M1; def m1_method; end; end\n" +
		"module M2; def m2_method; end; end\n" +
		"module M3\n" +
		"  class Chris\n" +
		"    include M1\n" +
		"    include M2\n" +
		"    def c_method\n" +
		"    end\n" +
		"  end\n" +
		"end\n" +
		"\n" +
		"ob = M3::Chris.new\n" +
		"ob";
		Node root = parser.parse(src).getAST();
		assertEquals("M3", ASTUtil.getNamespace(root, 82)); //_c_lass Chris
		assertEquals("M3", ASTUtil.getNamespace(root, 92)); // Chri_s_
		assertEquals("M3::Chris", ASTUtil.getNamespace(root, 93)); // end of line for "class Chris"
		assertEquals("M3::Chris", ASTUtil.getNamespace(root, 94)); // beginning of line after "class Chris"
	}
	
	public void testFullyQualifiedTypename() {
		RubyParser parser = new RubyParser();
		
		String src = "module M1; def m1_method; end; end\n" +
		"module M2; def m2_method; end; end\n" +
		"module M3\n" +
		"  class Chris\n" +
		"    include M1\n" +
		"    include M2\n" +
		"    def c_method\n" +
		"    end\n" +
		"  end\n" +
		"end\n" +
		"\n" +
		"ob = M3::Chris.new\n" +
		"ob";
		Node root = parser.parse(src).getAST();
		assertEquals("M3::Chris", ASTUtil.getFullyQualifiedTypeName(root, new ClosestNodeLocator().getClosestNodeAtOffset(root, 92))); // Chri_s_
	}
	
	public void testFullyQualifiedTypenameOfTypeAtOffsetZero() {
		RubyParser parser = new RubyParser();
		
		String src = "class MyClass; end";
		Node root = parser.parse(src).getAST();
		assertEquals("MyClass", ASTUtil.getFullyQualifiedTypeName(root, new ClosestNodeLocator().getClosestNodeAtOffset(root, 0)));
	}
}
