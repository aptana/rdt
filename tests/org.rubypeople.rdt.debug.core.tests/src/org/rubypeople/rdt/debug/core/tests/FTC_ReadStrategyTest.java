package org.rubypeople.rdt.debug.core.tests;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

import org.rubypeople.rdt.internal.debug.core.parsing.MultiReaderStrategy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class FTC_ReadStrategyTest extends TestCase {

	protected PrintWriter writer ;
	protected XmlPullParser xpp ;
	
	public FTC_ReadStrategyTest(String name) {
		super(name);
	}
	
	
	
	public void setUp() throws Exception {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance("org.kxml2.io.KXmlParser,org.kxml2.io.KXmlSerializer", null);
		xpp = factory.newPullParser();
		PipedOutputStream outputStream = new PipedOutputStream() ;
		PipedInputStream inputStream = new PipedInputStream(outputStream) ;
		xpp.setInput(new InputStreamReader(inputStream)) ;
		writer = new PrintWriter(new OutputStreamWriter(outputStream)) ;			
	}


	public void testSingleReaderStrategy() throws Exception {
		final TestXmlStreamReader reader = new TestXmlStreamReader(xpp) ;
		reader.acceptTag("testTag") ;
		Thread testTagReaderThread = new Thread() {
			public void run() {
				try {
					reader.read() ;
				} catch (Exception ex) {
					
				}
			}
		} ;
		testTagReaderThread.start() ;
		writer.println("<testTag/>") ;
		writer.flush() ;		 
		testTagReaderThread.join() ;
		assertTrue("testTag", reader.isTagRead()) ;
	}

	public void testMultiReaderStrategy() throws Exception {
		MultiReaderStrategy multiReaderStrategy = new MultiReaderStrategy(xpp) ;
		final TestXmlStreamReader testTagReader = new TestXmlStreamReader(multiReaderStrategy) ;
		testTagReader.acceptTag("testTag") ;
		Thread testTagReaderThread = new Thread() {
			public void run() {
				try {
					testTagReader.read() ;
				} catch (Exception ex) {
					
				}
			}
		} ;
		testTagReaderThread.start() ;
		final TestXmlStreamReader breakpointReader = new TestXmlStreamReader(multiReaderStrategy) ;
		breakpointReader.acceptTag("breakpoint") ;
		Thread breakpointReaderThread = new Thread() {
			public void run() {
				try {
					breakpointReader.read() ;
				} catch (Exception ex) {
					
				}
				
			}
		} ;
		breakpointReaderThread.start() ;
		Thread.sleep(500) ; // wait for threads to be started
		assertTrue(testTagReaderThread.isAlive()) ;
		assertTrue(breakpointReaderThread.isAlive()) ;
		
		writer.println("<testTag/>") ;
		writer.flush() ;
		testTagReaderThread.join() ;		 
		assertTrue("testTag was read.", testTagReader.isTagRead()) ;
		assertTrue("breakpoint was not yet read.", !breakpointReader.isTagRead()) ;		
		assertTrue("breakpointReaderThread has not yet finished.",breakpointReaderThread.isAlive()) ; 
		writer.println("<breakpoint/>") ;
		writer.flush() ;		 
		breakpointReaderThread.join() ;
		assertTrue("breakpoint was read.", breakpointReader.isTagRead()) ;		
		
	}
	
	public void testMultiReaderStrategyWithMultipleTags() throws Exception {
		// make sure that a complete element gets dispatched to the reader
		MultiReaderStrategy multiReaderStrategy = new MultiReaderStrategy(xpp);
		final TestXmlStreamReader testTagReader = new TestXmlStreamReader(multiReaderStrategy) ;
		testTagReader.acceptTag("testTag");
		Thread testTagReaderThread = new Thread() {
			public void run() {
				try {
					testTagReader.read() ;
				} catch (Exception ex) {
					
				}
			}
		} ;
		testTagReaderThread.start();
		Thread.sleep(500); // wait for threads to be started
		writer.println("<testTag><testTag><testTag/><testTag/></testTag>") ;
		writer.flush() ;
		testTagReaderThread.join();		 
		assertEquals("testTag was read 3 times.", 3, testTagReader.getTagReadCount());		
	}

	public void testMultiReaderStrategyDontMissTag() throws Exception {
		// make sure the element is delivered to the reader, even if the reader is started after
		// the tag is fetched from the xpp 
		writer.println("<testTag><testTag/></testTag>") ;
		writer.flush() ;
		MultiReaderStrategy multiReaderStrategy = new MultiReaderStrategy(xpp) ;
		final TestXmlStreamReader testTagReader = new TestXmlStreamReader(multiReaderStrategy) ;
		testTagReader.acceptTag("testTag") ;
		Thread testTagReaderThread = new Thread() {
			public void run() {
				try {
					testTagReader.read() ;
				} catch (Exception ex) {
					fail() ;
				}
			}
		} ;
		Thread.sleep(500) ; // wait to make this test more evil
		testTagReaderThread.start() ;
		Thread.sleep(500) ; // wait for threads to be started				
		testTagReaderThread.join() ;		 
		assertEquals("testTag was read 2 times.", 2, testTagReader.getTagReadCount()) ;		
	}



}
