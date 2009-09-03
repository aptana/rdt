/*
 * Created on Mar 1, 2005
 *
 */
package org.rubypeople.rdt.internal.debug.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;


/**
 * @author Chris
 *
 */
public class DebugUiPreferenceInitializer extends AbstractPreferenceInitializer {

	public DebugUiPreferenceInitializer() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences node = new DefaultScope().getNode(RdtDebugUiPlugin.PLUGIN_ID);
		node.put(RdtDebugUiConstants.PREFERENCE_KEYWORDS, getDefaultKeywords());
        
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(RdtDebugUiPlugin.getDefault().getBundle().getEntry("/expressions/rdt.xml").openStream())) ;
            StringBuffer fileContent = new StringBuffer() ;
            while (reader.ready()) {
              fileContent.append(reader.readLine()) ;
            }
			node.put(RdtDebugUiConstants.EVALUATION_EXPRESSIONS_PREFERENCE, fileContent.toString()) ;

		} catch (IOException e) {
            RdtDebugUiPlugin.log(e) ;
		}
	}

	private String getDefaultKeywords() {
		return "class,def,end,if,module,new,puts,require,rescue,throw,while";
	}
	
}
