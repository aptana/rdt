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

package org.rubypeople.rdt.refactoring.editprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EditProviderGroups {

	private Map<String, EditProviderGroup> groups;

	public EditProviderGroups() {
		groups = new LinkedHashMap<String, EditProviderGroup>();
	}

	public void add(String groupName, EditProvider editProvider) {

		EditProviderGroup group;
		if (groups.containsKey(groupName))
			group = groups.get(groupName);
		else {
			group = new EditProviderGroup();
			groups.put(groupName, group);
		}
		group.add(editProvider);
	}

	public void add(String groupName, Collection<EditProvider> editProviders) {
		for (EditProvider provider : editProviders) {
			add(groupName, provider);
		}
	}

	public boolean hasGroup(String name) {
		return groups.containsKey(name);
	}

	public Collection<EditProvider> getGroup(String groupName) {
		return groups.get(groupName).getEditProviders();
	}

	public Collection<EditProvider> getAllEditProviders() {
		Collection<EditProvider> providers = new ArrayList<EditProvider>();
		for (EditProviderGroup group : groups.values()) {
			providers.addAll(group.getEditProviders());
		}
		return providers;
	}
}
