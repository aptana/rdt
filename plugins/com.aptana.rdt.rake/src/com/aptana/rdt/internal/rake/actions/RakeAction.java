package com.aptana.rdt.internal.rake.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.RakePlugin;

public class RakeAction implements IObjectActionDelegate, IMenuCreator {

	private static final String RAKE_NAMESPACE_DELIMETER = ":";
	private boolean fFillMenu;
	private IAction fDelegateAction;
	private IStructuredSelection fSelection;
	private HashMap<String, MenuManager> fNamespaces;
	private Menu menu;
	
	public RakeAction() {
		super();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// We don't have a need for the active part.		
	}

	public void run(IAction action) {
		// Never called because we become a menu.		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// if the selection is an IResource, save it and enable our action
		if (selection instanceof IStructuredSelection) {
			fFillMenu = true;
			if (fDelegateAction != action) {
				fDelegateAction = action;
				fDelegateAction.setMenuCreator(this);
			}
			// save selection and enable our menu
			fSelection = (IStructuredSelection) selection;
			action.setEnabled(true);
			return;
		}
		action.setEnabled(false);		
	}

	public void dispose() {
		if (menu != null)
			menu.dispose();
		menu = null;
	}

	public Menu getMenu(Control parent) {
		// never called
		return null;
	}

	public Menu getMenu(Menu parent) {
		// Create the new menu. The menu will get filled when it is about to be shown. see fillMenu(Menu).
		 menu = new Menu(parent);
		/**
		 * Add listener to re-populate the menu each time
		 * it is shown because MenuManager.update(boolean, boolean) 
		 * doesn't dispose pull-down ActionContribution items for each popup menu.
		 */
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				if (fFillMenu) {
					Menu m = (Menu)e.widget;
					MenuItem[] items = m.getItems();
					for (int i=0; i < items.length; i++) {
						items[i].dispose();
					}
					fillMenu(m);
					fFillMenu = false;
				}
			}
		});
		return menu;
	}
	
	/**
     * Fills the menu with applicable launch shortcuts
     * @param menu The menu to fill
     */
	protected void fillMenu(Menu menu) {
		if (fSelection == null) {
			return;
		}
		IResource resource = (IResource) fSelection.getFirstElement();		
		IProject project = resource.getProject();
		Map<String, String> tasks = getRakeHelper().getTasks(project, new NullProgressMonitor());	
				
		fNamespaces = new HashMap<String, MenuManager>();
		// Please note that tehre's a lot of code mixed up in here to ensure that the menus, items and sub-menus all appear alphabetically
		List<String> values = new ArrayList<String>(tasks.keySet());
		Collections.sort(values);
		for (String task : values) {
			String[] paths = task.split(RAKE_NAMESPACE_DELIMETER);
			if (paths.length == 1) {
				IAction action = new RunRakeAction(project, task, tasks.get(task));
			    ActionContributionItem item= new ActionContributionItem(action);
			    item.fill(menu, -1);
			} else {
				MenuManager manager = getOrCreate(paths);
				manager.add(new RunRakeAction(project, task, tasks.get(task)));
			}
		}
		values = new ArrayList<String>(fNamespaces.keySet());
		Collections.sort(values);
		Collections.reverse(values);
		for (String path : values) {
			MenuManager manager = fNamespaces.get(path);
			String[] parts = path.split(RAKE_NAMESPACE_DELIMETER);
			if (parts.length == 1) {
				int index = getInsertIndex(menu, manager);				
				manager.fill(menu, index);
			} else {
				MenuManager parent = getParent(parts);
				if (parent != null)
				{
					int index = getInsertIndex(parent, manager);
					parent.insert(index, manager);
				}
				else 
				{
					int index = getInsertIndex(menu, manager);				
					manager.fill(menu, index);
				}
			}			
		}
	}

	/**
	 * For inserting submenus under submenus
	 * @param parent
	 * @param item
	 * @return
	 */
	private int getInsertIndex(MenuManager parent, MenuManager item) {
		if (parent == null || item == null) return 0;
		String text = item.getMenuText();
		if (text == null) return 0;
		IContributionItem[] items = parent.getItems();
		if (items == null) return 0;
		int index = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) continue;
			if (items[i] instanceof ActionContributionItem) {
				ActionContributionItem actionItem = (ActionContributionItem) items[i];
				IAction action = actionItem.getAction();
				if (action == null) continue;
				String other = action.getText();
				if (text.compareTo(other) >= 0) {
					index = i + 1;
				} else {
					break;
				}
			}			
		}
		return index;
	}

	/**
	 * For inserting submenus at first level.
	 * 
	 * @param parent
	 * @param item
	 * @return
	 */
	private int getInsertIndex(Menu parent, MenuManager item) {
		String text = item.getMenuText();
		MenuItem[] items = parent.getItems();
		int index = 0;
		for (int i = 0; i < items.length; i++) {
			String other = items[i].getText();
			if (text.compareTo(other) >= 0) {
				index = i + 1;
			} else {
				break;
			}
		}
		return index;
	}

	protected IRakeHelper getRakeHelper() {
		return RakePlugin.getDefault().getRakeHelper();
	}

	private MenuManager getParent(String[] parts) {
		String[] part = stripLastItem(parts);
		return fNamespaces.get(join(part));
	}

	private String join(String[] part) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < part.length; i++) {
			if (i != 0) buffer.append(RAKE_NAMESPACE_DELIMETER);
			buffer.append(part[i]);
		}
		return buffer.toString();
	}

	private MenuManager getOrCreate(String[] paths) {
		String[] part = stripLastItem(paths);
		MenuManager manager = fNamespaces.get(join(part));
		if (manager == null) {
			manager = new MenuManager(part[part.length - 1]);
			fNamespaces.put(join(part), manager);
		}
		return manager;
	}

	private String[] stripLastItem(String[] paths) {
		String[] part = new String[paths.length - 1];
		System.arraycopy(paths, 0, part, 0, part.length);
		return part;
	}
}
