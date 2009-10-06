package org.rubypeople.rdt.debug.core.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.rubypeople.rdt.internal.debug.core.ExceptionSuspensionPoint;
import org.rubypeople.rdt.internal.debug.core.StepSuspensionPoint;
import org.rubypeople.rdt.internal.debug.core.SuspensionPoint;
import org.rubypeople.rdt.internal.debug.core.commands.AbstractCommand;
import org.rubypeople.rdt.internal.debug.core.commands.AbstractDebuggerConnection;
import org.rubypeople.rdt.internal.debug.core.commands.BreakpointCommand;
import org.rubypeople.rdt.internal.debug.core.commands.GenericCommand;
import org.rubypeople.rdt.internal.debug.core.commands.StepCommand;
import org.rubypeople.rdt.internal.debug.core.model.RubyProcessingException;
import org.rubypeople.rdt.internal.debug.core.model.RubyStackFrame;
import org.rubypeople.rdt.internal.debug.core.model.RubyThread;
import org.rubypeople.rdt.internal.debug.core.model.RubyVariable;
import org.rubypeople.rdt.internal.debug.core.model.ThreadInfo;
import org.rubypeople.rdt.internal.debug.core.parsing.AbstractReadStrategy;
import org.rubypeople.rdt.internal.debug.core.parsing.BreakpointModificationReader;
import org.rubypeople.rdt.internal.debug.core.parsing.EvalReader;
import org.rubypeople.rdt.internal.debug.core.parsing.FramesReader;
import org.rubypeople.rdt.internal.debug.core.parsing.LoadResultReader;
import org.rubypeople.rdt.internal.debug.core.parsing.SuspensionReader;
import org.rubypeople.rdt.internal.debug.core.parsing.ThreadInfoReader;
import org.rubypeople.rdt.internal.debug.core.parsing.VariableReader;

public abstract class FTC_AbstractDebuggerCommunicationTest extends TestCase {

	private static final boolean VERBOSE = false;
	private static String tmpDir;

	protected static String getTmpDir() {
		if (tmpDir == null) {
			tmpDir = System.getProperty("java.io.tmpdir");
			if (tmpDir.charAt(tmpDir.length() - 1) != File.separatorChar) {
				tmpDir = tmpDir + File.separator;
			}
		}
		return tmpDir;
	}

	public static String RUBY_INTERPRETER;
	static {
		RUBY_INTERPRETER = System.getProperty("rdt.rubyInterpreter");
		if (RUBY_INTERPRETER == null) {
			RUBY_INTERPRETER = "ruby";
		}
	}

	private static long TIMEOUT_MS = 30000;

	protected Process process;

	protected OutputRedirectorThread rubyStdoutRedirectorThread;

	protected OutputRedirectorThread rubyStderrRedirectorThread;

	private AbstractDebuggerConnection debuggerConnection;

	// for timeout handling
	private Thread mainThread;

	private Thread timeoutThread;
	private AbstractReadStrategy readStrategy;

	public FTC_AbstractDebuggerCommunicationTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(FTC_ClassicDebuggerCommunicationTest.class);
	}

	protected String getTestFilename() {
		return getTmpDir() + "test.rb";
	}

	protected String getRubyTestFilename() {
		return getTestFilename().replace('\\', '/');
	}

	protected SuspensionReader getSuspensionReader() throws Exception {
		return new SuspensionReader(readStrategy);
	}

	protected VariableReader getVariableReader() throws Exception {
		return new VariableReader(readStrategy);
	}

	protected EvalReader getEvalExceptionReader() throws Exception {
		return new EvalReader(readStrategy);
	}

	protected FramesReader getFramesReader() throws Exception {
		return new FramesReader(readStrategy);
	}

	protected ThreadInfoReader getThreadInfoReader() throws Exception {
		return new ThreadInfoReader(readStrategy);
	}

	protected LoadResultReader getLoadResultReader() throws Exception {
		return new LoadResultReader(readStrategy);
	}

	protected EvalReader getEvalReader() throws Exception {
		return new EvalReader(readStrategy);
	}

	protected BreakpointModificationReader getBreakpointAddedReader() throws Exception {
		return new BreakpointModificationReader(readStrategy);
	}

	protected String getOSIndependent(String path) {
		return path.replace('\\', '/');
	}

	public void setUp() throws Exception {
		if (!new File(getTmpDir()).exists() || !new File(getTmpDir()).isDirectory()) {
			throw new RuntimeException("Temp directory does not exist: " + getTmpDir());
		}
		// if a reader hangs, because the expected data from the ruby process
		// does not arrive, it gets interrupted from the timeout watchdog.
		mainThread = Thread.currentThread();
		timeoutThread = new Thread() {
			public void run() {
				try {
					while (true) {
						log("Starting timeout watchdog.");
						Thread.sleep(TIMEOUT_MS);
						log("Timeout reached.");
						mainThread.interrupt();
					}
				} catch (InterruptedException e) {
					log("Watchdog deactivated.");
				}
			}
		};
		timeoutThread.start();
		log("Setup finished.");
	}

	public void tearDown() {
		log("TearDown");
		timeoutThread.interrupt();
		if (process == null) {
			// here we go if there was an error in the creation of the process
			// (process == null)
			// or there was an error creating the socket, e.g. ruby process has
			// died early
			return;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			debuggerConnection.exit();
		} catch (IOException e) {
			log("Exception while closing socket.");
			e.printStackTrace();
		}
		try {
			if (process.exitValue() != 0) {
				log("Ruby finished with exit value: " + process.exitValue());
			}
		} catch (IllegalThreadStateException ex) {
			process.destroy();
			log("Ruby process had to be destroyed.");
			// wait so that the debugger port will be availabel for the next
			// test
			// There seems to be a delay after the destroying of a process and
			// freeing the server port
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				log("Exception while sleeping after destroying the ruby process");
				// TODO: find out what causes interruption!
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					log("Exception again while sleeping after destroying the ruby process");
				}
			}
		}

		log("Waiting for stdout redirector thread..");
		try {
			rubyStdoutRedirectorThread.join();
		} catch (InterruptedException e) {
			log("Exception while waiting for stdout redirector");
			e.printStackTrace();
		}
		log("..done");
		log("Waiting for stderr redirector thread..");
		try {
			rubyStderrRedirectorThread.join();
		} catch (InterruptedException e) {
			log("Exception while waiting for stderr redirector");
			e.printStackTrace();
		}
		log("..done");
	}

	protected void writeFile(String name, String[] content) throws Exception {
		PrintWriter writer = new PrintWriter(new FileOutputStream(getTmpDir() + name));
		for (int i = 0; i < content.length; i++) {
			writer.println(content[i]);
		}
		writer.close();
	}

	protected abstract AbstractDebuggerConnection createDebuggerConnection();

	protected abstract void startRubyProcess() throws Exception;

	protected void createSocket(String[] lines) throws Exception {
		writeFile("test.rb", lines);
		startRubyProcess();
		Thread.sleep(1000);
		debuggerConnection = createDebuggerConnection();
		debuggerConnection.connect();
		// new AbstractDebuggerConnection
		// try {
		// socket = new Socket("localhost", 1098);
		// } catch (ConnectException cex) {
		// throw new RuntimeException(
		// "Ruby process finished prematurely. Last line in stderr: "
		// + rubyStderrRedirectorThread.getLastLine(), cex);
		// }
		// multiReaderStrategy = new MultiReaderStrategy(getXpp(socket));

		// Runnable runnable = new Runnable() {
		// public void run() {
		// try {
		// while (true) {
		// new WasteReader(multiReaderStrategy).read();
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// };
		// };
		// new Thread(runnable).start();
		// Thread.sleep(500);
		// out = new PrintWriter(socket.getOutputStream(), true);
		// createControlSocket() ;
	}

	protected SuspensionReader startDebugger() throws Exception {
		return debuggerConnection.start();
	}

	protected void sendCommand(AbstractCommand command) throws Exception {
		command.execute(debuggerConnection);
	}

	/*
	 * @deprecated
	 */
	protected void sendRuby(String debuggerCommand) throws Exception {
		try {
			process.exitValue();
			throw new RuntimeException("Ruby debugger has finished prematurely.");
		} catch (IllegalThreadStateException ex) {
			// not yet finished, normal behaviour
			// why does process does not have a function like isRunning() ?
			// TODO: remove generic command
			GenericCommand command = new GenericCommand(debuggerCommand, false /* iscontrol */);
			command.execute(debuggerConnection);
			readStrategy = command.getReadStrategy();
		}
	}

	// public void testNameError() throws Exception {
	// createSocket(new String[] { "puts 'x'" });
	// sendRuby("cont");
	// SuspensionPoint hit = getSuspensionReader().readSuspension();
	// // TODO: assertion is wrong, I want the debugger to suspend here
	// // but there must be several catchpoints available
	// assertNull(hit);
	// }

	protected void runToLine(int lineNumber) throws Exception {
		runTo("test.rb", lineNumber);
	}

	private void runTo(String filename, int lineNumber) throws Exception {
		setBreakpoint(filename, lineNumber) ;
		SuspensionReader reader;
		if (!debuggerConnection.isStarted()) {
			reader = debuggerConnection.start();
		} else {
			StepCommand stepCommand = new StepCommand("cont");
			stepCommand.execute(debuggerConnection);
			reader = stepCommand.getSuspensionReader();
		}
		SuspensionPoint hit = reader.readSuspension();
		assertNotNull(hit);
		assertTrue(hit.isBreakpoint());
		assertEquals(lineNumber, hit.getLine());
	}

	private void setBreakpoint(String filename, int line) throws Exception{
		String command = "b " + filename + ":" + line;
		new BreakpointCommand(command).executeWithResult(debuggerConnection);
	}	
	
	private void setBreakpoint(int line) throws Exception {
		setBreakpoint("test.rb", line) ;
	}

	public void testBreakpointOnFirstLine() throws Exception {
		createSocket(new String[] { "puts 'a'" });
		runTo("test.rb", 1);
		sendRuby("exit") ;
	}

	public void testBreakpointAddAndRemove() throws Exception {
		createSocket(new String[] { "1.upto(3) {", "puts 'a'", "puts 'b'", "puts 'c'", "}" });
		int breakpointId1 = new BreakpointCommand("b test.rb:2").executeWithResult(debuggerConnection);
		assertEquals(1, breakpointId1);
		int breakpointId2 = new BreakpointCommand("b test.rb:4").executeWithResult(debuggerConnection);
		assertEquals(2, breakpointId2);

		SuspensionPoint hit1 = startDebugger().readSuspension();
		assertBreakpoint(hit1, "test.rb", 2);
		SuspensionPoint hit2 = new StepCommand("cont").readSuspension(debuggerConnection);
		assertBreakpoint(hit2, "test.rb", 4);
		SuspensionPoint hit3 = new StepCommand("cont").readSuspension(debuggerConnection);
		assertBreakpoint(hit3, "test.rb", 2);
		int idDeleted = new BreakpointCommand("delete 100").executeWithResult(debuggerConnection);
		assertEquals(-1, idDeleted);
		idDeleted = new BreakpointCommand("delete 2").executeWithResult(debuggerConnection);
		assertEquals(2, idDeleted);
		SuspensionPoint hit4 = new StepCommand("cont").readSuspension(debuggerConnection);
		assertBreakpoint(hit4, "test.rb", 2);
		SuspensionPoint hit5 = new StepCommand("cont").readSuspension(debuggerConnection);
		assertNull("null expected, but was: " + hit5, hit5);
	}

	public void testSimpleCycleSteppingWorks() throws Exception {
		createSocket(new String[] { "1.upto(2) {", "puts 'a'", "}", "puts 'b'" });
		int breakpointId1 = new BreakpointCommand("b test.rb:2").executeWithResult(debuggerConnection);
		assertEquals(1, breakpointId1);
		int breakpointId2 = new BreakpointCommand("b test.rb:4").executeWithResult(debuggerConnection);
		assertEquals(2, breakpointId2);
		SuspensionReader reader = startDebugger();
		assertBreakpoint(reader.readSuspension(), "test.rb", 2);
		sendRuby("cont"); // 2 -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 2);
		sendRuby("cont"); // 2 -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 4);
		sendRuby("cont"); // 4 -> finish
		SuspensionPoint hit = getSuspensionReader().readSuspension();
		assertNull("null expected, but was: " + hit, hit);
	}

	public void testStoppingOnOneLineTwice() throws Exception {
		createSocket(new String[] { "1.upto(2) {", "if true", "puts 'a'", "end", "}", "puts 'b'" });
		sendRuby("b test.rb:2");
		assertEquals(1, getBreakpointAddedReader().readBreakpointNo());
		sendRuby("b test.rb:6");
		assertEquals(2, getBreakpointAddedReader().readBreakpointNo());
		sendRuby("cont"); // -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 2);
		sendRuby("cont"); // 2 -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 2);
		sendRuby("cont"); // 2 -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 2);
		sendRuby("cont"); // 2 -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 2);
		sendRuby("cont"); // 2 -> 2
		assertBreakpoint(getSuspensionReader().readSuspension(), "test.rb", 6);
		sendRuby("cont"); // 6 -> finish
		SuspensionPoint hit = getSuspensionReader().readSuspension();
		assertNull("null expected, but was: " + hit, hit);
	}

	public void assertBreakpoint(SuspensionPoint hit, String file, int line) {
		assertNotNull(hit);
		assertTrue(hit.isBreakpoint());
		assertEquals(line, hit.getLine());
		assertEquals(file, hit.getFile());
	}

	public void testException() throws Exception {
		// per default catch is set to StandardError, i.e. every raise of a
		// subclass of StandardError
		// will suspend
		createSocket(new String[] { "puts 'a'", "raise 'message \\dir\\file: <xml/>\n<8>'", "puts 'c'" });
		GenericCommand catchCommand = new GenericCommand("catch StandardError", true /* iscontrol */);
		catchCommand.execute(debuggerConnection);
		SuspensionPoint hit = startDebugger().readSuspension();
		assertNotNull(hit);
		assertEquals(3, hit.getLine());
		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), hit.getFile());
		assertTrue(hit.isException());
		assertEquals("message \\dir\\file: <xml/> <8>", ((ExceptionSuspensionPoint) hit).getExceptionMessage());
		assertEquals("RuntimeError", ((ExceptionSuspensionPoint) hit).getExceptionType());
		// TODO: test catch off with a second exception
		sendRuby("catch off");
		sendRuby("cont");
	}

	public void testExceptionsIgnoredByDefault() throws Exception {
		createSocket(new String[] { "puts 'a'", "raise 'dont stop'" });
		SuspensionPoint hit = startDebugger().readSuspension();
		assertNull(hit);
	}

	public void testExceptionHierarchy() throws Exception {
		createSocket(new String[] { "class MyError < StandardError", "end", "begin", "raise StandardError.new", "rescue", "end", "raise MyError.new" });
		GenericCommand catchCommand = new GenericCommand("catch MyError", true /* iscontrol */);
		catchCommand.execute(debuggerConnection);
		SuspensionPoint hit = startDebugger().readSuspension();
		assertNotNull(hit);
		assertEquals(7, hit.getLine());
		assertEquals("MyError", ((ExceptionSuspensionPoint) hit).getExceptionType());
		sendRuby("cont");
		hit = getSuspensionReader().readSuspension();
		assertNull(hit);
	}

	public void testBreakpointNeverReached() throws Exception {
		createSocket(new String[] { "puts 'a'", "puts 'b'", "puts 'c'" });
		new BreakpointCommand("b test.rb:10").executeWithResult(debuggerConnection);
		log("Waiting for breakpoint..");
		SuspensionPoint hit = startDebugger().readSuspension();
		assertNull(hit);
	}

	private void log(String string) {
		if (VERBOSE)
			System.out.println(string);
	}

	public void testStepOver() throws Exception {
		createSocket(new String[] { "puts 'a'", "puts 'b'", "puts 'c'" });
		BreakpointCommand breakpointCommand = new BreakpointCommand("b test.rb:2");
		sendCommand(breakpointCommand);
		breakpointCommand.getBreakpointAddedReader().readBreakpointNo();

		startDebugger().readSuspension();

		SuspensionPoint info = new StepCommand("next").readSuspension(debuggerConnection);
		assertEquals(3, info.getLine());
		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), info.getFile());
		assertTrue(info.isStep());
		assertEquals(1, ((StepSuspensionPoint) info).getFramesNumber());

		info = new StepCommand("next").readSuspension(debuggerConnection);
		assertNull(info);
	}

	public void testStepOverFrames() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "puts 'a'", "Test2.new.print()" });
		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
		runTo("test2.rb", 3);
		sendRuby("next");
		SuspensionPoint info = getSuspensionReader().readSuspension();
		assertEquals(4, info.getLine());
		assertEquals(getOSIndependent(getTmpDir() + "test2.rb"), info.getFile());
		assertTrue(info.isStep());
		assertEquals(2, ((StepSuspensionPoint) info).getFramesNumber());
		sendRuby("next");
		info = getSuspensionReader().readSuspension();
		assertNull(info);
	}

	public void testStepOverInDifferentFrame() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "Test2.new.print()", "puts 'a'" });
		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
		runTo("test2.rb", 4);
		sendRuby("next");
		SuspensionPoint info = getSuspensionReader().readSuspension();
		assertEquals(3, info.getLine());
		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), info.getFile());
		assertTrue(info.isStep());
		assertEquals(1, ((StepSuspensionPoint) info).getFramesNumber());
		sendRuby("cont");
	}

	public void testStepReturn() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "Test2.new.print()", "puts 'a'" });
		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
		runTo("test2.rb", 4);
		sendRuby("next");
		SuspensionPoint info = getSuspensionReader().readSuspension();
		assertEquals(3, info.getLine());
		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), info.getFile());
		assertTrue(info.isStep());
		assertEquals(1, ((StepSuspensionPoint) info).getFramesNumber());
		sendRuby("cont");
	}

	public void testHitBreakpointWhileSteppingOver() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "Test2.new.print()", "puts 'a'" });
		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
		new BreakpointCommand("b test2.rb:4").executeWithResult(debuggerConnection);
		runTo("test.rb", 2);
		sendRuby("next");
		SuspensionPoint info = getSuspensionReader().readSuspension();
		assertEquals(4, info.getLine());
		assertEquals("test2.rb", info.getFile());
		assertTrue(info.isBreakpoint());
		sendRuby("cont");
	}

	public void testStepInto() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "puts 'a'", "Test2.new.print()" });
		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'XX'", "puts 'XX'", "end", "end" });
		runTo("test.rb", 3);
		sendRuby("step");
		SuspensionPoint info = getSuspensionReader().readSuspension();
		assertEquals(3, info.getLine());
		assertEquals(getOSIndependent(getTmpDir() + "test2.rb"), info.getFile());
		assertTrue(info.isStep());
		assertEquals(2, ((StepSuspensionPoint) info).getFramesNumber());
		sendRuby("cont");
	}

	protected RubyStackFrame createStackFrame() throws Exception {
		RubyStackFrame stackFrame = new RubyStackFrame(null, "", 5, 1); // RubyThread
		// thread
		// = new
		// RubyThread(null)
		// ;
		// thread.addStackFrame(stackFrame) ;
		return stackFrame;
	}

	public void testCommandList() throws Exception {
		// test that commands separated by comma will be processed
		createSocket(new String[] { "a=5", "puts a" });
		runToLine(2);
		sendRuby("v l ; v i a");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(0, variables.length);
		// escaped semicolon
		sendRuby("v inspect a=1\\;a+1");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals("Handled escaded semicolon", 1, variables.length);
		assertEquals("2", variables[0].getValue().getValueString());
		sendRuby("cont");
	}

	public void testVariableNil() throws Exception {
		createSocket(new String[] { "puts 'a'", "puts 'b'", "stringA='XX'" });
		runToLine(2);
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("stringA", variables[0].getName());
		assertEquals("nil", variables[0].getValue().getValueString());
		assertEquals(null, variables[0].getValue().getReferenceTypeName());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testVariableWithXmlContent() throws Exception {
		createSocket(new String[] { "stringA='<start test=\"&\"/>'", "testHashValue=Hash[ '$&' => nil]", "puts 'b'" });
		runToLine(3);
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(2, variables.length);
		assertEquals("stringA", variables[0].getName());
		assertEquals("<start test=\"&\"/>", variables[0].getValue().getValueString());
		assertTrue(variables[0].isLocal());
		// the testHashValue contains an example, where the name consists of
		// special characters
		sendRuby("v i testHashValue");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("'$&'", variables[0].getName());
		sendRuby("cont");
	}

	public void testVariableInObject() throws Exception {
		createSocket(new String[] { "class Test", "def initialize", "@y=5", "puts @y", "end", "def to_s", "'test'", "end", "end", "Test.new()" });
		runTo("test.rb", 4);
		// Read numerical variable
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("self", variables[0].getName());
		assertEquals("test", variables[0].getValue().getValueString());
		assertEquals("Test", variables[0].getValue().getReferenceTypeName());
		assertTrue(variables[0].getValue().hasVariables());
		sendRuby("v i self");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("@y", variables[0].getName());
		assertEquals("5", variables[0].getValue().getValueString());
		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
		assertTrue(!variables[0].isStatic());
		assertTrue(!variables[0].isLocal());
		assertTrue(variables[0].isInstance());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testStaticVariables() throws Exception {
		createSocket(new String[] { "class Test", "@@staticVar=55", "def method", "puts 'a'", "end", "end", "test=Test.new()", "test.method()" });
		runTo("test.rb", 4);
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("self", variables[0].getName());
		assertTrue(variables[0].getValue().hasVariables());
		sendRuby("v i self");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("@@staticVar", variables[0].getName());
		assertEquals("55", variables[0].getValue().getValueString());
		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
		assertTrue(variables[0].isStatic());
		assertTrue(!variables[0].isLocal());
		assertTrue(!variables[0].isInstance());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testSingletonStaticVariables() throws Exception {
		createSocket(new String[] { "class Test", "def method", "puts 'a'", "end", "class << Test", "@@staticVar=55", "end", "end", "Test.new().method()" });
		runTo("test.rb", 3);
		sendRuby("v i self");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("@@staticVar", variables[0].getName());
		assertEquals("55", variables[0].getValue().getValueString());
		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
		assertTrue(variables[0].isStatic());
		assertTrue(!variables[0].isLocal());
		assertTrue(!variables[0].isInstance());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testConstants() throws Exception {
		createSocket(new String[] { "class Test", "TestConstant=5", "end", "test=Test.new()", "puts 'a'" });
		runTo("test.rb", 5);
		sendRuby("v i test");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("TestConstant", variables[0].getName());
		assertEquals("5", variables[0].getValue().getValueString());
		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
		assertTrue(variables[0].isConstant());
		assertTrue(!variables[0].isStatic());
		assertTrue(!variables[0].isLocal());
		assertTrue(!variables[0].isInstance());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testConstantDefinedInBothClassAndSuperclass() throws Exception {
		createSocket(new String[] { "class A", "TestConstant=5", "TestConstant2=2", "end", "class B < A", "TestConstant=6", "end", "b=B.new()", "puts 'a'" });
		runTo("test.rb", 9);
		sendRuby("v i b");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("TestConstant", variables[0].getName());
		assertEquals("6", variables[0].getValue().getValueString());
		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
		assertTrue(variables[0].isConstant());
		assertTrue(!variables[0].isStatic());
		assertTrue(!variables[0].isLocal());
		assertTrue(!variables[0].isInstance());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testVariableString() throws Exception {
		createSocket(new String[] { "stringA='XX'", "puts stringA" });
		runToLine(2);
		// Read numerical variable
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("stringA", variables[0].getName());
		assertEquals("XX", variables[0].getValue().getValueString());
		assertEquals("String", variables[0].getValue().getReferenceTypeName());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testVariableLocal() throws Exception {
		createSocket(new String[] { "class User", "def initialize(id)", "@id=id", "end", "end", "class CallClass", "def method(user)", "puts user", "end", "end", "CallClass.new.method(User.new(22))" });
		runTo("test.rb", 8);
		sendRuby("v local");
		RubyVariable[] localVariables = getVariableReader().readVariables(createStackFrame());
		assertEquals(2, localVariables.length);
		RubyVariable userVariable = localVariables[1];
		// sendRuby("v i 1 " + userVariable.getObjectId());
		sendRuby("v i " + userVariable.getObjectId());
		RubyVariable[] userVariables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, userVariables.length);
		assertEquals("@id", userVariables[0].getName());
		assertEquals("22", userVariables[0].getValue().getValueString());
		assertEquals("Fixnum", userVariables[0].getValue().getReferenceTypeName());
		assertTrue(!userVariables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testVariableInstance() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "customObject=Test2.new", "puts customObject" });
		writeFile("test2.rb", new String[] { "class Test2", "def initialize", "@y=5", "end", "def to_s", "'test'", "end", "end" });
		runTo("test2.rb", 6);
		sendRuby("frame 2 ; v i customObject");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("@y", variables[0].getName());
		assertEquals("5", variables[0].getValue().getValueString());
		assertEquals("Fixnum", variables[0].getValue().getReferenceTypeName());
		assertTrue(!variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testVariableArray() throws Exception {
		createSocket(new String[] { "array = []", "array << 1", "array << 2", "puts 'a'" });
		runTo("test.rb", 4);
		sendRuby("v local");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("array", variables[0].getName());
		assertTrue("array has children", variables[0].getValue().hasVariables());
		sendRuby("v i array");
		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
		assertEquals(2, elements.length);
		assertEquals("[0]", elements[0].getName());
		assertEquals("1", elements[0].getValue().getValueString());
		assertEquals("Fixnum", elements[0].getValue().getReferenceTypeName());
		assertEquals("array[0]", elements[0].getQualifiedName());
		sendRuby("cont");
	}

	public void testVariableHashWithStringKeys() throws Exception {
		createSocket(new String[] { "hash = Hash['a' => 'z', 'b' => 'y']", "puts 'a'" });
		runTo("test.rb", 2);
		sendRuby("v local");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("hash", variables[0].getName());
		assertTrue("hash has children", variables[0].getValue().hasVariables());
		sendRuby("v i hash");
		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
		assertEquals(2, elements.length);
		assertEquals("'a'", elements[0].getName());
		assertEquals("z", elements[0].getValue().getValueString());
		assertEquals("String", elements[0].getValue().getReferenceTypeName());
		assertEquals("hash['a']", elements[0].getQualifiedName());
		sendRuby("cont");
	}

	public void testVariableHashWithObjectKeys() throws Exception {
		createSocket(new String[] { "class KeyAndValue", "def initialize(v)", "@a=v", "end", "def to_s", "return @a.to_s", "end", "end", "hash = Hash[KeyAndValue.new(55) => KeyAndValue.new(66)]", "puts 'a'" });
		runTo("test.rb", 10);
		sendRuby("v local");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("hash", variables[0].getName());
		assertTrue("hash has children", variables[0].getValue().hasVariables());
		sendRuby("frame 1 ; v i " + variables[0].getObjectId());
		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
		assertEquals(1, elements.length);
		assertEquals("55", elements[0].getName());
		// assertEquals("z", elements[0].getValue().getValueString());
		assertEquals("KeyAndValue", elements[0].getValue().getReferenceTypeName());
		// get the value
		sendRuby("frame 1 ; v i " + elements[0].getObjectId());
		RubyVariable[] values = getVariableReader().readVariables(variables[0]);
		assertEquals(1, values.length);
		assertEquals("@a", values[0].getName());
		assertEquals("Fixnum", values[0].getValue().getReferenceTypeName());
		assertEquals("66", values[0].getValue().getValueString());
		sendRuby("cont");
	}

	public void testVariableArrayEmpty() throws Exception {
		createSocket(new String[] { "emptyArray = []", "puts 'a'" });
		runTo("test.rb", 2);
		sendRuby("v local");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("emptyArray", variables[0].getName());
		assertTrue("array does not have children", !variables[0].getValue().hasVariables());
		sendRuby("cont");
	}

	public void testVariableInstanceNested() throws Exception {
		createSocket(new String[] { "class Test", "def initialize(test)", "@privateTest = test", "end", "end", "test2 = Test.new(Test.new(nil))", "puts test2" });
		runToLine(7);
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		RubyVariable test2Variable = variables[0];
		assertEquals("test2", test2Variable.getName());
		assertEquals("test2", test2Variable.getQualifiedName());
		sendRuby("v i " + test2Variable.getQualifiedName());
		variables = getVariableReader().readVariables(test2Variable);
		assertEquals(1, variables.length);
		RubyVariable privateTestVariable = variables[0];
		assertEquals("@privateTest", privateTestVariable.getName());
		assertEquals("test2.@privateTest", privateTestVariable.getQualifiedName());
		assertTrue(privateTestVariable.getValue().hasVariables());
		sendRuby("v i " + privateTestVariable.getQualifiedName());
		variables = getVariableReader().readVariables(privateTestVariable);
		assertEquals(1, variables.length);
		RubyVariable privateTestprivateTestVariable = variables[0];
		assertEquals("@privateTest", privateTestprivateTestVariable.getName());
		assertEquals("test2.@privateTest.@privateTest", privateTestprivateTestVariable.getQualifiedName());
		assertEquals("nil", privateTestprivateTestVariable.getValue().getValueString());
		assertTrue(!privateTestprivateTestVariable.getValue().hasVariables());
		sendRuby("cont");
	}

	public void testInspect() throws Exception {
		createSocket(new String[] { "class Test", "def calc(a)", "a = a*2", "return a", "end", "end", "test=Test.new()", "a=3", "test.calc(a)" });
		runToLine(4);
		// test variable value in stack 1 (top stack frame)
		sendRuby("frame 1 ; v inspect  a*2");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals("There is one variable returned.", 1, variables.length);
		assertEquals("Result in frame 1 is 12", "12", variables[0].getValue().getValueString());
		// test variable value in stack 2 (caller stack)
		sendRuby("frame 2 ; v inspect a*2");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals("There is one variable returned.", 1, variables.length);
		assertEquals("Result in frame 2 is 6", "6", variables[0].getValue().getValueString());
		// test more complex expression
		sendRuby("frame 1 ;  v inspect Test.new().calc(5)");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals("There is one variable returned.", 1, variables.length);
		assertEquals("Result is 10", "10", variables[0].getValue().getValueString());
		sendRuby("cont");
	}

	public void testInspectTemporaryArray() throws Exception {
		createSocket(new String[] { "a=0", "puts a=2" });
		runToLine(2);
		sendRuby("v inspect  %w[a b c]");
		// this inspection will create a new temporary arrray object
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals("There is one variable returned which contains the array.", 1, variables.length);
		// the following two commands starts the garbage collector. This test
		// is somehow implementation aware. It makes sure that the objects which
		// are
		// created as a result of an expression are referenced so that they will
		// not be
		// swept away from the GC
		// TODO: actually garbage_collect does nothing, also the GC can not be
		// enabled with GC.enable()
		sendRuby("v inspect ObjectSpace.garbage_collect\\;sleep(2)");
		RubyVariable[] gcResult = getVariableReader().readVariables(createStackFrame());
		assertEquals("There is one variable returned as result of running the GC", 1, gcResult.length);
		sendRuby("v i " + variables[0].getObjectId());
		RubyVariable[] elements = getVariableReader().readVariables(variables[0]);
		assertEquals("The array contains 3 elements", 3, elements.length);
		sendRuby("cont");
	}

	public void testInspectNil() throws Exception {
		createSocket(new String[] { "puts 'dummy'", "puts 'dummy'" });
		runToLine(2);
		sendRuby("v inspect nil");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals("There is one variable returned which is nil.", 1, variables.length);
		assertEquals("nil", variables[0].getValue().getValueString());
		sendRuby("cont");
	}

	public void testSendCommandWithSpecialCharacters() throws Exception {
		// the inspect command can contain arbitrary characters
		// a %w for example can raise an error when it is directly given to
		// printf
		sendRuby("v inspect  %w[a b]");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		sendRuby("cont");
		// just do not fail
	}

	public void testInspectError() throws Exception {
		createSocket(new String[] { "puts 'test'", "puts 'test'" });
		runToLine(2);
		sendRuby("v inspect a*2");
		try {
			getVariableReader().readVariables(createStackFrame());
			fail("RubyProcessingException not thrown.");
		} catch (RubyProcessingException e) {
			assertNotNull(e.getMessage());
			assertFalse(e.getMessage().indexOf("Timeout") > -1) ;
			sendRuby("cont");
		}
	}
	public void testInspectTimeout() throws Exception {
		createSocket(new String[] { "puts 'test'", "puts 'test'" });
		runToLine(2);
		// timeout is 10 seconds
		sendRuby("v inspect sleep(15)");
		try {
			getVariableReader().readVariables(createStackFrame());
			fail("Timeout did not occur.");
		} catch (RubyProcessingException e) {
			assertTrue(e.getMessage().indexOf("Timeout") > -1) ;
			sendRuby("cont");
		}
	}
	
	
	public void testEvalError() throws Exception {
		createSocket(new String[] { "puts 'test'", "puts 'test'" });
		runToLine(2);
		sendRuby("eval unknown_");
		try {
			getEvalReader().readEvalResult();
		} catch (RubyProcessingException e) {
			assertNotNull(e.getMessage());
			sendRuby("cont");
			return;
		}
		fail("RubyProcessingException not thrown.");
	}

	public void testStaticVariableInstanceNested() throws Exception {
		createSocket(new String[] { "class TestStatic", "def initialize(no)", "@no = no", "end", "@@staticVar=TestStatic.new(2)", "end", "test = TestStatic.new(1)", "puts test" });
		runToLine(8);
		sendRuby("v i test.@@staticVar");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(2, variables.length);
		assertEquals("@no", variables[0].getName());
		assertEquals("2", variables[0].getValue().getValueString());
		assertEquals("@@staticVar", variables[1].getName());
		assertTrue("2", variables[1].getValue().hasVariables());
	}

	public void testVariablesInFrames() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "y=5", "Test2.new().test()" });
		writeFile("test2.rb", new String[] { "class Test2", "def test", "y=6", "puts y", "end", "end" });
		runTo("test2.rb", 4);
		sendRuby("v l");
		RubyVariable[] variables = getVariableReader().readVariables(createStackFrame());
		// there are 2 variables self and y
		assertEquals(2, variables.length);
		// the variables are sorted: self = variables[0], y = variables[1]
		assertEquals("y", variables[1].getName());
		assertEquals("6", variables[1].getValue().getValueString());
		sendRuby("frame 1 ; v l");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(2, variables.length);
		assertEquals("y", variables[1].getName());
		assertEquals("6", variables[1].getValue().getValueString());
		sendRuby("frame 2 ; v l");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("y", variables[0].getName());
		assertEquals("5", variables[0].getValue().getValueString());
		// 20 is out of range, then the top level (the one with the highest
		// number)
		// will be used, in this case 2
		sendRuby("frame 20 ; v l");
		variables = getVariableReader().readVariables(createStackFrame());
		assertEquals(1, variables.length);
		assertEquals("y", variables[0].getName());
		assertEquals("5", variables[0].getValue().getValueString());
	}

	public void testFrames() throws Exception {
		createSocket(new String[] { "require 'test2.rb'", "test = Test2.new()", "test.print()", "test.print()" });
		writeFile("test2.rb", new String[] { "class Test2", "def print", "puts 'Test2.print'", "end", "end" });
		runTo("test2.rb", 3);
		sendRuby("b test.rb:4");
		getBreakpointAddedReader().readBreakpointNo();
		sendRuby("w");
		RubyThread thread = new RubyThread(null, 0, "run");
		getFramesReader().readFrames(thread);
		assertEquals(2, thread.getStackFrames().length);
		RubyStackFrame frame1 = (RubyStackFrame) thread.getStackFrames()[0];
		assertEquals(getOSIndependent(getTmpDir() + "test2.rb"), frame1.getFileName());
		assertEquals(1, frame1.getIndex());
		assertEquals(3, frame1.getLineNumber());
		RubyStackFrame frame2 = (RubyStackFrame) thread.getStackFrames()[1];
		assertEquals(getOSIndependent(getTmpDir() + "test.rb"), frame2.getFileName());
		assertEquals(2, frame2.getIndex());
		assertEquals(3, frame2.getLineNumber());
		sendRuby("cont");
		getSuspensionReader().readSuspension();
		sendRuby("w");
		getFramesReader().readFrames(thread);
		assertEquals(1, thread.getStackFramesSize());
		sendRuby("cont");
	}

	public void testFramesWhenThreadSpawned() throws Exception {
		createSocket(new String[] { "def startThread", "Thread.new() {  a = 5  }", "end", "def calc", "5 + 5", "end", "startThread()", "calc()" });
		runTo("test.rb", 5);
		RubyThread thread = new RubyThread(null, 0, "run");
		sendRuby("w");
		getFramesReader().readFrames(thread);
		assertEquals(2, thread.getStackFramesSize());
	}

	public void testThreadFramesAndVariables() throws Exception {
		createSocket(new String[] { "Thread.new {", "a=5", "x=6", "puts 'x'", "}", "b=10", "b=11" });
		setBreakpoint(7) ;
		runToLine(3);
		sendRuby("th l");
		ThreadInfo[] threads = getThreadInfoReader().readThreads();

		sendRuby("th resume 1");
		getSuspensionReader().readSuspension();
		getSuspensionReader().readSuspension();
		// the main thread and the "puts 'a'" - thread are active
		sendRuby("th l");
		threads = getThreadInfoReader().readThreads();
		assertEquals(2, threads.length);

		sendRuby("th " + threads[0].getId() + " ; w ");
		RubyStackFrame[] stackFrames = getFramesReader().readFrames(new RubyThread(null, 1, "run"));
		assertEquals(1, stackFrames.length);
		assertEquals(7, stackFrames[0].getLineNumber());
		sendRuby("th " + threads[0].getId() + " ; v l");
		RubyVariable[] variables = getVariableReader().readVariables(stackFrames[0]);
		assertEquals(1, variables.length);
		assertEquals("b", variables[0].getName());
		sendRuby("th " + threads[1].getId() + " ; w");
		stackFrames = getFramesReader().readFrames(new RubyThread(null, 1, "run"));
		assertEquals(1, stackFrames.length);
		assertEquals(3, stackFrames[0].getLineNumber());
		sendRuby("th " + threads[1].getId() + " ; v l");
		variables = getVariableReader().readVariables(stackFrames[0]);
		assertEquals("a", variables[0].getName());
		assertEquals("b", variables[1].getName());
		// there is a third variable 'x' for ruby 1.8.0
		sendRuby("next");
		getSuspensionReader().readSuspension();
		sendRuby("th " + threads[1].getId() + " ; v l");
		variables = getVariableReader().readVariables(stackFrames[0]);
		assertEquals(3, variables.length);
		assertEquals("a", variables[0].getName());
		assertEquals("b", variables[1].getName());
		assertEquals("x", variables[2].getName());

	}

}
