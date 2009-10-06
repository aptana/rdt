package org.rubypeople.rdt.internal.ui.text.ruby;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.text.IRubyColorConstants;
import org.rubypeople.rdt.internal.ui.text.RubyColorManager;

public class TC_RubyTokenScanner extends TestCase {

	private RubyColoringTokenScanner fScanner;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RubyColorManager colorManager = new RubyColorManager(false);
		fScanner = new RubyColoringTokenScanner(colorManager, RubyPlugin.getDefault().getPreferenceStore()) {
		
			@Override
			public Token getToken(String key) {
				return new Token(key);
			}
		
		};
	}

	private void setUpScanner(String code) {
		setUpScanner(code, 0, code.length());
	}
	
	private void setUpScanner(String code, int offset, int length) {
		Document doc = new Document(code);
		fScanner.setRange(doc, offset, length);		
	}

	private void assertToken(String color, int offset, int length) {
		IToken token = fScanner.nextToken();
		assertEquals("Offsets don't match", offset, fScanner.getTokenOffset());
		assertEquals("Lengths don't match", length, fScanner.getTokenLength());	
		assertEquals("Colors don't match", color, token.getData()); // call getToken so we bypass the scanner's overriding in doGetToken
	}
	
	public void testSimpleClassDefinition() {
		String code = "class Chris\nend\n";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 0, 5);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 5, 6);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 11, 1);
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 12, 3);
	}
	
	public void testSymbolAtEndOfLine() {
		String code = "  helper_method :logged_in?\n" +
				"  def method\n" +
				"    \n" +
				"  end";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 15);  // '  helper_method'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 15, 2);  // ' :'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 17, 10);  // 'logged_in?'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 27, 1);  // '\n'
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 28, 5);  // '  def'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 33, 7);  // ' method'	
	}	
	
	public void testSymbolInsideBrackets() {
		String code = "test[:begin]";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 4);  // 'test'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 1);  // '['
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 5, 1);  // ' :'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 6, 5);  // 'begin'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 11, 1);  // ']'	
	}
	
	public void testSymbolInsideParentheses() {
		String code = "Object.const_defined?(:RedCloth)";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 6);  // 'Object'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 6, 1);  // '.'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 7, 14);  // 'const_define?'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 21, 1);  // '('
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 22, 1);  // ':'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 23, 8);  // 'RedCloth'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 31, 1);  // ')'
	}
	
	public void testAliasWithTwoSymbols() {
		String code = "alias :tsort_each_child :each_key";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 0, 5);  // 'alias'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 5, 2);  // ' :'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 7, 16);  // 'tsort_each_child'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 23, 2);  // ' :'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 25, 8);  // 'each_key'
	}
	
	public void testSymbolInsideBracketsTwo() {
		String code = "@repository=params[:repository]";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_INSTANCE_VARIABLE, 0, 11);  // '@repository'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 11, 1);  // '='
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 12, 6);  // 'params'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 18, 1);  // '['
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 19, 1);  // ':'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 20, 10);  // 'repository'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 30, 1);  // ']'
	}
	
	public void testTertiaryConditional() {
		String code = "multiparameter_name = true ? value.method : value";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 19);  // 'multiparameter_name'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 19, 2);  // ' ='
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 21, 5);  // ' true'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 26, 2);  // ' ?'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 28, 6);  // ' value'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 34, 1);  // '.'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 35, 6);  // 'method'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 41, 2);  // ' :'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 43, 6);  // ' value'
	}    
	
	public void testWhen() {
		String code = "case value\n" +
					"when FalseClass: 0\n" +
					"else value\n" +
					"end";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 0, 4);  // 'case'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 6);  // ' value'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 10, 1);  // '\n'
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 11, 4);  // 'when'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 15, 11);  // ' FalseClass'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 26, 1);  // ':'
		assertToken(IRubyColorConstants.RUBY_FIXNUM, 27, 2);  // ' 0'
	}    

	public void testAppendSymbol() {
		String code = "puts(:<<)";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 4);  // 'puts'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 1);  // '('
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 5, 1);  // ':'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 6, 2);  // '<<'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 8, 1);  // ')'
	}    
	
	public void testDollarDollarSymbol() {
		String code = "puts(:$$)";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 4);  // 'puts'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 1);  // '('
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 5, 1);  // ':'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 6, 2);  // '$$'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 8, 1);  // ')'
	} 
	
	public void testTertiaryConditionalWithNoSpaces() {
		String code = "puts(a?b:c)";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 4);  // 'puts'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 1);  // '('
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 5, 2);  // 'a?'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 7, 1);  // 'b'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 8, 1);  // ':'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 9, 1);  // 'c'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 10, 1);  // ')'
	} 	
	
	public void testClassVariable() {
		String code = "@@var = 1";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_CLASS_VARIABLE, 0, 5);  // '@@var'
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 5, 2);  // ' ='
		assertToken(IRubyColorConstants.RUBY_FIXNUM, 7, 2);  // ' 1'
	} 	
}
