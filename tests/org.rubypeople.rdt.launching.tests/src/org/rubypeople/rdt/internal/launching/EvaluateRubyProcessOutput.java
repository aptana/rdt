package org.rubypeople.rdt.internal.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/*
  There is a different behaviour, when a ruby application ist
started from commandlined compared to a ruby application started with RDT.
E.g. there must be additional STDOUT.flush commands for RDT started applications
in order to achieve same bahvior.
*/

public class EvaluateRubyProcessOutput implements Runnable {
	// Allocate 1K buffers for Input and Error Streams..
	private byte[] inBuffer = new byte[1024];
	private byte[] errBuffer = new byte[1024];
	// Declare internal variables we will need..
	private Process process;
	private InputStream pErrorStream;
	private InputStream pInputStream;
	private OutputStream pOutputStream;
	private PrintWriter outputWriter;
	private Thread inReadThread;
	private Thread errReadThread;
	
	public EvaluateRubyProcessOutput(Process p) {
		// Save variables..
		process = p;		
		// Get the streams..
		pErrorStream = process.getErrorStream();
		pInputStream = process.getInputStream();
		pOutputStream = process.getOutputStream();
		// Create a PrintWriter on top of the output stream..
		// Create the threads and start them..
		inReadThread = new Thread(this);
		errReadThread = new Thread(this);
		outputWriter = new PrintWriter(pOutputStream, true);
		new Thread() {
			public void run() {
				try {
					// This Thread just waits for the process to
					// end and notifies the handler..
					process.waitFor() ;
					System.out.println("Process endend.") ;
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}.start();
		inReadThread.start();
		errReadThread.start();
	}
	private void processNewInput(String input) {
		// Handle process new input..
		//handler.processNewInput(input);
		System.out.println(input) ;
	}
	private void processNewError(String error) {
		// Handle process new error..
		//handler.processNewError(error);
	}
	// Run the command and return the ExecHelper wrapper object..

	// Send the output string through the print writer..
	public void sendOutput(String output) {
		outputWriter.println(output);
	}
	public void run() {
		// Are we on the InputRead Thread?
		if (inReadThread == Thread.currentThread()) {
			try {
				// Read the InputStream in a loop until we find no
				// more bytes to read..
				for (int i = 0; i > -1; i = pInputStream.read(inBuffer)) {
					// We have a new segment of input, so process
					// it as a String..
					processNewInput(new String(inBuffer, 0, i));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			// Are we on the ErrorRead Thread?
		} else if (errReadThread == Thread.currentThread()) {
			try {
				// Read the ErrorStream in a loop until we find no
				// more bytes to read..
				for (int i = 0; i > -1; i = pErrorStream.read(errBuffer)) {
					// We have a new segment of error, so process
					// it as a String..
					processNewError(new String(errBuffer, 0, i));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		/* file D:\\Temp\\test.rb:
		class Hello
		  attr_reader :msg
		  def initialize
		    @msg = "Hello, World\n"
		  end
		end

		h = Hello.new
		puts h.msg
		print "Press RETURN"
		STDOUT.flush
		input = $stdin.gets
		puts "You entered #{input}"
		*/
		Process p = Runtime.getRuntime().exec("D:\\ruby-1.8.0\\ruby\\bin\\ruby.exe D:\\Temp\\test.rb");
		EvaluateRubyProcessOutput erpo = new EvaluateRubyProcessOutput(p) ;
		String in = new BufferedReader(new InputStreamReader(System.in)).readLine() ;
		erpo.sendOutput(in) ;
		
	}
}