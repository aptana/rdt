package org.rubypeople.rdt.ui.rubyeditor;

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.ISourceReference;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyAbstractEditor;

public interface ICustomRubyOutlinePage extends IContentOutlinePage {

	public boolean isEnabled(IRubyElement inputElement);

	public void init(String outlinerContextMenuId,
			RubyAbstractEditor rubyAbstractEditor);

	public void select(ISourceReference reference);

	public void setInput(IRubyElement inputElement);

}
