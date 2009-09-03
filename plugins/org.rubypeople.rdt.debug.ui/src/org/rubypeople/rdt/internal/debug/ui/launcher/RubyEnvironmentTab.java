package org.rubypeople.rdt.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.rubypeople.rdt.core.ILoadpathEntry;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.core.RubyModelManager;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.AddVMDialog;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.IAddVMDialogRequestor;
import org.rubypeople.rdt.internal.debug.ui.rubyvms.RubyVMMessages;
import org.rubypeople.rdt.internal.launching.RubyLaunchConfigurationAttribute;
import org.rubypeople.rdt.internal.launching.RuntimeLoadpathEntry;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.launching.IRuntimeLoadpathEntry;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.RubyRuntime;
import org.rubypeople.rdt.launching.VMStandin;

public class RubyEnvironmentTab extends AbstractLaunchConfigurationTab implements SelectionListener
{
	protected ListViewer loadPathListViewer;
	protected List<IVMInstall> installedInterpretersWorkingCopy;
	protected Combo interpreterCombo;
	protected Button loadPathDefaultButton;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fUpButton;
	private Button fDownButton;

	public RubyEnvironmentTab()
	{
		super();
	}

	public void createControl(Composite parent)
	{
		Composite composite = createPageRoot(parent);

		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gridData);

		addLoadPathTab(tabFolder);
		addInterpreterTab(tabFolder);
	}

	protected void addLoadPathTab(TabFolder tabFolder)
	{
		Composite comp = new Composite(tabFolder, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		comp.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		loadPathListViewer = new ListViewer(comp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		loadPathListViewer.setContentProvider(new LoadPathContentProvider());
		loadPathListViewer.setLabelProvider(new LoadPathEntryLabelProvider());
		loadPathListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite pathButtonComp = new Composite(comp, SWT.NONE);
		GridLayout pathButtonLayout = new GridLayout();
		pathButtonLayout.marginHeight = 0;
		pathButtonLayout.marginWidth = 0;
		pathButtonComp.setLayout(pathButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		pathButtonComp.setLayoutData(gd);

		fAddButton = createPushButton(pathButtonComp, "Add...");
		fAddButton.addSelectionListener(this);

		fRemoveButton = createPushButton(pathButtonComp, "Remove");
		fRemoveButton.addSelectionListener(this);

		fUpButton = createPushButton(pathButtonComp, RubyVMMessages.VMLibraryBlock_4);
		fUpButton.addSelectionListener(this);

		fDownButton = createPushButton(pathButtonComp, RubyVMMessages.VMLibraryBlock_5);
		fDownButton.addSelectionListener(this);

		loadPathDefaultButton = new Button(comp, SWT.CHECK);
		loadPathDefaultButton
				.setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_loadPathDefaultButton_label);
		loadPathDefaultButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		loadPathDefaultButton.addSelectionListener(getLoadPathDefaultButtonSelectionListener());
		loadPathDefaultButton.setEnabled(true);

		TabItem loadPathTab = new TabItem(tabFolder, SWT.NONE, 0);
		loadPathTab.setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_loadPathTab_label);
		loadPathTab.setControl(comp);
		loadPathTab.setData(loadPathListViewer);
	}

	/**
	 * Creates and returns a button
	 * 
	 * @param parent
	 *            parent widget
	 * @param label
	 *            label
	 * @return Button
	 */
	protected Button createPushButton(Composite parent, String label)
	{
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);
		// fDialog.setButtonLayoutData(button);
		return button;
	}

	protected SelectionListener getLoadPathSelectionListener()
	{
		return new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				System.out.println("Loadpath list selection occurred: " + e.getSource());
			}
		};
	}

	protected SelectionListener getLoadPathDefaultButtonSelectionListener()
	{
		return new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setUseLoadPathDefaults(((Button) e.getSource()).getSelection());
			}
		};
	}

	protected void addInterpreterTab(TabFolder tabFolder)
	{
		Composite interpreterComposite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		interpreterComposite.setLayout(layout);
		interpreterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createVerticalSpacer(interpreterComposite, 2);

		interpreterCombo = new Combo(interpreterComposite, SWT.READ_ONLY);
		interpreterCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initializeInterpreterCombo(interpreterCombo);
		interpreterCombo.addModifyListener(getInterpreterComboModifyListener());

		Button interpreterAddButton = new Button(interpreterComposite, SWT.PUSH);
		interpreterAddButton
				.setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_interpreterAddButton_label);
		interpreterAddButton.addSelectionListener(new AddInterpreterSelectionAdapter(interpreterCombo, getShell()));

		TabItem interpreterTab = new TabItem(tabFolder, SWT.NONE);
		interpreterTab.setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_interpreterTab_label);
		interpreterTab.setControl(interpreterComposite);
	}

	private static class AddInterpreterSelectionAdapter extends SelectionAdapter implements IAddVMDialogRequestor
	{

		private Combo fCombo;
		private Shell fShell;

		public AddInterpreterSelectionAdapter(Combo combo, Shell shell)
		{
			fCombo = combo;
			fShell = shell;
		}

		public void widgetSelected(SelectionEvent evt)
		{
			AddVMDialog dialog = new AddVMDialog(this, fShell, RubyRuntime.getVMInstallTypes(), null);
			dialog.setTitle(RubyVMMessages.InstalledJREsBlock_7);
			if (dialog.open() != Window.OK)
			{
				return;
			}
		}

		public boolean isDuplicateName(String name)
		{
			return false;
		}

		public void vmAdded(IVMInstall vm)
		{
			// FIXME Use something like JREsUpdater?
			if (vm instanceof VMStandin)
			{
				VMStandin standin = (VMStandin) vm;
				standin.convertToRealVM();
			}
			fCombo.add(vm.getName());
			fCombo.select(fCombo.indexOf(vm.getName()));
		}

	}

	protected ModifyListener getInterpreterComboModifyListener()
	{
		return new ModifyListener()
		{
			public void modifyText(ModifyEvent evt)
			{
				updateLaunchConfigurationDialog();
			}
		};
	}

	protected void createVerticalSpacer(Composite comp, int colSpan)
	{
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		IVMInstall defaultInterpreter = RubyRuntime.getDefaultVMInstall();
		if (defaultInterpreter != null)
		{
			configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, defaultInterpreter
					.getName());
			configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, defaultInterpreter
					.getVMInstallType().getId());
		}
	}

	public void initializeFrom(ILaunchConfiguration configuration)
	{
		initializeLoadPath(configuration);
		initializeInterpreterSelection(configuration);
	}

	protected void initializeLoadPath(ILaunchConfiguration configuration)
	{
		boolean useDefaultLoadPath = true;
		try
		{
			useDefaultLoadPath = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_DEFAULT_LOADPATH,
					true);
			setUseLoadPathDefaults(useDefaultLoadPath);

			if (useDefaultLoadPath)
			{
				String projectName = configuration
						.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
				if (projectName.length() != 0)
				{
					IRubyProject project = RubyModelManager.getRubyModelManager().getRubyModel().getRubyProject(
							projectName);
					if (project != null)
					{
						ILoadpathEntry[] entries = project.getRawLoadpath();
						entries = filterStandard(entries);
						loadPathListViewer.setInput(entries);
					}
				}
			}
			else
			{
				// use custom loadpath
				List<String> entries = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_LOADPATH,
						Collections.EMPTY_LIST);
				ILoadpathEntry[] rtes = new ILoadpathEntry[entries.size()];
				int i = 0;
				for (String entry : entries)
				{
					rtes[i] = RubyRuntime.newRuntimeLoadpathEntry(entry).getLoadpathEntry();
					i++;
				}
				loadPathListViewer.setInput(rtes);
			}
		}
		catch (CoreException e)
		{
			log(e);
		}
	}

	private ILoadpathEntry[] filterStandard(ILoadpathEntry[] entries)
	{
		List<ILoadpathEntry> copy = new ArrayList<ILoadpathEntry>();
		for (ILoadpathEntry loadpathEntry : entries)
		{
			if ((loadpathEntry.getEntryKind() == ILoadpathEntry.CPE_VARIABLE)
					|| (loadpathEntry.getEntryKind() == ILoadpathEntry.CPE_CONTAINER))
				continue;
			copy.add(loadpathEntry);
		}
		return copy.toArray(new ILoadpathEntry[copy.size()]);
	}

	protected void setUseLoadPathDefaults(boolean useDefaults)
	{
		loadPathListViewer.getList().setEnabled(!useDefaults);
		fAddButton.setEnabled(!useDefaults);
		fRemoveButton.setEnabled(!useDefaults);
		fUpButton.setEnabled(!useDefaults);
		fDownButton.setEnabled(!useDefaults);
		loadPathDefaultButton.setSelection(useDefaults);
		setDirty(true);
		updateLaunchConfigurationDialog();
	}

	protected void initializeInterpreterSelection(ILaunchConfiguration configuration)
	{
		String interpreterName = null;
		try
		{
			interpreterName = configuration.getAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
					(String) null);
		}
		catch (CoreException e)
		{
			log(e);
		}
		if (interpreterName != null && !interpreterName.equals(""))
			interpreterCombo.select(interpreterCombo.indexOf(interpreterName));
	}

	protected void initializeInterpreterCombo(Combo interpreterCombo)
	{
		installedInterpretersWorkingCopy = new ArrayList<IVMInstall>();
		List<IVMInstall> standins = new ArrayList<IVMInstall>();
		IVMInstallType[] types = RubyRuntime.getVMInstallTypes();
		for (int i = 0; i < types.length; i++)
		{
			IVMInstallType type = types[i];
			IVMInstall[] installs = type.getVMInstalls();
			for (int j = 0; j < installs.length; j++)
			{
				IVMInstall install = installs[j];
				standins.add(new VMStandin(install));
			}
		}
		installedInterpretersWorkingCopy.addAll(standins);

		String[] interpreterNames = new String[installedInterpretersWorkingCopy.size()];
		for (int interpreterIndex = 0; interpreterIndex < installedInterpretersWorkingCopy.size(); interpreterIndex++)
		{
			IVMInstall interpreter = (IVMInstall) installedInterpretersWorkingCopy.get(interpreterIndex);
			interpreterNames[interpreterIndex] = interpreter.getName();
		}
		interpreterCombo.setItems(interpreterNames);

		IVMInstall selectedInterpreter = RubyRuntime.getDefaultVMInstall();
		if (selectedInterpreter != null)
			interpreterCombo.select(interpreterCombo.indexOf(selectedInterpreter.getName()));
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		int selectionIndex = interpreterCombo.getSelectionIndex();
		if (selectionIndex >= 0)
		{
			IVMInstall vm = installedInterpretersWorkingCopy.get(selectionIndex);
			configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vm.getName());
			configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, vm.getVMInstallType()
					.getId());
		}

		setAttribute(IRubyLaunchConfigurationConstants.ATTR_DEFAULT_LOADPATH, configuration, loadPathDefaultButton
				.getSelection(), true);

		if (!loadPathDefaultButton.getSelection())
		{
			ILoadpathEntry[] loadPathEntries = (ILoadpathEntry[]) loadPathListViewer.getInput();
			List<String> loadPathStrings = new ArrayList<String>();
			for (int i = 0; i < loadPathEntries.length; i++)
			{
				try
				{
					ILoadpathEntry entry = loadPathEntries[i];
					if (entry.getEntryKind() == ILoadpathEntry.CPE_SOURCE)
					{
						if (entry.getPath().equals(
								new Path("/"
										+ configuration.getAttribute(
												IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""))))
						{
							continue;
						}
					}
					IRuntimeLoadpathEntry runtime = new RuntimeLoadpathEntry(entry);
					loadPathStrings.add(runtime.getMemento());
				}
				catch (CoreException e)
				{
					log(e);
				}
			}
			configuration.setAttribute(IRubyLaunchConfigurationConstants.ATTR_LOADPATH, loadPathStrings);
		}
	}

	protected Composite createPageRoot(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		createVerticalSpacer(composite, 2);
		setControl(composite);

		return composite;
	}

	public String getName()
	{
		return RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_name;
	}

	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		try
		{
			String selectedInterpreter = launchConfig.getAttribute(
					RubyLaunchConfigurationAttribute.SELECTED_INTERPRETER, "");
			if (selectedInterpreter.length() == 0)
			{
				setErrorMessage(RdtDebugUiMessages.LaunchConfigurationTab_RubyEnvironment_interpreter_not_selected_error_message);
				return false;
			}
		}
		catch (CoreException e)
		{
			log(e);
		}

		setErrorMessage(null);
		return true;
	}

	protected void log(Throwable t)
	{
		RdtDebugUiPlugin.log(t);
	}

	public Image getImage()
	{
		return RubyPluginImages.get(RubyPluginImages.IMG_CTOOLS_RUBY);
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

	public void widgetSelected(SelectionEvent e)
	{
		if (e.getSource().equals(fAddButton))
		{
			// Pop open a directory dialog, add it to the loadpath
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			String result = dialog.open();
			if (result != null)
			{
				// Add to custom loadpath!
				ILoadpathEntry[] loadPathEntries = (ILoadpathEntry[]) loadPathListViewer.getInput();
				ILoadpathEntry[] copy = new ILoadpathEntry[loadPathEntries.length + 1];
				System.arraycopy(loadPathEntries, 0, copy, 0, loadPathEntries.length);
				copy[loadPathEntries.length] = RubyCore.newLibraryEntry(new Path(result));
				loadPathListViewer.setInput(copy);
			}
		}
		else if (e.getSource().equals(fRemoveButton))
		{
			ISelection selection = loadPathListViewer.getSelection();
			if (selection instanceof IStructuredSelection)
			{
				IStructuredSelection structured = (IStructuredSelection) selection;
				ILoadpathEntry firstElement = (ILoadpathEntry) structured.getFirstElement();
				ILoadpathEntry[] loadPathEntries = (ILoadpathEntry[]) loadPathListViewer.getInput();
				ILoadpathEntry[] copy = new ILoadpathEntry[loadPathEntries.length - 1];
				int i = 0;
				for (ILoadpathEntry entry : loadPathEntries)
				{
					if (entry.equals(firstElement))
						continue;
					copy[i++] = entry;
				}
				loadPathListViewer.setInput(copy);
			}
		}
		// TODO Re-order selected loadpath when up/down clicked
	}

}