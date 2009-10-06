package org.rubypeople.rdt.internal.ti;

import java.util.List;

import org.jruby.lexer.yacc.ISourcePosition;

import junit.framework.TestCase;

/**
 * Tests related to matching references.
 * 
 * @author Jason Morrison
 *
 */
public class ReferenceMatchTest extends TestCase {
	
	private IReferenceFinder referenceFinder;
	public void setUp() {
		referenceFinder = new DefaultReferenceFinder(); 
	}
	
	private void assertReferencesEquals( String source, int offset, String matchName, int[][] offsets )
	{
		List <ISourcePosition> references = referenceFinder.findReferences(source, offset);
		assertEquals( offsets.length, references.size() );
		
		for ( int i = 0; i < offsets.length; i++ ) {
			assertEquals( offsets[i][0], references.get(i).getStartOffset() );
			assertEquals( offsets[i][1], references.get(i).getEndOffset() );
			assertEquals( matchName, source.substring(references.get(i).getStartOffset(), references.get(i).getEndOffset()));
		}
	}

	/**
	 * Match locals within ClassNode::DefnNode
	 */
	public void testLocalVariableMatches() {
		String source = "class Klass;def foo(x);puts x*2;end;def bar;my_var = 5;my_var = 6;puts my_var;foo(my_var);end;end";
		int[][] offsets = {{44,50},{55,61},{71,77},{82,88}};
		assertReferencesEquals( source, 46, "my_var", offsets );
	}
	
	/**
	 * Match locals within Kernel::DefnNode
	 */
	public void testLocalVariablesInKernelDefnScope() {
		String source = "def foo;my_var=5;puts my_var*2;end";
		int[][] offsets = {{8,14},{22,28}};
		assertReferencesEquals( source, 10, "my_var", offsets );
	}
	
	/**
	 * Match locals in Kernel
	 */
	public void testLocalVariablesInKernelScope() {
		String source = "my_var = 5;puts my_var*2;other_var = my_var * my_var";
		int[][] offsets = {{0,6},{16,22},{37,43},{46,52}};
		assertReferencesEquals( source, 1, "my_var", offsets );
	}
	
	/**
	 * Match instance vars inside one ClassNode across DefnNode
	 */
	public void testInstanceVariableMatches() {
		String source = "class Klass;def foo(param);@inst_var = param;puts @inst_var;end;def bar;y @inst_var;end;end";
		int[][] offsets = {{27,36},{50,59},{74,83}};
		assertReferencesEquals( source, 29, "@inst_var", offsets );	
	}

	/**
	 * Match instance vars inside two DefnNodes, each in a separate ClassNode (for the same class)
	 */
	public void testInstanceVariableMatchInReopenedClass() {
		String source = "class Klass;def foo;@inst_var=5;end;end;class Klass;def bar;@inst_var=6;end;end";
		int[][] offsets = {{20,29},{60,69}};
		assertReferencesEquals( source, 23, "@inst_var", offsets );
	}

	/**
	 * Test matching a local variable before, inside, and after a block.
	 */
	public void testLocalVariableMatchesIntoBlockScope() {
		String source = "class Klass;def foo;my_var = 5;5.times { puts my_var };puts my_var;end;end";
		int[][] offsets = {{20,26},{46,52},{60,66}};
		assertReferencesEquals( source, 23, "my_var", offsets );
	}
	
	/**
	 * Test referencing a global variable in various contexts.
	 *
	 */
	public void testGlobalVariableMatches() {
		String source = "$foo = 'bar';class Klass;def foo;$foo = 5;end;def bar;puts $foo;end;end;puts $foo";
		int[][] offsets = {{0,4},{33,37},{59,63},{77,81}};
		assertReferencesEquals(source, 0, "$foo", offsets);
	}

//todo: Method invocation tests get into territory where a more formal approach is needed (i.e. DDP)
//      Sub-goals are becoming necessary, i.e. for determining arg-type to match selectors by more
//      than name, and determining receiver-type to match selectors applied to other same-typed receivers.
	
//	public void testMethodInvocationMatchInsideMethod() {
//		String source = "class Klass;def foo;my_var = 5;y = my_var.to_s;puts y.to_s;end;end";
//		int[][] offsets = {{42,46},{54,58}};
//		assertReferencesEquals( source, 42, "to_s", offsets );		
//	}
//	
//	public void testMethodInvocationMatchInKernelScope() {
//		String source = "my_var = 5;y = my_var.to_s;puts y.to_s";
//		int[][] offsets = {{22,26},{34,38}};
//		assertReferencesEquals(source, 22, "to_s", offsets);
//	}
//	
//	public void testStaticMethodInvocationMatchInKernelScope() {
//		String source = "puts 5;puts 6;puts 7;";
//		int[][] offsets = {{0,4},{7,11},{14,18}};
//		assertReferencesEquals( source, 0, "puts", offsets );		
//	}
//	
//	public void testMethodInvocationMatchAgainstMultipleInstancesOfSameType() {
//		String source = "xvar = 5;yvar = 6;puts xvar.to_s;puts yvar.to_s";
//		int[][] offsets = {{28,32},{43,47}};
//		assertReferencesEquals(source, 28, "to_s", offsets); 
//	}
	
	/**
	 * Test matching against ConstNodes; specifically class references
	 */
	public void testTypeMatches() {
		String source = "f = String.new;class Klass;def foo;c = String;end;end;class MyString < String;end";
		int[][] offsets = {{4,10},{39,45},{71,77}};
		assertReferencesEquals( source, 5, "String", offsets );
	}
	
}
