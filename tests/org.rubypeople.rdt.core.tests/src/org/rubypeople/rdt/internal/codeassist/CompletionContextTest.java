package org.rubypeople.rdt.internal.codeassist;

import org.eclipse.core.resources.IFile;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;

public class CompletionContextTest extends ModifyingResourceTest
{

	private IRubyProject rubyProject;

	public CompletionContextTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		rubyProject = createRubyProject("completion");
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		deleteProject(rubyProject.getElementName());
		rubyProject = null;
	}

	public void testCorrectsSourceAndOffsetofCodeCompletionAfterArrayLiteral() throws Exception
	{
		IFile file = createFile(rubyProject.getPath().append("chris.rb").toPortableString(), "[1, 2, 3].");
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 9);
		assertEquals("[1, 2, 3]", context.getCorrectedSource());
		assertEquals(8, context.getOffset());
	}

	public void testCompletionOnStringLiteral() throws Exception
	{
		IFile file = createFile(rubyProject.getPath().append("hello_world.rb").toPortableString(),
				"puts \"Hello World\".");
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 18);
		assertEquals("puts \"Hello World\"", context.getCorrectedSource());
		assertEquals(17, context.getOffset());
		assertFalse(context.isConstant());
		assertFalse(context.fullPrefixIsConstant());
		assertEquals("", context.getPartialPrefix());
		// assertEquals("\"Hello World\".", context.getFullPrefix()); // FIXME This isn't the case - it's stopping at
		// space inside string
	}

	public void testCompletionOnNamespacedConstants() throws Exception
	{
		IFile file = createFile(rubyProject.getPath().append("namespace.rb").toPortableString(),
				"class Chris < ActiveRecord::");
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 27);
		assertEquals("class Chris < ActiveRecord", context.getCorrectedSource());
		assertEquals(14, context.getOffset());
		assertTrue(context.isDoubleSemiColon());
		assertFalse(context.isConstant());
		assertTrue(context.fullPrefixIsConstant());
		assertEquals("ActiveRecord::", context.getFullPrefix());
		assertEquals("", context.getPartialPrefix());
	}

	public void testCompletionOnNamespacedConstantsSecondPortionStarted() throws Exception
	{
		IFile file = createFile(rubyProject.getPath().append("namespace.rb").toPortableString(),
				"class Chris < ActiveRecord::B");
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 28);
		assertEquals("class Chris < ActiveRecord::B", context.getCorrectedSource());
		assertEquals(14, context.getOffset());
		assertTrue(context.isDoubleSemiColon());
		assertTrue(context.isConstant());
		assertTrue(context.fullPrefixIsConstant());
		assertEquals("ActiveRecord::B", context.getFullPrefix());
		assertEquals("B", context.getPartialPrefix());
	}

	public void testCompletionOnClassVarPrefix() throws Exception
	{
		IFile file = createFile(rubyProject.getPath().append("class_var.rb").toPortableString(),
				"class User\n  cattr :variable\n  def thing\n    @@\n  end\nend\n");
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 46);
		assertEquals("class User\n  cattr :variable\n  def thing\n    \n  end\nend\n", context.getCorrectedSource());
		assertEquals(45, context.getOffset());
		assertFalse(context.emptyPrefix());
		assertFalse(context.isConstant());
		assertFalse(context.fullPrefixIsConstant());
		assertEquals("@@", context.getFullPrefix());
		assertEquals("@@", context.getPartialPrefix());
	}

	public void testCompletionOnInstanceVariablePrefix() throws Exception
	{
		String src = 
			"class User\n" +
			"  attr :variable\n" +
			"  def thing\n" +
			"    @v\n" +
			"  end\n" +
			"end";
		IFile file = createFile(rubyProject.getPath().append("instance_var.rb").toPortableString(), src);
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 45);
		assertEquals(src, context.getCorrectedSource());
		assertEquals(44, context.getOffset());
		assertFalse(context.emptyPrefix());
		assertFalse(context.hasReceiver());
		assertFalse(context.inComment());
		assertFalse(context.isBroken());
		assertFalse(context.isConstant());
		assertFalse(context.fullPrefixIsConstant());
		assertFalse(context.isClassVariable());
		assertTrue(context.isInstanceVariable());
		assertEquals("@v", context.getFullPrefix());
		assertEquals("@v", context.getPartialPrefix());
	}
	
	public void testCompletionOnInstanceOrClassVariablePrefix() throws Exception
	{
		String src = 
			"class User\n" +
			"  attr :variable\n" +
			"  cattr :class_var\n" +
			"  def thing\n" +
			"    @\n" +
			"  end\n" +
			"end";
		IFile file = createFile(rubyProject.getPath().append("instance_or_class_var.rb").toPortableString(), src);
		IRubyScript script = RubyCore.create(file);
		CompletionContext context = new CompletionContext(script, 63);
		assertEquals(new StringBuilder(src).deleteCharAt(63).toString(), context.getCorrectedSource());
		assertEquals(63, context.getOffset());
		assertFalse(context.emptyPrefix());
		assertFalse(context.hasReceiver());
		assertFalse(context.inComment());
		assertTrue(context.isBroken());
		assertFalse(context.isConstant());
		assertFalse(context.isInstanceVariable());
		assertFalse(context.isClassVariable());
		assertTrue(context.isInstanceOrClassVariable());
		assertFalse(context.fullPrefixIsConstant());
		assertEquals("@", context.getFullPrefix());
		assertEquals("@", context.getPartialPrefix());
	}
}
