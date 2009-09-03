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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.rubypeople.rdt.refactoring.util.NameValidator;

/**
 * @author Mirko Stocker, initial code from http://www.eclipse.org/swt/snippets
 * 
 */
public class ParametersTableCellEditorListener extends Observable implements Listener {

	private final class TextListener implements Listener {
		private final MethodArgumentTableItem item;

		private final Text text;

		private TextListener(MethodArgumentTableItem item, Text text) {
			this.item = item;
			this.text = text;
		}

		public void handleEvent(final Event e) {
			if (e.type == SWT.FocusOut) {
				setNewName(item, text);
				text.dispose();
				table.setFocus();
			} else if (e.type == SWT.Traverse) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					setNewName(item, text);
				}
				if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_ESCAPE) {
					text.dispose();
					e.doit = false;
				}
			}
		}
	}

	private final Table table;

	private final IValidationController validationController;

	public ParametersTableCellEditorListener(Table parametersTable, IValidationController validationController) {
		this.table = parametersTable;
		this.validationController = validationController;
	}

	private boolean areAllNamesUnique() {
		TableItem[] items = table.getItems();
		boolean unique = true;

		for (int outer = 0; outer < items.length; outer++) {
			for (int inner = 0; inner < items.length; inner++) {
				if (outer == inner) {
					continue;
				} else if (items[outer].getText().equals(items[inner].getText())) {
					unique = false;
				}
			}
		}
		return unique;
	}

	private boolean areAllNamesValid(StringBuilder message) {
		TableItem[] items = table.getItems();
		boolean valid = true;
		for (TableItem item : items) {
			if (!NameValidator.isValidLocalVariableName(item.getText())) {
				valid = false;
				item.setBackground(new Color(table.getBackground().getDevice(), 255, 0, 0));
				message.append('\'');
				message.append(item.getText());
				message.append(Messages.ParametersTableCellEditorListener_IsNotValidParameterName);
			} else if (nameAlreadyUsed(item.getText())) {
				valid = false;
				message.append('\'');
				message.append(item.getText());
				message.append(Messages.ParametersTableCellEditorListener_IsAlreadyUsed);
			} else {
				item.setBackground(table.getBackground());
			}
		}
		return valid;
	}

	private boolean nameAlreadyUsed(String name) {
		return validationController.getInvalidNames().contains(name);
	}

	private void setNewName(final MethodArgumentTableItem item, final Text text) {
		String oldName = item.getItemName();
		item.setItemName(text.getText());
		item.setText(text.getText());

		StringBuilder message = new StringBuilder();

		if (areAllNamesValid(message)) {
			setChanged();
			notifyObservers(new ParameterTextChanged(table.getSelectionIndex(), table.getSelectionIndex(), oldName, text.getText()));
		}

		if (!areAllNamesUnique()) {
			message.append(Messages.ParametersTableCellEditorListener_CannotHaveParametersWithEqualNames);
		}

		if (message.toString().equals("")) { //$NON-NLS-1$
			validationController.setError(null);
			validationController.setComplete(this, true);
		} else {
			validationController.setError(message.toString());
			validationController.setComplete(this, false);
		}
	}

	public void handleEvent(Event event) {

		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		Rectangle clientArea = table.getClientArea();
		Rectangle bounds = table.getSelection()[0].getBounds();
		Point pt = new Point(bounds.x, bounds.y);
		int index = table.getTopIndex();
		while (index < table.getItemCount()) {
			boolean visible = false;
			final MethodArgumentTableItem item = (MethodArgumentTableItem) table.getItem(index);
			for (int i = 0; i < table.getColumnCount(); i++) {
				Rectangle rect = item.getBounds(i);
				if (rect.contains(pt)) {

					final Text text = new Text(table, SWT.NONE);
					Listener textListener = new TextListener(item, text);

					text.addListener(SWT.FocusOut, textListener);
					text.addListener(SWT.Traverse, textListener);
					editor.setEditor(text, item, i);
					text.setText(item.getText(i));
					text.selectAll();
					text.setFocus();
					return;
				}
				if (!visible && rect.intersects(clientArea)) {
					visible = true;
				}
			}
			if (!visible) {
				return;
			}
			index++;
		}
	}
}
