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

package org.rubypeople.rdt.refactoring.tests.core.splitlocal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import junit.framework.TestCase;

import org.rubypeople.rdt.refactoring.core.splitlocal.LocalVarFinder;
import org.rubypeople.rdt.refactoring.core.splitlocal.LocalVarUsage;
import org.rubypeople.rdt.refactoring.tests.FileTestData;

public class TC_LocalVarFinder extends TestCase {

	private Collection<LocalVarUsage> find(String name, int pos) throws FileNotFoundException, IOException {
		LocalVarFinder finder = new LocalVarFinder();
		Collection<LocalVarUsage> variables = finder.findLocalUsages(new FileTestData(name, "", ""), pos);
		return variables;
	}
	
	public void testFindLocalUsages_1() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_1.rb", 0);
		
		assertNotNull(variables);
		assertEquals(1, variables.size());
		
		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];
		
		assertNotNull(found.getNode());
		assertEquals("a", found.getName());
		assertEquals(0, found.getFromPosition());
		assertEquals(12, found.getToPosition());
	}	
	
	public void testFindLocalUsages_2() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_2.rb", 0);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());

		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];

		assertNotNull(found.getNode());
		assertEquals("a", found.getName());
		assertEquals(0, found.getFromPosition());
		assertEquals(12, found.getToPosition());

		found = (LocalVarUsage) variables.toArray()[1];

		assertNotNull(found.getNode());
		assertEquals("a", found.getName());
		assertEquals(13, found.getFromPosition());
		assertEquals(25, found.getToPosition());
	}

	public void testFindLocalUsages_3() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_3.rb", 0);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());

		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];

		assertNotNull(found.getNode());
		assertEquals("a", found.getName());
		assertEquals(0, found.getFromPosition());
		assertEquals(25, found.getToPosition());

		found = (LocalVarUsage) variables.toArray()[1];

		assertNotNull(found.getNode());
		assertEquals("a", found.getName());
		assertEquals(26, found.getFromPosition());
		assertEquals(38, found.getToPosition());
	}

	public void testFindLocalUsages_4() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_4.rb", 14);
		
		assertNotNull(variables);
		assertEquals(1, variables.size());

		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];

		assertNotNull(found.getNode());
		assertEquals("a", found.getName());
		assertEquals(14, found.getFromPosition());
		assertEquals(84, found.getToPosition());

//		found = (LocalVarUsage) variables.toArray()[1];
//
//		assertNotNull(found.getNode());
//		assertEquals("a", found.getName());
//		assertEquals(38, found.getFromPosition());
//		assertEquals(79, found.getToPosition());
	}

	public void testFindLocalUsages_5() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_5.rb", 14);
		
		assertNotNull(variables);
		assertEquals(1, variables.size());

		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];

		assertNotNull(found.getNode());
		assertEquals("i", found.getName());
		assertEquals(14, found.getFromPosition());
		assertEquals(32, found.getToPosition());

	}

	public void testFindLocalUsages_6() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_6.rb", 14);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());
	}
	
	public void testFindLocalUsages_7() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_7.rb", 14);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());
		
		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];
		
		assertNotNull(found.getNode());
		
		found = (LocalVarUsage) variables.toArray()[1];
		
		assertNotNull(found.getNode());
	}
	
	public void testFindLocalUsages_8() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_8.rb", 21);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());
		
		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];
		
		assertNotNull(found.getNode());		
		
		found = (LocalVarUsage) variables.toArray()[1];
		
		assertNotNull(found.getNode());
	}
	
	public void testFindLocalUsages_9() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_9.rb", 33);
		
		assertNotNull(variables);
		assertEquals(2, variables.size());
		
		LocalVarUsage found = (LocalVarUsage) variables.toArray()[0];
		
		assertNotNull(found.getNode());
	}

	public void testFindLocalUsages_10() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_9.rb", 0);
		
		assertNull(variables);
	}
	
	public void testFindLocalUsages_11() throws FileNotFoundException, IOException {
		
		Collection<LocalVarUsage> variables = find("testFindLocalUsages_11.rb", 36);
		
		assertNull(variables);
	}
}
