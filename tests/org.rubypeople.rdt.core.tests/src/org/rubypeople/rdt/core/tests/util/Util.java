package org.rubypeople.rdt.core.tests.util;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class Util {
	
	// Trace for delete operation
	/*
	 * Maximum time wasted repeating delete operations while running RDT/Core tests.
	 */
	private static int DELETE_MAX_TIME = 0;
	/**
	 * Trace deletion operations while running RDT/Core tests.
	 */
	public static boolean DELETE_DEBUG = false;
	/**
	 * Maximum of time in ms to wait in deletion operation while running RDT/Core tests.
	 * Default is 10 seconds. This number cannot exceed 1 minute (ie. 60000).
	 * <br>
	 * To avoid too many loops while waiting, the ten first ones are done waiting
	 * 10ms before repeating, the ten loops after are done waiting 100ms and
	 * the other loops are done waiting 1s...
	 */
	public static int DELETE_MAX_WAIT = 10000;
	
	public static String convertToIndependantLineDelimiter(String source) {
		if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = source.length(); i < length; i++) {
			char car = source.charAt(i);
			if (car == '\r') {
				buffer.append('\n');
				if (i < length-1 && source.charAt(i+1) == '\n') {
					i++; // skip \n after \r
				}
			} else {
				buffer.append(car);
			}
		}
		return buffer.toString();
	}

	/**
	 * Delete a file or directory and insure that the file is no longer present
	 * on file system. In case of directory, delete all the hierarchy underneath.
	 * 
	 * @param resource The resource to delete
	 * @return true iff the file was really delete, false otherwise
	 */
	public static boolean delete(IResource resource) {
		try {
			resource.delete(true, null);
			if (isResourceDeleted(resource)) {
				return true;
			}
		}
		catch (CoreException e) {
			//	skip
		}
		return waitUntilResourceDeleted(resource);
	}
	
	/**
	 * Returns whether a resource is really deleted or not.
	 * Does not only rely on {@link IResource#isAccessible()} method but also
	 * look if it's not in its parent children {@link #getParentChildResource(IResource)}.
	 * 
	 * @param resource The resource to test if deleted
	 * @return true if the resource is not accessible and was not found in its parent children.
	 */
	public static boolean isResourceDeleted(IResource resource) {
		return !resource.isAccessible() && getParentChildResource(resource) == null;
	}
	
	/**
	 * Returns parent's child resource matching the given resource or null if not found.
	 * 
	 * @param resource The searched file in parent
	 * @return The parent's child matching the given file or null if not found.
	 */
	private static IResource getParentChildResource(IResource resource) {
		IContainer parent = resource.getParent();
		if (parent == null || !parent.exists()) return null;
		try {
			IResource[] members = parent.members();
			int length = members ==null ? 0 : members.length;
			if (length > 0) {
				for (int i=0; i<length; i++) {
					if (members[i] == resource) {
						return members[i];
					} else if (members[i].equals(resource)) {
						return members[i];
					} else if (members[i].getFullPath().equals(resource.getFullPath())) {
						return members[i];
					}
				}
			}
		}
		catch (CoreException ce) {
			// skip
		}
		return null;
	}
	
	/**
	 * Wait until a resource is _really_ deleted on file system.
	 * 
	 * @param resource Deleted resource
	 * @return true if the file was finally deleted, false otherwise
	 */
	private static boolean waitUntilResourceDeleted(IResource resource) {
		File file = resource.getLocation().toFile();
		if (DELETE_DEBUG) {
			System.out.println();
			System.out.println("WARNING in test: "+getTestName());
			System.out.println("	- problems occured while deleting resource "+resource);
			printRdtCoreStackTrace(null, 1);
			printFileInfo(file.getParentFile(), 1, -1); // display parent with its children
			System.out.print("	- wait for ("+DELETE_MAX_WAIT+"ms max): ");
		}
		int count = 0;
		int delay = 10; // ms
		int maxRetry = DELETE_MAX_WAIT / delay;
		int time = 0;
		while (count < maxRetry) {
			try {
				count++;
				Thread.sleep(delay);
				time += delay;
				if (time > DELETE_MAX_TIME) DELETE_MAX_TIME = time;
				if (DELETE_DEBUG) System.out.print('.');
				if (resource.isAccessible()) {
					try {
						resource.delete(true, null);
						if (isResourceDeleted(resource) && isFileDeleted(file)) {
							// SUCCESS
							if (DELETE_DEBUG) {
								System.out.println();
								System.out.println("	=> resource really removed after "+time+"ms (max="+DELETE_MAX_TIME+"ms)");
								System.out.println();
							}
							return true;
						}
					}
					catch (CoreException e) {
						//	skip
					}
				}
				if (isResourceDeleted(resource) && isFileDeleted(file)) {
					// SUCCESS
					if (DELETE_DEBUG) {
						System.out.println();
						System.out.println("	=> resource disappeared after "+time+"ms (max="+DELETE_MAX_TIME+"ms)");
						System.out.println();
					}
					return true;
				}
				// Increment waiting delay exponentially
				if (count >= 10 && delay <= 100) {
					count = 1;
					delay *= 10;
					maxRetry = DELETE_MAX_WAIT / delay;
					if ((DELETE_MAX_WAIT%delay) != 0) {
						maxRetry++;
					}
				}
			}
			catch (InterruptedException ie) {
				break; // end loop
			}
		}
		if (!DELETE_DEBUG) {
			System.out.println();
			System.out.println("WARNING in test: "+getTestName());
			System.out.println("	- problems occured while deleting resource "+resource);
			printRdtCoreStackTrace(null, 1);
			printFileInfo(file.getParentFile(), 1, -1); // display parent with its children
		}
		System.out.println();
		System.out.println("	!!! ERROR: "+resource+" was never deleted even after having waited "+DELETE_MAX_TIME+"ms!!!");
		System.out.println();
		return false;
	}
	
	/**
	 * Returns the test name from stack elements info.
	 * 
	 * @return The name of the test currently running
	 */
	private static String getTestName() {
		StackTraceElement[] elements = new Exception().getStackTrace();
		int idx = 0, length=elements.length;
		while (idx<length && !elements[idx++].getClassName().startsWith("org.rubypeoplee.rdt")) {
			// loop until RDT/Core class appears in the stack
		}
		if (idx<length) {
			StackTraceElement testElement = null;
			while (idx<length && elements[idx].getClassName().startsWith("org.rubypeople.rdt")) {
				testElement = elements[idx++];
			}
			if (testElement != null) {
				return testElement.getClassName() + " - " + testElement.getMethodName();
			}
		}
		return "?";
	}

	/**
	 * Returns whether a file is really deleted or not.
	 * Does not only rely on {@link File#exists()} method but also
	 * look if it's not in its parent children {@link #getParentChildFile(File)}.
	 * 
	 * @param file The file to test if deleted
	 * @return true if the file does not exist and was not found in its parent children.
	 */
	public static boolean isFileDeleted(File file) {
		return !file.exists() && getParentChildFile(file) == null;
	}
	
	private static File getParentChildFile(File file) {
		File parent = file.getParentFile();
		if (parent == null || !parent.exists()) return null;
		File[] files = parent.listFiles();
		int length = files==null ? 0 : files.length;
		if (length > 0) {
			for (int i=0; i<length; i++) {
				if (files[i] == file) {
					return files[i];
				} else if (files[i].equals(file)) {
					return files[i];
				} else if (files[i].getPath().equals(file.getPath())) {
					return files[i];
				}
			}
		}
		return null;
	}
	
	/**
	 * Print given file information with specified indentation.
	 * These information are:<ul>
	 * 	<li>read {@link File#canRead()}</li>
	 * 	<li>write {@link File#canWrite()}</li>
	 * 	<li>exists {@link File#exists()}</li>
	 * 	<li>is file {@link File#isFile()}</li>
	 * 	<li>is directory {@link File#isDirectory()}</li>
	 * 	<li>is hidden {@link File#isHidden()}</li>
	 * </ul>
	 * May recurse several level in parents hierarchy.
	 * May also display children, but then will not recusre in parent
	 * hierarchy to avoid infinite loop...
	 * 
	 * @param file The file to display information
	 * @param indent Number of tab to print before the information
	 * @param recurse Display also information on <code>recurse</code>th parents in hierarchy.
	 * 	If negative then display children information instead.
	 */
	private static void printFileInfo(File file, int indent, int recurse) {
		String tab = "";
		for (int i=0; i<indent; i++) tab+="\t";
		System.out.print(tab+"- "+file.getName()+" file info: ");
		String sep = "";
		if (file.canRead()) {
			System.out.print("read");
			sep = ", ";
		}
		if (file.canWrite()) {
			System.out.print(sep+"write");
			sep = ", ";
		}
		if (file.exists()) {
			System.out.print(sep+"exist");
			sep = ", ";
		}
		if (file.isDirectory()) {
			System.out.print(sep+"dir");
			sep = ", ";
		}
		if (file.isFile()) {
			System.out.print(sep+"file");
			sep = ", ";
		}
		if (file.isHidden()) {
			System.out.print(sep+"hidden");
			sep = ", ";
		}
		System.out.println();
		File[] files = file.listFiles();
		int length = files==null ? 0 : files.length;
		if (length > 0) {
			boolean children = recurse < 0;
			System.out.print(tab+"	+ children: ");
			if (children) System.out.println();
			for (int i=0; i<length; i++) {
				if (children) { // display children
					printFileInfo(files[i], indent+2, -1);
				} else {
					if (i>0) System.out.print(", ");
					System.out.print(files[i].getName());
					if (files[i].isDirectory()) System.out.print("[dir]");
					else if (files[i].isFile()) System.out.print("[file]");
					else System.out.print("[?]");
				}
			}
			if (!children) System.out.println();
		}
		if (recurse > 0) {
			File parent = file.getParentFile();
			if (parent != null) printFileInfo(parent, indent+1, recurse-1);
		}
	}
	
	/**
	 * Print stack trace with only RDT/Core elements.
	 * 
	 * @param exception Exception of the stack trace. May be null, then a fake exception is used.
	 * @param indent Number of tab to display before the stack elements to display.
	 */
	private static void printRdtCoreStackTrace(Exception exception, int indent) {
		String tab = "";
		for (int i=0; i<indent; i++) tab+="\t";
		StackTraceElement[] elements = (exception==null?new Exception():exception).getStackTrace();
		int idx = 0, length=elements.length;
		while (idx<length && !elements[idx++].getClassName().startsWith("org.rubypeople.rdt")) {
			// loop until JDT/Core class appears in the stack
		}
		if (idx<length) {
			System.out.print(tab+"- stack trace");
			if (exception == null)
				System.out.println(":");
			else
				System.out.println(" for exception "+exception+":");
			while (idx<length && elements[idx].getClassName().startsWith("org.rubypeople.rdt")) {
				StackTraceElement testElement = elements[idx++];
				System.out.println(tab+"	-> "+testElement);
			}
		} else {
			exception.printStackTrace(System.out);
		}
	}

	public static String displayString(String inputString, int indent) {
		return displayString(inputString, indent, false);
	}
	
	public static String displayString(String inputString){
		return displayString(inputString, 0);
	}
	
	public static String displayString(String inputString, int indent, boolean shift) {
		if (inputString == null)
			return "null";
		int length = inputString.length();
		StringBuffer buffer = new StringBuffer(length);
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
		for (int i = 0; i < indent; i++) buffer.append("\t");
		if (shift) indent++;
		buffer.append("\"");
		while (tokenizer.hasMoreTokens()){

			String token = tokenizer.nextToken();
			if (token.equals("\r")) {
				buffer.append("\\r");
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("\n")) {
						buffer.append("\\n");
						if (tokenizer.hasMoreTokens()) {
							buffer.append("\" + \n");
							for (int i = 0; i < indent; i++) buffer.append("\t");
							buffer.append("\"");
						}
						continue;
					}
					buffer.append("\" + \n");
					for (int i = 0; i < indent; i++) buffer.append("\t");
					buffer.append("\"");
				} else {
					continue;
				}
			} else if (token.equals("\n")) {
				buffer.append("\\n");
				if (tokenizer.hasMoreTokens()) {
					buffer.append("\" + \n");
					for (int i = 0; i < indent; i++) buffer.append("\t");
					buffer.append("\"");
				}
				continue;
			}	

			StringBuffer tokenBuffer = new StringBuffer();
			for (int i = 0; i < token.length(); i++){ 
				char c = token.charAt(i);
				switch (c) {
					case '\r' :
						tokenBuffer.append("\\r");
						break;
					case '\n' :
						tokenBuffer.append("\\n");
						break;
					case '\b' :
						tokenBuffer.append("\\b");
						break;
					case '\t' :
						tokenBuffer.append("\t");
						break;
					case '\f' :
						tokenBuffer.append("\\f");
						break;
					case '\"' :
						tokenBuffer.append("\\\"");
						break;
					case '\'' :
						tokenBuffer.append("\\'");
						break;
					case '\\' :
						tokenBuffer.append("\\\\");
						break;
					default :
						tokenBuffer.append(c);
				}
			}
			buffer.append(tokenBuffer.toString());
		}
		buffer.append("\"");
		return buffer.toString();
	}

	/**
	 * Delete a file or directory and insure that the file is no longer present
	 * on file system. In case of directory, delete all the hierarchy underneath.
	 * 
	 * @param file The file or directory to delete
	 * @return true iff the file was really delete, false otherwise
	 */
	public static boolean delete(File file) {
		// flush all directory content
		if (file.isDirectory()) {
			flushDirectoryContent(file);
		}
		// remove file
		file.delete();
		if (isFileDeleted(file)) {
			return true;
		}
		return waitUntilFileDeleted(file);
	}
	
	/**
	 * Flush content of a given directory (leaving it empty),
	 * no-op if not a directory.
	 */
	public static void flushDirectoryContent(File dir) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for (int i = 0, max = files.length; i < max; i++) {
			delete(files[i]);
		}
	}
	
	/**
	 * Wait until the file is _really_ deleted on file system.
	 * 
	 * @param file Deleted file
	 * @return true if the file was finally deleted, false otherwise
	 */
	private static boolean waitUntilFileDeleted(File file) {
		if (DELETE_DEBUG) {
			System.out.println();
			System.out.println("WARNING in test: "+getTestName());
			System.out.println("	- problems occured while deleting "+file);
			printRdtCoreStackTrace(null, 1);
			printFileInfo(file.getParentFile(), 1, -1); // display parent with its children
			System.out.print("	- wait for ("+DELETE_MAX_WAIT+"ms max): ");
		}
		int count = 0;
		int delay = 10; // ms
		int maxRetry = DELETE_MAX_WAIT / delay;
		int time = 0;
		while (count < maxRetry) {
			try {
				count++;
				Thread.sleep(delay);
				time += delay;
				if (time > DELETE_MAX_TIME) DELETE_MAX_TIME = time;
				if (DELETE_DEBUG) System.out.print('.');
				if (file.exists()) {
					if (file.delete()) {
						// SUCCESS
						if (DELETE_DEBUG) {
							System.out.println();
							System.out.println("	=> file really removed after "+time+"ms (max="+DELETE_MAX_TIME+"ms)");
							System.out.println();
						}
						return true;
					}
				}
				if (isFileDeleted(file)) {
					// SUCCESS
					if (DELETE_DEBUG) {
						System.out.println();
						System.out.println("	=> file disappeared after "+time+"ms (max="+DELETE_MAX_TIME+"ms)");
						System.out.println();
					}
					return true;
				}
				// Increment waiting delay exponentially
				if (count >= 10 && delay <= 100) {
					count = 1;
					delay *= 10;
					maxRetry = DELETE_MAX_WAIT / delay;
					if ((DELETE_MAX_WAIT%delay) != 0) {
						maxRetry++;
					}
				}
			}
			catch (InterruptedException ie) {
				break; // end loop
			}
		}
		if (!DELETE_DEBUG) {
			System.out.println();
			System.out.println("WARNING in test: "+getTestName());
			System.out.println("	- problems occured while deleting "+file);
			printRdtCoreStackTrace(null, 1);
			printFileInfo(file.getParentFile(), 1, -1); // display parent with its children
		}
		System.out.println();
		System.out.println("	!!! ERROR: "+file+" was never deleted even after having waited "+DELETE_MAX_TIME+"ms!!!");
		System.out.println();
		return false;
	}
}
