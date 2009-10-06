package org.rubypeople.rdt.internal.ui.search;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.Position;
import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.ui.search.OccurrencesFinder;

/**
 * Tests related to matching occurrences.
 * 
 * @author Jason Morrison
 *`
 */
public class MarkOccurrencesTest extends TestCase {
	
	private IOccurrencesFinder occurrencesFinder;
	public void setUp() {
		occurrencesFinder = new OccurrencesFinder();
		occurrencesFinder.setFMarkConstantOccurrences(true);
		occurrencesFinder.setFMarkFieldOccurrences(true);
		occurrencesFinder.setFMarkLocalVariableOccurrences(true);
		occurrencesFinder.setFMarkMethodExitPoints(true);
		occurrencesFinder.setFMarkMethodOccurrences(true);
		occurrencesFinder.setFMarkOccurrenceAnnotations(true);
		occurrencesFinder.setFMarkTypeOccurrences(true);
		occurrencesFinder.setFStickyOccurrenceAnnotations(true);
	}

	private void  assertOccurrencesEqual(String source, int offset, String matchName, int[][] offsets )
	{
		RubyParser parser = new RubyParser();
		occurrencesFinder.initialize(parser.parse(source).getAST(), offset, 0);
		List<Position> occurrences = occurrencesFinder.perform();
		assertEquals( offsets.length, occurrences.size() );
		
		for ( int i = 0; i < offsets.length; i++ ) {
			int start  = occurrences.get(i).getOffset();
			int length = occurrences.get(i).getLength();
			int end    = occurrences.get(i).getOffset() + length;

			Position testPosition = new Position( start, length );
			
			assertTrue( occurrences.contains( testPosition ) );

//			assertEquals( offsets[i][0], start );
//			assertEquals( offsets[i][1], end );
			assertEquals( matchName, source.substring(start, end));
		}
	}

	/**
	 * Match locals within ClassNode::DefnNode
	 */
	public void testLocalVariableMatches() {
		String source = "class Klass;def foo(x);puts x*2;end;def bar;my_var = 5;my_var = 6;puts my_var;foo(my_var);end;end";
		int[][] offsets = {{44,50},{55,61},{71,77},{82,88}};
		assertOccurrencesEqual( source, 46, "my_var", offsets );
	}
	
	/**
	 * Match args to locals
	 */
	public void testArgMatches() {
		String source = "class Klass;def foo(my_arg);puts my_arg*2;end;end";
		int[][] offsets = {{20,26},{33,39}};
		assertOccurrencesEqual( source, 22, "my_arg", offsets );
	}
	
	/**
	 * Match locals within Kernel::DefnNode
	 */
	public void testLocalVariablesInKernelDefnScope() {
		String source = "def foo;my_var=5;puts my_var*2;end";
		int[][] offsets = {{8,14},{22,28}};
		assertOccurrencesEqual( source, 10, "my_var", offsets );
	}
	
	/**
	 * Match locals in Kernel
	 */
	public void testLocalVariablesInKernelScope() {
		String source = "my_var = 5;puts my_var*2;other_var = my_var * my_var";
		int[][] offsets = {{0,6},{16,22},{37,43},{46,52}};
		assertOccurrencesEqual( source, 1, "my_var", offsets );
	}
	
	/**
	 * Match instance vars inside one ClassNode across DefnNode
	 */
	public void testInstanceVariableMatches() {
		String source = "class Klass;def foo(param);@inst_var = param;puts @inst_var;end;def bar;y @inst_var;end;end";
		int[][] offsets = {{27,36},{50,59},{74,83}};
		assertOccurrencesEqual( source, 29, "@inst_var", offsets );	
	}

	/**
	 * Match instance vars inside two DefnNodes, each in a separate ClassNode (for the same class)
	 */
	public void testInstanceVariableMatchInReopenedClass() {
		String source = "class Klass;def foo;@inst_var=5;end;end;class Klass;def bar;@inst_var=6;end;end";
		int[][] offsets = {{20,29},{60,69}};
		assertOccurrencesEqual( source, 23, "@inst_var", offsets );
	}

	/**
	 * Test matching a local variable before, inside, and after a block.
	 */
	public void testLocalVariableMatchesIntoBlockScope() {
		String source = "class Klass;def foo;my_var = 5;5.times { puts my_var };puts my_var;end;end";
		int[][] offsets = {{20,26},{46,52},{60,66}};
		assertOccurrencesEqual( source, 23, "my_var", offsets );
	}
	
	/**
	 * Test referencing a global variable in various contexts.
	 *
	 */
	public void testGlobalVariableMatches() {
		String source = "$foo = 'bar';class Klass;def foo;$foo = 5;end;def bar;puts $foo;end;end;puts $foo";
		int[][] offsets = {{0,4},{33,37},{59,63},{77,81}};
		assertOccurrencesEqual(source, 0, "$foo", offsets);
	}

	/**
	 * Test referencing a symbol in various contexts.
	 *
	 */
	public void testSymbolMatches() {
		String source = "a_var = :foo;class Klass;def foo;l = :foo;end;def bar;puts :foo;end;end;puts :foo.to_s";
//		String source = "$foo = 'bar';class Klass;def foo;$foo = 5;end;def bar;puts $foo;end;end;puts $foo";
		int[][] offsets = {{8,12},{37,41},{59,63},{77,81}};
		assertOccurrencesEqual(source, 9, ":foo", offsets);
	}

//TODO: Method invocation tests need to know the type of their receiver.
//      Sub-goals are becoming necessary, i.e. for determining arg-type to match selectors by more
//      than name, and determining receiver-type to match selectors applied to other same-typed receivers.
	
//	public void testMethodInvocationMatchInsideMethod() {
//		String source = "class Klass;def foo;my_var = 5;y = my_var.to_s;puts y.to_s;end;end";
//		int[][] offsets = {{42,46},{54,58}};
//		assertOccurrencesEquals( source, 42, "to_s", offsets );		
//	}
//	
//	public void testMethodInvocationMatchInKernelScope() {
//		String source = "my_var = 5;y = my_var.to_s;puts y.to_s";
//		int[][] offsets = {{22,26},{34,38}};
//		assertOccurrencesEquals(source, 22, "to_s", offsets);
//	}
//	
//	public void testStaticMethodInvocationMatchInKernelScope() {
//		String source = "puts 5;puts 6;puts 7;";
//		int[][] offsets = {{0,4},{7,11},{14,18}};
//		assertOccurrencesEquals( source, 0, "puts", offsets );		
//	}
//	
//	public void testMethodInvocationMatchAgainstMultipleInstancesOfSameType() {
//		String source = "xvar = 5;yvar = 6;puts xvar.to_s;puts yvar.to_s";
//		int[][] offsets = {{28,32},{43,47}};
//		assertOccurrencesEquals(source, 28, "to_s", offsets); 
//	}
	
	/**
	 * Test matching against ConstNodes; specifically class occurrences
	 */
	public void testTypeMatches() {
		String source = "f = String.new;class Klass;def foo;c = String;end;end;class MyString < String;end";
		int[][] offsets = {{4,10},{39,45},{71,77}};
		assertOccurrencesEqual( source, 4, "String", offsets );
	}
	
	public void testConstNodeToClassDeclNode() {
		String source = "class Klass;def foo;5;end;end;k = Klass.new";
		int[][] offsets = {{6,11},{34,39}};
		assertOccurrencesEqual( source, 34, "Klass", offsets );
	}
	
	public void testBlockArguments() {
		String source = "[1,2,3].each { |number| puts number }";
		int[][] offsets = {{16,22},{29,35}};
		assertOccurrencesEqual(source, 16, "number", offsets);
//		assertOccurrencesEqual(source, 29, "number", offsets);
	}
	

	/**
	 * Match instance vars inside one ClassNode across DefnNode
	 */
	public void testClassVariableMatches() {
		String source = "class Klass;def foo(param);@@cls_var = param;puts @@cls_var;end;def bar;y @@cls_var;end;end";
		int[][] offsets = {{27,36},{50,59},{74,83}};
		assertOccurrencesEqual( source, 29, "@@cls_var", offsets );	
	}

	/**
	 * Match instance vars inside two DefnNodes, each in a separate ClassNode (for the same class)
	 */
	public void testClassVariableMatchInReopenedClass() {
		String source = "class Klass;def foo;@@cls_var=5;end;end;class Klass;def bar;@@cls_var=6;end;end";
		int[][] offsets = {{20,29},{60,69}};
		assertOccurrencesEqual( source, 23, "@@cls_var", offsets );
	}
	
// Oddity with local/dvars:
//	
//	  foo = Printer.new
//	  [person, place, thing].each do |noun|
//	    foo.print(noun)
//	  end
//	  noun = "hi"
//
//    There are 3 refs to "noun".  Clicking the first highlights 1&2, clicking 2 highlights 1&2
//   but clicking 3 highlights 2&3 - ????!	  
	
}
