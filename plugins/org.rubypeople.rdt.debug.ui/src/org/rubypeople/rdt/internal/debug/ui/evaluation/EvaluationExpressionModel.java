/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 */

package org.rubypeople.rdt.internal.debug.ui.evaluation;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;

public class EvaluationExpressionModel implements Preferences.IPropertyChangeListener {

	public void propertyChange(PropertyChangeEvent event) {
		this.loadExpressions();
	}

	private EvaluationExpression[] evaluationExpressions;

	public EvaluationExpression[] getEvaluationExpressions() {
		if (evaluationExpressions == null) {
			getPreferences().addPropertyChangeListener(this);
			this.loadExpressions();
		}
		return evaluationExpressions;
	}

	private void loadExpressions() {
		String expressionsXml = this.getPreferences().getString(RdtDebugUiConstants.EVALUATION_EXPRESSIONS_PREFERENCE);
		try {
			evaluationExpressions = new EvaluationExpressionReaderWriter().read(new StringReader(expressionsXml), null);
		} catch (IOException e) {
			evaluationExpressions = new EvaluationExpression[0];
			RdtDebugUiPlugin.log(e);
		}
	}

	private Preferences getPreferences() {
		return RdtDebugUiPlugin.getDefault().getPluginPreferences();
	}
}