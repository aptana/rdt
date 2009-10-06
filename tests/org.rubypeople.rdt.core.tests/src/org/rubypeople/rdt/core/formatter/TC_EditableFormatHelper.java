/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.rubypeople.rdt.core.formatter;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.rubypeople.rdt.internal.core.parser.RubyParser;
import org.rubypeople.rdt.internal.core.parser.RubyParserWithComments;

public class TC_EditableFormatHelper extends TestCase
{

	private EditableFormatHelper formatter;

	@Override
	public void setUp()
	{
		formatter = new EditableFormatHelper("\n");
	}

	private String format(String original)
	{
		StringWriter writer = new StringWriter();
		ReWriterFactory factory = new ReWriterFactory(new ReWriterContext(new PrintWriter(writer), original, formatter));
		RubyParser parser = new RubyParserWithComments();
		ReWriteVisitor visitor = factory.createReWriteVisitor();
		parser.parse(original).getAST().accept(visitor);
		visitor.flushStream();
		return writer.getBuffer().toString();
	}

	private void assertEqualSource(String expectedResult, String toBeFormatted)
	{
		assertEquals(expectedResult, format(toBeFormatted));
	}

	public void testBeforeAndAfterAssignment()
	{
		formatter.setSpacesBeforeAndAfterAssignments(false);
		assertEqualSource("a=5", "a =5");
		formatter.setSpacesBeforeAndAfterAssignments(true);
		assertEqualSource("a = 5", "a= 5");
	}

	public void testMatchOperator()
	{
		formatter.setSpacesBeforeAndAfterAssignments(false);
		assertEqualSource("a=~/5/", "a  =~ /5/");
		formatter.setSpacesBeforeAndAfterAssignments(true);
		assertEqualSource("a =~ /5/", "a  =~/5/");
	}

	public void testBeforeAndAfterCallArguments()
	{
		formatter.setAlwaysParanthesizeMethodCalls(false);
		assertEqualSource("puts 1", "puts 1");
		assertEqualSource("puts(1, 2)", "puts 1, 2");
		formatter.setAlwaysParanthesizeMethodCalls(true);
		assertEqualSource("puts(1)", "puts 1");
		assertEqualSource("puts(1, 2)", "puts 1, 2");
	}

	public void testBeforeAndAfterHashContent()
	{
		formatter.setSpacesBeforeAndAfterHashContent(false);
		assertEqualSource("{:a => 5}", "{ :a => 5 }");
		formatter.setSpacesBeforeAndAfterHashContent(true);
		assertEqualSource("{ :a => 5 }", "{ :a => 5 }");
	}

	public void testBeforeIterVars()
	{
		formatter.setSpaceAfterIterVars(false);
		formatter.setSpaceBeforeClosingIterBrackets(false);
		formatter.setSpaceBeforeIterVars(false);
		assertEqualSource("[].each {|a|}", "[].each { | a | }");
		formatter.setSpaceBeforeIterVars(true);
		assertEqualSource("[].each { |a|}", "[].each { | a | }");
	}

	public void testAfterIterVars()
	{
		formatter.setSpaceBeforeIterVars(false);
		formatter.setSpaceBeforeClosingIterBrackets(false);
		formatter.setSpaceAfterIterVars(false);
		assertEqualSource("[].each {|a|}", "[].each { | a | }");
		formatter.setSpaceAfterIterVars(true);
		assertEqualSource("[].each {|a| }", "[].each { | a | }");
	}

	public void testBeforeAndAfterMethodArguments()
	{
		formatter.setAlwaysParanthesizeMethodDefs(false);
		assertEqualSource("def test arg, arg2\nend", "def test(arg, arg2)\nend");
		formatter.setAlwaysParanthesizeMethodDefs(true);
		assertEqualSource("def test(arg, arg2)\nend", "def test arg, arg2\nend");
	}

	public void testBeforeIterBrackets()
	{
		formatter.setSpaceBeforeIterVars(false);
		formatter.setSpaceAfterIterVars(false);
		formatter.setSpaceBeforeClosingIterBrackets(false);
		formatter.setSpaceBeforeIterBrackets(false);
		assertEqualSource("[].each{|a|}", "[].each { | a | }");
		formatter.setSpaceBeforeIterBrackets(true);
		assertEqualSource("[].each {|a|}", "[].each { | a | }");
	}

	public void testBeforeClosingIterBrackets()
	{
		formatter.setSpaceBeforeIterVars(false);
		formatter.setSpaceAfterIterVars(false);
		formatter.setSpaceBeforeClosingIterBrackets(false);
		assertEqualSource("[].each {|a|}", "[].each { | a | }");
		formatter.setSpaceBeforeClosingIterBrackets(true);
		assertEqualSource("[].each {|a| }", "[].each { | a | }");
	}

	public void testClassBodyElementsSeparator()
	{
		formatter.setNewlineBetweenClassBodyElements(false);
		assertEqualSource("class C\n" + "  def test\n" + "  end\n" + "  def test\n" + "  end\n" + "end",

		"class C\n" + "  def test\n" + "  end\n" + "  def test\n" + "  end\n" + "end");

		formatter.setNewlineBetweenClassBodyElements(true);
		assertEqualSource("class C\n" + "  def test\n" + "  end\n" + "\n" + "  def test\n" + "  end\n" + "end",

		"class C\n" + "  def test\n" + "  end\n" + "  def test\n" + "  end\n" + "end");
	}

	public void testGetListSeparator()
	{
		formatter.setSpaceAfterCommaInListings(false);
		assertEqualSource("[1,2,3]", "[1 , 2  , 3,  ]");
		formatter.setSpaceAfterCommaInListings(true);
		assertEqualSource("[1, 2, 3]", "[1 , 2  , 3  ]");
	}

	public void testHashAssignment()
	{
		formatter.setSpacesAroundHashAssignment(false);
		assertEqualSource("{:a=>5, b=>10}", "{ :a => 5 , b => 10 }");
		formatter.setSpacesAroundHashAssignment(true);
		assertEqualSource("{:a => 5, b => 10}", "{ :a => 5 , b => 10 }");
	}

	public void testIndentationSteps()
	{
		formatter.setIndentationSteps(0);
		assertEqualSource("class C\n" + "def test\n" + "end\n" + "end",

		"class C\n" + "  def test\n" + "    end\n" + " end");
		formatter.setIndentationSteps(3);
		assertEqualSource("class C\n" + "   def test\n" + "   end\n" + "end",

		"class C\n" + "  def test\n" + "    end\n" + " end");
	}

	public void testIndentationChar()
	{
		formatter.setTabInsteadOfSpaces(false);
		assertEqualSource("class C\n" + "  def test\n" + "  end\n" + "end",

		"class C\n" + "  def test\n" + "    end\n" + " end");
		formatter.setTabInsteadOfSpaces(true);
		formatter.setIndentationSteps(1);
		assertEqualSource("class C\n" + "\tdef test\n" + "\tend\n" + "end",

		"class C\n" + "  def test\n" + "    end\n" + " end");
	}

}
