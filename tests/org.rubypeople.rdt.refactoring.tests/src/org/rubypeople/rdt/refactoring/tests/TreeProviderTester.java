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
 * Copyright (C) 2006 Lukas Felber <lfelber@hsr.ch>
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

package org.rubypeople.rdt.refactoring.tests;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;

public abstract class TreeProviderTester extends RefactoringTestCase {
	public TreeProviderTester() {
		this("Tree Provider Tester");
	}
	
	public TreeProviderTester(String fileName) {
		super(fileName);
	}

	private Map<String, Entry> elements;

	private ITreeContentProvider provider;

	@Override
	public void setUp() {
		elements = new HashMap<String, Entry>();
	}

	public void addContent(String[] content) {
		Map<String, Entry> aktMap = elements;
		for (String str : content)
		{
			if (!aktMap.containsKey(str))
			{
				aktMap.put(str, new Entry(str));
			}
			aktMap = aktMap.get(str).getChilds();
		}
	}

	public void validate(ITreeContentProvider provider) {
		this.provider = provider;
		checkElements(elements, provider.getElements(null));
	}

	private void checkElements(Map<String, Entry> expectedElements, Object[] elements) {

		assertNotNull(elements);
		assertEquals(expectedElements.size(), elements.length);
		for (Object aktElement : elements)
		{
			assertTrue(expectedElements.containsKey(aktElement.toString()));
			Entry aktEntry = expectedElements.get(aktElement.toString());
			if (provider.hasChildren(aktElement))
			{
				checkElements(aktEntry.getChilds(), provider
						.getChildren(aktElement));
			} else
			{
				assertEquals(0, aktEntry.getChilds().size());
			}
		}
	}

	static class Entry {
		private String name;

		private Map<String, Entry> childs;

		public Entry(String name) {
			this.name = name;
			childs = new HashMap<String, Entry>();
		}

		public String getName() {
			return name;
		}

		public void addChild(Entry child) {
			childs.put(child.getName(), child);
		}

		public Map<String, Entry> getChilds() {
			return childs;
		}
	}
}
