/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.aptana.rdt.internal.rake.view;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.RubyProjectSelectionAction;
import org.rubypeople.rdt.internal.ui.RubyExplorerTracker.IRubyProjectListener;
import org.rubypeople.rdt.internal.ui.text.RubyColorManager;
import org.rubypeople.rdt.launching.IVMInstall;
import org.rubypeople.rdt.launching.IVMInstallChangedListener;
import org.rubypeople.rdt.launching.RubyRuntime;

import com.aptana.rdt.rake.IRakeHelper;
import com.aptana.rdt.rake.PreferenceConstants;
import com.aptana.rdt.rake.RakePlugin;

/**
 * RakeTasksView
 * 
 * @author cwilliams
 * @author Kevin Sawicki (added labels)
 */
public class RakeTasksView extends ViewPart implements IVMInstallChangedListener, IPropertyChangeListener,
		IRubyProjectListener
{

	private static final String PROJECT = "Current Ruby Project: ";

	private StackLayout fViewLayout;
	private Composite fRakeTasksView;
	private RubyProjectSelectionAction projectSelectionAction;
	private Label fSpecifyRakePath;
	private Label fSelectRailsProjectView;
	private Composite fParent;

	private Composite basicControls;
	private Label projectNameLabel;
	private Composite tasksComp;
	private Label tasksLabel;
	private Combo fTasksCombo;
	private Label paramLabel;
	private Text fParamText;
	private Button genButton;
	private Label descriptionLabel;
	private Label fDescripText;

	private RubyColorManager fColorManager;

	private Map<String, String> fTasks;

	private IProject project;

	private Job updateRakeTasksJob;

	private Cursor hand;
	private Button pretendButton;
	private Button quietButton;
	private Button backtraceButton;
	private Button systemButton;
	private Image fRunIcon;
	private Image fMaximizeIcon;
	private Image fMinimizeIcon;

	/**
	 * RakeTasksView
	 */
	public RakeTasksView()
	{
		super();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent)
	{
		fColorManager = new RubyColorManager(true);
		fParent = parent;
		hand = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		fViewLayout = new StackLayout();
		parent.setLayout(fViewLayout);

		fRakeTasksView = new Composite(parent, SWT.NONE);
		fRakeTasksView.setLayout(new GridLayout(1, true));
		fRakeTasksView.setLayoutData(new GridData(GridData.FILL_BOTH));

		createRakeControls(fRakeTasksView);
		createAdvancedSection(fRakeTasksView);

		fSpecifyRakePath = new Label(parent, SWT.NULL);
		fSpecifyRakePath.setText(RakeViewMessages.SpecifyRakePath_message);
		fSelectRailsProjectView = new Label(parent, SWT.NULL);
		fSelectRailsProjectView.setText(RakeViewMessages.SelectRubyProject_message);

		if (emptyRakePath())
		{
			fViewLayout.topControl = fSpecifyRakePath;
		}
		else
		{
			if (getSelectedRubyProject() != null)
			{
				fViewLayout.topControl = fRakeTasksView;
			}
			else
			{
				fViewLayout.topControl = fSelectRailsProjectView;
			}
		}
		parent.layout();

		getProjectTracker().addProjectListener(this);
		RubyRuntime.addVMInstallChangedListener(this);
		RakePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);

		projectSelectionAction = new RubyProjectSelectionAction();
		projectSelectionAction.setListener(this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(projectSelectionAction);

		IProject project = getProjectTracker().getSelectedRubyProject();
		IProject[] projects = RubyCore.getRubyProjects();
		if (project != null)
		{
			this.projectSelected(project);

		}
		else if (projects != null && projects.length > 0)
		{
			this.projectSelected(projects[0]);
		}
	}

	/**
	 * Create the rake controls
	 * 
	 * @param parent
	 * @return
	 */
	protected Composite createRakeControls(Composite parent)
	{
		basicControls = new Composite(parent, SWT.NULL);
		basicControls.setLayout(new GridLayout(3, false));
		basicControls.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

		projectNameLabel = new Label(basicControls, SWT.LEFT);
		projectNameLabel.setText(PROJECT);
		projectNameLabel.setForeground(fColorManager.getColor(new RGB(128, 128, 128)));
		GridData pnlData = new GridData(SWT.FILL, SWT.FILL, true, false);
		pnlData.horizontalSpan = 3;
		projectNameLabel.setLayoutData(pnlData);

		// Create the combo box of tasks
		tasksComp = new Composite(basicControls, SWT.LEFT);
		tasksComp.setLayout(new GridLayout(2, false));
		tasksLabel = new Label(tasksComp, SWT.LEFT);
		tasksLabel.setText("Tasks:");
		fTasksCombo = new Combo(tasksComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		fTasksCombo.setVisibleItemCount(20);
		fTasksCombo.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{
				// Do nothing
			}

			public void widgetSelected(SelectionEvent e)
			{
				setCurrentSelectedTaskDescription();
			}

		});

		// Create the parameters text field
		Composite paramsComp = new Composite(basicControls, SWT.LEFT);
		paramsComp.setLayout(new GridLayout(2, false));
		paramsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		paramLabel = new Label(paramsComp, SWT.LEFT);
		paramLabel.setText("Parameters:");
		fParamText = new Text(paramsComp, SWT.BORDER);
		GridData paramTextData = new GridData(GridData.FILL_HORIZONTAL);
		paramTextData.widthHint = 300;
		fParamText.setLayoutData(paramTextData);
		fParamText.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e)
			{
				// Do nothing
			}

			public void keyReleased(KeyEvent e)
			{
				// Take action if Enter was pressed
				if (e.character == SWT.CR)
				{
					runRakeTask();
				}
			}
		});

		// Create the Go button
		genButton = new Button(basicControls, SWT.PUSH);
		genButton.setToolTipText("Run Rake Task");
		genButton.setImage(getRunIcon());
		genButton.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				runRakeTask();
			}

		});

		// Create the text area for the task descriptions
		Composite descripComp = new Composite(basicControls, SWT.LEFT);
		descripComp.setLayout(new GridLayout(2, false));
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessHorizontalSpace = false;
		descripComp.setLayoutData(gd);
		descriptionLabel = new Label(descripComp, SWT.LEFT);
		descriptionLabel.setText("Description:");
		fDescripText = new Label(descripComp, SWT.WRAP);
		return basicControls;
	}

	private Composite createAdvancedSection(final Composite parent)
	{
		final Composite advanced = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		advanced.setLayout(layout);
		GridData advancedData = new GridData(SWT.FILL, SWT.FILL, true, false);
		advancedData.horizontalSpan = 3;
		advanced.setLayoutData(advancedData);

		final Font boldFont = new Font(advanced.getDisplay(), boldFont(advanced.getFont()));
		advanced.addDisposeListener(new DisposeListener()
		{

			public void widgetDisposed(DisposeEvent e)
			{
				if (hand != null && !hand.isDisposed())
				{
					hand.dispose();
				}
				if (boldFont != null && !boldFont.isDisposed())
				{
					boldFont.dispose();
				}
			}

		});

		final Label advancedIcon = new Label(advanced, SWT.LEFT);
		advancedIcon.setImage(getMaximizeIcon());
		advancedIcon.setCursor(hand);
		advancedIcon.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		Label advancedLabel = new Label(advanced, SWT.LEFT);
		advancedLabel.setText("Advanced Options");
		advancedLabel.setCursor(hand);
		advancedLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		advancedLabel.setFont(boldFont);

		final Composite advancedOptions = new Composite(advanced, SWT.NONE);
		layout = new GridLayout();
		layout.marginLeft = 15;
		advancedOptions.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		gridData.exclude = true;
		advancedOptions.setLayoutData(gridData);
		advancedOptions.setVisible(false);

		MouseAdapter expander = new MouseAdapter()
		{

			public void mouseDown(MouseEvent e)
			{
				if (advancedOptions.isVisible())
				{
					advancedOptions.setVisible(false);
					advancedIcon.setImage(getMaximizeIcon());
					((GridData) advancedOptions.getLayoutData()).exclude = true;
				}
				else
				{
					advancedOptions.setVisible(true);
					advancedIcon.setImage(getMinimizeIcon());
					((GridData) advancedOptions.getLayoutData()).exclude = false;
				}
				parent.pack(true);
				parent.layout(true, true);
			}

		};
		advancedIcon.addMouseListener(expander);
		advancedLabel.addMouseListener(expander);

		// Create the text field for the options information
		Group optionsGroup = new Group(advancedOptions, SWT.NULL);
		optionsGroup.setLayout(new GridLayout(7, false));
		optionsGroup.setText("Options");

		pretendButton = new Button(optionsGroup, SWT.CHECK);
		pretendButton.setText("Dry run");
		quietButton = new Button(optionsGroup, SWT.CHECK);
		quietButton.setText("Quiet");
		backtraceButton = new Button(optionsGroup, SWT.CHECK);
		backtraceButton.setText("Trace");
		systemButton = new Button(optionsGroup, SWT.CHECK);
		systemButton.setText("System");

		return advanced;
	}

	private Image getRunIcon()
	{
		if (fRunIcon == null)
		{
			fRunIcon = RakePlugin.imageDescriptorFromPlugin(RakePlugin.PLUGIN_ID, "icons/nav_go.gif").createImage();
		}
		return fRunIcon;
	}

	private Image getMaximizeIcon()
	{
		if (fMaximizeIcon == null)
		{
			fMaximizeIcon = RakePlugin.imageDescriptorFromPlugin(RakePlugin.PLUGIN_ID, "icons/maximize.png")
					.createImage();
		}
		return fMaximizeIcon;
	}

	private Image getMinimizeIcon()
	{
		if (fMinimizeIcon == null)
		{
			fMinimizeIcon = RakePlugin.imageDescriptorFromPlugin(RakePlugin.PLUGIN_ID, "icons/minimize.png")
					.createImage();
		}
		return fMinimizeIcon;
	}

	private static FontData[] boldFont(Font font)
	{
		FontData[] datas = font.getFontData();
		if (datas.length > 0)
		{
			for (int i = 0; i < datas.length; i++)
			{
				FontData data = datas[i];
				data.setStyle(data.getStyle() | SWT.BOLD);
			}
		}
		return datas;
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose()
	{
		super.dispose();
		disposeIcon(fMaximizeIcon);
		disposeIcon(fMinimizeIcon);
		disposeIcon(fRunIcon);
		fColorManager.dispose();
		getProjectTracker().removeProjectListener(this);
		RubyRuntime.removeVMInstallChangedListener(this);
		RakePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(this);
	}

	private void disposeIcon(Image icon)
	{
		if (icon == null)
			return;
		icon.dispose();
		icon = null;
	}

	private RubyExplorerTracker getProjectTracker()
	{
		return RubyPlugin.getDefault().getProjectTracker();
	}

	private void runRakeTask()
	{
		final IProject project = getSelectedRubyProject();
		if (project == null)
			return; // TODO Show error

		final String task = fTasksCombo.getText();
		final String args = getArgs();
		Job job = new Job(MessageFormat.format("Running rake task {0} {1}", task, args))
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				getRakeTasksHelper().runRakeTask(project, task, args, monitor);
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();

	}

	private String getArgs()
	{
		String args = "";
		if (pretendButton.getSelection())
			args += "--dry-run ";
		if (quietButton.getSelection())
			args += "--quiet ";
		if (backtraceButton.getSelection())
			args += "--trace ";
		if (systemButton.getSelection())
			args += "--system ";
		return args + fParamText.getText();
	}

	private IProject getSelectedRubyProject()
	{
		return this.project;
	}

	private IRakeHelper getRakeTasksHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	/**
	 * Updates the rake tasks
	 */
	protected void updateRakeTasks(final boolean force)
	{
		fTasksCombo.removeAll();
		if (project == null)
			return;
		// Part of ROR-1098 - We shouldn't allow multiple instances of this job to run simultaneously!
		if (updateRakeTasksJob != null)
		{
			updateRakeTasksJob.cancel();
		}
		updateRakeTasksJob = new Job("Update rake tasks")
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.beginTask("Loading rake tasks", 2);
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						if (fDescripText != null && !fDescripText.isDisposed())
						{
							fDescripText.setText("Please wait, loading rake tasks...");
						}
					}
				});
				monitor.worked(1);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				fTasks = getRakeTasksHelper().getTasks(getSelectedRubyProject(), force, monitor);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						if (fTasks.isEmpty())
						{
							if (!fDescripText.isDisposed())
							{
								fDescripText.redraw();
								setTaskDescription("No Rake Tasks found. Likely cause is no Rakefile for project.");
							}
							setEnabled(false);
							return;
						}
						Collection<String> sortedItems = new TreeSet<String>(fTasks.keySet());
						if (!fTasksCombo.isDisposed())
						{
							fTasksCombo.setItems(sortedItems.toArray(new String[sortedItems.size()]));
							fTasksCombo.pack(true);
							if (fTasks != null && !fTasks.isEmpty())
								fTasksCombo.select(0);
							setCurrentSelectedTaskDescription();
						}
						if (!genButton.isDisposed())
							genButton.setEnabled(true);
					}
				});
				monitor.worked(1);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		updateRakeTasksJob.schedule();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		fTasksCombo.setFocus();
		setCurrentSelectedTaskDescription();
	}

	/**
	 * @see org.rubypeople.rdt.launching.IVMInstallChangedListener#defaultVMInstallChanged(org.rubypeople.rdt.launching.IVMInstall,
	 *      org.rubypeople.rdt.launching.IVMInstall)
	 */
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current)
	{
		handlePossibleRakeChange(getRakePath());
	}

	private String getRakePath()
	{
		return RakePlugin.getDefault().getRakePath();
	}

	/**
	 * @see org.rubypeople.rdt.launching.IVMInstallChangedListener#vmAdded(org.rubypeople.rdt.launching.IVMInstall)
	 */
	public void vmAdded(IVMInstall newVm)
	{
		// ignore

	}

	/**
	 * @see org.rubypeople.rdt.launching.IVMInstallChangedListener#vmChanged(org.rubypeople.rdt.launching.PropertyChangeEvent)
	 */
	public void vmChanged(org.rubypeople.rdt.launching.PropertyChangeEvent event)
	{
		// ignore

	}

	/**
	 * @see org.rubypeople.rdt.launching.IVMInstallChangedListener#vmRemoved(org.rubypeople.rdt.launching.IVMInstall)
	 */
	public void vmRemoved(IVMInstall removedVm)
	{
		// ignore
	}

	/**
	 * propertyChange
	 * 
	 * @param event
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals(PreferenceConstants.PREF_RAKE_PATH))
		{
			handlePossibleRakeChange(event.getNewValue());
		}
	}

	private void handlePossibleRakeChange(final Object value)
	{
		if (!fParent.isDisposed())
		{
			Display.getDefault().asyncExec(new Runnable()
			{

				public void run()
				{
					if (value == null || value.equals(""))
					{
						fViewLayout.topControl = fSpecifyRakePath;
					}
					else
					{
						fViewLayout.topControl = fRakeTasksView;
					}
					fParent.layout();
				}

			});
		}
	}

	/**
	 * Sets the widget enablement
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
	{
		fTasksCombo.setEnabled(enabled);
		fParamText.setEnabled(enabled);
		genButton.setEnabled(enabled);
	}

	/**
	 * @see org.rubypeople.rdt.internal.ui.RubyExplorerTracker.IRubyProjectListener#projectSelected(org.eclipse.core.resources.IProject)
	 */
	public void projectSelected(IProject project)
	{
		if (fParent.isDisposed())
		{
			return;
		}
		if (project != null && RubyCore.isRubyProject(project) && project.exists() && project.isOpen())
		{
			projectNameLabel.setText(PROJECT + project.getName());
			this.project = project;
			setEnabled(true);
			updateRakeTasks(false);
		}
		else
		{
			fViewLayout.topControl = fSelectRailsProjectView;
			projectNameLabel.setText(PROJECT + "<Select an open Ruby project>");
			setEnabled(false);
			this.project = null;
			clear();
			fDescripText.setText("Selected project is not open or is not a Ruby project.");
		}
		if (emptyRakePath())
		{
			fViewLayout.topControl = fSpecifyRakePath;
		}
		else
		{
			fViewLayout.topControl = fRakeTasksView;
		}
		fParent.layout();
	}

	private void clear()
	{
		fTasksCombo.removeAll();
		fParamText.setText("");
		fDescripText.setText("");
	}

	private boolean emptyRakePath()
	{
		return getRakePath() == null || getRakePath().equals("");
	}

	private void setCurrentSelectedTaskDescription()
	{
		if (fTasks == null || fTasks.isEmpty() || fTasksCombo == null)
			return;
		String descrip = fTasks.get(fTasksCombo.getText());
		if (descrip == null)
			return;
		setTaskDescription(descrip);
	}

	private void setTaskDescription(String descrip)
	{
		Point size = this.fParent.getSize();		
		fDescripText.setText(descrip);
		GridData gd = (GridData) fDescripText.getParent().getLayoutData();
		gd.widthHint = size.x - 75;
		fTasksCombo.setToolTipText(descrip);
		fDescripText.pack(true);
		gd = new GridData();
		gd.widthHint = size.x - 150;
		fDescripText.setLayoutData(gd);
		fDescripText.getParent().pack(true);
		fRakeTasksView.pack(true);
		fRakeTasksView.layout(true, true);
	}
}
