package com.aptana.rdt.ui.gems;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.internal.ui.RubyPluginImages;
import org.rubypeople.rdt.internal.ui.util.CollectionContentProvider;
import org.rubypeople.rdt.ui.TableViewerSorter;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.core.gems.LogicalGem;

/**
 * InstallGemDialog
 * 
 * @author Kevin Sawicki (added async jobs)
 */
public class InstallGemDialog extends Dialog
{

	private static final Gem LOADING_GEM = new Gem("Please wait", "N/A", "Loading remote gem listing...")
	{
		public boolean isInstallable()
		{
			return false;
		}
	};
	private Text nameText;
	private Combo versionCombo;

	private String name;
	private String version;

	private boolean filterByText = true;

	private TableViewer gemViewer;
	private IStructuredContentProvider contentProvider;
	private Combo sourceURLCombo;

	private String sourceURL = IGemManager.DEFAULT_GEM_HOST;
	private Set<Gem> gems;
	private Job gemJob;
	private Button sourceButton;
	private Image refreshImage;

	/**
	 * InstallGemDialog
	 * 
	 * @param parentShell
	 */
	public InstallGemDialog(Shell parentShell)
	{
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		contentProvider = new CollectionContentProvider();
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText(GemsMessages.InstallGemDialog_dialog_title);
		Composite control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		control.setLayout(layout);

		Label sourceLabel = new Label(control, SWT.LEFT);
		sourceLabel.setText("Source URL");

		sourceURLCombo = new Combo(control, SWT.DROP_DOWN);
		GridData sourceTextData = new GridData();
		sourceTextData.widthHint = 300;
		sourceTextData.horizontalSpan = 2;
		sourceURLCombo.setLayoutData(sourceTextData);
		updateSourceURLs();
		sourceURLCombo.setText(sourceURL);
		sourceURLCombo.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				sourceURL = sourceURLCombo.getText();
			}

		});
		sourceURLCombo.addTraverseListener(new TraverseListener()
		{

			public void keyTraversed(TraverseEvent e)
			{
				if (e.keyCode == SWT.CR)
				{
					loadSourceURL();
					e.doit = false;
				}
			}
		});

		sourceButton = new Button(control, SWT.NONE);
		sourceButton.setToolTipText("Refresh");
		refreshImage = RubyPluginImages.TOOLBAR_REFRESH.createImage();
		sourceButton.setImage(refreshImage);
		sourceButton.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				loadSourceURL();
			}

		});

		Label nameLabel = new Label(control, SWT.LEFT);
		nameLabel.setText(GemsMessages.InstallGemDialog_name_label);

		nameText = new Text(control, SWT.BORDER | SWT.SEARCH);
		GridData nameTextData = new GridData();
		nameTextData.widthHint = 150;
		nameText.setLayoutData(nameTextData);
		nameText.setEnabled(false);
		nameText.setMessage("Type portion of gem name to filter listing");

		Label versionLabel = new Label(control, SWT.RIGHT);
		versionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		versionLabel.setText(GemsMessages.InstallGemDialog_version_label);

		versionCombo = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		GridData versionComboData = new GridData();
		versionComboData.widthHint = 150;
		versionCombo.setLayoutData(versionComboData);
		versionCombo.setEnabled(false);

		gems = new HashSet<Gem>();
		gems.add(LOADING_GEM);

		final Table gemsTable = new Table(parent, SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION);
		gemsTable.setItemCount(gems.size());

		nameText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				if (filterByText)
				{
					getShell().getDisplay().asyncExec(new Runnable()
					{

						public void run()
						{
							// Filtering with the virtual table doesn't work with just using a ViewerFilter like it did
							// on normal table
							Set<Gem> filtered = filter(nameText.getText(), gems);
							gemsTable.setItemCount(filtered.size());
							gemsTable.clearAll();
							gemViewer.setInput(filtered);

						}

						private Set<Gem> filter(String filter, Set<Gem> gems)
						{
							Set<Gem> filtered = new HashSet<Gem>();
							for (Gem gem : gems)
							{
								if (gem.getName().toLowerCase().startsWith(filter))
									filtered.add(gem);
							}
							return filtered;
						}
					});
				}
				filterByText = true;
			}

		});

		gemViewer = new TableViewer(gemsTable);
		gemsTable.setHeaderVisible(true);
		gemsTable.setLinesVisible(false);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 400;
		gemsTable.setLayoutData(data);

		TableColumn nameColumn = new TableColumn(gemsTable, SWT.LEFT);
		nameColumn.setText(GemsMessages.GemsView_NameColumn_label);
		nameColumn.setWidth(150);

		TableColumn versionColumn = new TableColumn(gemsTable, SWT.LEFT);
		versionColumn.setText(GemsMessages.GemsView_VersionColumn_label);
		versionColumn.setWidth(75);

		TableColumn descriptionColumn = new TableColumn(gemsTable, SWT.LEFT);
		descriptionColumn.setText(GemsMessages.GemsView_DescriptionColumn_label);
		descriptionColumn.setWidth(275);

		gemViewer.setLabelProvider(new GemLabelProvider());
		gemViewer.setContentProvider(contentProvider);
		TableViewerSorter.bind(gemViewer);
		gemViewer.setInput(gems);

		gemViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection)
				{
					IStructuredSelection structured = (IStructuredSelection) selection;
					Gem gem = (Gem) structured.getFirstElement();
					if (gem == null)
					{
						versionCombo.removeAll();
						return;
					}
					filterByText = false; // don't filter list when I programmatically set text
					nameText.setText(gem.getName());
					versionCombo.removeAll();
					String lastVersion = null;
					if (gem instanceof LogicalGem)
					{
						LogicalGem logical = (LogicalGem) gem;
						SortedSet<String> versions = logical.getVersions();
						for (String version : versions)
						{
							versionCombo.add(version);
							lastVersion = version;
						}
					}
					else
					{
						versionCombo.add(gem.getVersion());
						lastVersion = gem.getVersion();
					}
					versionCombo.setText(lastVersion);
				}
			}

		});

		setGems(IGemManager.DEFAULT_GEM_HOST);

		return control;
	}

	private void updateTable()
	{
		gemViewer.setInput(gems);
		gemViewer.getTable().setItemCount(gems.size());
		gemViewer.refresh();
	}

	/**
	 * Sets the gems in the table
	 * 
	 * @param url
	 */
	protected void setGems(final String url)
	{
		if (gemJob != null)
		{
			gemJob.cancel();
		}
		gemJob = new Job("Loading remote gem listing")
		{

			protected IStatus run(IProgressMonitor gemJobMonitor)
			{
				if (gemJobMonitor != null && gemJobMonitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				gems = AptanaRDTPlugin.getDefault().getGemManager().getRemoteGems(url, gemJobMonitor);
				if (gemJobMonitor != null && gemJobMonitor.isCanceled())
				{
					return Status.CANCEL_STATUS;
				}
				UIJob updatingTable = new UIJob("Updating gem table")
				{

					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						if (monitor != null && monitor.isCanceled())
						{
							return Status.CANCEL_STATUS;
						}
						if (gemViewer != null && !gemViewer.getTable().isDisposed())
						{
							updateTable();
						}
						if (nameText != null && !nameText.isDisposed())
						{
							nameText.setEnabled(true);
						}
						if (versionCombo != null && !versionCombo.isDisposed())
						{
							versionCombo.setEnabled(true);
						}
						return Status.OK_STATUS;
					}

				};
				updatingTable.schedule();
				return Status.OK_STATUS;
			}

		};
		gemJob.schedule();
	}

	/**
	 * Update the source urls
	 */
	protected void updateSourceURLs()
	{
		sourceURLCombo.removeAll();
		Set<String> urls = AptanaRDTPlugin.getDefault().getGemManager().getSourceURLs();
		for (String url : urls)
		{
			sourceURLCombo.add(url);
		}
	}

	@Override
	protected void okPressed()
	{
		name = nameText.getText();
		version = versionCombo.getText();
		super.okPressed();
	}

	/**
	 * Gets the gem
	 * 
	 * @return - new gem
	 */
	public Gem getGem()
	{
		return new Gem(name, version, "");
	}

	/**
	 * Get source url
	 * 
	 * @return - url
	 */
	public String getSourceURL()
	{
		return sourceURL;
	}

	@Override
	public boolean close()
	{
		if (refreshImage != null)
			refreshImage.dispose();
		return super.close();
	}

	private void loadSourceURL()
	{
		String curr = sourceURLCombo.getText();
		gems = new HashSet<Gem>();
		gems.add(LOADING_GEM);
		updateTable();
		nameText.setEnabled(false);
		versionCombo.setEnabled(false);
		setGems(sourceURL);
		updateSourceURLs();
		sourceURLCombo.setText(curr);
	}

}
