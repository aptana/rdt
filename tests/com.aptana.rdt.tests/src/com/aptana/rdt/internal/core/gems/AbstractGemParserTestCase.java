package com.aptana.rdt.internal.core.gems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rubypeople.rdt.core.util.Util;

import com.aptana.rdt.AptanaRDTTests;

public abstract class AbstractGemParserTestCase extends TestCase
{

	public AbstractGemParserTestCase()
	{
		super();
	}

	public AbstractGemParserTestCase(String name)
	{
		super(name);
	}

	protected abstract IGemParser getParser();

	protected String getContents(String path)
	{
		String result = tryResourceAsStream(path);
		if (result != null)
			return result;

		File file = grabFile(path);
		if (file == null)
			fail("Unable to grab contents of " + path);

		return readFile(file);
	}

	private String readFile(File file)
	{
		BufferedReader reader = null;
		StringBuffer buffer;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			buffer = new StringBuffer();
			while ((line = reader.readLine()) != null)
			{
				buffer.append(line);
				buffer.append("\n");
			}
			buffer.deleteCharAt(buffer.length() - 1); // delete last newline
			return buffer.toString();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		return null;
	}

	private File grabFile(String path)
	{
		File file = null;
		try
		{
			file = AptanaRDTTests.getFileInPlugin(new Path(path));
		}
		catch (Throwable e)
		{
			// We're not running as plugin test
			File dir = new File("..");
			try
			{
				file = new File(dir.getCanonicalFile(), "com.aptana.rdt.tests" + File.separator + path);
			}
			catch (IOException e1)
			{
				// ignore
			}
		}
		return file;
	}

	private String tryResourceAsStream(String path)
	{
		try
		{
			IPath thing = new Path(path);
			String fileName = thing.lastSegment();
			InputStream stream = this.getClass().getResourceAsStream(fileName);
			if (stream != null)
			{
				return new String(Util.getInputStreamAsCharArray(stream, -1, null));
			}
		}
		catch (IOException e)
		{
			// ignore
		}
		return null;
	}

}