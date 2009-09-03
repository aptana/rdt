package org.rubypeople.rdt.internal.debug.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class RubyExecutionArgumentsPage extends PropertyPage implements IWorkbenchPropertyPage {
	protected Text interpreterArgumentsText, programArgumentsText;
	
	public RubyExecutionArgumentsPage() {
	}

	protected Control createContents(Composite parent)  {
		noDefaultAndApplyButton();

		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyArguments_interpreter_args_box_title);
		new Label(composite, SWT.NONE).setText("                      ");
		interpreterArgumentsText = new Text(composite, SWT.BORDER);
		GridData interpreterArgumentsData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		interpreterArgumentsData.horizontalSpan = 2;
		interpreterArgumentsText.setLayoutData(interpreterArgumentsData);
		interpreterArgumentsText.setText(getArgument("interpreter"));
		
		new Label(composite, SWT.NONE).setText(RdtDebugUiMessages.LaunchConfigurationTab_RubyArguments_program_args_box_title);
		programArgumentsText = new Text(composite, SWT.BORDER);
		GridData programArgumentsData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		programArgumentsData.horizontalSpan = 2;
		programArgumentsText.setLayoutData(programArgumentsData);
		programArgumentsText.setText(getArgument("program"));
		
		return composite;
	}
	
	protected String getArgument(String name) {
		String argumentValue = null;
		try {
			argumentValue = ((IFile)getElement()).getPersistentProperty(new QualifiedName("executionArguments", name));
		} catch(CoreException e) {}
		
		return argumentValue != null ? argumentValue : "";
	}
	
	public boolean performOk() {
		IFile rubyFile = (IFile)getElement();
		try {
			rubyFile.setPersistentProperty(new QualifiedName("executionArguments", "interpreter"), interpreterArgumentsText.getText());
			rubyFile.setPersistentProperty(new QualifiedName("executionArguments", "program"), programArgumentsText.getText());
		} catch(CoreException e) {
			RdtDebugUiPlugin.log(e);
			return false;
		}
		return true;
	}

}
