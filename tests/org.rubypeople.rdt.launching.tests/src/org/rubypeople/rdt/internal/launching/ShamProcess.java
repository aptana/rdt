/**
 * 
 */
package org.rubypeople.rdt.internal.launching;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ShamProcess extends Process {

	public void destroy() {
	}

	public int exitValue() {
		return 0;
	}

	public InputStream getErrorStream() {
        return new ByteArrayInputStream(new byte[0]);
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream(1024);
	}

	public int waitFor() throws InterruptedException {
		return 0;
	}

}