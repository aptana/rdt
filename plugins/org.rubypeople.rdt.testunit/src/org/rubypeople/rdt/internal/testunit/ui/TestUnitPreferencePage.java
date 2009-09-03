/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids: sdavids@gmx.de
 *******************************************************************************/
package org.rubypeople.rdt.internal.testunit.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.rubypeople.rdt.internal.testunit.util.LayoutUtil;
import org.rubypeople.rdt.internal.ui.util.SWTUtil;
import org.rubypeople.rdt.internal.ui.util.TableLayoutComposite;
import org.rubypeople.rdt.ui.ISharedImages;
import org.rubypeople.rdt.ui.RubyUI;

/**
 * Preference page for TestUnit settings. Supports to define the failure
 * stack filter patterns.
 */
public class TestUnitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DEFAULT_NEW_FILTER_TEXT= ""; //$NON-NLS-1$
	private static final Image IMG_CUNIT= RubyUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
	private static final Image IMG_PKG= RubyUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_SOURCE_FOLDER);
	
	// enable assertions widget
//	private Button fEnableAssertionsCheckBox;

	// Step filter widgets
	private Label fFilterViewerLabel;
	private CheckboxTableViewer fFilterViewer;
	private Table fFilterTable;

	private Button fRemoveFilterButton;
	private Button fAddFilterButton;

	private Button fEnableAllButton;
	private Button fDisableAllButton;

	private Text fEditorText;
	private String fInvalidEditorText= null;
	private TableEditor fTableEditor;
	private TableItem fNewTableItem;
	private Filter fNewStackFilter;

	private StackFilterContentProvider fStackFilterContentProvider;

	/**
	 * Model object that represents a single entry in the filter table.
	 */
	private static class Filter {

		private String fName;
		private boolean fChecked;

		public Filter(String name, boolean checked) {
			setName(name);
			setChecked(checked);
		}

		public String getName() {
			return fName;
		}
		
		public void setName(String name) {
			fName= name;
		}
				
		public boolean isChecked() {
			return fChecked;
		}

		public void setChecked(boolean checked) {
			fChecked= checked;
		}

		public boolean equals(Object o) {
			if (!(o instanceof Filter))
				return false;

			Filter other= (Filter) o;
			return (getName().equals(other.getName()));
		}

		public int hashCode() {
			return fName.hashCode();
		}
	}

	/**
	 * Sorter for the filter table; sorts alphabetically ascending.
	 */
	private static class FilterViewerSorter extends WorkbenchViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			ILabelProvider lprov= (ILabelProvider) ((ContentViewer) viewer).getLabelProvider();
			String name1= lprov.getText(e1);
			String name2= lprov.getText(e2);
			if (name1 == null)
				name1= ""; //$NON-NLS-1$

			if (name2 == null)
				name2= ""; //$NON-NLS-1$

			if (name1.length() > 0 && name2.length() > 0) {
				char char1= name1.charAt(name1.length() - 1);
				char char2= name2.charAt(name2.length() - 1);
				if (char1 == '*' && char1 != char2)
					return -1;

				if (char2 == '*' && char2 != char1)
					return 1;
			}
			return name1.compareTo(name2);
		}
	}

	/**
	 * Label provider for Filter model objects
	 */
	private static class FilterLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object object, int column) {
			return (column == 0) ? ((Filter) object).getName() : ""; //$NON-NLS-1$
		}

		public String getText(Object element) {
			return ((Filter) element).getName();
		}

		public Image getColumnImage(Object object, int column) {
			String name= ((Filter) object).getName();
			if (name.endsWith(".*") || name.equals(TestUnitMessages.TestUnitMainTab_label_defaultpackage)) {  //$NON-NLS-1$
				//package
				return IMG_PKG;
			} else if ("".equals(name)) { //$NON-NLS-1$
				//needed for the in-place editor
				return null;
			} else if ((Character.isUpperCase(name.charAt(0))) && (name.indexOf('.') < 0)) {
				//class in default package
				return IMG_CUNIT;
			} else {
				//fully-qualified class or other filter
				final int lastDotIndex= name.lastIndexOf('.');
				if ((-1 != lastDotIndex) && ((name.length() - 1) != lastDotIndex) && Character.isUpperCase(name.charAt(lastDotIndex + 1)))
					return IMG_CUNIT;
			}
			//other filter
			return null;
		}
	}

	/**
	 * Content provider for the filter table.  Content consists of instances of
	 * Filter.
	 */
	private class StackFilterContentProvider implements IStructuredContentProvider {

		private List fFilters;

		public StackFilterContentProvider() {
			List active= createActiveStackFiltersList();
			List inactive= createInactiveStackFiltersList();
			populateFilters(active, inactive);
		}

		public void setDefaults() {
			fFilterViewer.remove(fFilters.toArray());
			List active= TestUnitPreferencesConstants.createDefaultStackFiltersList();
			List inactive= new ArrayList();
			populateFilters(active, inactive);
		}

		protected void populateFilters(List activeList, List inactiveList) {
			fFilters= new ArrayList(activeList.size() + inactiveList.size());
			populateList(activeList, true);
			if (inactiveList.size() != 0)
				populateList(inactiveList, false);
		}

		protected void populateList(List list, boolean checked) {
			Iterator iterator= list.iterator();

			while (iterator.hasNext()) {
				String name= (String) iterator.next();
				addFilter(name, checked);
			}
		}

		public Filter addFilter(String name, boolean checked) {
			Filter filter= new Filter(name, checked);
			if (!fFilters.contains(filter)) {
				fFilters.add(filter);
				fFilterViewer.add(filter);
				fFilterViewer.setChecked(filter, checked);
			}
			updateActions();
			return filter;
		}

		public void saveFilters() {
			List active= new ArrayList(fFilters.size());
			List inactive= new ArrayList(fFilters.size());
			Iterator iterator= fFilters.iterator();
			while (iterator.hasNext()) {
				Filter filter= (Filter) iterator.next();
				String name= filter.getName();
				if (filter.isChecked())
					active.add(name);
				else
					inactive.add(name);
			}
			String pref= TestUnitPreferencesConstants.serializeList((String[]) active.toArray(new String[active.size()]));
			getPreferenceStore().setValue(TestUnitPreferencesConstants.PREF_ACTIVE_FILTERS_LIST, pref);
			pref= TestUnitPreferencesConstants.serializeList((String[]) inactive.toArray(new String[inactive.size()]));
			getPreferenceStore().setValue(TestUnitPreferencesConstants.PREF_INACTIVE_FILTERS_LIST, pref);
		}

		public void removeFilters(Object[] filters) {
			for (int i= (filters.length - 1); i >= 0; --i) {
				Filter filter= (Filter) filters[i];
				fFilters.remove(filter);
			}
			fFilterViewer.remove(filters);
			updateActions();
		}

		public void toggleFilter(Filter filter) {
			boolean newState= !filter.isChecked();
			filter.setChecked(newState);
			fFilterViewer.setChecked(filter, newState);
		}

		public Object[] getElements(Object inputElement) {
			return fFilters.toArray();
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		public void dispose() {}

	}
		
	public TestUnitPreferencePage() {
		super();
		setDescription(TestUnitMessages.TestUnitPreferencePage_description); 
		setPreferenceStore(TestunitPlugin.getDefault().getPreferenceStore());
	}

	protected Control createContents(Composite parent) {
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ITestUnitHelpContextIds.TestUNIT_PREFERENCE_PAGE);

		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		GridData data= new GridData();
		data.verticalAlignment= GridData.FILL;
		data.horizontalAlignment= GridData.FILL;
		composite.setLayoutData(data);

//		createEnableAssertionsCheckbox(composite);
		createStackFilterPreferences(composite);
		Dialog.applyDialogFont(composite);
		return composite;
	}

//	private void createEnableAssertionsCheckbox(Composite container) {
//		fEnableAssertionsCheckBox= new Button(container, SWT.CHECK | SWT.WRAP);
//		fEnableAssertionsCheckBox.setText(TestUnitMessages.TestUnitPreferencePage_enableassertionscheckbox_label); 
//		fEnableAssertionsCheckBox.setToolTipText(TestUnitMessages.TestUnitPreferencePage_enableassertionscheckbox_tooltip); 
//		GridData gd= getButtonGridData(fEnableAssertionsCheckBox);
//		fEnableAssertionsCheckBox.setLayoutData(gd);
//		SWTUtil.setButtonDimensionHint(fEnableAssertionsCheckBox);
//		setAssertionCheckBoxSelection(AssertionVMArg.getEnableAssertionsPreference());
//	}
	
	/**
	 * Programatic access to enable assertions checkbox
	 * @return boolean indicating check box selected or not
	 */
//	public boolean getAssertionCheckBoxSelection() {
//		return fEnableAssertionsCheckBox.getSelection();
//	}
//
//	public void setAssertionCheckBoxSelection(boolean selected) {
//		fEnableAssertionsCheckBox.setSelection(selected);
//	}

	/*
	 * Create a group to contain the step filter related widgets
	 */
	private void createStackFilterPreferences(Composite composite) {
		fFilterViewerLabel= new Label(composite, SWT.SINGLE | SWT.LEFT);
		fFilterViewerLabel.setText(TestUnitMessages.TestUnitPreferencePage_filter_label);
		
		Composite container= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		container.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);

		createFilterTable(container);
		createStepFilterButtons(container);
	}

	private void createFilterTable(Composite container) {
		TableLayoutComposite layouter= new TableLayoutComposite(container, SWT.NONE);
		layouter.addColumnData(new ColumnWeightData(100));
		layouter.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fFilterTable= new Table(layouter, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		new TableColumn(fFilterTable, SWT.NONE);
		fFilterViewer= new CheckboxTableViewer(fFilterTable);
		fTableEditor= new TableEditor(fFilterTable);
		fFilterViewer.setLabelProvider(new FilterLabelProvider());
		fFilterViewer.setSorter(new FilterViewerSorter());
		fStackFilterContentProvider= new StackFilterContentProvider();
		fFilterViewer.setContentProvider(fStackFilterContentProvider);
		// input just needs to be non-null
		fFilterViewer.setInput(this);
		fFilterViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Filter filter= (Filter) event.getElement();
				fStackFilterContentProvider.toggleFilter(filter);
			}
		});
		fFilterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection= event.getSelection();
				fRemoveFilterButton.setEnabled(!selection.isEmpty());
			}
		});
	}

	private void createStepFilterButtons(Composite container) {
		Composite buttonContainer= new Composite(container, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout= new GridLayout();
		buttonLayout.numColumns= 1;
		buttonLayout.marginHeight= 0;
		buttonLayout.marginWidth= 0;
		buttonContainer.setLayout(buttonLayout);

		fAddFilterButton= new Button(buttonContainer, SWT.PUSH);
		fAddFilterButton.setText(TestUnitMessages.TestUnitPreferencePage_addfilterbutton_label); 
		fAddFilterButton.setToolTipText(TestUnitMessages.TestUnitPreferencePage_addfilterbutton_tooltip); 
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		fAddFilterButton.setLayoutData(gd);
		LayoutUtil.setButtonDimensionHint(fAddFilterButton);
		fAddFilterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				editFilter();
			}
		});

		fRemoveFilterButton= new Button(buttonContainer, SWT.PUSH);
		fRemoveFilterButton.setText(TestUnitMessages.TestUnitPreferencePage_removefilterbutton_label); 
		fRemoveFilterButton.setToolTipText(TestUnitMessages.TestUnitPreferencePage_removefilterbutton_tooltip); 
		gd= getButtonGridData(fRemoveFilterButton);
		fRemoveFilterButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fRemoveFilterButton);
		fRemoveFilterButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				removeFilters();
			}
		});
		fRemoveFilterButton.setEnabled(false);

		fEnableAllButton= new Button(buttonContainer, SWT.PUSH);
		fEnableAllButton.setText(TestUnitMessages.TestUnitPreferencePage_enableallbutton_label); 
		fEnableAllButton.setToolTipText(TestUnitMessages.TestUnitPreferencePage_enableallbutton_tooltip); 
		gd= getButtonGridData(fEnableAllButton);
		fEnableAllButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fEnableAllButton);
		fEnableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				checkAllFilters(true);
			}
		});

		fDisableAllButton= new Button(buttonContainer, SWT.PUSH);
		fDisableAllButton.setText(TestUnitMessages.TestUnitPreferencePage_disableallbutton_label); 
		fDisableAllButton.setToolTipText(TestUnitMessages.TestUnitPreferencePage_disableallbutton_tooltip); 
		gd= getButtonGridData(fDisableAllButton);
		fDisableAllButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(fDisableAllButton);
		fDisableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				checkAllFilters(false);
			}
		});

	}

	private GridData getButtonGridData(Button button) {
		GridData gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		int widthHint= convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		gd.widthHint= Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		return gd;
	}

	public void init(IWorkbench workbench) {}
	
	/**
	 * Create a new filter in the table (with the default 'new filter' value),
	 * then open up an in-place editor on it.
	 */
	private void editFilter() {
		// if a previous edit is still in progress, finish it
		if (fEditorText != null)
			validateChangeAndCleanup();

		fNewStackFilter= fStackFilterContentProvider.addFilter(DEFAULT_NEW_FILTER_TEXT, true);
		fNewTableItem= fFilterTable.getItem(0);

		// create & configure Text widget for editor
		// Fix for bug 1766.  Border behavior on for text fields varies per platform.
		// On Motif, you always get a border, on other platforms,
		// you don't.  Specifying a border on Motif results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		int textStyles= SWT.SINGLE | SWT.LEFT;
		if (!SWT.getPlatform().equals("motif")) //$NON-NLS-1$
			textStyles |= SWT.BORDER;

		fEditorText= new Text(fFilterTable, textStyles);
		GridData gd= new GridData(GridData.FILL_BOTH);
		fEditorText.setLayoutData(gd);

		// set the editor
		fTableEditor.horizontalAlignment= SWT.LEFT;
		fTableEditor.grabHorizontal= true;
		fTableEditor.setEditor(fEditorText, fNewTableItem, 0);

		// get the editor ready to use
		fEditorText.setText(fNewStackFilter.getName());
		fEditorText.selectAll();
		setEditorListeners(fEditorText);
		fEditorText.setFocus();
	}

	private void setEditorListeners(Text text) {
		// CR means commit the changes, ESC means abort and don't commit
		text.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.character == SWT.CR) {
					if (fInvalidEditorText != null) {
						fEditorText.setText(fInvalidEditorText);
						fInvalidEditorText= null;
					} else
						validateChangeAndCleanup();
				} else if (event.character == SWT.ESC) {
					removeNewFilter();
					cleanupEditor();
				}
			}
		});
		// Consider loss of focus on the editor to mean the same as CR
		text.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				if (fInvalidEditorText != null) {
					fEditorText.setText(fInvalidEditorText);
					fInvalidEditorText= null;
				} else
					validateChangeAndCleanup();
			}
		});
		// Consume traversal events from the text widget so that CR doesn't 
		// traverse away to dialog's default button.  Without this, hitting
		// CR in the text field closes the entire dialog.
		text.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				event.doit= false;
			}
		});
	}

	private void validateChangeAndCleanup() {
		String trimmedValue= fEditorText.getText().trim();
		// if the new value is blank, remove the filter
		if (trimmedValue.length() < 1)
			removeNewFilter();

		// if it's invalid, beep and leave sitting in the editor
		else if (!validateEditorInput(trimmedValue)) {
			fInvalidEditorText= trimmedValue;
			fEditorText.setText(TestUnitMessages.TestUnitPreferencePage_invalidstepfilterreturnescape); 
			getShell().getDisplay().beep();
			return;
			// otherwise, commit the new value if not a duplicate
		} else {
			Object[] filters= fStackFilterContentProvider.getElements(null);
			for (int i= 0; i < filters.length; i++) {
				Filter filter= (Filter) filters[i];
				if (filter.getName().equals(trimmedValue)) {
					removeNewFilter();
					cleanupEditor();
					return;
				}
			}
			fNewTableItem.setText(trimmedValue);
			fNewStackFilter.setName(trimmedValue);
			fFilterViewer.refresh();
		}
		cleanupEditor();
	}

	/*
	 * Cleanup all widgets & resources used by the in-place editing
	 */
	private void cleanupEditor() {
		if (fEditorText == null)
			return;

		fNewStackFilter= null;
		fNewTableItem= null;
		fTableEditor.setEditor(null, null, 0);
		fEditorText.dispose();
		fEditorText= null;
	}

	private void removeNewFilter() {
		fStackFilterContentProvider.removeFilters(new Object[] { fNewStackFilter });
	}

	/*
	 * A valid step filter is simply one that is a valid Java identifier.
	 * and, as defined in the JDI spec, the regular expressions used for
	 * step filtering must be limited to exact matches or patterns that
	 * begin with '*' or end with '*'. Beyond this, a string cannot be validated
	 * as corresponding to an existing type or package (and this is probably not
	 * even desirable).  
	 */
	private boolean validateEditorInput(String trimmedValue) {
		char firstChar= trimmedValue.charAt(0);
		if ((!(Character.isJavaIdentifierStart(firstChar)) || (firstChar == '*')))
			return false;

		int length= trimmedValue.length();
		for (int i= 1; i < length; i++) {
			char c= trimmedValue.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				if (c == '.' && i != (length - 1))
					continue;
				if (c == '*' && i == (length - 1))
					continue;

				return false;
			}
		}
		return true;
	}
	
	private void removeFilters() {
		IStructuredSelection selection= (IStructuredSelection) fFilterViewer.getSelection();
		fStackFilterContentProvider.removeFilters(selection.toArray());
	}

	private void checkAllFilters(boolean check) {
		Object[] filters= fStackFilterContentProvider.getElements(null);
		for (int i= (filters.length - 1); i >= 0; --i)
			 ((Filter) filters[i]).setChecked(check);

		fFilterViewer.setAllChecked(check);
	}
	
	public boolean performOk() {
//		AssertionVMArg.setEnableAssertionsPreference(getAssertionCheckBoxSelection());
		fStackFilterContentProvider.saveFilters();
		return true;
	}

	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();
	}

	private void setDefaultValues() {
//		fEnableAssertionsCheckBox.setSelection(false);
		fStackFilterContentProvider.setDefaults();
	}

	/**
	 * Returns a list of active stack filters.
	 * 
	 * @return list
	 */
	protected List<String> createActiveStackFiltersList() {
		return Arrays.asList(getFilterPatterns());
	}

	/**
	 * Returns a list of active stack filters.
	 * 
	 * @return list
	 */
	protected List<String> createInactiveStackFiltersList() {
		String[] strings=
			TestUnitPreferencesConstants.parseList(getPreferenceStore().getString(TestUnitPreferencesConstants.PREF_INACTIVE_FILTERS_LIST));
		return Arrays.asList(strings);
	}

	protected void updateActions() {
		if (fEnableAllButton == null)
			return;

		boolean enabled= fFilterViewer.getTable().getItemCount() > 0;
		fEnableAllButton.setEnabled(enabled);
		fDisableAllButton.setEnabled(enabled);
	}

	public static String[] getFilterPatterns() {
		IPreferenceStore store= TestunitPlugin.getDefault().getPreferenceStore();
		return TestUnitPreferencesConstants.parseList(store.getString(TestUnitPreferencesConstants.PREF_ACTIVE_FILTERS_LIST));
	}

	public static boolean getFilterStack() {
		IPreferenceStore store= TestunitPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(TestUnitPreferencesConstants.DO_FILTER_STACK);
	}

	public static void setFilterStack(boolean filter) {
		IPreferenceStore store= TestunitPlugin.getDefault().getPreferenceStore();
		store.setValue(TestUnitPreferencesConstants.DO_FILTER_STACK, filter);
	}
}
