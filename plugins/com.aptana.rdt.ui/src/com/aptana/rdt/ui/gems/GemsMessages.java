package com.aptana.rdt.ui.gems;

import org.eclipse.osgi.util.NLS;

public class GemsMessages extends NLS {
	
	private static final String BUNDLE_NAME = GemsMessages.class.getName();

	public static String GemsView_NameColumn_label;
	public static String GemsView_VersionColumn_label;
	public static String GemsView_DescriptionColumn_label;
	public static String InstallGemDialog_dialog_title;
	public static String InstallGemDialog_version_label;
	public static String InstallGemDialog_name_label;
	public static String GemManager_loading_local_gems;
	public static String GemManager_loading_remote_gems;
	public static String RemoveGemDialog_msg;
	public static String RemoveGemDialog_dialog_title;
	public static String RemoveGemDialog_version_label;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, GemsMessages.class);
	}

}
