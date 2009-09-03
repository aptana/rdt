package com.aptana.rdt.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferencesMessages extends NLS {
	
	private static final String BUNDLE_NAME = PreferencesMessages.class.getName();
	
	public static String LicenseConfigurationBlock_company_name_label;
	public static String LicenseConfigurationBlock_email_label;
	public static String LicenseConfigurationBlock_description;
	public static String LicenseConfigurationBlock_license_key_label;
	public static String LicenseConfigurationBlock_valid_license_label;
	public static String LicenseConfigurationBlock_invalid_license_label;

	static {
		NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
	}
}
