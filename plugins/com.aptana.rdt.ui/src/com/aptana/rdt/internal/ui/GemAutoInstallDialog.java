package com.aptana.rdt.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.internal.ui.util.CollectionContentProvider;
import org.rubypeople.rdt.ui.TableViewerSorter;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.LocalFileGem;
import com.aptana.rdt.ui.AptanaRDTUIPlugin;
import com.aptana.rdt.ui.preferences.IPreferenceConstants;

/**
 * GemAutoInstallDialog
 * 
 * @author Kevin Sawicki (tweaked buttons and make modal)
 */
public class GemAutoInstallDialog extends StatusDialog
{

	private CheckboxTableViewer gemViewer;
	private IStructuredContentProvider contentProvider;
	private Collection<Gem> input;

	/**
	 * Selected gem list
	 */
	protected List<Gem> selected;
	private Button dontPromptButton;

	/**
	 * GemAutoInstallDialog
	 * 
	 * @param shell
	 * @param gems
	 */
	protected GemAutoInstallDialog(Shell shell, Collection<Gem> gems)
	{
		super(shell);
		contentProvider = new CollectionContentProvider();
		selected = new ArrayList<Gem>();
		this.input = gems;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText("Auto-install gems");
		Composite control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		control.setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 400;
		control.setLayoutData(data);

		Label sourceLabel = new Label(control, SWT.LEFT | SWT.WRAP);
		sourceLabel
				.setText("Plugins have contributed local copies of the following gems for automatic installation. Please select which gems you'd like installed for you. To not be prompted about installing particular gems, please select the gems to ignore and click the 'Ignore Selected' button at bottom.");
		sourceLabel.setLayoutData(data);

		Table gemsTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.CHECK);
		gemViewer = new CheckboxTableViewer(gemsTable);
		gemsTable.setHeaderVisible(true);
		gemsTable.setLinesVisible(false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 400;
		gemsTable.setLayoutData(data);

		TableColumn nameColumn = new TableColumn(gemsTable, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(200);

		TableColumn versionColumn = new TableColumn(gemsTable, SWT.LEFT);
		versionColumn.setText("Version");
		versionColumn.setWidth(100);

		TableColumn descriptionColumn = new TableColumn(gemsTable, SWT.LEFT);
		descriptionColumn.setText("Platform");
		descriptionColumn.setWidth(100);

		gemViewer.setLabelProvider(new ITableLabelProvider()
		{

			public void removeListener(ILabelProviderListener listener)
			{
			}

			public boolean isLabelProperty(Object element, String property)
			{
				return false;
			}

			public void dispose()
			{
			}

			public void addListener(ILabelProviderListener listener)
			{
			}

			public String getColumnText(Object element, int columnIndex)
			{
				Gem gem = (Gem) element;
				switch (columnIndex)
				{
					case 0:
						return gem.getName();
					case 1:
						return gem.getVersion();
					case 2:
						return gem.getPlatform();
					default:
						break;
				}
				return null;
			}

			public Image getColumnImage(Object element, int columnIndex)
			{
				return null;
			}

		});
		gemViewer.setContentProvider(contentProvider);
		gemViewer.setInput(input);
		TableViewerSorter.bind(gemViewer, 1);
		gemViewer.addCheckStateListener(new ICheckStateListener()
		{

			public void checkStateChanged(CheckStateChangedEvent event)
			{
				Object[] checked = gemViewer.getCheckedElements();
				selected.clear();
				for (int i = 0; i < checked.length; i++)
				{
					selected.add((Gem) checked[i]);
				}
				checkDependencies();
			}

		});
		gemViewer.setAllChecked(true);

		Composite dontPromptComp = new Composite(parent, SWT.NONE);
		dontPromptComp.setLayout(new GridLayout(2, false));

		dontPromptButton = new Button(dontPromptComp, SWT.CHECK);
		dontPromptButton.setSelection(!Platform.getPreferencesService().getBoolean(AptanaRDTUIPlugin.PLUGIN_ID, IPreferenceConstants.PROMPT_TO_AUTO_INSTALL_GEMS, true, null));
		Label dontPromptLabel = new Label(dontPromptComp, SWT.NONE);
		dontPromptLabel.setText("Don't prompt me about any gems (can be turned back on in preferences).");

		selected = new ArrayList<Gem>(input);

		setImage(RubyPluginImages.get(RubyPluginImages.IMG_CTOOLS_RUBY));
		return control;
	}

	@Override
	protected void cancelPressed()
	{
		storeDontPromptValue();
		super.cancelPressed();
	}

	@Override
	protected void okPressed()
	{
		storeDontPromptValue();
		super.okPressed();
	}

	private void storeDontPromptValue()
	{
		new InstanceScope().getNode(AptanaRDTUIPlugin.PLUGIN_ID).putBoolean(
				IPreferenceConstants.PROMPT_TO_AUTO_INSTALL_GEMS, !dontPromptButton.getSelection());
	}

	/**
	 * @see org.eclipse.jface.dialogs.StatusDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++)
		{
			if (children[i] instanceof Button)
			{
				Button button = (Button) children[i];
				Object data = button.getData();
				if (data instanceof Integer)
				{
					Integer value = (Integer) data;
					if (value.intValue() == IDialogConstants.OK_ID)
					{
						button.setText("Install");
					}
					else if (value.intValue() == IDialogConstants.CANCEL_ID)
					{
						button.setText("Close");
					}
				}
			}
		}
		Button dontAsk = createButton(parent, 3, "Ignore Selected", false);
		dontAsk.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// save a pref to not prompt user about the selected gems
				IPreferenceStore prefs = AptanaRDTUIPlugin.getDefault().getPreferenceStore();
				Collection<Gem> selectedGems = getSelectedGems();
				for (Gem gem : selectedGems)
				{
					prefs.setValue(getIgnorePrefKey(gem), true);
				}
				Collection<Gem> toRemove = new ArrayList<Gem>();
				TableItem[] items = gemViewer.getTable().getItems();
				for (int x = 0; x < items.length; x++)
				{
					if (items[x].getChecked())
					{
						items[x].setChecked(false);
						items[x].setGrayed(true);
						toRemove.add((Gem) items[x].getData());
					}
				}
				gemViewer.remove(toRemove.toArray(new Gem[toRemove.size()]));
				selected.clear();
				super.widgetSelected(e);
			}
		});
	}

	static String getIgnorePrefKey(Gem gem)
	{
		return "ignore-auto-install-" + gem.getName() + "-" + gem.getVersion();
	}

	/**
	 * Check the list of gems we're being asked to install, and make sure that their dependencies can be met
	 */
	protected void checkDependencies()
	{
		StringBuffer buffer = new StringBuffer();
		for (Gem gem : selected)
		{
			LocalFileGem local = (LocalFileGem) gem;
			Set<String> dependencies = local.getDependencies();
			for (String dependency : dependencies)
			{
				if (getGemManager().gemInstalled(dependency))
					continue; // already installed in system, so we're ok
				if (!contains(selected, dependency))
				{ // it's not installed, and not checked to be installed
					if (buffer.length() > 0)
						buffer.append("\n");
					buffer.append(local.getName() + " requires " + dependency);
				}
			}
		}
		if (buffer.length() > 0)
			updateStatus(new Status(Status.WARNING, AptanaRDTUIPlugin.PLUGIN_ID, -1, buffer.toString(), null));
		else
			updateStatus(Status.OK_STATUS);
	}

	private IGemManager getGemManager()
	{
		return AptanaRDTPlugin.getDefault().getGemManager();
	}

	private boolean contains(Collection<Gem> gems, String name)
	{
		for (Gem gem : gems)
		{
			if (gem.getName().equals(name))
				return true;
		}
		return false;
	}

	/**
	 * Gets the selected gems
	 * 
	 * @return - collection of selected gems
	 */
	public Collection<Gem> getSelectedGems()
	{
		return selected;
	}

}
