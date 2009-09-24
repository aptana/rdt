package org.rubypeople.rdt.internal.debug.ui;

import java.util.Hashtable;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.debug.ui.IEvaluationContextManager;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.core.model.RubyVariable;
import org.rubypeople.rdt.internal.debug.ui.evaluation.EvaluationExpressionModel;
import org.rubypeople.rdt.ui.PreferenceConstants;
import org.rubypeople.rdt.ui.text.RubyTextTools;
import org.rubypeople.rdt.ui.viewsupport.ImageDescriptorRegistry;

/**
 * RdtDebugUiPlugin class
 */
public class RdtDebugUiPlugin extends AbstractUIPlugin implements RdtDebugUiConstants
{

	/**
	 * PLUGIN_ID
	 */
	public static final String PLUGIN_ID = "org.rubypeople.rdt.debug.ui"; //$NON-NLS-1$

	/**
	 * plugin object
	 */
	protected static RdtDebugUiPlugin plugin;
	private EvaluationExpressionModel evaluationExpressionModel;

	private ImageDescriptorRegistry fImageDescriptorRegistry;
	private RubyTextTools fTextTools;

	private static Hashtable<String, Image> images = new Hashtable<String, Image>();

	private static IEvaluationContextManager manager;

	/**
	 * RdtDebugUiPlugin
	 */
	public RdtDebugUiPlugin()
	{
		super();
	}

	/**
	 * Gets the workbench window
	 * 
	 * @return - window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow()
	{
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Gets the active page
	 * 
	 * @return - page
	 */
	public static IWorkbenchPage getActivePage()
	{
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null)
		{
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Gets the plugin
	 * 
	 * @return - default plugin
	 */
	public static RdtDebugUiPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Gets the workspace
	 * 
	 * @return - workspace
	 */
	public static IWorkspace getWorkspace()
	{
		return RubyCore.getWorkspace();
	}

	/**
	 * Logs a status object
	 * 
	 * @param status
	 */
	public static void log(IStatus status)
	{
		getDefault().getLog().log(status);
	}

	/**
	 * Logs a throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e)
	{
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
				RdtDebugUiMessages.RdtDebugUiPlugin_internalErrorOccurred, e));
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path); //$NON-NLS-1$
	}

	/**
	 * getImage
	 * 
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path)
	{
		if (images.get(path) == null)
		{
			ImageDescriptor id = getImageDescriptor(path);

			if (id == null)
			{
				return null;
			}

			Image i = id.createImage();

			images.put(path, i);

			return i;
		}
		else
		{
			return (Image) images.get(path);
		}
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		plugin = this;
		super.start(context);
		Job job = new Job("RDT Debug UI Startup")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					Platform.getAdapterManager().registerAdapters(new ActionFilterAdapterFactory(), RubyVariable.class);
					new CodeReloader();
				}
				catch (Throwable e)
				{
					log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();

		job = new Job("Startup evaluation context manager")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					IEvaluationContextManager manager = getEvaluationContextManager();
					if (manager != null)
						manager.startup();
				}
				catch (Throwable e)
				{
					log(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(2000);
	}

	public static IEvaluationContextManager getEvaluationContextManager()
	{
		if (manager == null)
		{
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"evaluationContextManagers");
			if (extension == null)
				return EvaluationContextManager.instance();
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; i++)
			{
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++)
				{
					final IConfigurationElement configElement = configElements[j];
					String elementName = configElement.getName();
					if (!("evaluationContextManager".equals(elementName))) { //$NON-NLS-1$
						continue;
					}
					try
					{
						manager = (IEvaluationContextManager) configElement.createExecutableExtension("class");
						if (manager != null)
						{
							return manager;
						}
					}
					catch (Exception e)
					{
						log(e);
					}
				}
			}
		}
		return manager;
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		try
		{
			if (fImageDescriptorRegistry != null)
			{
				fImageDescriptorRegistry.dispose();
			}
		}
		finally
		{
			super.stop(context);
		}
	}

	/**
	 * Gets the evaluation expression model
	 * 
	 * @return - model
	 */
	public EvaluationExpressionModel getEvaluationExpressionModel()
	{
		if (evaluationExpressionModel == null)
		{
			evaluationExpressionModel = new EvaluationExpressionModel();
		}
		return evaluationExpressionModel;
	}

	/**
	 * Gets the plugin id
	 * 
	 * @return - id
	 */
	public static String getUniqueIdentifier()
	{
		return PLUGIN_ID;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the thread calling this method has an
	 * associated display. If so, this display is returned. Otherwise the method returns the default display.
	 * 
	 * @return - display
	 */
	public static Display getStandardDisplay()
	{
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the image descriptor registry used for this plugin.
	 * 
	 * @return - registry
	 */
	public static ImageDescriptorRegistry getImageDescriptorRegistry()
	{
		if (getDefault().fImageDescriptorRegistry == null)
		{
			getDefault().fImageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return getDefault().fImageDescriptorRegistry;
	}

	/**
	 * Gets the ruby text tools
	 * 
	 * @return - tools
	 */
	public RubyTextTools getRubyTextTools()
	{
		if (fTextTools == null)
		{
			fTextTools = new RubyTextTools(PreferenceConstants.getPreferenceStore());
		}
		return fTextTools;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell()
	{
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null)
		{
			return window.getShell();
		}
		return null;
	}

	/**
	 * Opens an error dialog
	 * 
	 * @param message
	 * @param status
	 */
	public static void errorDialog(String message, IStatus status)
	{
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null)
		{
			ErrorDialog.openError(shell, "Error", message, status);
		}
	}

	/**
	 * Utility method with conventions
	 * 
	 * @param message
	 * @param t
	 */
	public static void errorDialog(String message, Throwable t)
	{
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null)
		{
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), RdtDebugUiConstants.INTERNAL_ERROR,
					"Error logged from RDT Debug UI: ", t); //$NON-NLS-1$	
			ErrorDialog.openError(shell, "Error", message, status);
		}
	}

	public static void statusDialog(String title, IStatus status)
	{
		Shell shell = getActiveWorkbenchShell();
		if (shell != null)
		{
			switch (status.getSeverity())
			{
				case IStatus.ERROR:
					ErrorDialog.openError(shell, title, null, status);
					break;
				case IStatus.WARNING:
					MessageDialog.openWarning(shell, title, status.getMessage());
					break;
				case IStatus.INFO:
					MessageDialog.openInformation(shell, title, status.getMessage());
					break;
			}
		}
	}
}
