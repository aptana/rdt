package org.rubypeople.rdt.debug.core.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;



public class NonBlockingSocketReader {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader reader;
    private Process process;
    private OutputRedirectorThread rubyStdoutRedirectorThread;

    public void setUp() throws Exception {
        String binDir = this.getClass().getResource("/").getFile();
        if (binDir.startsWith("/") && File.separatorChar == '\\') {
            binDir = binDir.substring(1);
        }
        String cmd = "ruby " + binDir + "../ruby/testNonBlockingSocketReader.rb";
        System.out.println("Starting: " + cmd);
        process = Runtime.getRuntime().exec(cmd);
        rubyStdoutRedirectorThread = new OutputRedirectorThread(process.getInputStream());
        rubyStdoutRedirectorThread.start();
        rubyStdoutRedirectorThread = new OutputRedirectorThread(process.getErrorStream());
        rubyStdoutRedirectorThread.start();
        Thread.sleep(3500);
        socket = new Socket("localhost", 12134);
        out = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    protected void tearDown() throws Exception {
        socket.close();
        process.destroy();
        rubyStdoutRedirectorThread.join();
    }


    public void printBaseOperationsPerSecond() throws Exception {
        int operationsPerSecond = Integer.parseInt(reader.readLine());
        System.out.println("Operations Per Second (base): " + operationsPerSecond);
    }

    public void testOperationsPerSecond(double sleepTime, double blockingTime) throws Exception {
		DecimalFormat format = new DecimalFormat() ;
		format.setMaximumFractionDigits(10) ;
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US)) ;
    	out.println("sleepTime " + format.format(sleepTime)) ; 
    	out.println("blockingTime " + format.format(blockingTime)) ;
        out.println("startCalculation");
        Thread.sleep(2000);
        out.println("stopCalculation");
        Thread.sleep(500);
        int operationsPerSecond = Integer.parseInt(reader.readLine());
        System.out.println("sleeptime/blockingTime/OperationsPerSecond: " + format.format(sleepTime) + "/" + format.format(blockingTime) + "/" + operationsPerSecond);
    }

	public static void main(String[] args) throws Exception {
		NonBlockingSocketReader test = new NonBlockingSocketReader();
		test.setUp();
		test.printBaseOperationsPerSecond() ;
		test.testOperationsPerSecond(0.1, 1);
		test.testOperationsPerSecond(0.1, 0.1);
		test.testOperationsPerSecond(0.1, 0.001);
		test.testOperationsPerSecond(0.1, 0.000001);
		test.testOperationsPerSecond(0.1, 0.0);
		test.testOperationsPerSecond(0.5, 1);
		test.testOperationsPerSecond(0.5, 0.1);
		test.testOperationsPerSecond(0.5, 0.001);
		test.testOperationsPerSecond(0.5, 0.000001);
		test.testOperationsPerSecond(0.5, 0.0);
		
		test.tearDown();

	}


}
