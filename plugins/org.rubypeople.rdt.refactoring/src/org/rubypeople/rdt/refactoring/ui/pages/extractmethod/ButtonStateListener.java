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

package org.rubypeople.rdt.refactoring.ui.pages.extractmethod;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ButtonStateListener implements Observer, Listener {

	private final Table table;

	private final Button upButton;

	private final Button downButton;

	private final Button editButton;

	public ButtonStateListener(Table table, Button upButton, Button downButton, Button editButton) {
		this.table = table;
		this.upButton = upButton;
		this.downButton = downButton;
		this.editButton = editButton;
	}

	public void handleEvent(Event event) {
		setButtonStates();
	}

	private void setButtonStates() {
		TableItem[] tableItems = table.getSelection();
		if (tableItems == null || tableItems.length < 1) {
			editButton.setEnabled(false);
			return;
		}
		MethodArgumentTableItem item = (MethodArgumentTableItem) tableItems[0];

		upButton.setEnabled(true);
		downButton.setEnabled(true);
		editButton.setEnabled(true);

		if (item.isMoveable()) {
			if (table.getSelectionIndex() == 0) {
				upButton.setEnabled(false);
			}
			if (table.getSelectionIndex() + 1 == table.getItemCount()) {
				downButton.setEnabled(false);
			}

			MethodArgumentTableItem next = getNextItem(table.getSelectionIndex());
			if (next != null && !next.isMoveable()) {
				downButton.setEnabled(false);
			}
		} else {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		}
	}

	private MethodArgumentTableItem getNextItem(int selected) {
		if (selected + 1 >= table.getItemCount()) {
			return null;
		}
		return (MethodArgumentTableItem) table.getItem(selected + 1);

	}

	public void update(Observable arg0, Object arg1) {
		setButtonStates();
	}

}
