package org.rubypeople.rdt.internal.debug.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.Bundle;

public class RubyDebugImages
{

	private static String ICONS_PATH = "$nl$/icons/full/"; //$NON-NLS-1$

	private static ImageRegistry fgImageRegistry;
	
	public static final String IMG_OBJS_EXCEPTION= "IMG_OBJS_EXCEPTION";			//$NON-NLS-1$
	public static final String IMG_OBJS_EXCEPTION_DISABLED= "IMG_OBJS_EXCEPTION_DISABLED";			//$NON-NLS-1$
	public static final String IMG_OBJS_ERROR= "IMG_OBJS_ERROR";			//$NON-NLS-1$	
	
	public static final String IMG_OVR_BREAKPOINT_INSTALLED= "IMG_OBJS_BREAKPOINT_INSTALLED";	//$NON-NLS-1$
	public static final String IMG_OVR_BREAKPOINT_INSTALLED_DISABLED= "IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED";	//$NON-NLS-1$
	
	public static final String IMG_OVR_METHOD_BREAKPOINT_ENTRY= "IMG_OBJS_METHOD_BREAKPOINT_ENTRY";	//$NON-NLS-1$
	public static final String IMG_OVR_METHOD_BREAKPOINT_ENTRY_DISABLED= "IMG_OBJS_METHOD_BREAKPOINT_ENTRY_DISABLED";	//$NON-NLS-1$
	public static final String IMG_OVR_METHOD_BREAKPOINT_EXIT= "IMG_OBJS_METHOD_BREAKPOINT_EXIT";	//$NON-NLS-1$
	public static final String IMG_OVR_METHOD_BREAKPOINT_EXIT_DISABLED= "IMG_OBJS_METHOD_BREAKPOINT_EXIT_DISABLED";	//$NON-NLS-1$
	
	public static final String IMG_OVR_CONDITIONAL_BREAKPOINT= "IMG_OBJS_CONDITIONAL_BREAKPOINT";	//$NON-NLS-1$
	public static final String IMG_OVR_CONDITIONAL_BREAKPOINT_DISABLED= "IMG_OBJS_CONDITIONAL_BREAKPOINT_DISABLED";	//$NON-NLS-1$

	public static final String IMG_OVR_SCOPED_BREAKPOINT= "IMG_OBJS_SCOPED_BREAKPOINT";	//$NON-NLS-1$
	public static final String IMG_OVR_SCOPED_BREAKPOINT_DISABLED= "IMG_OBJS_SCOPED_BREAKPOINT_DISABLED";	//$NON-NLS-1$
	
	public static final String IMG_OVR_UNCAUGHT_BREAKPOINT= "IMG_OBJS_UNCAUGHT_BREAKPOINT";	//$NON-NLS-1$
	public static final String IMG_OVR_UNCAUGHT_BREAKPOINT_DISABLED= "IMG_OBJS_UNCAUGHT_BREAKPOINT_DISABLED";	//$NON-NLS-1$
	
	public static final String IMG_OVR_CAUGHT_BREAKPOINT= "IMG_OBJS_CAUGHT_BREAKPOINT";	//$NON-NLS-1$
	public static final String IMG_OVR_CAUGHT_BREAKPOINT_DISABLED= "IMG_OBJS_CAUGHT_BREAKPOINT_DISABLED";	//$NON-NLS-1$
	
	public static final String IMG_OVR_OWNED = "IMG_OVR_OWNED";			//$NON-NLS-1$
	public static final String IMG_OVR_OWNS_MONITOR = "IMG_OVR_OWNS_MONITOR";		//$NON-NLS-1$
	public static final String IMG_OVR_IN_CONTENTION = "IMG_OVR_IN_CONTENTION";			//$NON-NLS-1$
	public static final String IMG_OVR_IN_CONTENTION_FOR_MONITOR = "IMG_OVR_IN_CONTENTION_FOR_MONITOR";		//$NON-NLS-1$
	public static final String IMG_OVR_IN_DEADLOCK = "IMG_OVR_IN_DEADLOCK"; //$NON-NLS-1$

	public static final String IMG_OVR_OUT_OF_SYNCH = "IMG_OVR_OUT_OF_SYNCH"; //$NON-NLS-1$
	public static final String IMG_OVR_MAY_BE_OUT_OF_SYNCH = "IMG_OVR_MAY_BE_OUT_OF_SYNCH"; //$NON-NLS-1$
	public static final String IMG_OVR_SYNCHRONIZED = "IMG_OVR_SYNCHRONIZED"; //$NON-NLS-1$

	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ= ICONS_PATH + "obj16/"; 		//$NON-NLS-1$
	private static final String T_OVR= ICONS_PATH + "ovr16/"; 		//$NON-NLS-1$
//	private static final String T_WIZBAN= ICONS_PATH + "wizban/"; 	//$NON-NLS-1$
//	private static final String T_EVIEW= ICONS_PATH + "eview16/"; 	//$NON-NLS-1$
//	private static final String T_DLCL= ICONS_PATH + "dtool16/"; 	//$NON-NLS-1$
//	private static final String T_ELCL= ICONS_PATH + "etool16/"; 	//$NON-NLS-1$
//    private static final String E_LCL = ICONS_PATH + "elcl16/"; //$NON-NLS-1$

	/**
	 * Returns the <code>ImageDescriptor</code> identified by the given key, or <code>null</code> if it does not exist.
	 */
	public static ImageDescriptor getImageDescriptor(String key)
	{
		return getImageRegistry().getDescriptor(key);
	}

	/*
	 * Helper method to access the image registry from the JDIDebugUIPlugin class.
	 */
	/* package */static ImageRegistry getImageRegistry()
	{
		if (fgImageRegistry == null)
		{
			initializeImageRegistry();
		}
		return fgImageRegistry;
	}

	private static void initializeImageRegistry()
	{
		fgImageRegistry = new ImageRegistry(RdtDebugUiPlugin.getStandardDisplay());
		declareImages();
	}

	private static void declareImages()
	{		
		declareRegistryImage(IMG_OBJS_EXCEPTION, T_OBJ + "jexception_obj.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OBJS_EXCEPTION_DISABLED, T_OBJ + "jexceptiond_obj.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_BREAKPOINT_INSTALLED, T_OVR + "installed_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_BREAKPOINT_INSTALLED_DISABLED, T_OVR + "installed_ovr_disabled.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_METHOD_BREAKPOINT_ENTRY, T_OVR + "entry_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_METHOD_BREAKPOINT_ENTRY_DISABLED, T_OVR + "entry_ovr_disabled.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_METHOD_BREAKPOINT_EXIT, T_OVR + "exit_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_METHOD_BREAKPOINT_EXIT_DISABLED, T_OVR + "exit_ovr_disabled.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_CONDITIONAL_BREAKPOINT, T_OVR + "conditional_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_CONDITIONAL_BREAKPOINT_DISABLED, T_OVR + "conditional_ovr_disabled.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_SCOPED_BREAKPOINT, T_OVR + "scoped_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_SCOPED_BREAKPOINT_DISABLED, T_OVR + "scoped_ovr_disabled.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_UNCAUGHT_BREAKPOINT, T_OVR + "uncaught_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_UNCAUGHT_BREAKPOINT_DISABLED, T_OVR + "uncaught_ovr_disabled.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_CAUGHT_BREAKPOINT, T_OVR + "caught_ovr.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_OVR_CAUGHT_BREAKPOINT_DISABLED, T_OVR + "caught_ovr_disabled.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OBJS_ERROR, T_OBJ + "jrtexception_obj.gif"); //$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_OUT_OF_SYNCH, T_OVR + "error_co.gif");			//$NON-NLS-1$
		declareRegistryImage(IMG_OVR_MAY_BE_OUT_OF_SYNCH, T_OVR + "warning_co.gif");		//$NON-NLS-1$
		declareRegistryImage(IMG_OVR_SYNCHRONIZED, T_OVR + "sync_ovr.gif");				//$NON-NLS-1$
		
		declareRegistryImage(IMG_OVR_OWNED, T_OVR + "owned_ovr.gif");			//$NON-NLS-1$
		declareRegistryImage(IMG_OVR_OWNS_MONITOR, T_OVR +  "ownsmonitor_ovr.gif");		//$NON-NLS-1$
		declareRegistryImage(IMG_OVR_IN_CONTENTION, T_OVR + "contention_ovr.gif");			//$NON-NLS-1$
		declareRegistryImage(IMG_OVR_IN_CONTENTION_FOR_MONITOR, T_OVR + "contentionformonitor_ovr.gif");		//$NON-NLS-1$
		declareRegistryImage(IMG_OVR_IN_DEADLOCK, T_OVR + "deadlock_ovr.gif");		//$NON-NLS-1$
	}

	/**
	 * Declare an Image in the registry table.
	 * 
	 * @param key
	 *            The key to use when registering the image
	 * @param path
	 *            The path where the image can be found. This path is relative to where this plugin class is found (i.e.
	 *            typically the packages directory)
	 */
	private final static void declareRegistryImage(String key, String path)
	{
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		Bundle bundle = Platform.getBundle(RdtDebugUiPlugin.getUniqueIdentifier());
		URL url = null;
		if (bundle != null)
		{
			url = FileLocator.find(bundle, new Path(path), null);
			desc = ImageDescriptor.createFromURL(url);
		}
		fgImageRegistry.put(key, desc);
	}
}
