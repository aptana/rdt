package org.rubypeople.rdt.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.rubypeople.rdt.core.util.Util.Displayable;

public class UtilTest extends TestCase
{

	public void testGetFileByteContent() throws Exception
	{
		byte[] content = new byte[] { 0, 123 };
		File file = File.createTempFile("rdt_core_util_test_byte", "txt");
		try
		{
			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream(file);
				out.write(content);
			}
			finally
			{
				if (out != null)
					out.close();
			}
			assertArrayContentsEquals(content, Util.getFileByteContent(file));
		}
		finally
		{
			file.delete();
		}
	}

	private void assertArrayContentsEquals(byte[] expected, byte[] actual)
	{
		if (expected == null)
		{
			assertNull(actual);
			return;
		}
		assertEquals(expected.length, actual.length);
		int length = expected.length;
		for (int i = 0; i < length; i++)
		{
			assertEquals(expected[i], actual[i]);
		}
	}

	private void assertArrayContentsEquals(char[] expected, char[] actual)
	{
		if (expected == null)
		{
			assertNull(actual);
			return;
		}
		assertEquals(expected.length, actual.length);
		int length = expected.length;
		for (int i = 0; i < length; i++)
		{
			assertEquals(expected[i], actual[i]);
		}
	}

	public void testToStringObjectArrayDisplayable()
	{
		assertEquals("1, nil, 2", Util.toString(new Object[] { 1, null, 2 }, new Displayable()
		{

			public String displayString(Object o)
			{
				if (o == null)
					return "nil";
				return o.toString();
			}

		}));
	}

	public void testToStringObjectArray()
	{
		assertEquals("1, 2, 3", Util.toString(new Object[] { 1, 2, 3 }));
		assertEquals("1, null, 3", Util.toString(new Object[] { 1, null, 3 }));
		assertEquals("", Util.toString(null));
		assertEquals("1, string, true, 0.3", Util.toString(new Object[] { 1, "string", true, 0.3f }));
	}

	public void testGetFileCharContent() throws Exception
	{
		char[] content = new char[] { '1', 'a' };
		File file = File.createTempFile("rdt_core_util_test_char", "txt");
		try
		{
			FileWriter out = null;
			try
			{
				out = new FileWriter(file);
				out.write(content);
			}
			finally
			{
				if (out != null)
					out.close();
			}
			assertArrayContentsEquals(content, Util.getFileCharContent(file, null));
		}
		finally
		{
			file.delete();
		}
	}

	public void testCamelCaseToUnderscores()
	{
		assertEquals("this_method", Util.camelCaseToUnderscores("thisMethod"));
		assertEquals("this_method", Util.camelCaseToUnderscores("ThisMethod"));
		assertEquals("self", Util.camelCaseToUnderscores("self"));
	}

	public void testUnderscoresToCamelCase()
	{
		assertEquals("ThisMethod", Util.underscoresToCamelCase("this_method"));
		assertEquals("Self", Util.underscoresToCamelCase("self"));
	}

	public void testIsOperator()
	{
		assertTrue(Util.isOperator("+"));
		assertTrue(Util.isOperator("/="));
		assertTrue(Util.isOperator("/"));
		assertTrue(Util.isOperator("*="));
		assertTrue(Util.isOperator("**"));
		assertTrue(Util.isOperator("*"));
		assertTrue(Util.isOperator("-="));
		assertTrue(Util.isOperator("="));
		assertTrue(Util.isOperator("+="));
		assertTrue(Util.isOperator("-"));
		assertTrue(Util.isOperator(">>"));
		assertTrue(Util.isOperator("<<"));
		assertTrue(Util.isOperator("&"));
		assertTrue(Util.isOperator("|"));
		assertTrue(Util.isOperator("&&"));
		assertTrue(Util.isOperator("||"));
		assertTrue(Util.isOperator("||="));
		assertTrue(Util.isOperator(">"));
		assertTrue(Util.isOperator("<"));
		assertTrue(Util.isOperator("<="));
		assertTrue(Util.isOperator(">="));
		assertTrue(Util.isOperator("<=>"));
		assertTrue(Util.isOperator("=="));
		assertTrue(Util.isOperator("==="));
		assertTrue(Util.isOperator("!="));
		assertTrue(Util.isOperator("=~"));
		assertTrue(Util.isOperator("!~"));
		assertTrue(Util.isOperator(".."));
		assertTrue(Util.isOperator("..."));
		assertTrue(Util.isOperator("^"));
		assertTrue(Util.isOperator("."));
		assertTrue(Util.isOperator("%"));
		assertTrue(Util.isOperator("[]"));
		assertTrue(Util.isOperator("!"));
		assertTrue(Util.isOperator("~"));

		assertFalse(Util.isOperator(","));
		assertFalse(Util.isOperator("chris"));
		assertFalse(Util.isOperator("null"));
		assertFalse(Util.isOperator("1"));
	}

	public void testIsKeyword()
	{
		assertTrue(Util.isKeyword("true"));
		assertTrue(Util.isKeyword("false"));
		assertTrue(Util.isKeyword("def"));
		assertTrue(Util.isKeyword("end"));
		assertTrue(Util.isKeyword("retry"));
		assertTrue(Util.isKeyword("return"));
		assertTrue(Util.isKeyword("class"));
		assertTrue(Util.isKeyword("module"));
		assertTrue(Util.isKeyword("self"));
		assertTrue(Util.isKeyword("super"));
		assertTrue(Util.isKeyword("nil"));

		assertFalse(Util.isKeyword(null));
		assertFalse(Util.isKeyword("chris"));
		assertFalse(Util.isKeyword("test"));
	}

	public void testFindFileWithOptionalSuffix() throws Exception
	{
		List<File> filesToCleanUp = new ArrayList<File>();
		String tmpDir = System.getProperty("java.io.tmpdir");
		File dirToUse = new File(tmpDir, "coreUtilTest");
		dirToUse.mkdir();
		File exeFile = new File(dirToUse, "ruby1.9.exe");
		filesToCleanUp.add(exeFile);
		File binFile = new File(dirToUse, "ruby1.6");
		filesToCleanUp.add(binFile);
		File txtFile = new File(dirToUse, "ruby18.txt");
		filesToCleanUp.add(txtFile);
		for (File file : filesToCleanUp)
		{
			file.createNewFile();
		}
		filesToCleanUp.add(dirToUse);
		try
		{
			assertEquals(exeFile.getAbsolutePath(), Util.findFileWithOptionalSuffix(
					dirToUse.getAbsolutePath() + File.separator + "ruby.exe").getAbsolutePath());
			assertEquals(txtFile.getAbsolutePath(), Util.findFileWithOptionalSuffix(
					dirToUse.getAbsolutePath() + File.separator + "ruby.txt").getAbsolutePath());
			assertEquals(binFile.getAbsolutePath(), Util.findFileWithOptionalSuffix(
					dirToUse.getAbsolutePath() + File.separator + "ruby").getAbsolutePath());
		}
		finally
		{
			for (File file : filesToCleanUp)
			{
				if (!file.delete())
				{
					file.deleteOnExit();
				}
			}
		}

	}
}
