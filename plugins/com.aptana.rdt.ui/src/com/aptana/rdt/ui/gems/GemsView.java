package com.aptana.rdt.ui.gems;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.rubypeople.rdt.internal.ui.util.CollectionContentProvider;
import org.rubypeople.rdt.ui.TableViewerSorter;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.Gem;
import com.aptana.rdt.core.gems.GemListener;
import com.aptana.rdt.core.gems.IGemManager;

public class GemsView extends ViewPart implements GemListener
{

	private TableViewer gemViewer;
	private GemManagerSelectionAction gemManagerSelectionAction;
	private IGemManager gemManager;

	@Override
	public void createPartControl(Composite parent)
	{
		parent.setLayout(new GridLayout());

		gemViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION);
		final Table gemTable = gemViewer.getTable();
		gemTable.setHeaderVisible(true);
		gemTable.setLinesVisible(true);
		gemTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		gemTable.addKeyListener(new KeyListener()
		{

			public void keyReleased(KeyEvent e)
			{
				// ignore
			}

			public void keyPressed(KeyEvent e)
			{
				if (e.keyCode == SWT.DEL)
				{
					TableItem item = gemTable.getItem(gemTable.getSelectionIndex());
					final Gem gem = (Gem) item.getData();
					if (MessageDialog.openConfirm(gemTable.getShell(), null, GemsMessages.bind(
							GemsMessages.RemoveGemDialog_msg, gem.getName())))
					{
						Job job = null;
						if (gem.hasMultipleVersions())
						{
							final RemoveGemDialog dialog = new RemoveGemDialog(Display.getDefault().getActiveShell(),
									gem.versions());
							if (dialog.open() == RemoveGemDialog.OK)
							{
								job = new Job("Removing gem")
								{
									@Override
									protected IStatus run(IProgressMonitor monitor)
									{
										return getGemManager().removeGem(
												new Gem(gem.getName(), dialog.getVersion(), gem.getDescription()),
												monitor);
									}
								};
							}
						}
						else
						{
							job = new Job("Removing gem")
							{
								@Override
								protected IStatus run(IProgressMonitor monitor)
								{
									return getGemManager().removeGem(gem, monitor);
								}
							};
						}
						if (job != null)
						{
							job.setUser(true);
							job.schedule();
						}
					}
				}
			}

		});

		TableColumn nameColumn = new TableColumn(gemTable, SWT.LEFT);
		nameColumn.setText(GemsMessages.GemsView_NameColumn_label);
		nameColumn.setWidth(150);

		TableColumn versionColumn = new TableColumn(gemTable, SWT.LEFT);
		versionColumn.setText(GemsMessages.GemsView_VersionColumn_label);
		versionColumn.setWidth(75);

		TableColumn descriptionColumn = new TableColumn(gemTable, SWT.LEFT);
		descriptionColumn.setText(GemsMessages.GemsView_DescriptionColumn_label);
		descriptionColumn.setWidth(275);

		gemViewer.setLabelProvider(new GemLabelProvider());
		gemViewer.setContentProvider(new CollectionContentProvider());
		TableViewerSorter.bind(gemViewer);
		getSite().setSelectionProvider(gemViewer);

		gemViewer.setInput(getSortedGems());
		createPopupMenu();

		getGemManager().addGemListener(this);

		gemManagerSelectionAction = new GemManagerSelectionAction(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(gemManagerSelectionAction);
	}

	public IGemManager getGemManager()
	{
		if (gemManager == null)
		{
			gemManager = AptanaRDTPlugin.getDefault().getGemManager();
		}
		return gemManager;
	}

	@Override
	public void dispose()
	{
		getGemManager().removeGemListener(this);
		super.dispose();
	}

	@Override
	public void setFocus()
	{
		gemViewer.getTable().setFocus();
	}

	/**
	 * Creates and registers the context menu
	 */
	private void createPopupMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);

		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				IContributionItem[] items = getViewSite().getActionBars().getToolBarManager().getItems();
				for (int i = 0; i < items.length; i++)
				{
					if (items[i] instanceof ActionContributionItem)
					{
						ActionContributionItem aci = (ActionContributionItem) items[i];
						manager.add(aci.getAction());
					}
				}
			}
		});
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); // Allow
		// other
		// plugins
		// to
		// add
		// here
		Menu menu = menuMgr.createContextMenu(gemViewer.getControl());
		gemViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, gemViewer);
	}

	public void gemsRefreshed()
	{
		doRefresh();
	}

	public void gemUpdated(Gem gem)
	{
	}

	private void doRefresh()
	{
		Display.getDefault().asyncExec(new Runnable()
		{

			public void run()
			{
				gemViewer.setInput(getSortedGems());
				gemViewer.refresh();
			}

		});
	}

	private Set<Gem> getSortedGems()
	{
		return Collections.unmodifiableSortedSet(new TreeSet<Gem>(getGemManager().getGems()));
	}

	public void gemAdded(final Gem gem)
	{
		doRefresh();
	}

	public void gemRemoved(final Gem gem)
	{
		doRefresh();
	}

	public void managerInitialized()
	{
		// ignore
	}

	public void setGemManager(IGemManager gemManager)
	{
		this.gemManager = gemManager;
		doRefresh();
	}

}
