/* Copyright (c) 2005 RubyPeople.
* 
* Author: Markus
* 
* This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT
* is subject to the "Common Public License (CPL) v 1.0". You may not use RDT
* except in compliance with the License. For further information see
* org.rubypeople.rdt/rdt.license.
* 
*/

package org.rubypeople.rdt.internal.ui.search;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

public class MockTreeViewer extends AbstractTreeViewer {
	private Map<Object, Object> hashtable = new Hashtable<Object, Object>();
	public void add(Object parentElement, Object childElement) {
		hashtable.put(parentElement, childElement);
	}

	public boolean isParentAdded(Object parentElement) {
		return hashtable.containsKey(parentElement);
	}
	
	public Object childFrom(Object parentElement) {
		return hashtable.get(parentElement);
	}
	
	protected void addTreeListener(Control control, TreeListener listener) {
	// TODO Auto-generated method stub

	}

	protected void doUpdateItem(Item item, Object element) {
	// TODO Auto-generated method stub

	}

	protected Item[] getChildren(Widget widget) {
		return new Item[0];
	}

	protected boolean getExpanded(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	protected int getItemCount(Control control) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int getItemCount(Item item) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected Item[] getItems(Item item) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Item getParentItem(Item item) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Item[] getSelection(Control control) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Item newItem(Widget parent, int style, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void removeAll(Control control) {
	// TODO Auto-generated method stub

	}

	protected void setExpanded(Item item, boolean expand) {
	// TODO Auto-generated method stub

	}

	protected void setSelection(List items) {
	// TODO Auto-generated method stub

	}

	protected void showItem(Item item) {
	// TODO Auto-generated method stub

	}

	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

}
