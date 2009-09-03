/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 * This file is based on org.eclipse.ui.texteditor.templates.TemplatePreferencePage
 * 
 */

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.rubypeople.rdt.internal.debug.ui.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.evaluation.EvaluationExpression;
import org.rubypeople.rdt.internal.debug.ui.evaluation.EvaluationExpressionReaderWriter;
import org.rubypeople.rdt.internal.ui.util.SWTUtil;


public class EvaluationExpressionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Label provider for templates.
	 */
	private class EvaluationExpressionLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			EvaluationExpression exp = (EvaluationExpression) element;

			switch (columnIndex) {
			case 0:
				return exp.getName();
			case 1:
				return exp.getDescription();
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}

	private class EvaluationExpressionContentProvider implements IStructuredContentProvider {

		private EditableExpressionModel model;

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object input) {
			return model.getExpressionsAsArray();
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			model = (EditableExpressionModel) newInput;
		}

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
			model = null;
		}

	}

	private class EditableExpressionModel {

		private ArrayList expressions;

		public EditableExpressionModel() {
			this.load();
		}

		public Collection getExpressions() {
			return expressions;
		}

		public Object[] getExpressionsAsArray() {
			return expressions.toArray(new EvaluationExpression[expressions.size()]);
		}

		public void addExpression(EvaluationExpression expr) {
			this.expressions.add(expr);
		}

		public void replaceExpression(EvaluationExpression old, EvaluationExpression updated) {
			int index = this.expressions.indexOf(old);
			if (index >= 0) {
				this.expressions.set(index, updated);
			}
		}

		public void removeExpression(EvaluationExpression expr) {
			this.expressions.remove(expr);
		}

		public void save() {
			StringWriter xmlWriter = new StringWriter();
			try {
				new EvaluationExpressionReaderWriter().save((EvaluationExpression[]) this.getExpressionsAsArray(), xmlWriter);
				RdtDebugUiPlugin.getDefault().getPluginPreferences().setValue(RdtDebugUiConstants.EVALUATION_EXPRESSIONS_PREFERENCE, xmlWriter.toString());
			} catch (IOException e) {
				RdtDebugUiPlugin.log(e) ;
			}
		}

		public void load() {
			EvaluationExpression[] exprs = RdtDebugUiPlugin.getDefault().getEvaluationExpressionModel().getEvaluationExpressions();
			expressions = new ArrayList(exprs.length);
			for (int i = 0; i < exprs.length; i++) {
				EvaluationExpression expression = exprs[i];
				expressions.add(expression);
			}
		}

		public void importExpressions(InputStream in) throws IOException, FileNotFoundException {
			EvaluationExpression[] importedExprs = new EvaluationExpressionReaderWriter().read(in, null);
			for (int i = 0; i < importedExprs.length; i++) {
				expressions.add(importedExprs[i]);
			}
		}

		public void exportExpressions(EvaluationExpression[] expressions, OutputStream out) throws IOException {
			new EvaluationExpressionReaderWriter().save(expressions, out);
		}

	}

	private CheckboxTableViewer fTableViewer;

	/* buttons */
	private Button fAddButton;
	private Button fEditButton;
	private Button fImportButton;
	private Button fExportButton;
	private Button fRemoveButton;
	private EditableExpressionModel fModel;

	/**
	 * Creates a new template preference page.
	 */
	public EvaluationExpressionsPreferencePage() {
		super();
		fModel = new EditableExpressionModel();
		setDescription(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_description);
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite ancestor) {
		Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		parent.setLayout(layout);

		Composite innerParent = new Composite(parent, SWT.NONE);
		GridLayout innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = 0;
		innerLayout.marginWidth = 0;
		innerParent.setLayout(innerLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		innerParent.setLayoutData(gd);

		Table table = new Table(innerParent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(3);
		data.heightHint = convertHeightInCharsToPixels(10);
		table.setLayoutData(data);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_column_name);

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_column_description);

		fTableViewer = new CheckboxTableViewer(table);
		fTableViewer.setLabelProvider(new EvaluationExpressionLabelProvider());
		fTableViewer.setContentProvider(new EvaluationExpressionContentProvider());

		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent e) {
				edit();
			}
		});

		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent e) {
				selectionChanged1();
			}
		});

		fTableViewer.addCheckStateListener(new ICheckStateListener(){

			public void checkStateChanged(CheckStateChangedEvent event) {
				((EvaluationExpression) event.getElement()).setEnabled(event.getChecked());
			}
		});

		Composite buttons = new Composite(innerParent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		fAddButton = new Button(buttons, SWT.PUSH);
		fAddButton.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_new);
		fAddButton.setLayoutData(getButtonGridData(fAddButton));
		fAddButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				add();
			}
		});

		fEditButton = new Button(buttons, SWT.PUSH);
		fEditButton.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_edit);
		fEditButton.setLayoutData(getButtonGridData(fEditButton));
		fEditButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				edit();
			}
		});

		fRemoveButton = new Button(buttons, SWT.PUSH);
		fRemoveButton.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_remove);
		fRemoveButton.setLayoutData(getButtonGridData(fRemoveButton));
		fRemoveButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				remove();
			}
		});

		createSeparator(buttons);

		fImportButton = new Button(buttons, SWT.PUSH);
		fImportButton.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_import);
		fImportButton.setLayoutData(getButtonGridData(fImportButton));
		fImportButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				importFile();
			}
		});

		fExportButton = new Button(buttons, SWT.PUSH);
		fExportButton.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export);
		fExportButton.setLayoutData(getButtonGridData(fExportButton));
		fExportButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				exportFile();
			}
		});

		fTableViewer.setInput(fModel);
		setEnabledExpresions(fTableViewer);
		updateButtons();
		configureTableResizing(innerParent, buttons, table, column1, column2);

		Dialog.applyDialogFont(parent);
		return parent;
	}

	/**
	 * Sets the list checkboxes to proper state
	 *
	 * @param viewer checkbox viewer
	 */
	private void setEnabledExpresions(CheckboxTableViewer viewer) {
		for(Object o : fModel.getExpressionsAsArray()) {
			EvaluationExpression expr = (EvaluationExpression)o;
			viewer.setChecked(o, expr.isEnabled());
		}
	}

	/**
	 * Creates a separator between buttons
	 * 
	 * @param parent
	 * @return
	 */
	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		separator.setVisible(false);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	/**
	 * Correctly resizes the table so no phantom columns appear
	 */
	private static void configureTableResizing(final Composite parent, final Composite buttons, final Table table, final TableColumn column1, final TableColumn column2) {
		parent.addControlListener(new ControlAdapter() {

			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width - 2 * table.getBorderWidth();
				if (preferredSize.y > area.height) {
					// Subtract the scrollbar width from the total column width
					// if a vertical scrollbar will be required
					Point vBarSize = table.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				width -= buttons.getSize().x;
				Point oldSize = table.getSize();
				if (oldSize.x > width) {
					// table is getting smaller so make the columns
					// smaller first and then resize the table to
					// match the client area width
					column1.setWidth(width / 3);
					column2.setWidth(width - column1.getWidth());
					//column3.setWidth(width - (column1.getWidth() +
					// column2.getWidth()));
					table.setSize(width, area.height);
				} else {
					// table is getting bigger so make the table
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(width, area.height);
					column1.setWidth(width / 3);
					column2.setWidth(width - column1.getWidth());
					//column3.setWidth(width - (column1.getWidth() +
					// column2.getWidth()));
				}
			}
		});
	}

	private static GridData getButtonGridData(Button button) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint= SWTUtil.getButtonWidthHint(button);
		data.heightHint= SWTUtil.getButtonHeightHint(button);
		return data;
	}

	private void selectionChanged1() {
		updateButtons();
	}

	/**
	 * Updates the buttons.
	 */
	protected void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
		int selectionCount = selection.size();
		int itemCount = fTableViewer.getTable().getItemCount();
		fEditButton.setEnabled(selectionCount == 1);
		fExportButton.setEnabled(selectionCount > 0);
		fRemoveButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
	}

	private void add() {

		EvaluationExpression evalExpression = new EvaluationExpression("", "", "", false);
		String title = RdtDebugUiMessages.EditEvaluationExpressionDialog_add;
		Dialog dialog = new EditEvaluationExpressionDialog(getShell(), title, evalExpression);
		if (dialog.open() == Window.OK) {
			fModel.addExpression(evalExpression);
			fTableViewer.refresh();
			fTableViewer.setSelection(new StructuredSelection(evalExpression));
		}
	}

	private void edit() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();

		Object[] objects = selection.toArray();
		if ((objects == null) || (objects.length != 1)) return;

		EvaluationExpression data = (EvaluationExpression) selection.getFirstElement();
		edit(data);
	}

	private void edit(EvaluationExpression evalExpression) {
		String title = RdtDebugUiMessages.EditEvaluationExpressionDialog_edit;
		Dialog dialog = new EditEvaluationExpressionDialog(getShell(), title, evalExpression);
		if (dialog.open() == Window.OK) {
			fTableViewer.refresh();
		}
	}

	private void importFile() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_import_title);
		dialog.setFilterExtensions(new String[] { RdtDebugUiMessages.EvaluationExpressionsPreferencePage_importexport_extension});
		String path = dialog.open();

		if (path == null) return;

		TemplateReaderWriter reader = new TemplateReaderWriter();
		File file = new File(path);
		if (file.exists()) {
			try {
				InputStream input = new BufferedInputStream(new FileInputStream(file));
				fModel.importExpressions(input);
			} catch (Exception e) {
                RdtDebugUiPlugin.log(e) ;
			}
		}

		fTableViewer.refresh();

	}

	private void exportFile() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
		Object[] expressions = selection.toArray();

		EvaluationExpression[] datas = new EvaluationExpression[expressions.length];
		for (int i = 0; i != expressions.length; i++)
			datas[i] = (EvaluationExpression) expressions[i];

		export(datas);
	}

	private void export(EvaluationExpression[] expressions) {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(RdtDebugUiMessages.getFormattedString(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_title, new Integer(expressions.length)));
		dialog.setFilterExtensions(new String[] { RdtDebugUiMessages.EvaluationExpressionsPreferencePage_importexport_extension});
		dialog.setFileName(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_filename);
		String path = dialog.open();

		if (path == null) return;

		File file = new File(path);

		if (file.isHidden()) {
			String title = RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_error_title;
			String message = RdtDebugUiMessages.getFormattedString(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_error_hidden, file.getAbsolutePath());
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (file.exists() && !file.canWrite()) {
			String title = RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_error_title;
			String message = RdtDebugUiMessages.getFormattedString(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_error_canNotWrite, file.getAbsolutePath());
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (!file.exists() || confirmOverwrite(file)) {
			try {
				OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
				fModel.exportExpressions(expressions, output);
			} catch (Exception e) {
				//openWriteErrorDialog(e);
			}
		}
	}

	private boolean confirmOverwrite(File file) {
		return MessageDialog.openQuestion(getShell(), RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_exists_title,
				RdtDebugUiMessages.getFormattedString(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_export_exists_message, file.getAbsolutePath()));
	}

	private void remove() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();

		Iterator elements = selection.iterator();
		while (elements.hasNext()) {
			fModel.removeExpression((EvaluationExpression) elements.next());
		}
		fTableViewer.refresh();
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) setTitle(RdtDebugUiMessages.EvaluationExpressionsPreferencePage_title);
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		RdtDebugUiPlugin.getDefault().getPluginPreferences().setToDefault(RdtDebugUiConstants.EVALUATION_EXPRESSIONS_PREFERENCE) ;
		fModel.load();
		fTableViewer.refresh();
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {

		fModel.save();
		return super.performOk();
	}

	/*
	 * @see PreferencePage#performCancel()
	 */
	public boolean performCancel() {

		fModel.load();
		return super.performCancel();
	}

	protected CheckboxTableViewer getTableViewer() {
		return fTableViewer;
	}
}