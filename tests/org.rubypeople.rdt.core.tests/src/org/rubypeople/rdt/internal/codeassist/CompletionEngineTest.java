package org.rubypeople.rdt.internal.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rubypeople.rdt.core.CompletionProposal;
import org.rubypeople.rdt.core.CompletionRequestor;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;

public class CompletionEngineTest extends ModifyingResourceTest
{

	private IRubyProject rubyProject;
	private TestCompletionRequestor requestor;

	public CompletionEngineTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		rubyProject = createRubyProject("completion");
		requestor = new TestCompletionRequestor();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		deleteProject(rubyProject.getElementName());
		rubyProject = null;
	}

	public void testArrayLiteral() throws Exception
	{
		createFile(rubyProject.getPath().append("array.rb").toPortableString(), "class Array\n  def at\n  end\nend\n");
		final boolean[] isDone = new boolean[1];
		IProgressMonitor monitor = new NullProgressMonitor()
		{
			@Override
			public void done()
			{
				isDone[0] = true;
				super.done();
			}
		};
		rubyProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
		String src = "[1, 2, 3].";
		IRubyScript script = createScript(src);
		long start = System.currentTimeMillis();
		while (!isDone[0])
		{
			Thread.yield();
			if (System.currentTimeMillis() > start + 60000)
				fail("Timed out waiting for indexer on project");
		}
		script.codeComplete(9, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "Array", "at"));
	}

	public void testUserClass() throws Exception
	{
		String src = "class MyClass\n" + "  def aMethod( var )\n" + "  end\n" + "\n" + "  def anotherMethod\n"
				+ "  end\n" + "end\n" + "\n" + "myob = MyClass.new\n" + "\n" + "myob.";
		IRubyScript script = createScript(src);
		script.codeComplete(src.length() - 1, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "MyClass", "aMethod"));
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "MyClass", "anotherMethod"));
	}

	public void testSuggestsConstructorForExplicitMethodInvokationOnConstant() throws Exception
	{
		String src = "class User\n" + "end\n" + "\n" + "@user = User.";
		IRubyScript script = createScript(src);
		script.codeComplete(src.length() - 1, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "User", "new"));
	}

	public void testClassIncludesModules() throws Exception
	{
		String src = "module M1; def m1_method; end; end\n" + "module M2; def m2_method; end; end\n" + "module M3\n"
				+ "  class C\n" + "    include M1\n" + "    include M2\n" + "    def c_method\n" + "    end\n"
				+ "  end\n" + "end\n" + "\n" + "ob = M3::C.new\n" + "ob.";
		IRubyScript script = createScript(src);
		script.codeComplete(src.length() - 1, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "M3::C", "c_method"));
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "M1", "m1_method"));
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "M2", "m2_method"));
	}

	public void testConstantInBrokenScript() throws Exception
	{
		String src = "class X\n" + "  class Y\n" + "    class Z\n" + "      def self.z1; \"z1\"; end\n"
				+ "      def z2; \"z2\"; end\n" + "    end\n" + "    def self.y1; \"y1\"; end\n"
				+ "    def y2; \"y2\"; end\n" + "  end\n" + "  def self.x1; \"x1\"; end\n" + "  def x2; \"x2\"; end\n"
				+ "  def p; end\n" + "  def q; return 'q'; end\n" + "end\n" + "X::";
		IRubyScript script = createScript(src);
		script.codeComplete(src.length() - 1, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "X", "x1"));
		// assertTrue(requestor.containsProposal(CompletionProposal.METHOD_REF, "X", "new"));
		assertTrue(requestor.containsProposal(CompletionProposal.CONSTANT_REF, "X", "Y"));
	}

	private IRubyScript createScript(String src) throws CoreException
	{
		IFile file = createFile(rubyProject.getPath().append("chris.rb").toPortableString(), src);
		IRubyScript script = RubyCore.create(file);
		return script;
	}
	
	public void testSuggestInstanceVariablesDefinedInAttrMethodCalls() throws Exception
	{
		String src = "class User\n" +
				"  attr :variable\n" +
				"  def thing\n" +
				"    \n" +
				"  end\n" +
				"end";
		IRubyScript script = createScript(src);
		script.codeComplete(44, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.INSTANCE_VARIABLE_REF, null, "@variable"));
	}
	
	public void testSuggestInstanceVariablesDefinedInAttrMethodCallsWithPrefix() throws Exception
	{
		String src = "class User\n" +
				"  attr :variable\n" +
				"  def thing\n" +
				"    @\n" +
				"  end\n" +
				"end";
		IRubyScript script = createScript(src);
		script.codeComplete(44, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.INSTANCE_VARIABLE_REF, null, "@variable"));
	}
	
	public void testSuggestClassVariablesDefinedInAttrMethodCalls() throws Exception
	{
		String src = "class User\n" +
				"  cattr :variable\n" +
				"  def thing\n" +
				"    \n" +
				"  end\n" +
				"end";
		IRubyScript script = createScript(src);
		script.codeComplete(45, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.CLASS_VARIABLE_REF, null, "@@variable"));
	}
	
	public void testSuggestClassVariablesDefinedInAttrMethodCallsWithPrefix() throws Exception
	{
		String src = "class User\n" +
				"  cattr :variable\n" +
				"  def thing\n" +
				"    @@\n" +
				"  end\n" +
				"end";
		IRubyScript script = createScript(src);
		script.codeComplete(46, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.CLASS_VARIABLE_REF, null, "@@variable"));
	}
	
	public void testDontSuggestVariableWereCurrentlyTyping() throws Exception
	{
		String src = 
		"class User\n" +
		"  attr :variable\n" +
		"  def thing\n" +
		"    @v\n" +
		"  end\n" +
		"end";
		IRubyScript script = createScript(src);
		script.codeComplete(45, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.INSTANCE_VARIABLE_REF, null, "@variable"));
		assertFalse(requestor.containsProposal(CompletionProposal.INSTANCE_VARIABLE_REF, null, "@v"));
	}
	
	public void testDoesSuggestVariableWereCurrentlyTypingIfPreviouslyDefined() throws Exception
	{
		String src = 
		"class User\n" +
		"  attr :variable\n" +
		"  def thing\n" +
		"    @v = 1\n" +
		"    @v\n" +
		"  end\n" +
		"end";
		IRubyScript script = createScript(src);
		script.codeComplete(56, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.INSTANCE_VARIABLE_REF, null, "@variable"));
		assertTrue(requestor.containsProposal(CompletionProposal.INSTANCE_VARIABLE_REF, null, "@v"));
	}
	
	public void testDontSuggestClassVariableWereCurrentlyTyping() throws Exception
	{
		String src = 
		"class User\n" +
		"  cattr :variable\n" +
		"  def thing\n" +
		"    @@v\n" +
		"  end\n" +
		"end";
		IRubyScript script = createScript(src);
		script.codeComplete(47, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.CLASS_VARIABLE_REF, null, "@@variable"));
		assertFalse(requestor.containsProposal(CompletionProposal.CLASS_VARIABLE_REF, null, "@@v"));
	}
	
	public void testDontSuggestLocalVariableWereCurrentlyTyping() throws Exception
	{
		String src = 
		"class User\n" +
		"  def thing\n" +
		"    variable = 1\n" + 
		"    v\n" +
		"  end\n" +
		"end";
		IRubyScript script = createScript(src);
		script.codeComplete(44, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.LOCAL_VARIABLE_REF, null, "variable"));
		assertFalse(requestor.containsProposal(CompletionProposal.LOCAL_VARIABLE_REF, null, "v"));
	}
	
	public void testConstant() throws Exception
	{
		String src = 
		"class User\n" +
		"  CONSTANT_NAME = 1\n" + 
		"  def thing\n" +
		"    CON\n" +
		"  end\n" +
		"end";
		IRubyScript script = createScript(src);
		script.codeComplete(44, requestor);
		assertTrue(requestor.containsProposal(CompletionProposal.CONSTANT_REF, null, "CONSTANT_NAME"));
	}

	private static class TestCompletionRequestor extends CompletionRequestor
	{

		private List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

		@Override
		public void accept(CompletionProposal proposal)
		{
			proposals.add(proposal);
		}

		public boolean containsProposal(int completionType, String declaringType, String name)
		{
			for (CompletionProposal proposal : proposals)
			{
				if (proposal.getKind() != completionType)
					continue;
				if (!proposal.getName().equals(name))
					continue;
				if (declaringType == null && proposal.getDeclaringType().length() > 0)
					continue;
				if (declaringType != null && !declaringType.equals(proposal.getDeclaringType()))
					continue;
				return true;
			}
			return false;
		}

		public int proposalCount()
		{
			return proposals.size();
		}
	};
}
