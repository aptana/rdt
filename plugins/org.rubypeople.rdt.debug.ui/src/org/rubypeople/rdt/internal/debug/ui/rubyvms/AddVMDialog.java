package org.rubypeople.rdt.internal.debug.ui.rubyvms;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rubypeople.rdt.internal.ui.dialogs.StatusDialog;
import org.rubypeople.rdt.internal.ui.dialogs.StatusInfo;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.DialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.rubypeople.rdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallType;
import org.rubypeople.rdt.launching.VMStandin;

public class AddVMDialog extends StatusDialog {
	
	protected IStatus[] allStatus = new IStatus[2];
	
	protected IVMInstall fEditedVM;
	private StringButtonDialogField fRubyVMRoot;
	private StringDialogField fVMName;
	
	private StringDialogField fVMArgs;
	
	private IVMInstallType fSelectedVMType;
	private IVMInstallType[] fVMTypes;
	private ComboDialogField fVMTypeCombo;
	private VMLibraryBlock fLibraryBlock;
	
	private IStatus[] fStati;
	
	private int fPrevIndex = -1;

	private IAddVMDialogRequestor fRequestor;

	public AddVMDialog(IAddVMDialogRequestor requestor, Shell shell, IVMInstallType[] vmInstallTypes, IVMInstall editedVM) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fRequestor= requestor;
		fStati= new IStatus[5];
		for (int i= 0; i < fStati.length; i++) {
			fStati[i]= new StatusInfo();
		}
		
		fVMTypes= vmInstallTypes;
		fSelectedVMType= editedVM != null ? editedVM.getVMInstallType() : vmInstallTypes[0];
		
		fEditedVM= editedVM;
	}
	
	protected void setSystemLibraryStatus(IStatus status) {
		fStati[3]= status;
	}
	
	protected IStatus validateInterpreterLocationText() {
		String locationName= fRubyVMRoot.getText();
		IStatus s = null;
		File file = null;
		if (locationName.length() == 0) {
			s = new StatusInfo(IStatus.INFO, RubyVMMessages.addVMDialog_enterLocation); 
		} else {
			file= new File(locationName);
			if (!file.exists()) {
				s = new StatusInfo(IStatus.ERROR, RubyVMMessages.addVMDialog_locationNotExists); 
			} else {
				final IStatus[] temp = new IStatus[1];
				final File tempFile = file; 
				Runnable r = new Runnable() {
					/**
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						temp[0] = getVMType().validateInstallLocation(tempFile);
					}
				};
				BusyIndicator.showWhile(getShell().getDisplay(), r);
				s = temp[0];
			}
		}
		if (s.isOK()) {
			if (file.getName().equals("bin")) {
				file = file.getParentFile();
			}			
			fLibraryBlock.setHomeDirectory(file);
			String name = fVMName.getText();
			if (name == null || name.trim().length() == 0) {
				// auto-generate VM name
				try {
					String genName = null;
					IPath path = new Path(file.getCanonicalPath());
					int segs = path.segmentCount();
					if (segs == 1) {
						genName = path.segment(0);
					} else if (segs >= 2) {
						genName = path.lastSegment();
					}
					if (genName != null) {
						fVMName.setText(genName);
					}
				} catch (IOException e) {}
			}
		} else {
			fLibraryBlock.setHomeDirectory(null);
		}
		fLibraryBlock.restoreDefaultLibraries();
		return s;
	}
	
	private IVMInstallType getVMType() {
		return fSelectedVMType;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#setButtonLayoutData(org.eclipse.swt.widgets.Button)
	 */
	protected void setButtonLayoutData(Button button) {
		super.setButtonLayoutData(button);
	}

	protected void browseForInstallLocation() {
		DirectoryDialog dialog= new DirectoryDialog(getShell());
		dialog.setFilterPath(fRubyVMRoot.getText());
		dialog.setMessage(RubyVMMessages.addVMDialog_pickJRERootDialog_message); 
		String newPath= dialog.open();
		if (newPath != null) {
			fRubyVMRoot.setText(newPath);
		}
	}

	protected void okPressed() {
		doOkPressed();
		super.okPressed();
	}
	
	private void doOkPressed() {
		if (fEditedVM == null) {
			IVMInstall vm= new VMStandin(fSelectedVMType, createUniqueId(fSelectedVMType));
			setFieldValuesToVM(vm);
			fRequestor.vmAdded(vm);
		} else {
			setFieldValuesToVM(fEditedVM);
		}
	}
	
	public void create() {
		super.create();
		fVMName.setFocus();
		selectVMType();  
	}
	
	private String createUniqueId(IVMInstallType vmType) {
		String id= null;
		do {
			id= String.valueOf(System.currentTimeMillis());
		} while (vmType.findVMInstall(id) != null);
		return id;
	}
	
	private void selectVMType() {
		for (int i= 0; i < fVMTypes.length; i++) {
			if (fSelectedVMType == fVMTypes[i]) {
				fVMTypeCombo.selectItem(i);
				return;
			}
		}
	}
	
	private void updateVMType() {
		int selIndex= fVMTypeCombo.getSelectionIndex();
		if (selIndex == fPrevIndex) {
			return;
		}
		fPrevIndex = selIndex;
		if (selIndex >= 0 && selIndex < fVMTypes.length) {
			fSelectedVMType= fVMTypes[selIndex];
		}
		setRubyVMLocationStatus(validateInterpreterLocationText());
		fLibraryBlock.initializeFrom(fEditedVM, fSelectedVMType);
		updateStatusLine();
	}	
	
	private void setRubyVMLocationStatus(IStatus status) {
		fStati[1]= status;
	}
		
	protected void updateStatusLine() {
		IStatus max= null;
		for (int i= 0; i < fStati.length; i++) {
			IStatus curr= fStati[i];
			if (curr.matches(IStatus.ERROR)) {
				updateStatus(curr);
				return;
			}
			if (max == null || curr.getSeverity() > max.getSeverity()) {
				max= curr;
			}
		}
		updateStatus(max);
	}
	
	protected Control createDialogArea(Composite ancestor) {	
		createDialogFields();
		Composite parent = (Composite)super.createDialogArea(ancestor);
		((GridLayout)parent.getLayout()).numColumns= 3;
		
		fVMTypeCombo.doFillIntoGrid(parent, 3);
		((GridData)fVMTypeCombo.getComboControl(null).getLayoutData()).widthHint= convertWidthInCharsToPixels(50);

		Label l = new Label(parent, SWT.NONE);
		l.setText(RubyVMMessages.enterRubyInstallLocation); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		l.setLayoutData(gd);	
		
		fRubyVMRoot.doFillIntoGrid(parent, 3);
		
		fVMName.doFillIntoGrid(parent, 3);		
		
		fVMArgs.doFillIntoGrid(parent, 3);
		((GridData)fVMArgs.getTextControl(null).getLayoutData()).widthHint= convertWidthInCharsToPixels(50);
		
		l = new Label(parent, SWT.NONE);
		l.setText(RubyVMMessages.AddVMDialog_JRE_system_libraries__1); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		l.setLayoutData(gd);	
		
		fLibraryBlock = new VMLibraryBlock(this);
		Control block = fLibraryBlock.createControl(parent);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		block.setLayoutData(gd);
		
		Text t= fRubyVMRoot.getTextControl(parent);
		gd= (GridData)t.getLayoutData();
		gd.grabExcessHorizontalSpace=true;
		gd.widthHint= convertWidthInCharsToPixels(50);
		
		initializeFields();
		createFieldListeners();
		applyDialogFont(parent);
		return parent;
	}
	
	private void initializeFields() {
		fVMTypeCombo.setItems(getVMTypeNames());
		if (fEditedVM == null) {
			fVMName.setText(""); //$NON-NLS-1$
			fRubyVMRoot.setText(""); //$NON-NLS-1$
			fLibraryBlock.initializeFrom(null, fSelectedVMType);
			fVMArgs.setText(""); //$NON-NLS-1$
		} else {
			fVMTypeCombo.setEnabled(false);
			fVMName.setText(fEditedVM.getName());
			fRubyVMRoot.setText(fEditedVM.getInstallLocation().getAbsolutePath());
			fLibraryBlock.initializeFrom(fEditedVM, fSelectedVMType);
			String vmArgs = fEditedVM.getVMArgs();
			if (vmArgs != null) {
				fVMArgs.setText(vmArgs);
			}			
		}
		setVMNameStatus(validateVMName());
		updateStatusLine();
	}
	
	private void setVMNameStatus(IStatus status) {
		fStati[0]= status;
	}
	
	protected void createFieldListeners() {
		fVMTypeCombo.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				updateVMType();
			}
		});
		
		fVMName.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				setVMNameStatus(validateVMName());
				updateStatusLine();
			}
		});
		
		fRubyVMRoot.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				setRubyVMLocationStatus(validateInterpreterLocationText());
				updateStatusLine();
			}
		});
	}
	
	private IStatus validateVMName() {
		StatusInfo status= new StatusInfo();
		String name= fVMName.getText();
		if (name == null || name.trim().length() == 0) {
			status.setInfo(RubyVMMessages.addVMDialog_enterName); 
		} else {
			if (fRequestor.isDuplicateName(name) && (fEditedVM == null || !name.equals(fEditedVM.getName()))) {
				status.setError(RubyVMMessages.addVMDialog_duplicateName); 
			} else {
				IStatus s = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
				if (!s.isOK()) {
					status.setError(MessageFormat.format(RubyVMMessages.AddVMDialog_JRE_name_must_be_a_valid_file_name___0__1, s.getMessage())); 
				}
			}
		}
		return status;
	}
	
	protected void createDialogFields() {
		fVMTypeCombo= new ComboDialogField(SWT.READ_ONLY);
		fVMTypeCombo.setLabelText(RubyVMMessages.addVMDialog_jreType); 
		fVMTypeCombo.setItems(getVMTypeNames());
		
		fVMName= new StringDialogField();
		fVMName.setLabelText(RubyVMMessages.addVMDialog_jreName); 
		
		fRubyVMRoot= new StringButtonDialogField(new IStringButtonAdapter() {
			public void changeControlPressed(DialogField field) {
				browseForInstallLocation();
			}
		});
		fRubyVMRoot.setLabelText(RubyVMMessages.addVMDialog_jreHome); 
		fRubyVMRoot.setButtonLabel(RubyVMMessages.addVMDialog_browse1); 
			
		fVMArgs= new StringDialogField();
		fVMArgs.setLabelText(RubyVMMessages.AddVMDialog_23); 
	}
	
	private String[] getVMTypeNames() {
		String[] names=  new String[fVMTypes.length];
		for (int i= 0; i < fVMTypes.length; i++) {
			names[i]= fVMTypes[i].getName();
		}
		return names;
	}
	
	protected void setFieldValuesToVM(IVMInstall vm) {
		File dir = new File(fRubyVMRoot.getText());
		if (dir.getName().equals("bin")) {
			dir = dir.getParentFile();
		}			
		try {
			vm.setInstallLocation(dir.getCanonicalFile());
		} catch (IOException e) {
			vm.setInstallLocation(dir.getAbsoluteFile());
		}
		vm.setName(fVMName.getText());
		
		String argString = fVMArgs.getText().trim();
		if (argString != null && argString.length() >0) {
			vm.setVMArgs(argString);			
		} else {
			vm.setVMArgs(null);
		}	

		fLibraryBlock.performApply(vm);
	}

}