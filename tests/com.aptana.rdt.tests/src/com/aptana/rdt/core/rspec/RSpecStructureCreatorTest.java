package com.aptana.rdt.core.rspec;

import junit.framework.TestCase;

import org.jruby.ast.Node;
import org.rubypeople.rdt.internal.core.parser.RubyParser;

public class RSpecStructureCreatorTest extends TestCase
{

	public void testEmptySource() throws Exception
	{
		String src = "";
		Node ast = new RubyParser().parse(src).getAST();
		RSpecStructureCreator creator = new RSpecStructureCreator();
		creator.acceptNode(ast);
		assertEquals(0, creator.getBehaviors().length);
	}

	public void testOneBehaviorNoExamples() throws Exception
	{
		String src = "describe Bowling do\nend";
		Node ast = new RubyParser().parse(src).getAST();
		RSpecStructureCreator creator = new RSpecStructureCreator();
		creator.acceptNode(ast);
		assertEquals(1, creator.getBehaviors().length);
		Behavior behavior = (Behavior) creator.getBehaviors()[0];
		assertEquals("Bowling", behavior.getClassName());
		assertEquals("Bowling", behavior.getSource()); // source really shouldn't equal class name, but is never used!
		assertEquals(0, behavior.getSourceRange().getOffset());
		assertEquals(src.length(), behavior.getSourceRange().getLength());
		assertEquals(0, behavior.getExamples().length);
	}

	public void testOneBehaviorOneExample() throws Exception
	{
		String src = "describe Bowling do\n  it 'should score a gutter game as 0' do\n  end\nend";
		Node ast = new RubyParser().parse(src).getAST();
		RSpecStructureCreator creator = new RSpecStructureCreator();
		creator.acceptNode(ast);
		assertEquals(1, creator.getBehaviors().length);
		Behavior behavior = (Behavior) creator.getBehaviors()[0];
		assertEquals("Bowling", behavior.getClassName());
		assertEquals("Bowling", behavior.getSource());
		assertEquals(0, behavior.getSourceRange().getOffset());
		assertEquals(src.length(), behavior.getSourceRange().getLength());
		assertEquals(1, behavior.getExamples().length);
		Example example = (Example) behavior.getExamples()[0];
		assertEquals(behavior, example.getBehavior());
		assertEquals("should score a gutter game as 0", example.getDescription());
		assertEquals("should score a gutter game as 0", example.getSource()); // source really shouldn't equal
																				// description, but is never used!
		assertEquals(22, example.getSourceRange().getOffset());
		assertEquals(45, example.getSourceRange().getLength());
	}

}
