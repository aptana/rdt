package com.aptana.rdt.ui.gems;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.aptana.rdt.core.gems.Gem;

public class GemLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final int NAME_COLUMN = 0;
	private static final int VERSION_COLUMN = 1;
	private static final int DESCRIPTION_COLUMN = 2;
	
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		Gem server = (Gem) element;

		switch (columnIndex) {
		case NAME_COLUMN:
			return server.getName();
		case VERSION_COLUMN:
			return server.getVersion();
		case DESCRIPTION_COLUMN:
			return server.getDescription();
		default:
			return "";
		}
	}


}
