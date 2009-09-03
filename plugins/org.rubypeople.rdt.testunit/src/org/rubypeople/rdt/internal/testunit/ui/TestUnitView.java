package org.rubypeople.rdt.internal.testunit.ui;

import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.rubypeople.rdt.core.ElementChangedEvent;
import org.rubypeople.rdt.core.IElementChangedListener;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyElementDelta;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.internal.core.RubyModelManager;
import org.rubypeople.rdt.launching.IRubyLaunchConfigurationConstants;
import org.rubypeople.rdt.testunit.ITestRunListener;
import org.rubypeople.rdt.testunit.launcher.TestUnitLaunchConfigurationDelegate;

public class TestUnitView extends ViewPart implements ITestRunListener3 {

	public static final String NAME = "org.rubypeople.rdt.testunit.views.TestUnitView";
	
	private static final String RERUN_LAST_COMMAND = "org.rubypeople.rdt.testunit.rerunLastTest";

	static final int REFRESH_INTERVAL = 200;

	public static final String ID_EXTENSION_POINT_TESTRUN_TABS = TestunitPlugin.PLUGIN_ID + "." + "internalTestRunTabs"; //$NON-NLS-1$ //$NON-NLS-2$
	
	static enum VIEW_ORIENTATION {VERTICAL, HORIZONTAL, AUTOMATIC};
	
	private VIEW_ORIENTATION fOrientation= VIEW_ORIENTATION.AUTOMATIC;
	
	private VIEW_ORIENTATION fCurrentOrientation;

	private ToggleOrientationAction[] fToggleOrientationActions;

	final Image fStackViewIcon= TestUnitView.createImage("eview16/stackframe.gif");//$NON-NLS-1$
	final Image fTestRunOKIcon= TestUnitView.createImage("eview16/testunitsucc.gif"); //$NON-NLS-1$
	final Image fTestRunFailIcon= TestUnitView.createImage("eview16/testuniterr.gif"); //$NON-NLS-1$
	final Image fTestRunOKDirtyIcon= TestUnitView.createImage("eview16/testunitsuccq.gif"); //$NON-NLS-1$
	final Image fTestRunFailDirtyIcon= TestUnitView.createImage("eview16/testuniterrq.gif"); //$NON-NLS-1$

	/**
	 * The currently active run tab
	 */
	private TestRunTab fActiveRunTab;

	/**
	 * The collection of ITestRunTabs
	 */
	protected Vector<TestRunTab> fTestRunTabs = new Vector<TestRunTab>();

	/**
	 * Map storing TestInfos for each executed test keyed by the test name.
	 */
	private Map<String, TestRunInfo> fTestInfos = new HashMap<String, TestRunInfo>();

	/**
	 * Is the UI disposed
	 */
	private boolean fIsDisposed = false;
	/**
	 * The client side of the remote test runner
	 */
	private RemoteTestRunnerClient fTestRunnerClient;

	/**
	 * The launcher that has started the test
	 */
	private String fLaunchMode;
	private ILaunch fLastLaunch;

	/**
	 * Actions
	 */
	private Action fRerunLastTestAction;

	/**
	 * Number of executed tests during a test run
	 */
	protected volatile int fExecutedTests;
	/**
	 * Number of errors during this test run
	 */
	protected volatile int fErrorCount;
	/**
	 * Number of failures during this test run
	 */
	protected volatile int fFailureCount;
	/**
	 * Number of tests run
	 */
	protected volatile int fTestCount;

	/**
	 * The first failure of a test run. Used to reveal the first failed tests at
	 * the end of a run.
	 */
	private List<TestRunInfo> fFailures = new ArrayList<TestRunInfo>();

	protected boolean fShowOnErrorOnly = false;

	private CounterPanel fCounterPanel;
	private TestUnitProgressBar fProgressBar;
	protected ProgressImages fProgressImages;
	protected Image fViewImage;
	private Composite fCounterComposite;
	private Composite fParent;
	private SashForm fSashForm;
	private CTabFolder fTabFolder;
	private FailureTrace fFailureTrace;
	private Clipboard fClipboard;
	protected volatile String fStatus;
	
	Image fOriginalViewImage;
	IElementChangedListener fDirtyListener;

	private UpdateUIJob fUpdateJob;

	/**
	 * Whether the output scrolls and reveals tests as they are executed.
	 */
	private boolean fAutoScroll = true;

	private ScrollLockAction fScrollLockAction;
    private IRubyProject fTestProject;
	private IMenuListener fViewMenuListener;
	private ActivateOnErrorAction fActivateOnErrorAction;

	private boolean fIsRunning = false;
	private boolean fIsStopped = false;
	private int fStartedCount = 0;
	
	private IPartListener2 fPartListener= new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) { }
		public void partBroughtToTop(IWorkbenchPartReference ref) { }
		public void partInputChanged(IWorkbenchPartReference ref) { }
		public void partClosed(IWorkbenchPartReference ref) { }
		public void partDeactivated(IWorkbenchPartReference ref) { }
		public void partOpened(IWorkbenchPartReference ref) { }
		
		public void partVisible(IWorkbenchPartReference ref) {
			if (getSite().getId().equals(ref.getId())) {
				fPartIsVisible= true;
			}
		}
		
		public void partHidden(IWorkbenchPartReference ref) {
			if (getSite().getId().equals(ref.getId())) {
				fPartIsVisible= false;
			}
		}
	};
	
	protected boolean fPartIsVisible= false;

	private IHandlerActivation fRerunLastActivation;

	/**
	 * The constructor.
	 */
	public TestUnitView() {}

	public static Image createImage(String path) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(TestunitPlugin.makeIconFileURL(path));
			return id.createImage();
		} catch (MalformedURLException e) {
			// fall through
		}
		return null;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		fParent = parent;
		addResizeListener(parent);
		
		fClipboard = new Clipboard(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);

		configureToolBar();

		fCounterComposite = createProgressCountPanel(parent);
		fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		SashForm sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		fOriginalViewImage= getTitleImage();
		fProgressImages= new ProgressImages();
		
		getViewSite().getPage().addPartListener(fPartListener);
	}
	
	private class ToggleOrientationAction extends Action {
		private final VIEW_ORIENTATION fActionOrientation;
		
		public ToggleOrientationAction(TestUnitView v, VIEW_ORIENTATION orientation) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$

			if (orientation == VIEW_ORIENTATION.HORIZONTAL) {
				setText(TestUnitMessages.TestRunnerViewPart_toggle_horizontal_label); 
				setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/th_horizontal.gif")); //$NON-NLS-1$				
			} else if (orientation == VIEW_ORIENTATION.VERTICAL) {
				setText(TestUnitMessages.TestRunnerViewPart_toggle_vertical_label); 
				setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/th_vertical.gif")); //$NON-NLS-1$				
			} else if (orientation == VIEW_ORIENTATION.AUTOMATIC) {
				setText(TestUnitMessages.TestRunnerViewPart_toggle_automatic_label);  
				setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/th_automatic.gif")); //$NON-NLS-1$				
			}
			fActionOrientation= orientation;
		}
		
		public VIEW_ORIENTATION getOrientation() {
			return fActionOrientation;
		}
		
		public void run() {
			if (isChecked()) {
				fOrientation= fActionOrientation;
				computeOrientation();
			}
		}		
	}

	private void configureToolBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		// TODO Uncomment when other actions are available
		IMenuManager viewMenu = actionBars.getMenuManager();
		fRerunLastTestAction = new RerunLastAction();
		IHandlerService handlerService= (IHandlerService) getSite().getWorkbenchWindow().getService(IHandlerService.class);
		IHandler handler = new AbstractHandler() {
			public Object execute(ExecutionEvent event) throws ExecutionException {
				fRerunLastTestAction.run();
				return null;
			}
			public boolean isEnabled() {
				return fRerunLastTestAction.isEnabled();
			}
		};
        fRerunLastActivation= handlerService.activateHandler(RERUN_LAST_COMMAND, handler);
		
		fScrollLockAction = new ScrollLockAction(this);
		//fNextAction= new ShowNextFailureAction(this);
		//fPreviousAction= new ShowPreviousFailureAction(this);
		//fStopAction= new StopAction();
		//fNextAction.setEnabled(false);
		//fPreviousAction.setEnabled(false);
		//fStopAction.setEnabled(false);
		//actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(),
		// fNextAction);
		//actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(),
		// fPreviousAction);

		//toolBar.add(fNextAction);
		//toolBar.add(fPreviousAction);
		//toolBar.add(fStopAction);
		toolBar.add(new Separator());
		toolBar.add(fRerunLastTestAction);
		toolBar.add(fScrollLockAction);
		
		fToggleOrientationActions =
			new ToggleOrientationAction[] {
				new ToggleOrientationAction(this, VIEW_ORIENTATION.VERTICAL),
				new ToggleOrientationAction(this, VIEW_ORIENTATION.HORIZONTAL),
				new ToggleOrientationAction(this, VIEW_ORIENTATION.AUTOMATIC)};

		MenuManager layoutSubMenu= new MenuManager(TestUnitMessages.TestRunnerViewPart_layout_menu);
		for (int i = 0; i < fToggleOrientationActions.length; ++i) {
			layoutSubMenu.add(fToggleOrientationActions[i]);
		}
		viewMenu.add(layoutSubMenu);
		viewMenu.add(new Separator());
		
		fScrollLockAction.setChecked(!fAutoScroll);
		
		fActivateOnErrorAction= new ActivateOnErrorAction();
		viewMenu.add(fActivateOnErrorAction);
		fViewMenuListener= new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fActivateOnErrorAction.update();
			}
		};

		viewMenu.addMenuListener(fViewMenuListener);

		actionBars.updateActionBars();
	}

	private SashForm createSashForm(Composite parent) {
		fSashForm = new SashForm(parent, SWT.VERTICAL);
		ViewForm top = new ViewForm(fSashForm, SWT.NONE);
		fTabFolder = createTestRunTabs(top);
		fTabFolder.setLayoutData(new TabFolderLayout());
		top.setContent(fTabFolder);

		ViewForm bottom = new ViewForm(fSashForm, SWT.NONE);
		CLabel label = new CLabel(bottom, SWT.NONE);
		label.setText(TestUnitMessages.TestRunnerViewPart_label_failure);
		label.setImage(fStackViewIcon);
		bottom.setTopLeft(label);

		ToolBar failureToolBar = new ToolBar(bottom, SWT.FLAT | SWT.WRAP);
		bottom.setTopCenter(failureToolBar);
		fFailureTrace = new FailureTrace(bottom, fClipboard, this, failureToolBar);
		bottom.setContent(fFailureTrace.getComposite());

		fSashForm.setWeights(new int[] { 50, 50});
		return fSashForm;
	}

	protected CTabFolder createTestRunTabs(Composite parent) {
		CTabFolder tabFolder = new CTabFolder(parent, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));

		loadTestRunTabs(tabFolder);
		tabFolder.setSelection(0);
		fActiveRunTab = fTestRunTabs.firstElement();

		tabFolder.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				testTabChanged(event);
			}
		});
		return tabFolder;
	}

	private void testTabChanged(SelectionEvent event) {
		for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
			TestRunTab v = (TestRunTab) e.nextElement();
			if (((CTabFolder) event.widget).getSelection().getText() == v.getName()) {
				v.setSelectedTest(fActiveRunTab.getSelectedTestId());
				fActiveRunTab = v;
				fActiveRunTab.activate();
			}
		}
	}

	private void loadTestRunTabs(CTabFolder tabFolder) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ID_EXTENSION_POINT_TESTRUN_TABS);
		if (extensionPoint == null) { return; }
		IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
		MultiStatus status = new MultiStatus(TestunitPlugin.PLUGIN_ID, IStatus.OK, "Could not load some testRunTabs extension points", null); //$NON-NLS-1$ 	

		for (int i = 0; i < configs.length; i++) {
			try {
				TestRunTab testRunTab = (TestRunTab) configs[i].createExecutableExtension("class"); //$NON-NLS-1$
				testRunTab.createTabControl(tabFolder, fClipboard, this);
				fTestRunTabs.addElement(testRunTab);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
		if (!status.isOK()) {
			TestunitPlugin.log(status);
		}
	}

	protected Composite createProgressCountPanel(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		setCounterColumns(layout);

		fCounterPanel = new CounterPanel(composite);
		fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		fProgressBar = new TestUnitProgressBar(composite);
		fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	private void setCounterColumns(GridLayout layout) {
		if (fCurrentOrientation == VIEW_ORIENTATION.HORIZONTAL)
			layout.numColumns = 2;
		else
			layout.numColumns = 1;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (fActiveRunTab != null) fActiveRunTab.setFocus();
	}

	public void showTest(TestRunInfo test) {
		fActiveRunTab.setSelectedTest(test.getTestId());
		handleTestSelected(test.getTestId());
		// TODO Allow OpenTestAction again!
		//	new OpenTestAction(this, test.getClassName(),
		// test.getTestMethodName()).run();
	}

	public void handleTestSelected(String testId) {
		TestRunInfo testInfo = getTestInfo(testId);

		if (testInfo == null) {
			showFailure(null); //$NON-NLS-1$
		} else {
			showFailure(testInfo);
		}
	}

	public TestRunInfo getTestInfo(String testId) {
		if (testId == null) return null;
		return fTestInfos.get(testId);
	}

	private void showFailure(final TestRunInfo failure) {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (!isDisposed()) fFailureTrace.showFailure(failure);
			}
		});
	}

	private void postSyncRunnable(Runnable r) {
		if (!isDisposed()) getDisplay().syncExec(r);
	}

	private boolean isDisposed() {
		return fIsDisposed || fCounterPanel.isDisposed();
	}

	private Display getDisplay() {
		return getViewSite().getShell().getDisplay();
	}

	public synchronized void dispose() {
		fIsDisposed = true;
		stopTest();
		
		IHandlerService handlerService= (IHandlerService) getSite().getWorkbenchWindow().getService(IHandlerService.class);
		handlerService.deactivateHandler(fRerunLastActivation);
		
		if (fProgressImages != null)
				fProgressImages.dispose();
//		TestunitPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		fTestRunOKIcon.dispose();
		fTestRunFailIcon.dispose();
		fStackViewIcon.dispose();
		fTestRunOKDirtyIcon.dispose();
		fTestRunFailDirtyIcon.dispose();
		getViewSite().getPage().removePartListener(fPartListener);
		if (fClipboard != null) fClipboard.dispose();
	}

	public void rerunTest(String testId, String className, String testName, String launchMode) {
		DebugUITools.saveAndBuildBeforeLaunch();
		if (lastLaunchIsKeptAlive())
			fTestRunnerClient.rerunTest(testId, className, testName);
		else if (fLastLaunch != null) {
			// run the selected test using the previous launch configuration
			ILaunchConfiguration launchConfiguration = fLastLaunch.getLaunchConfiguration();
			if (launchConfiguration != null) {
				// TODO Cleanup
				//rerunWithNewPort(className, launchMode, launchConfiguration);
				try {
					String name = className;
					if (testName != null) name += "." + testName; //$NON-NLS-1$
					String configName = TestUnitMessages.getFormattedString(TestUnitMessages.TestRunnerViewPart_configName, name);
					ILaunchConfigurationWorkingCopy tmp = launchConfiguration.copy(configName);
					// fix for bug: 64838 junit view run single test does not
					// use
					// correct class [JUnit]
					tmp.setAttribute(TestUnitLaunchConfigurationDelegate.TESTTYPE_ATTR, className);
					
					if (testName != null) {
						tmp.setAttribute(TestUnitLaunchConfigurationDelegate.TESTNAME_ATTR, testName);
					}
					tmp.launch(launchMode, null);
					return;
				} catch (CoreException e) {
					ErrorDialog.openError(getSite().getShell(), TestUnitMessages.TestRunnerViewPart_error_cannotrerun, e.getMessage(), e.getStatus() //$NON-NLS-1$
							);
				}
			}
			MessageDialog.openInformation(getSite().getShell(), TestUnitMessages.TestRunnerViewPart_cannotrerun_title,
					TestUnitMessages.TestRunnerViewPart_cannotrerurn_message
					);
		}
	}

	public boolean lastLaunchIsKeptAlive() {
		return fTestRunnerClient != null && fTestRunnerClient.isRunning() && ILaunchManager.DEBUG_MODE.equals(fLaunchMode);
	}

	public void startTestRunListening(int port, IType type, ILaunch launch, Set<ITestRunListener> testRunListeners) {
	    if(type != null) fTestProject= type.getRubyProject();
	    else {
	    	try {
				String projectName = launch.getLaunchConfiguration().getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
				fTestProject = RubyModelManager.getRubyModelManager().getRubyModel().getRubyProject(projectName);
	    	} catch (CoreException e) {
				TestunitPlugin.log(e);
			}
	    }
		fLaunchMode = launch.getLaunchMode();
		aboutToLaunch();

		if (fTestRunnerClient != null) {
			stopTest();
		}
		fTestRunnerClient = new RemoteTestRunnerClient();

		// add the TestUnitView to the list of registered listeners
		ITestRunListener[] listenerArray = new ITestRunListener[testRunListeners.size() + 1];
		listenerArray[0] = this;
		Iterator<ITestRunListener> iter = testRunListeners.iterator();
		for (int i = 0; i < testRunListeners.size(); i++) {
			listenerArray[i + 1] = iter.next();
		}
		fTestRunnerClient.startListening(listenerArray, port);

		fLastLaunch = launch;
		setViewPartTitle(type);
		if (type instanceof IType)
			setTitleToolTip(((IType)type).getFullyQualifiedName());
		else if (type != null)
			setTitleToolTip(type.getElementName());
	}

	protected void aboutToLaunch() {
		String msg = TestUnitMessages.TestRunnerViewPart_message_launching;
		showInformation(msg);
		setInfoMessage(msg);
		fViewImage= fOriginalViewImage;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	protected void showInformation(final String info) {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (!isDisposed()) fFailureTrace.setInformation(info);
			}
		});
	}

	protected void setInfoMessage(final String message) {
		fStatus = message;
	}

	/**
	 * Stops the currently running test and shuts down the RemoteTestRunner
	 */
	public void stopTest() {
		if (fTestRunnerClient != null) fTestRunnerClient.stopTest();
		stopUpdateJob();
	}

	private void stopUpdateJob() {
		if (fUpdateJob != null) {
			fUpdateJob.stop();
			fUpdateJob = null;
		}
	}

	public void setAutoScroll(boolean scroll) {
		fAutoScroll = scroll;
	}

	public boolean isAutoScroll() {
		return fAutoScroll;
	}

	public boolean isCreated() {
		return fCounterPanel != null;
	}

	public void reset() {
		reset(0);
		setViewPartTitle(null);
		clearStatus();
		resetViewIcon();
	}

	private void clearStatus() {
		getStatusLine().setMessage(null);
		getStatusLine().setErrorMessage(null);
	}

	private IStatusLineManager getStatusLine() {
		// we want to show messages globally hence we
		// have to go through the active part
		IViewSite site = getViewSite();
		IWorkbenchPage page = site.getPage();
		IWorkbenchPart activePart = page.getActivePart();

		if (activePart instanceof IViewPart) {
			IViewPart activeViewPart = (IViewPart) activePart;
			IViewSite activeViewSite = activeViewPart.getViewSite();
			return activeViewSite.getActionBars().getStatusLineManager();
		}

		if (activePart instanceof IEditorPart) {
			IEditorPart activeEditorPart = (IEditorPart) activePart;
			IEditorActionBarContributor contributor = activeEditorPart.getEditorSite().getActionBarContributor();
			if (contributor instanceof EditorActionBarContributor) return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		// no active part
		return getViewSite().getActionBars().getStatusLineManager();
	}

	private void resetViewIcon() {
		fViewImage = fOriginalViewImage;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	private void setViewPartTitle(IRubyElement type) {
		String title;
		if (type == null)
			title = " "; //$NON-NLS-1$
		else {
			if (type instanceof IType) {
				title = ((IType) type).getFullyQualifiedName();
			} else
				title = type.getElementName();
		}
		setContentDescription(title);
	}

	private void reset(final int testCount) {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				fCounterPanel.reset();
				fFailureTrace.clear();
				fProgressBar.reset();
				//				 TODO enable stop action
				//fStopAction.setEnabled(true);
				clearStatus();
				start(testCount);
			}
		});
		fExecutedTests = 0;
		fFailureCount = 0;
		fErrorCount = 0;
		fTestCount = testCount;
		fIsRunning = false;
		fIsStopped = false;
		fStartedCount = 0;
		aboutToStart();
		fTestInfos.clear();
		fFailures = new ArrayList<TestRunInfo>();
	}

	protected void start(final int total) {
		resetProgressBar(total);
		fCounterPanel.setTotal(total);
		fCounterPanel.setRunValue(0);
	}

	private void resetProgressBar(final int total) {
		fProgressBar.reset();
		fProgressBar.setMaximum(total);
	}

	private void aboutToStart() {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (!isDisposed()) {
					for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
						TestRunTab v = (TestRunTab) e.nextElement();
						v.aboutToStart();
					}
					// TODO Re-enable actions
					//fNextAction.setEnabled(false);
					//fPreviousAction.setEnabled(false);
				}
			}
		});
	}

	/*
	 * @see ITestRunListener#testEnded
	 */
	public void testEnded(String testId, String testName) {
		postEndTest(testId, testName);
		fExecutedTests++;
	}

	/*
	 * @see ITestRunListener#testFailed
	 */
	public void testFailed(int status, String testId, String testName, String trace) {
		testFailed(status, testId, testName, trace, null, null);
	}

	/*
	 * @see ITestRunListener#testFailed
	 */
	public void testFailed(int status, String testId, String testName, String trace, String expected, String actual) {
		TestRunInfo testInfo = getTestInfo(testId);
		if (testInfo == null) {
			testInfo = new TestRunInfo(testId, testName);
			fTestInfos.put(testName, testInfo);
		}
		testInfo.setTrace(trace);
		testInfo.setStatus(status);
		if (expected != null) {
			testInfo.setExpected(expected.substring(0, expected.length() - 1));
		}
		if (actual != null) testInfo.setActual(actual.substring(0, actual.length() - 1));

		if (status == ITestRunListener.STATUS_ERROR)
			fErrorCount++;
		else
			fFailureCount++;
		fFailures.add(testInfo);
		// show the view on the first error only
		if (fShowOnErrorOnly && (fErrorCount + fFailureCount == 1)) postShowTestResultsView();
	}

	protected void postShowTestResultsView() {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				showTestResultsView();
			}
		});
	}

	public void showTestResultsView() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		TestUnitView testRunner = null;

		if (page != null) {
			try { // show the result view
				testRunner = (TestUnitView) page.findView(TestUnitView.NAME);
				if (testRunner == null) {
					IWorkbenchPart activePart = page.getActivePart();
					testRunner = (TestUnitView) page.showView(TestUnitView.NAME);
					//restore focus stolen by the creation of the console
					page.activate(activePart);
				} else {
					page.bringToTop(testRunner);
				}
			} catch (PartInitException pie) {
				TestunitPlugin.log(pie);
			}
		}
	}

	/*
	 * @see ITestRunListener#testReran
	 */
	public void testReran(String testId, String className, String testName, int status, String trace) {
		if (status == ITestRunListener.STATUS_ERROR) {
			String msg = TestUnitMessages.getFormattedString(TestUnitMessages.TestRunnerViewPart_message_error, new String[] { testName, className});
			postError(msg);
		} else if (status == ITestRunListener.STATUS_FAILURE) {
			String msg = TestUnitMessages.getFormattedString(TestUnitMessages.TestRunnerViewPart_message_failure, new String[] { testName, className});
			postError(msg);
		} else {
			String msg = TestUnitMessages.getFormattedString(TestUnitMessages.TestRunnerViewPart_message_success, new String[] { testName, className});
			setInfoMessage(msg);
		}
		TestRunInfo info = getTestInfo(testId);
		updateTest(info, status);
		if (info.getTrace() == null || !info.getTrace().equals(trace)) {
			info.setTrace(trace);
			showFailure(info);
		}
	}

	protected void postError(final String message) {
		fStatus = message;
	}

	private void updateTest(TestRunInfo info, final int status) {
		if (status == info.getStatus()) return;
		if (info.getStatus() == ITestRunListener.STATUS_OK) {
			if (status == ITestRunListener.STATUS_FAILURE)
				fFailureCount++;
			else if (status == ITestRunListener.STATUS_ERROR) fErrorCount++;
		} else if (info.getStatus() == ITestRunListener.STATUS_ERROR) {
			if (status == ITestRunListener.STATUS_OK)
				fErrorCount--;
			else if (status == ITestRunListener.STATUS_FAILURE) {
				fErrorCount--;
				fFailureCount++;
			}
		} else if (info.getStatus() == ITestRunListener.STATUS_FAILURE) {
			if (status == ITestRunListener.STATUS_OK)
				fFailureCount--;
			else if (status == ITestRunListener.STATUS_ERROR) {
				fFailureCount--;
				fErrorCount++;
			}
		}
		info.setStatus(status);
		final TestRunInfo finalInfo = info;
		postSyncRunnable(new Runnable() {

			public void run() {
				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
					TestRunTab v = (TestRunTab) e.nextElement();
					v.testStatusChanged(finalInfo);
				}
			}
		});

	}

	public void testReran(String testId, String className, String testName, int statusCode, String trace, String expectedResult, String actualResult) {
		testReran(testId, className, testName, statusCode, trace);
		TestRunInfo info = getTestInfo(testId);
		info.setActual(actualResult);
		info.setExpected(expectedResult);
		fFailureTrace.updateEnablement(info);
	}

	private void postEndTest(final String testId, final String testName) {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				handleEndTest();
				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
					TestRunTab v = (TestRunTab) e.nextElement();
					v.endTest(testId);
				}

				if (fFailureCount + fErrorCount > 0) {
					// TODO Re-enable actions
					//fNextAction.setEnabled(true);
					//fPreviousAction.setEnabled(true);
				}
			}
		});
	}

	private void handleEndTest() {
		fProgressBar.step(fFailureCount + fErrorCount);
		if (fShowOnErrorOnly) {
			Image progress = fProgressImages.getImage(fExecutedTests, fTestCount, fErrorCount, fFailureCount);
			if (progress != fViewImage) {
				fViewImage = progress;
				firePropertyChange(IWorkbenchPart.PROP_TITLE);
			}
		}
	}

	public void setShowOnErrorOnly(boolean showOnErrorOnly) {
		this.fShowOnErrorOnly = showOnErrorOnly;
	}
	
	public boolean getShowOnErrorOnly() {
		return this.fShowOnErrorOnly;
	}
	
	/*
	 * @see ITestRunListener#testStarted
	 */
	public void testStarted(String testId, String testName) {
		postStartTest(testId, testName);
		// reveal the part when the first test starts
		if (!fShowOnErrorOnly && fExecutedTests == 1) postShowTestResultsView();

		TestRunInfo testInfo = getTestInfo(testId);
		if (testInfo == null) {
			testInfo = new TestRunInfo(testId, testName);
			fTestInfos.put(testId, testInfo);
		}
		String className = testInfo.getClassName();
		String method = testInfo.getTestMethodName();
		String status = TestUnitMessages.getFormattedString(TestUnitMessages.TestRunnerViewPart_message_started, new String[] { className, method});
		setInfoMessage(status);
		fStartedCount++;
	}

	private void postStartTest(final String testId, final String testName) {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
					TestRunTab v = (TestRunTab) e.nextElement();
					v.startTest(testId);
				}
			}
		});
	}

	/*
	 * @see ITestRunListener#testRunStopped
	 */
	public void testRunStopped(final long elapsedTime) {
		setInfoMessage(TestUnitMessages.TestRunnerViewPart_message_stopped);
		handleStopped();
		fIsRunning = false;
		fIsStopped = true;
	}

	private void handleStopped() {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				resetViewIcon();
				//fStopAction.setEnabled(false);
				fProgressBar.stopped();
			}
		});
		stopUpdateJob();
	}

	/*
	 * @see ITestRunListener#testRunEnded
	 */
	public void testRunEnded(long elapsedTime) {
		fExecutedTests--;
		fIsRunning  = false;
		String[] keys = { elapsedTimeAsString(elapsedTime)};
		String msg = TestUnitMessages.getFormattedString(TestUnitMessages.TestRunnerViewPart_message_finish, keys);
		if (hasErrorsOrFailures())
			postError(msg);
		else
			setInfoMessage(msg);

		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				//fStopAction.setEnabled(lastLaunchIsKeptAlive());
				if (fFailures.size() > 0) {
					selectFirstFailure();
				}
				updateViewIcon();
				if (fDirtyListener == null) {
					fDirtyListener = new DirtyListener();
					RubyCore.addElementChangedListener(fDirtyListener);
				}
				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
					TestRunTab v = (TestRunTab) e.nextElement();
					v.aboutToEnd();
				}
			}
		});
		stopUpdateJob();
	}

	private String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double) runTime / 1000);
	}

	private boolean hasErrorsOrFailures() {
		return fErrorCount + fFailureCount > 0;
	}

	protected void selectFirstFailure() {
		TestRunInfo firstFailure = fFailures.get(0);
		if (firstFailure != null && fAutoScroll) {
			fActiveRunTab.setSelectedTest(firstFailure.getTestId());
			handleTestSelected(firstFailure.getTestId());
		}
	}

	/*
	 * @see ITestRunListener#testRunTerminated
	 */
	public void testRunTerminated() {
		String msg = TestUnitMessages.TestRunnerViewPart_message_terminated;
		showMessage(msg);
		handleStopped();
		fIsRunning = false;
		fIsStopped= true;
	}

	private void showMessage(String msg) {
		postError(msg);
	}

	/*
	 * @see ITestRunListener#testRunStarted(testCount)
	 */
	public void testRunStarted(final int testCount) {
		reset(testCount);
//		fShowOnErrorOnly = TestUnitPreferencePage.getShowOnErrorOnly();
		fExecutedTests++;
		stopUpdateJob();
		fUpdateJob = new UpdateUIJob(TestUnitMessages.TestRunnerViewPart_jobName); 
		fUpdateJob.schedule(REFRESH_INTERVAL);
		fIsRunning = true;
	}

	private void refreshCounters() {
		fCounterPanel.setErrorValue(fErrorCount);
		fCounterPanel.setFailureValue(fFailureCount);
		fCounterPanel.setRunValue(fExecutedTests);
		fProgressBar.refresh(fErrorCount + fFailureCount > 0);
	}

	protected void doShowStatus() {
		setContentDescription(fStatus);
	}

	/*
	 * @see ITestRunListener2#testTreeEntry
	 */
	public void testTreeEntry(final String treeEntry) {
		postSyncRunnable(new Runnable() {

			public void run() {
				if (isDisposed()) return;
				for (Enumeration e = fTestRunTabs.elements(); e.hasMoreElements();) {
					TestRunTab v = (TestRunTab) e.nextElement();
					v.newTreeEntry(treeEntry);
				}
			}
		});
	}

	/**
	 * Stops the currently running test and shuts down the RemoteTestRunner
	 */
	public void rerunTestRun() {
		if (lastLaunchIsKeptAlive()) {
			// prompt for terminating the existing run
			if (MessageDialog.openQuestion(getSite().getShell(), TestUnitMessages.TestRunnerViewPart_terminate_title, TestUnitMessages.TestRunnerViewPart_terminate_message)) {
				if (fTestRunnerClient != null) fTestRunnerClient.stopTest();
			}
		}
		if (fLastLaunch != null && fLastLaunch.getLaunchConfiguration() != null) {
			DebugUITools.launch(fLastLaunch.getLaunchConfiguration(), fLastLaunch.getLaunchMode());
		}
	}
	
	private void addResizeListener(Composite parent) {
		parent.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				computeOrientation();
			}
		});
	}
	
	void computeOrientation() {
		if (fOrientation != VIEW_ORIENTATION.AUTOMATIC) {
			fCurrentOrientation= fOrientation;
			setOrientation(fCurrentOrientation);
		}
		else {
			Point size= fParent.getSize();
			if (size.x != 0 && size.y != 0) {
				if (size.x > size.y) 
					setOrientation(VIEW_ORIENTATION.HORIZONTAL);
				else 
					setOrientation(VIEW_ORIENTATION.VERTICAL);
			}
		}
	}
	
	private void setOrientation(VIEW_ORIENTATION orientation) {
		if ((fSashForm == null) || fSashForm.isDisposed())
			return;
		boolean horizontal = orientation == VIEW_ORIENTATION.HORIZONTAL;
		fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
		for (int i = 0; i < fToggleOrientationActions.length; ++i)
			fToggleOrientationActions[i].setChecked(fOrientation == fToggleOrientationActions[i].getOrientation());
		fCurrentOrientation = orientation;
		GridLayout layout= (GridLayout) fCounterComposite.getLayout();
		setCounterColumns(layout); 
		fParent.layout();
	}
	
	public IRubyProject getLaunchedProject() {
		return fTestProject;
	}
	
	public ILaunch getLastLaunch() {
		return fLastLaunch;
	}
	
	private void processChangesInUI() {
		if (fSashForm.isDisposed())
			return;
		
//		doShowInfoMessage();
		doShowStatus();
		refreshCounters();
		
		if (!fPartIsVisible)
			updateViewTitleProgress();
		else {
			updateViewIcon();
		}
//		boolean hasErrorsOrFailures= hasErrorsOrFailures();
//		fNextAction.setEnabled(hasErrorsOrFailures);
//		fPreviousAction.setEnabled(hasErrorsOrFailures);
		
//		fTestViewer.processChangesInUI();
	}

	private void updateViewIcon() {
		if (fIsStopped || fIsRunning || fStartedCount == 0)
			fViewImage= fOriginalViewImage;
		else if (hasErrorsOrFailures())
			fViewImage= fTestRunFailIcon;
		else 
			fViewImage= fTestRunOKIcon;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);	
	}
	
	/*
	 * @see IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		if (fOriginalViewImage == null)
			fOriginalViewImage= super.getTitleImage();
			
		if (fViewImage == null)
			return super.getTitleImage();
		return fViewImage;
	}

	private void updateViewTitleProgress() {
//		if (fTestRunSession != null) {
			if (fIsRunning) {
				Image progress= fProgressImages.getImage(
						fStartedCount,
						fTestCount,
						fErrorCount,
						fFailureCount);
				if (progress != fViewImage) {
					fViewImage= progress;
					firePropertyChange(IWorkbenchPart.PROP_TITLE);
				}
			} else {
				updateViewIcon();
			}
//		} else {
//			resetViewIcon();
//		}
	}
	
	void codeHasChanged() {
		if (fDirtyListener != null) {
			RubyCore.removeElementChangedListener(fDirtyListener);
			fDirtyListener= null;
		}
		if (fViewImage == fTestRunOKIcon) 
			fViewImage= fTestRunOKDirtyIcon;
		else if (fViewImage == fTestRunFailIcon)
			fViewImage= fTestRunFailDirtyIcon;
		
		Runnable r= new Runnable() {
			public void run() {
				if (isDisposed())
					return;
				firePropertyChange(IWorkbenchPart.PROP_TITLE);
			}
		};
		if (!isDisposed())
			getDisplay().asyncExec(r);
	}
	
	class UpdateUIJob extends UIJob {

		private boolean fRunning = true;

		public UpdateUIJob(String name) {
			super(name);
			setSystem(true);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!isDisposed()) {
				processChangesInUI();
			}
			schedule(REFRESH_INTERVAL);
			return Status.OK_STATUS;
		}

		public void stop() {
			fRunning = false;
		}

		public boolean shouldSchedule() {
			return fRunning;
		}

	}

	private class RerunLastAction extends Action {

		public RerunLastAction() {
			setText(TestUnitMessages.TestRunnerViewPart_rerunaction_label);
			setToolTipText(TestUnitMessages.TestRunnerViewPart_rerunaction_tooltip);
			setDisabledImageDescriptor(TestunitPlugin.getImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
			setHoverImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
			setImageDescriptor(TestunitPlugin.getImageDescriptor("elcl16/relaunch.gif")); //$NON-NLS-1$
			setActionDefinitionId(RERUN_LAST_COMMAND);
		}

		public void run() {
			rerunTestRun();
		}
	}
	
	private class ActivateOnErrorAction extends Action {
		public ActivateOnErrorAction() {
			super(TestUnitMessages.TestRunnerViewPart_activate_on_failure_only, IAction.AS_CHECK_BOX);
			update();
		}
		public void update() {
			setChecked(getShowOnErrorOnly());
		}
		public void run() {
			boolean checked= isChecked();
			fShowOnErrorOnly= checked;
			IPreferenceStore store= TestunitPlugin.getDefault().getPreferenceStore();
			store.setValue(TestUnitPreferencesConstants.SHOW_ON_ERROR_ONLY, checked);
		}
	}

	/**
	 * Listen for for modifications to Ruby elements
	 */
	private class DirtyListener implements IElementChangedListener {
		public void elementChanged(ElementChangedEvent event) {
			processDelta(event.getDelta());				
		}
		
		private boolean processDelta(IRubyElementDelta delta) {
			int kind= delta.getKind();
			int details= delta.getFlags();
			int type= delta.getElement().getElementType();
			
			switch (type) {
				// Consider containers for class files.
				case IRubyElement.RUBY_MODEL:
				case IRubyElement.RUBY_PROJECT:
				case IRubyElement.SOURCE_FOLDER_ROOT:
				case IRubyElement.SOURCE_FOLDER:
					// If we did something different than changing a child we flush the undo / redo stack.
					if (kind != IRubyElementDelta.CHANGED || details != IRubyElementDelta.F_CHILDREN) {
						codeHasChanged();
						return false;
					}
					break;
				case IRubyElement.SCRIPT:
					// if we have changed a primary working copy (e.g created, removed, ...)
					// then we do nothing.
					if ((details & IRubyElementDelta.F_PRIMARY_WORKING_COPY) != 0) 
						return true;
					codeHasChanged();
					return false;
				default:
					codeHasChanged();
					return false;	
			}
				
			IRubyElementDelta[] affectedChildren= delta.getAffectedChildren();
			if (affectedChildren == null)
				return true;
	
			for (int i= 0; i < affectedChildren.length; i++) {
				if (!processDelta(affectedChildren[i]))
					return false;
			}
			return true;			
		}
	}
}