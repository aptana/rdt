package org.rubypeople.rdt.debug.core.tests;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.debug.core.SuspensionPoint;
import org.rubypeople.rdt.internal.debug.core.commands.AbstractDebuggerConnection;
import org.rubypeople.rdt.internal.debug.core.commands.ClassicDebuggerConnection;
import org.rubypeople.rdt.internal.debug.core.model.RubyVariable;
import org.rubypeople.rdt.internal.debug.core.model.ThreadInfo;
import org.rubypeople.rdt.internal.launching.LaunchingPlugin;

public class FTC_ClassicDebuggerCommunicationTest extends
		FTC_AbstractDebuggerCommunicationTest {
	public static junit.framework.TestSuite suite() {

		junit.framework.TestSuite suite = new junit.framework.TestSuite();
		//suite.addTest(new FTC_DebuggerCommunicationTest("testBreakpointOnFirstLine"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testBreakpointAddAndRemove"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testSimpleCycleSteppingWorks"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testStoppingonOneLineTwice"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableLocal"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableArray"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableArrayEmpty"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableHash"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableHashWithObjectKeys"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableHashWithStringKeys"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariableWithXmlContent"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testThreadIdsAndResume"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testThreadFramesAndVariables"));
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testFrames"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testThreads"));
		
		
//		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testVariablesInFrames"));
		
		
		//suite.addTest(new TC_DebuggerCommunicationTest("testConstants"));
		//suite.addTest(new TC_DebuggerCommunicationTest("testConstantDefinedInBothClassAndSuperclass"));
		
		//suite.addTest(new TC_DebuggerCommunicationTest("testVariablesInFrames"));
		//suite.addTest(new TC_DebuggerCommunicationTest("testFramesWhenThreadSpawned"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testThreadIdsAndResume"));
		//suite.addTest(new TC_DebuggerCommunicationTest("testThreadsAndFrames"));		
		//suite.addTest(new TC_DebuggerCommunicationTest("testStepOver"));		
		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableNil"));
		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableInstanceNested"));				
		//suite.addTest(new TC_DebuggerCommunicationTest("testStaticVariableInstanceNested"));			
		
		//suite.addTest(new TC_DebuggerCommunicationTest("testNameError"));
		//suite.addTest(new TC_DebuggerCommunicationTest("testVariablesInObject"));	
		//suite.addTest(new TC_DebuggerCommunicationTest("testStaticVariables"));		
		//suite.addTest(new TC_DebuggerCommunicationTest("testSingletonStaticVariables"));							
		//suite.addTest(new TC_DebuggerCommunicationTest("testVariableString"));	
		// suite.addTest(new TC_DebuggerCommunicationTest("testInspect"));
		//suite.addTest(new TC_DebuggerCommunicationTest("testInspectError"));
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testReloadAndInspect")) ;
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testReloadWithException")) ;
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testReloadAndStep")) ;
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testReloadInRequire")) ;
		suite.addTest(new FTC_ClassicDebuggerCommunicationTest("testReloadInStackFrame")) ;
//		suite.addTest(new TC_DebuggerCommunicationTest("testIgnoreException"));
//		suite.addTest(new TC_DebuggerCommunicationTest("testExceptionHierarchy"));
//		suite.addTest(new TC_DebuggerCommunicationTest("testException"));
        
		return suite;
	}

	public FTC_ClassicDebuggerCommunicationTest(String arg0) {
		super(arg0);
	}

	public void startRubyProcess() throws Exception {
		String cmd = FTC_ClassicDebuggerCommunicationTest.RUBY_INTERPRETER + " -I" + createIncludeDir() +  " -I" + getTmpDir().replace('\\', '/') + " -rclassic-debug-verbose.rb " + getRubyTestFilename();
		System.out.println("Starting: " + cmd);
		process = Runtime.getRuntime().exec(cmd);
		rubyStderrRedirectorThread = new OutputRedirectorThread(process.getErrorStream());
		rubyStderrRedirectorThread.start();
		rubyStdoutRedirectorThread = new OutputRedirectorThread(process.getInputStream());
		rubyStdoutRedirectorThread.start();
	
	}

	private String createIncludeDir() {
		String includeDir;
		if (LaunchingPlugin.getDefault() != null) {
			// being run as JUnit Plug-in Test, Eclipse is running			
			includeDir = RubyCore.getOSDirectory(LaunchingPlugin.getDefault()) + "ruby" ;
		}
		else {
		    // being run as "pure" JUnit Test without Eclipse running 
			// getResource delivers a URL, so we get slashes as Fileseparator
			includeDir = getClass().getResource("/").getFile();
			includeDir += "../../org.rubypeople.rdt.launching/ruby" ;
			// if on windows, remove a leading slash
			if (includeDir.startsWith("/") && File.separatorChar == '\\') {
				includeDir = includeDir.substring(1);
			}
		}
		// the ruby interpreter on linux does not like quotes, so we use them only if really necessary
		if (includeDir.indexOf(" ") == -1) {
			return includeDir ;
		}
		else {
			return '"' + includeDir + '"';
		}
	}

	public void testThreads() throws Exception {
		createSocket(new String[] { "Thread.new {", "puts 'a'", "}",
				"Thread.pass", "puts 'b'" });
		sendRuby("b test.rb:2");
		sendRuby("b test.rb:5");
		sendRuby("cont");
		SuspensionPoint point1 = getSuspensionReader().readSuspension();
		sendRuby("th l");
		ThreadInfo[] threadInfos = getThreadInfoReader().readThreads();
		assertEquals(2, threadInfos.length);
		sendRuby("cont");
		SuspensionPoint point2 = getSuspensionReader().readSuspension();
		sendRuby("th l");
		threadInfos = getThreadInfoReader().readThreads();
		assertEquals(1, threadInfos.length);
		assertNotSame(Integer.valueOf(point1.getThreadId()), Integer.valueOf(point2.getThreadId()));
	}

	public void testThreadIdsAndResume() throws Exception {
		createSocket(new String[] { "threads=[]", "threads << Thread.new {",
				"puts 'a'", "}", "threads << Thread.new{", "puts 'b'", "}",
				"puts 'c'", "threads.each{|t| t.join()}" });
		sendRuby("b test.rb:3");
		sendRuby("b test.rb:6");
		sendRuby("b test.rb:8");
		sendRuby("cont");
		getSuspensionReader().readSuspension();
		getSuspensionReader().readSuspension();
		getSuspensionReader().readSuspension();
	
		sendRuby("th l");
		ThreadInfo[] threads = getThreadInfoReader().readThreads();
		assertEquals(3, threads.length);
		int threadId1 = threads[0].getId();
		int threadId2 = threads[1].getId();
		int threadId3 = threads[2].getId();
		sendRuby("th " + threadId2 + " ; cont");
	
		sendRuby("th l");
		threads = getThreadInfoReader().readThreads();
		assertEquals(2, threads.length);
		assertEquals(threadId1, threads[0].getId());
		assertEquals(threadId3, threads[1].getId());
		sendRuby("th " + threadId3 + " ; cont");
	
		sendRuby("th l");
		threads = getThreadInfoReader().readThreads();
		assertEquals(1, threads.length);
		assertEquals(threadId1, threads[0].getId());
	}

	public void testReloadAndInspect() throws Exception {
		String[] lines = new String[] { "class Test", "def calc(a)", "a = a*2",
				"return a", "end", "end", "test=Test.new()" };
		createSocket(lines);
		runToLine(7);
		// test variable value in stack 1 (top stack frame)
		lines[2] = "a=a*4";
		writeFile("test.rb", lines);
		sendRuby("load " + getTmpDir() + "test.rb");
		IStatus loadResult = this.getLoadResultReader()
				.readLoadResult();
		assertTrue("No Exception from load", loadResult.isOK());
		sendRuby("v inspect Test.new.calc(2)");
		RubyVariable[] variables = getVariableReader().readVariables(
				createStackFrame());
		assertEquals("There is one variable returned.", 1, variables.length);
		assertEquals("Result is 8", "8", variables[0].getValue()
				.getValueString());
	}

	public void testReloadAndStep() throws Exception {
		String[] lines = new String[] { "puts 'a'", "puts 'b'", "puts 'c'" };
		createSocket(lines);
		runToLine(2);
		lines = new String[] { "puts 'd'", "puts 'e'", "puts 'f'" };
		writeFile("test.rb", lines);
		sendRuby("load " + getTmpDir() + "test.rb");
		this.getLoadResultReader().readLoadResult();
		sendRuby("next");
		SuspensionPoint info = getSuspensionReader().readSuspension();
		assertEquals(3, info.getLine());
	}

	public void testReloadWithException() throws Exception {
		createSocket(new String[] { "puts 'a'" });
		runToLine(1);
		// test variable value in stack 1 (top stack frame)
		String[] lines = new String[] { "classs A;end" };
		writeFile("test.rb", lines);
	
		sendRuby("load " + getTmpDir() + "test.rb");
		IStatus loadResult = this.getLoadResultReader()
				.readLoadResult();
		assertFalse("Exception from load", loadResult.isOK());
		assertTrue(loadResult.getMessage().startsWith("SyntaxError"));
	}

	public void testReloadInRequire() throws Exception {
		// Deadlock
		String[] lines = new String[] { "def endless", "sleep 0.1", "end" };
		writeFile("content file.rb", lines);
		createSocket(new String[] { "require 'content file'", "while true",
				"endless()", "end" });
		sendRuby("cont");
		// test variable value in stack 1 (top stack frame)
		lines[1] = "exit 0";
		writeFile("content file.rb", lines);
		sendRuby("load " + getTmpDir() + "content file.rb");
		IStatus loadResult = this.getLoadResultReader()
				.readLoadResult();
		assertTrue("No Exception from load", loadResult.isOK());
	}

	public void testReloadInStackFrame() throws Exception {
		String[] lines = new String[] { "class Test", "def calc(a)", "a = a*2",
				"return a", "end", "end", "result = Test.new.calc(2)",
				"result = Test.new.calc(2)", "puts result" };
		createSocket(lines);
		runToLine(3);
		// a has not yet been calculated ...
		sendRuby("v local");
		RubyVariable[] localVariables = getVariableReader().readVariables(
				createStackFrame());
		assertEquals("2", localVariables[0].getValue().getValueString());
		// now change the code ...
		lines[2] = "a=a*4";
		writeFile("test.rb", lines);
		sendRuby("load " + getTmpDir() + "test.rb");
		IStatus loadResult = this.getLoadResultReader()
				.readLoadResult();
		assertTrue("No Exception from load", loadResult.isOK());
		runToLine(4);
		// now a is calculated and the result is 4. That means that ruby does
		// not change the code which
		// currently being executed in a stack frame, Java would have reset the
		// instruction pointer and the
		// result would be 8
		sendRuby("v local");
		localVariables = getVariableReader().readVariables(createStackFrame());
		assertEquals("4", localVariables[0].getValue().getValueString());
	
		// Now check that the new code is executed with the next call to calc
		runToLine(3);
		runToLine(4);
		sendRuby("v local");
		localVariables = getVariableReader().readVariables(createStackFrame());
		assertEquals("8", localVariables[0].getValue().getValueString());
	}

	@Override
	protected AbstractDebuggerConnection createDebuggerConnection() {
		return new ClassicDebuggerConnection(1098) ;
	}

}
