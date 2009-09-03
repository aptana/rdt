package com.aptana.rdt.ui.gems;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.rubypeople.rdt.ui.IHasImageDescriptor;

import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.core.gems.IGemManager;
import com.aptana.rdt.ui.AptanaRDTUIPlugin;

class GemManagerSelectionAction extends Action implements IMenuCreator
{

	private Menu fMenu;
	private GemsView gemsView;

	GemManagerSelectionAction(GemsView gemsView)
	{
		this.gemsView = gemsView;
		setEnabled(getGemManagers().length > 0);
		setToolTipText("Select a gem repository");
		setImageDescriptor(AptanaRDTUIPlugin.imageDescriptorFromPlugin(AptanaRDTUIPlugin.PLUGIN_ID,
				"icons/rubygems.png"));
		setMenuCreator(this);
	}

	private IGemManager[] getGemManagers()
	{
		return AptanaRDTPlugin.getDefault().getGemManagers();
	}

	public void dispose()
	{
	}

	public Menu getMenu(Control parent)
	{
		if (fMenu != null && !fMenu.isDisposed())
		{
			fMenu.dispose();
		}

		fMenu = new Menu(parent);
		int accel = 1;
		IGemManager[] gemManagers = getGemManagers();
		for (IGemManager gemManager : gemManagers)
		{
			String label = gemManager.getName();
			ImageDescriptor image = null;
			if (gemManager instanceof IHasImageDescriptor)
			{
				image = ((IHasImageDescriptor) gemManager).getImageDescriptor();
			}
			addActionToMenu(fMenu, new GemManagerAction(label, image, gemManager), accel);
			accel++;
		}
		return fMenu;
	}

	public Menu getMenu(Menu parent)
	{
		return null;
	}

	private void addActionToMenu(Menu parent, Action action, int accelerator)
	{
		if (accelerator < 10)
		{
			StringBuffer label = new StringBuffer();
			// add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}

		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	private class GemManagerAction extends Action
	{
		private IGemManager gemManager;

		/**
		 * GemManagerAction
		 * 
		 * @param label
		 * @param image
		 * @param gemManager
		 */
		public GemManagerAction(String label, ImageDescriptor image, IGemManager gemManager)
		{
			setText(label);
			if (image != null)
			{
				setImageDescriptor(image);
			}
			this.gemManager = gemManager;
		}

		/**
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run()
		{
			if (gemsView != null)
			{
				gemsView.setGemManager(gemManager);
			}
		}

		/**
		 * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
		 */
		public void runWithEvent(Event event)
		{
			run();
		}
	}
}
