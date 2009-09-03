package org.rubypeople.rdt.internal.debug.ui.actions;

import org.eclipse.jface.viewers.Viewer;
import org.rubypeople.rdt.debug.core.model.IRubyVariable;
import org.rubypeople.rdt.debug.ui.RdtDebugUiConstants;

public class ShowStaticVariablesAction extends VariableFilterAction
{

	protected String getPreferenceKey()
	{
		return RdtDebugUiConstants.SHOW_STATIC_VARIABLES_PREFERENCE;
	}

	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (element instanceof IRubyVariable)
		{
			IRubyVariable variable = (IRubyVariable) element;
			if (!getValue())
			{
				// when not on, filter non-static finals
				return !variable.isStatic();
			}
		}
		return true;
	}
}
