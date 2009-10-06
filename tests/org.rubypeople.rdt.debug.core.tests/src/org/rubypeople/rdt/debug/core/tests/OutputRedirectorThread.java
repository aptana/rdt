package org.rubypeople.rdt.debug.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OutputRedirectorThread extends Thread {
	private InputStream inputStream;
	private String lastLine = "No output." ;
	public OutputRedirectorThread(InputStream aInputStream) {
		inputStream = aInputStream;
	}

	public void run() {
		System.out.println("OutputRedirectorThread started.");
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line ;
		try {
			while ((line = br.readLine()) != null) {
				System.out.println("RUBY: " + line);
				lastLine = line ;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("OutputRedirectorThread stopped.");
	}
	
	public String getLastLine() {
		return lastLine ;
	}
}
