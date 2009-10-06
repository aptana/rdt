package org.rubypeople.rdt.internal.ti;

/**
 * Contains the type inferrence tests that the DataFlow inferrer is able to complete beyond what the default type
 * inferrer can.
 * 
 * @author Jason Morrison
 */
public class DataFlowTypeInferrerTest extends CombinedTypeInferrerTest
{

	protected ITypeInferrer createTypeInferrer()
	{
		return new DataFlowTypeInferrer();
	}

	public void testLocalVariableAfterAssignmentWithOverwrite() throws Exception
	{
		assertInfersTypeFiftyFifty(inferrer.infer("x=5;x='foo';x", 12), "Fixnum", "String");
	}

	public void testClassVarAssignment() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("class Klass;@@x=5;@@x;end", 20), "Fixnum");
	}

	public void testInstVarAssignmentInDifferentClassesWithSameName() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("class X;@var=5;@var;end;class Y;@var='string';@var;end", 16),
				"Fixnum");
		assertInfersTypeWithoutDoubt(inferrer.infer("class X;@var=5;@var;end;class Y;@var='string';@var;end", 48),
				"String");
	}

	public void testGlobalVarAssignmentInDifferentClassesWithSameName() throws Exception
	{
		assertInfersTypeFiftyFifty(inferrer.infer("class X;$var=5;$var;end;class Y;$var='string';$var;end", 16),
				"Fixnum", "String");
	}

	public void testArg() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("def foo(var);var;end;foo(5)", 14), "Fixnum");
	}

	public void testArgTwoDegree() throws Exception
	{
		assertInfersTypeWithoutDoubt(inferrer.infer("def foo(var);var;end;def bar(var);foo(var);end;foo(5)", 14),
				"Fixnum");
	}

	public void testArgTwoWay() throws Exception
	{
		assertInfersTypeFiftyFifty(inferrer.infer("def foo(var);var;end;foo(5);foo('baz');", 14), "Fixnum", "String");
	}

	public void testMethodRetval() throws Exception
	{
		String script = "def foo;return 'baz';end;my_var = foo;my_var";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 40), "String");
	}

	public void testInstanceObjectMethodRetval() throws Exception
	{
		String script = "class X;def foo;return 'bar';end;end;my_instance = X.new;my_var = my_instance.foo;my_var";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 85), "String");
	}

	public void testMethodRetvalIsTypeCovariantWithArgument() throws Exception
	{
		String script = "def foo(arg);return arg;end;my_var = foo(5);my_var";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 48), "Fixnum");
	}

	public void testFactoryMethod() throws Exception
	{
		String script = "class Klass;end;class KlassFactory;def build;return Klass.new;end;end;factory = KlassFactory.new;inst=factory.build;inst";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 119), "Klass");
	}

	public void testSimpleMethodImplicitRetval() throws Exception
	{
		String script = "def foo;'baz';end;my_var = foo;my_var";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, 33), "String");
	}

	public void testBranchingMethodImplicitRetvalSameType() throws Exception
	{
		String script = "def foo;if true;'baz';else;'bar';end;end;my_var = foo;my_var";
		assertInfersTypeWithoutDoubt(inferrer.infer(script, script.length() - 2), "String");
	}

	public void testBranchingMethodImplicitRetvalDifferentTypes() throws Exception
	{
		String script = "def foo;if true;'baz';else;1;end;end;my_var = foo;my_var";
		assertInfersTypeFiftyFifty(inferrer.infer(script, script.length() - 2), "String", "Fixnum");
	}

}
