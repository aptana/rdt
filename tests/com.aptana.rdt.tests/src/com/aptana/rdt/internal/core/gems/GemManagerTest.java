package com.aptana.rdt.internal.core.gems;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import junit.framework.TestCase;

import com.aptana.rdt.core.gems.Gem;

public class GemManagerTest extends TestCase {
	
	public void testRemoteGemCacheCompressedToLogicalGems() throws Exception {
		GemManager manager = new GemManager() {
			
			protected Set<Gem> loadRemoteGems(String gemIndexUrl,
					IProgressMonitor monitor) {
				Set<Gem> gems = new HashSet<Gem>();
				gems.add(new Gem("test", "1.0.0", "testing", "ruby"));
				gems.add(new Gem("test", "2.0.0", "testing", "ruby"));
				return gems;
			}
			
			protected File getConfigFile(String fileName) {
				return null;
			}
			
			protected Set<String> loadSourceURLs() {
				return new HashSet<String>();
			}
			
			protected void addSourceURL(String sourceURL) {
			}
		
		};
		Set<Gem> gems = manager.getRemoteGems();
		assertEquals(1, gems.size());
	}

}
