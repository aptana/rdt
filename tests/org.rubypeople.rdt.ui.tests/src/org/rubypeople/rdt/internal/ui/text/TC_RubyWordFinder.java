package org.rubypeople.rdt.internal.ui.text;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;

public class TC_RubyWordFinder extends TestCase {
	
	private static final String document =
		  "class Product < ActiveRecord::Base\n"+
		  "  validates_presence_of :title, :description, :image_url\n"+
		  "end";
	
	private IRegion findWord(int offset) {
		return RubyWordFinder.findWord(new Document(document), offset);
	}
	
	private boolean regionEquals(IRegion region, int length){
		return region.getLength() == length && region.getLength() == length;
	}
	
	private boolean isWordAtPosition(String word, int cursorPosition) {
		return regionEquals(findWord(cursorPosition), word.length());
	}

	public void testFindFirstWord() {
		assertTrue (isWordAtPosition("class", 0));
		assertTrue (isWordAtPosition("class", 3));
		assertTrue (isWordAtPosition("class", 4));
		assertFalse(isWordAtPosition("class", 5));
		
		assertNull(findWord(-1));
	}

	public void testFindWord() {
		assertTrue (isWordAtPosition("Product", 6));
		assertFalse(isWordAtPosition("Product", 5));
		assertTrue (isWordAtPosition("Product", 12));

		assertTrue (isWordAtPosition("ActiveRecord::Base", 16));

		assertTrue (isWordAtPosition("<", 14));
	}
	
	public void testFindLastWord() {
		assertTrue (isWordAtPosition("end", document.length() -1));
		assertTrue (isWordAtPosition("end", document.length() -2));
		assertTrue (isWordAtPosition("end", document.length() -3));
		assertFalse(isWordAtPosition("end", document.length() -4));

		assertNull(findWord(document.length()));
	}
}
