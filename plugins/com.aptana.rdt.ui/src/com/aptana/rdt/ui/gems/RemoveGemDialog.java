package com.aptana.rdt.ui.gems;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class RemoveGemDialog extends Dialog {

	private Combo versionCombo;
	private String version;
	private List<String> versions;

	public RemoveGemDialog(Shell parentShell, List<String> versions) {
		super(parentShell);
		this.versions = versions;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(GemsMessages.RemoveGemDialog_dialog_title);
	
		Composite control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		control.setLayout(layout);
		
		Label versionLabel = new Label(control, SWT.LEFT);
		versionLabel.setText(GemsMessages.RemoveGemDialog_version_label);

		versionCombo = new Combo(control, SWT.DROP_DOWN);
		GridData versionComboData = new GridData();
		versionComboData.widthHint = 100;
		versionCombo.setLayoutData(versionComboData);
		
		if (versions != null && !versions.isEmpty()) {
			for (String version : versions) {
				versionCombo.add(version);
			}
			// Set the oldest version as default option		
			versionCombo.select(versions.size() - 1);
		}
		return control;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	public void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			version = versionCombo.getText();
			okPressed();
		} else if (buttonId == IDialogConstants.CANCEL_ID) {
			cancelPressed();
		}
	}
	
	public String getVersion() {
		return version;
	}

}
