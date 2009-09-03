package org.rubypeople.rdt.internal.debug.ui.preferences;

import java.io.File;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;

public class RubyInterpreterLabelProvider implements ITableLabelProvider {

	public RubyInterpreterLabelProvider() {
		super();
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		IVMInstall interpreter = (IVMInstall) element;
		switch (columnIndex) {
			case 0 :
				return interpreter.getName();
			case 1 :
				File installLocation = interpreter.getInstallLocation();
				return installLocation != null ? installLocation.getAbsolutePath() : "In user path";
			case 2 :
				IVMInstallType installType = interpreter.getVMInstallType();
				return installType != null ? installType.getName() : "Unknown";
			default :
				return "Unknown Column Index";
		}
	}

	public void addListener(ILabelProviderListener listener) {}

	public void dispose() {}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {}

}