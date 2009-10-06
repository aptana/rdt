/*
 * Created on Feb 19, 2005
 *
 */
package org.rubypeople.rdt.internal.ui.text;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IToken;
import org.rubypeople.rdt.internal.ui.text.RubyPartitionScanner.EndBraceFinder;

/**
 * @author Chris
 */
public class TC_RubyPartitionScanner extends TestCase
{

	private void assertContentType(String contentType, String code, int offset)
	{
		assertEquals("Content type doesn't match expectations for: " + code.charAt(offset), contentType,
				getContentType(code, offset));
	}

	private String getContentType(String content, int offset)
	{
		IDocument doc = new Document(content);
		FastPartitioner partitioner = new FastPartitioner(new MergingPartitionScanner(),
				RubyPartitionScanner.LEGAL_CONTENT_TYPES);
		partitioner.connect(doc);
		return partitioner.getContentType(offset);
	}

	public void testUnclosedInterpolationDoesntInfinitelyLoop()
	{
		getContentType("%[\"#{\"]", 0);
		assert (true);
	}

	/**
	 * http://www.aptana.com/trac/ticket/5730
	 */
	public void testBug5730()
	{
		getContentType("# Comment\n" + "=begin\n" + "puts 'hi'\n" + "=ne", 0);
		assert (true);
	}

	/**
	 * http://www.aptana.com/trac/ticket/6052
	 */
	public void testBug6052()
	{
		getContentType("# Use this class to maintain the decision process\n" + "# To choose a next aprt of text etc.\n"
				+ "class Logic\n" + "=begin\n" + "  def initialize\n" + "  end\n"
				+ "############################################################################################\n"
				+ "  private\n"
				+ "############################################################################################ \n"
				+ "end", 0);
		assert (true);
	}

	public void testPartitioningOfSingleLineComment()
	{
		String source = "# This is a comment\n";

		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 1);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 18);
	}

	public void testRecognizeSpecialCase()
	{
		String source = "a,b=?#,'This is not a comment!'\n";

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 5);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 6);
	}

	public void testMultilineComment()
	{
		String source = "=begin\nComment\n=end";

		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, 10);

		source = "=begin\n" + "  for multiline comments, the =begin and =end must\n" + "  appear in the first column\n"
				+ "=end";
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, source.length() / 2);
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, source.length() - 2);
	}

	public void testMultilineCommentNotOnFirstColumn()
	{
		String source = " =begin\nComment\n=end";

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 1);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 2);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 10);
	}

	public void testRecognizeDivision()
	{
		String source = "1/3 #This is a comment\n";

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 3);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 5);
	}

	public void testRecognizeOddballCharacters()
	{
		String source = "?\" #comment\n";

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 2);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 5);

		source = "?' #comment\n";

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 2);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 5);

		source = "?/ #comment\n";

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 2);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, 5);
	}

	public void testPoundCharacterIsntAComment()
	{
		String source = "?#";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, source, 1);
	}

	public void testSinglelineCommentJustAfterMultilineComment()
	{
		String source = "=begin\nComment\n=end\n# this is a singleline comment\n";

		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, 0);
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, source, 10);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, source, source.length() - 5);
	}

	public void testMultipleCommentsInARow()
	{
		String code = "# comment 1\n# comment 2\nclass Chris\nend\n";

		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 6);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 17);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 26);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 29);
	}

	public void testCommentAfterEnd()
	{
		String code = "class Chris\nend # comment\n";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 12);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 17);
	}

	public void testCommentAfterEndWhileEditing()
	{
		String code = "=begin\r\n" + "c\r\n" + "=end\r\n" + "#hmm\r\n" + "#comment here why is ths\r\n"
				+ "class Chris\r\n" + "  def thing\r\n" + "  end  #ocmm \r\n" + "end";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 76);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 83);
	}

	public void testCommentAtEndOfLineWithStringAtBeginning()
	{
		String code = "hash = {\n" + "  \"string\" => { # comment\n" + "    123\n" + "  }\n" + "}";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 0);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 4);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 6);

		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 8);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 12);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 18);

		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 19);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 22);

		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 25);
	}

	public void testLinesWithJustSpaceBeforeComment()
	{
		String code = "  \n" + "  # comment\n" + "  def method\n" + "    \n" + "  end";
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 5);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 14);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 20);
	}

	public void testCommentsWithAlotOfPrecedingSpaces()
	{
		String code = "                # We \n" + "                # caller-requested until.\n" + "return self\n";
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 16);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 63);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 70);
	}

	public void testCodeWithinString()
	{
		String code = "string = \"here's some code: #{1} there\"";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 2); // st'r'...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 10); // "'h'er...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 28); // '#'{1...
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 30); // '1'} t...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 31); // '}' th...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 35); // th'e're..
	}

	public void testCodeWithinSingleQuoteString()
	{
		String code = "string = 'here s some code: #{1} there'";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 2); // st'r'...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 10); // "'h'er...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 28); // '#'{1...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 30); // '1'} t...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 31); // '}' th...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 35); // th'e're..
	}

	public void testVariableSubstitutionWithinString()
	{
		String code = "string = \"here's some code: #$global there\"";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 2); // st'r'...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 10); // "'h'er...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 28); // '#'$glo...
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 29); // '$'global
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 36);// ' 'there...
	}

	public void testStringWithinCodeWithinString()
	{
		String code = "string = \"here's some code: #{var = 'string'} there\"";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 2); // st'r'...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 10); // "'h'er...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 28); // '#'{var
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 30); // 'v'ar =
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 36); // '''string
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 46); // 't'here
	}

	public void testStringWithEndBraceWithinCodeWithinString()
	{
		String code = "string = \"here's some code: #{var = '}'; 1} there\"";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 2); // st'r'...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 10); // "'h'er...
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 28); // '#'{var
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 30); // 'v'ar =
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 37); // '}';
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 39); // ';' 1}
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 41); // ; '1'}
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 42); // 1'}' t
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 44); // 't'here
	}

	public void testRegex()
	{
		String code = "regex = /hi there/";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 2); // re'g'ex
		assertContentType(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, code, 9); // '/'hi the
		assertContentType(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, code, 11); // /h'i' the
	}

	public void testRegexWithDynamicCode()
	{
		String code = "/\\.#{Regexp.escape(extension.to_s)}$/ # comment";
		assertContentType(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, code, 3);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 38); // '#' co
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 40); // # 'c'ommen
	}

	public void testEscapedCharactersAndSingleQuoteInsideDoubleQuote()
	{
		String code = "quoted_value = \"'#{quoted_value[1..-2].gsub(/\\'/, \"\\\\\\\\'\")}'\" if quoted_value.include?(\"\\\\\\'\") # (for ruby mode) \"\n"
				+ "quoted_value";
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 16); // 
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 19); // #{'q'uoted
		assertContentType(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, code, 44); // /
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 51); // "\
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 59); // '" if
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 62); // 'i'f quoted_
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 87); // include?('"'
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 95); // '#' (for ruby mode)
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, code.length() - 3);
	}

	public void testSingleQuotedString()
	{
		String code = "require 'commands/server'";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 8);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 9);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 17);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 18);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 24);
	}

	public void testCommands()
	{
		String code = "if OPTIONS[:detach]\n" + "  `mongrel_rails #{parameters.join(\" \")} -d`\n" + "else\n"
				+ "  ENV[\"RAILS_ENV\"] = OPTIONS[:environment]";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1);
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 22);
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 23);
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 38);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 50);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 55);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 58);
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 59);
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 63);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 65);
	}

	public void testPercentXCommand()
	{
		String code = "if (@options.do_it)\n" + "  %x{#{cmd}}\n" + "end\n";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1); // i'f'
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 22); // '%'x
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 24); // %x'{'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 27); // 'c'md
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 30); // cmd'}'
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 31); // cmd}'}'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 33); // 'e'nd
	}

	public void testHeredocInArgumentList()
	{
		String code = "connection.delete <<-end_sql, \"#{self.class.name} Destroy\"\n"
				+ "  DELETE FROM #{self.class.table_name}\n"
				+ "  WHERE #{connection.quote_column_name(self.class.primary_key)} = #{quoted_id}\n" + "end_sql\n";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1); // c'o'nnection
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 18); // '<'<-end_sql
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 33); // 's'elf.class
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 48); // '}' Destroy
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 75); // 's'elf.class
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 96); // name'}'\n
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 108); // {'c'onnection
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 160); // '}' =
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 166); // {'q'uoted
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 175); // _id'}'\n
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 177); // 'e'nd_sql
	}

	public void testScaryString()
	{
		String code = "puts \"match|#{$`}<<#{$&}>>#{$'}|\"\n" + "pp $~";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1); // p'u'ts
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 5); // '"'match
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 13); // #'{'$`
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 14); // $
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 15); // `
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 16); // }
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 20); // {
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 21); // $
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 22); // &
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 23); // }
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 27); // {
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 28); // $
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 29); // '
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 30); // }
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 34); // 'p'p $~
	}

	// TODO Handle yet even wackier heredoc syntax:
	// http://blog.jayfields.com/2006/12/ruby-multiline-strings-here-doc-or.html

	public void testBraceFinderHandlesWeirdGlobal()
	{
		EndBraceFinder finder = new EndBraceFinder("$'}|\"\npp $~");
		assertEquals(2, finder.find());
	}

	public void testNestedHeredocs()
	{
		String code = "methods += <<-BEGIN + nn_element_def(element) + <<-END\n" +
				"  def #{element.downcase}(attributes = {})\n" + 
				"BEGIN\n" + 
				"  end\n" + 
				"END\n" +
				"\n" + 
				"puts :symbol\n";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1);
		// _<_<-BEGIN
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 11);
		// <<-BEGI_N_
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 18);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 20);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 46);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 48);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 62); // #'{'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 63); // #{'e'lem
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 79); // case'}'
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 112); // EN'D'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 115); // 'p'uts
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 120); // ':'sym		
	}

	public void testBug5448()
	{
		String code = "m.class_collisions controller_class_path,       \"#{controller_class_name}Controller\", # Sessions Controller\r\n"
				+ "    \"#{controller_class_name}Helper\"";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 1);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 40);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 48);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 50);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 51);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 71);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 72);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 83);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 84);
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, code, 86);
	}

	public void testBug5208()
	{
		String code = "=begin\r\n" + "  This is a comment\r\n" + "=end\r\n" + "require 'gosu'";
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, code, 0);
		assertContentType(RubyPartitionScanner.RUBY_MULTI_LINE_COMMENT, code, 32); // =en'd'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 36); // 'r'equire
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 44);
	}

	public void testROR255()
	{
		String code = "\"all_of(#{@matchers.map { |matcher| matcher.mocha_inspect }.join(\", \") })\"";
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 1); // "'a'll_of
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 10); // #{'@'match
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 60); // }.'j'oin
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 70);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 71); // ) '}')"
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 72); // ) }')'"
	}

	public void testROR950()
	{
		String code = "config.load_paths += [\"#{RAILS_ROOT}/vendor/plugins/sql_session_store/lib\"]";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 0); // 'c'onfig
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 22); // ['"'#{
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 25); // #{'R'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 34);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 36); // '/'vendor

		code = "config.load_paths += %W(#{RAILS_ROOT}/vendor/plugins/sql_session_store/lib)";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 0); // 'c'onfig
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 26); // #{'R'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 35); // OO'T'}
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 37); // '/'vendor
	}

	public void testROR975()
	{
		String code = "exist_sym = :\"#{row['PROVVPI']}.#{row['vpi']}.#{row['vci']}.#{row['seq_num']}\"";
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 0); // 'e'xist
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 12); // ':'
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 13); // '"'#
	}
	
	public void testROR1278()
	{
		String code = "hash = {:user=>{:emailaddr=>'4mydemo@4mypasswords.com', :login=>'4MyDemo',\n" +
":password=>'password', :password_confirmation=>'password'},\n" +
":userpin => {:pin =>'test', :pin_confirmation=>'test'}\n" +
"}";
		
		RubyPartitionScanner scanner = new RubyPartitionScanner();
		IDocument document = new Document(code);
		scanner.setPartialRange(document, 136, 19, RubyPartitionScanner.RUBY_DEFAULT, 132);
		IToken token = scanner.nextToken();
		assertEquals(RubyPartitionScanner.RUBY_DEFAULT, token.getData());
	}
	
	public void testCGILib()
	{
		String code = "warn \"Warning:#{caller[0].sub(/'/, '')}: cgi-lib is deprecated after Ruby 1.8.1; use cgi instead\"";
		// warn
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 0);
		// "Warning
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 5);
		// caller
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 16);
		// /'/
		assertContentType(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, code, 30);
		assertContentType(RubyPartitionScanner.RUBY_REGULAR_EXPRESSION, code, 32);
		// ,
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 33);
		// ''
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 35);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 36); 
		// )
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 37);
		// }: cgi-lib
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 38);
	}
	
	public void testROR1307StringSymbols()
	{
		String code = "class AbiBuilderTest\n" +
"  def method\n" +
"    assert_equal array, Abi.send(:\"to_#{type}\", number), \"#{type} failed on Ruby number -> array\"\n\n" + 
"    assert_equal [255], Abi.signed_to_udword(-1), 'Failed on signed_to_udword'\n" +
"  end\n\n" +
"  def test_packed_number_encoding\n" +
"    packed = { :p => 0x123, :q => 0xABCDEF, :n => 5 }\n" +
"    gold_packed = [0x02, 0x00, 0x03, 0x00, 0x01, 0x00, 0x23, 0x01, 0xEF, 0xCD, 0xAB, 0x05]\n" +
"    assert_equal gold_packed, Abi.to_packed(packed), 'packed'\n" +
"  end\n" +
"end";
		// class
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 0);
		// send'('
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 66);
		// send(':'
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 67);
		// "to_#{
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 68);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 73);
		// type
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 74);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 77);
		// }"
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 78);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 79); 
		// , number)
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 80);
		// "#{
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 91);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 93);
		// type
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 94);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 97);
		// } failed on Ruby number -> array"
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 98);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 130);
		// assert_equal
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 137);
		// 'Failed on signed_to_udword'
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 183);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 210);
	}
	
	public void testROR1248()
	{
		String code = "`wc -l \"#{ f.gsub('\"','\\\"') }\"`\n\n" +
		"puts \"syntax hilighting broken here\"";
		// `wc -l "#{
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 0); // `
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 7); // "
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 9); // {
		// f.gsub(
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 11); // f
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 17); // (
		// '"'
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 18); // '
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 20); // '
		// , 
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 21); // ,
		// '\"'
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 22); // '
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 25); // '
		// )
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 26);
		// }"`
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 28);
		assertContentType(RubyPartitionScanner.RUBY_COMMAND, code, 30);
		// puts
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 33);
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, code, 36);
		// "syntax hilighting broken here"
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 38);
		assertContentType(RubyPartitionScanner.RUBY_STRING, code, 68);
	}
	
	public void testCGI()
	{
		String src = "module TagMaker # :nodoc:\n" +
"		\n" +
"    # Generate code for an element with required start and end tags.\n" +
"    #\n" +
"    #   - -\n" +
"    def nn_element_def(element)\n" +
"      nOE_element_def(element, <<-END)\n" +
"          if block_given?\n" +
"            yield.to_s\n" +
"          else\n" +
"            \"\"\n" +
"          end +\n" +
"          \"</#{element.upcase}>\"\n" +
"      END\n" +
"    end\n" +
"    \n" +
"    # Generate code for an empty element.\n" +
"    #\n" +
"    #   - O EMPTY\n" +
"    def nOE_element_def(element, append = nil)\n" +
"   end\n" +
"end";
		// module TagMaker # :nodoc:
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, src, 0); // m
		assertContentType(RubyPartitionScanner.RUBY_SINGLE_LINE_COMMENT, src, 16); // #

		// nOE_element_def(element, <<-END)
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, src, 177); // ,
		assertContentType(RubyPartitionScanner.RUBY_STRING, src, 179); // <
		assertContentType(RubyPartitionScanner.RUBY_STRING, src, 184); // D
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, src, 185); // )

		// </#{element.upcase}>
		assertContentType(RubyPartitionScanner.RUBY_STRING, src, 296); // {
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, src, 297); // e
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, src, 310); // e
		assertContentType(RubyPartitionScanner.RUBY_STRING, src, 311); // }	
		
		// END
		assertContentType(RubyPartitionScanner.RUBY_STRING, src, 321); // E
		assertContentType(RubyPartitionScanner.RUBY_STRING, src, 323); // D
		
		// end
		assertContentType(RubyPartitionScanner.RUBY_DEFAULT, src, 329); // e
	}
}
