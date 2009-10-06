package org.rubypeople.rdt.internal.formatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.rubypeople.rdt.core.formatter.CodeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class AbstractCodeFormatterTestCase extends TestCase
{

	private static class TestData
	{
		public TestData(String formattedText, String unformattedText, String assertionMessage)
		{
			this.formattedText = formattedText;
			this.unformattedText = unformattedText;
			this.assertionMessage = assertionMessage;
		}

		public String formattedText;
		public String unformattedText;
		public String assertionMessage;
	}

	private static final boolean VERBOSE = false; // set to true if you want output to command line

	private Hashtable<String, List<TestData>> testMap;

	// FIXME Use setup and teardown!
	public AbstractCodeFormatterTestCase(String name) throws SAXException, IOException, ParserConfigurationException,
			FactoryConfigurationError
	{
		super(name);
		testMap = new Hashtable<String, List<TestData>>();
		this.parseXmlConfiguration();
	}

	private String stripFirstNewLine(String input)
	{
		return input.substring(input.indexOf("\n") + 1);
	}

	private void parseXmlConfiguration() throws SAXException, IOException, ParserConfigurationException,
			FactoryConfigurationError
	{
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getInputDataStream());
		NodeList tests = document.getElementsByTagName("test");
		for (int i = 0; i < tests.getLength(); i++)
		{
			Node test = tests.item(i);
			String name = test.getAttributes().getNamedItem("ID").getNodeValue();
			List<TestData> partList = new ArrayList<TestData>();
			NodeList nl = test.getChildNodes();
			createTestData(partList, nl);
			for (int j = 0; j < nl.getLength(); j++)
			{
				Node partNode = nl.item(j);
				if (partNode.getNodeName().equals("part"))
				{
					createTestData(partList, partNode.getChildNodes());
				}
			}
			testMap.put(name, partList);
		}
	}

	private void createTestData(List<TestData> partList, NodeList partNodes)
	{
		String formattedText = null;
		String unformattedText = null;
		String assertionMessage = null;
		for (int k = 0; k < partNodes.getLength(); k++)
		{
			Node node = partNodes.item(k);
			if (node.getNodeName().equals("formatted"))
			{
				formattedText = this.stripFirstNewLine(node.getFirstChild().getNodeValue());
			}
			else if (node.getNodeName().equals("unformatted"))
			{
				unformattedText = this.stripFirstNewLine(node.getFirstChild().getNodeValue());
			}
			else if (node.getNodeName().equals("assertionMessage"))
			{
				assertionMessage = node.getFirstChild().getNodeValue().trim();
			}
		}
		if (formattedText != null && unformattedText != null)
		{
			partList.add(new TestData(formattedText, unformattedText, assertionMessage));
		}
	}

	protected void doTest(String name)
	{
		ArrayList<TestData> partList = (ArrayList<TestData>) testMap.get(name);
		for (int i = 0; i < partList.size(); i++)
		{
			TestData data = (TestData) partList.get(i);
			TextEdit edit = getCodeFormatter().format(-1, data.unformattedText, 0, data.unformattedText.length(), 0,
					"\n");
			IDocument doc = new org.eclipse.jface.text.Document(data.unformattedText);
			try
			{
				edit.apply(doc);
			}
			catch (MalformedTreeException e)
			{
				fail(e.getMessage());
			}
			catch (BadLocationException e)
			{
				fail(e.getMessage());
			}
			String formatted = doc.get();
			log("---------- " + data.assertionMessage + " --------");
			log(data.unformattedText);
			log("------------");
			log(formatted);
			Assert.assertEquals(data.assertionMessage, data.formattedText, formatted);
		}
	}

	protected abstract CodeFormatter getCodeFormatter();
	
	protected abstract InputStream getInputDataStream();
	
	private void log(String formatted)
	{
		if (VERBOSE)
			System.out.println(formatted);

	}

	public void testSimple()
	{
		this.doTest("Keywords");
	}

	public void testBlockWithBrackets()
	{
		this.doTest("blockWithBrackets");
	}

	public void testBlocks()
	{
		this.doTest("Blocks");
	}

	public void testParameters()
	{
		this.doTest("Parameters");
	}

	public void testLiterals()
	{
		this.doTest("Literals");
	}

	public void testLiteralsStartingWithPercentSign()
	{
		this.doTest("LiteralsStartingWithPercentSign");
	}

	public void testNegativeIndentation()
	{
		this.doTest("NegativeIndentation");
	}

	public void testRescueModifier()
	{
		doTest("RescueModifier");
	}

	public void testParen()
	{
		doTest("LineStartingWithParen");
	}

	public void testCaseWithWhens()
	{
		doTest("CaseWithWhens");
	}

}
