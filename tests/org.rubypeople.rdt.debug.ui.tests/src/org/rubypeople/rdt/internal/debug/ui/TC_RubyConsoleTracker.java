package org.rubypeople.rdt.internal.debug.ui;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleHyperlink;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.rubypeople.rdt.internal.debug.ui.console.RubyConsoleTracker;
import org.rubypeople.rdt.internal.debug.ui.console.RubyStackTraceHyperlink;
import org.rubypeople.rdt.internal.debug.ui.console.RubyConsoleTracker.FileExistanceChecker;

public class TC_RubyConsoleTracker extends TestCase {

	private static final String SYNTAX_IN_REQUIRE 			= " /RdtTest/FooTest.rb:2:in `require': /RdtTestLib/anotherFile.rb:9: parse error (SyntaxError)";
	private TestConsole console;
	private MockFileExistanceChecker fileChecker;
	
	public void setUp() throws Exception {
		fileChecker = new MockFileExistanceChecker();
		console = new TestConsole(fileChecker);
	}
	
	public void testCorrect() throws Exception {
		//data/test/configFile.rb:1:in `require': No such file to load -- inifile (LoadError)
		// *	from /data/test/configFile.rb:1
		// *	from /data/test/fetchmail.rb:2:in `require'
		 
		fileChecker.addKnownFile("/d/t.rb");
		fileChecker.addKnownFile("c:/d/abc.rb");
				
		console.lineAppend("/d/t.rb:1:in `require': No such file to load -- inifile (LoadError)") ;
		console.lineAppend("\tfrom c:/d/abc.rb:99") ;
		console.lineAppend(" /name with from in the middle/rb.rb:123") ;
		
		console.assertLink(0,  9, "/d/t.rb", 	  1, 0);
		console.assertLink(6, 14, "c:/d/abc.rb", 99, 1);
		console.assertLinkCount(2);				
	}
	
	public void testSyntaxErrorInRequire() throws Exception {
		fileChecker.addKnownFile("/RdtTest/FooTest.rb");
		fileChecker.addKnownFile("/RdtTestLib/anotherFile.rb");
			
		console.lineAppend(SYNTAX_IN_REQUIRE) ;
		 		
		console.assertLinkCount(2);
		console.assertLink( 1, 21, "/RdtTest/FooTest.rb",      2, 0);
		console.assertLink(37, 28, "/RdtTestLib/anotherFile.rb", 9, 1);
	}

	public void testSecondWithoutFirst() throws Exception {
		fileChecker.addKnownFile("c:/d/abc.rb");
		
		console.lineAppend("\tfrom c:/d/abc.rb:99") ;
				
		console.assertLink(6, 14, "c:/d/abc.rb", 99, 0);
	}
	
	public void testInCorrect() throws Exception {
		fileChecker.addKnownFile("/d/t.rb");
		
		console.lineAppend("/d/t.rb:a:in `require': No such file to load -- inifile (LoadError)") ;
		console.lineAppend("/d/t.rb:123 ");
		console.assertLinkCount(0);
	}
	
	/**
	 * From http://www.aptana.com/trac/ticket/5019
	 * @throws Exception
	 */
	public void testBackslashesInFilePath() throws Exception {
		fileChecker.addKnownFile("C:\\ruby\\lib\\ruby\\gems\\1.8\\gems\\rails-1.2.3\\lib/commands/server.rb");
		
		console.lineAppend("\tfrom C:\\ruby\\lib\\ruby\\gems\\1.8\\gems\\rails-1.2.3\\lib/commands/server.rb:1") ;
		console.assertLinkCount(1);
		console.assertLink(6, 67, "C:\\ruby\\lib\\ruby\\gems\\1.8\\gems\\rails-1.2.3\\lib/commands/server.rb", 1, 0);
	}	
	
	public void testWorkspaceRelativeStartingWithSlash() throws Exception {
		fileChecker.addKnownFile("/app/controllers/tags_controller.rb");
			
		console.lineAppend("\t/app/controllers/tags_controller.rb:5:in `index'") ;
		console.assertLinkCount(1);
		console.assertLink(1, 37, "/app/controllers/tags_controller.rb", 5, 0);
	}
	
	private final class MockFileExistanceChecker implements RubyConsoleTracker.FileExistanceChecker {
		private List knownFiles = new ArrayList();
		
		public void addKnownFile(String filename) {
			knownFiles.add(filename);
		}
		public boolean fileExists(String filename) {
			return knownFiles.contains(filename);
		}
	}
	
	public class TestConsole implements IConsole {
		private class MetaLink {
			public RubyStackTraceHyperlink link;
			public int offset;
			public int length;
		}
			
		private List metaLinks = new ArrayList();
		SimpleDocument doc = new SimpleDocument() ;
		RubyConsoleTracker tracker ;
		
		public TestConsole(FileExistanceChecker fileChecker) throws Exception {
			tracker = new RubyConsoleTracker(fileChecker){
			
				@Override
				protected IProject getProject() {
					return null;
				}
			
			};						
			tracker.init(this) ;			
		}
		
		public void assertLinkCount(int expectedLinkCount) {
			assertEquals(expectedLinkCount, metaLinks.size());
		}
		
		public void assertLink(int expectedOffset, int expectedLength, String expectedFilename, int expectedLineNumber, int linkIndex) {
			try {
				MetaLink metaLink = (MetaLink) metaLinks.get(linkIndex);
				assertNotNull(metaLink);
				assertEquals("Offset of link["+linkIndex+"]", expectedOffset, metaLink.offset);
				assertEquals(expectedLength, metaLink.length);
				assertEquals(expectedFilename, metaLink.link.getFilename());
				assertEquals(expectedLineNumber, metaLink.link.getLineNumber());
			} catch (IndexOutOfBoundsException e) {
				fail("Link index out of bounds: (" + linkIndex + ")");
			}
		}
				
		public void addLink(IConsoleHyperlink pLink, int pOffset, int pLength) {			
		}
		
		public void addLink(org.eclipse.ui.console.IHyperlink  pLink, int pOffset, int pLength) {
			MetaLink metaLink = new MetaLink();
			metaLink.link = (RubyStackTraceHyperlink) pLink ;
			metaLink.offset = pOffset ;
			metaLink.length = pLength ;
						
			metaLinks.add(metaLink);
		}
		public void connect(IStreamMonitor streamMonitor, String streamIdentifer) {
			throw new RuntimeException("Not Implemented Exception");
		}
		public void connect(IStreamsProxy streamsProxy) {
			throw new RuntimeException("Not Implemented Exception");
		}
		public IDocument getDocument() {
			return doc;
		}
		public IProcess getProcess() {
			throw new RuntimeException("Not Implemented Exception");
		}
		public IRegion getRegion(IConsole link) {
			throw new RuntimeException("Not Implemented Exception");
		}

		public void lineAppend(String pLine) throws Exception {
			doc.set(pLine) ;
			tracker.lineAppended(doc.getLineInformationOfOffset(1)) ;
		}

		public IRegion getRegion(IConsoleHyperlink link) {
			// TODO Auto-generated method stub
			return null;
		}

		public IRegion getRegion(org.eclipse.ui.console.IHyperlink link) {
			// TODO Auto-generated method stub
			return null;
		}

		public void addPatternMatchListener(IPatternMatchListener matchListener) {
			// TODO Auto-generated method stub
			
		}

		public void removePatternMatchListener(IPatternMatchListener matchListener) {
			// TODO Auto-generated method stub
			
		}

		public IOConsoleOutputStream getStream(String streamIdentifier) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class SimpleDocument extends AbstractDocument {
		public SimpleDocument() {
			// The text store is not really necessary, but there is a not null assert in AbstractDocument 
			this.setTextStore(new GapTextStore(1,2)) ;
			setLineTracker(new DefaultLineTracker());
			completeInitialization();
		}
	}
}
