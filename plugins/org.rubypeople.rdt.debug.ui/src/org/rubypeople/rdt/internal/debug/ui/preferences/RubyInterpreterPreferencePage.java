package org.rubypeople.rdt.internal.debug.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.AddVMDialog;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.IAddVMDialogRequestor;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.RubyVMMessages;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.RubyVMsUpdater;
import org.rubypeople.rdt.internal.ui.util.CollectionContentProvider;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.launching.VMStandin;

public class RubyInterpreterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IAddVMDialogRequestor {

	/**
	 * VMs being displayed
	 */
	private List<IVMInstall> fVMs = new ArrayList<IVMInstall>();

	protected CheckboxTableViewer fVMList;
	protected Button addButton, editButton, removeButton;

	public RubyInterpreterPreferencePage() {
		super();
		// only used when page is shown programatically
		setTitle(RubyVMMessages.JREsPreferencePage_1);

		setDescription(RubyVMMessages.JREsPreferencePage_2);
	}

	public void init(IWorkbench workbench) {}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();

		Composite composite = createPageRoot(parent);
		
		Label tableLabel = new Label(composite, SWT.NONE);
		tableLabel.setText(RubyVMMessages.InstalledJREsBlock_15); 
		GridData data = new GridData();
		data.horizontalSpan = 2;
		tableLabel.setLayoutData(data);
		Font font = parent.getFont();
		composite.setFont(font);	
		tableLabel.setFont(font);
		
		Table table = createInstalledInterpretersTable(composite);
		createInstalledInterpretersTableViewer(table);
		createButtonGroup(composite);

		fillWithWorkspaceRubyVMs();

		IVMInstall selectedInterpreter = RubyRuntime.getDefaultVMInstall();
		if (selectedInterpreter != null)
			fVMList.setChecked(selectedInterpreter, true);

		enableButtons();

		return composite;
	}

	private void fillWithWorkspaceRubyVMs() {
		// fill with Ruby VMs
		List<VMStandin> standins = new ArrayList<VMStandin>();
		IVMInstallType[] types = RubyRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++) {
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++) {
				IVMInstall install = installs[j];
				standins.add(new VMStandin(install));
			}
		}
		setJREs((IVMInstall[]) standins.toArray(new IVMInstall[standins.size()]));
	}

	/**
	 * Sets the JREs to be displayed in this block
	 * 
	 * @param vms
	 *            JREs to be displayed
	 */
	protected void setJREs(IVMInstall[] vms) {
		fVMs.clear();
		for (int i = 0; i < vms.length; i++) {
			fVMs.add(vms[i]);
		}
		fVMList.setInput(fVMs);
		fVMList.refresh();
	}

	protected void createButtonGroup(Composite composite) {
		Composite buttons = new Composite(composite, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		addButton = new Button(buttons, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(RdtDebugUiMessages.RubyInterpreterPreferencePage_addButton_label);
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addInterpreter();
			}
		});

		editButton = new Button(buttons, SWT.PUSH);
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.setText(RdtDebugUiMessages.RubyInterpreterPreferencePage_editButton_label);
		editButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editInterpreter();
			}
		});

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText(RdtDebugUiMessages.RubyInterpreterPreferencePage_removeButton_label);
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeInterpreter();
			}
		});
	}

	protected void createInstalledInterpretersTableViewer(Table table) {
		fVMList = new CheckboxTableViewer(table);

		fVMList.setLabelProvider(new RubyInterpreterLabelProvider());
		fVMList.setContentProvider(new CollectionContentProvider());

		fVMList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});

		fVMList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateSelectedInterpreter(event.getElement());
			}
		});

		fVMList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				editInterpreter();
			}
		});
	}

	protected Table createInstalledInterpretersTable(Composite composite) {
		Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText(RdtDebugUiMessages.RubyInterpreterPreferencePage_rubyInterpreterTable_interpreterName);
		column.setWidth(125);

		column = new TableColumn(table, SWT.NULL);
		column.setText(RdtDebugUiMessages.RubyInterpreterPreferencePage_rubyInterpreterTable_interpreterPath);
		column.setWidth(250);
		
		column = new TableColumn(table, SWT.NULL);
		column.setText(RdtDebugUiMessages.RubyInterpreterPreferencePage_rubyInterpreterTable_interpreterType);
		column.setWidth(125);

		return table;
	}

	protected Composite createPageRoot(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		return composite;
	}

	protected void addInterpreter() {
		AddVMDialog dialog = new AddVMDialog(this, getShell(), RubyRuntime.getVMInstallTypes(), null);
		dialog.setTitle(RubyVMMessages.InstalledJREsBlock_7);
		if (dialog.open() != Window.OK) {
			return;
		}
	}

	protected void removeInterpreter() {
		fVMs.remove(getSelectedInterpreter());
		fVMList.refresh();
	}

	protected void enableButtons() {
		if (getSelectedInterpreter() != null) {
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
		} else {
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
	}

	protected void updateSelectedInterpreter(Object interpreter) {
		Object[] checkedElements = fVMList.getCheckedElements();
		for (int i = 0; i < checkedElements.length; i++) {
			fVMList.setChecked(checkedElements[i], false);
		}

		fVMList.setChecked(interpreter, true);
	}

	protected void editInterpreter() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		IVMInstall vm = (IVMInstall) selection.getFirstElement();
		if (vm == null) {
			return;
		}
		// if (isContributed(vm)) {
		// VMDetailsDialog dialog= new VMDetailsDialog(getShell(), vm);
		// dialog.open();
		// } else {
		AddVMDialog dialog = new AddVMDialog(this, getShell(), RubyRuntime.getVMInstallTypes(), vm);
		dialog.setTitle(RubyVMMessages.InstalledJREsBlock_8);
		if (dialog.open() != Window.OK) {
			return;
		}
		fVMList.refresh(vm);
		// }
	}

	protected IVMInstall getSelectedInterpreter() {
		IStructuredSelection selection = (IStructuredSelection) fVMList.getSelection();
		return (IVMInstall) selection.getFirstElement();
	}

	public boolean performOk() {
		final boolean[] canceled = new boolean[] { false };
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				IVMInstall defaultVM = getCheckedRubyVM();
				IVMInstall[] vms = getRubyVMs();
				RubyVMsUpdater updater = new RubyVMsUpdater();
				if (!updater.updateRubyVMSettings(vms, defaultVM)) {
					canceled[0] = true;
				}
			}
		});

		if (canceled[0]) {
			return false;
		}

		return super.performOk();
	}

	/**
	 * Returns the checked RubyVM or <code>null</code> if none.
	 * 
	 * @return the checked RubyVM or <code>null</code> if none
	 */
	public IVMInstall getCheckedRubyVM() {
		Object[] objects = fVMList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (IVMInstall) objects[0];
	}

	/**
	 * Returns the RubyVMs currently being displayed in this block
	 * 
	 * @return RubyVMs currently being displayed in this block
	 */
	public IVMInstall[] getRubyVMs() {
		return (IVMInstall[]) fVMs.toArray(new IVMInstall[fVMs.size()]);
	}

	public boolean isDuplicateName(String name) {
		for (int i = 0; i < fVMs.size(); i++) {
			IVMInstall vm = (IVMInstall) fVMs.get(i);
			if (vm.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void vmAdded(IVMInstall vm) {
		fVMs.add(vm);
		fVMList.refresh();
	}

}