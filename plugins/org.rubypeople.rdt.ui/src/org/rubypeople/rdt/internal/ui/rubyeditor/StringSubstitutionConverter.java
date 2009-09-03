package org.rubypeople.rdt.internal.ui.rubyeditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.rubypeople.rdt.internal.ui.rubyeditor.RubyEditor.ITextConverter;
import org.rubypeople.rdt.internal.ui.text.IRubyPartitions;

/**
 * Takes commands inside strings, like selecting text and inserting '#' to mean to turn selected text into variable
 * substitution '#{text}'.
 * 
 * @author cwilliams
 */
class StringSubstitutionConverter implements ITextConverter
{

	private ILineTracker fLineTracker;
	private RubyEditor editor;

	public StringSubstitutionConverter(RubyEditor rubyEditor)
	{
		this.editor = rubyEditor;
	}

	public void setLineTracker(ILineTracker lineTracker)
	{
		fLineTracker = lineTracker;
	}

	public void customizeDocumentCommand(IDocument document, DocumentCommand command)
	{
		String text = command.text;
		if (text == null)
			return;

		String textSelected = "";
		ISelection selection = editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection)
		{
			ITextSelection textSelect = (ITextSelection) selection;
			textSelected = textSelect.getText();
		}
		if (textSelected == null || textSelected.trim().length() == 0)
			return;

		if (text.equals("#"))
		{
			doStringSubstitution(document, command, textSelected);
			return;
		}
		else if (text.equals("'") || text.equals("\"") || text.equals("`"))
		{
			doStringWrapping(document, command, textSelected);
			return;
		}

	}

	private void doStringSubstitution(IDocument document, DocumentCommand command, String textSelected)
	{
		fLineTracker.set(command.text);
		try
		{
			// Determine if we're in a string at this offset!
			ITypedRegion partition = TextUtilities.getPartition(document, IRubyPartitions.RUBY_PARTITIONING,
					command.offset, true);
			if (!partition.getType().equals(IRubyPartitions.RUBY_STRING)
					&& !partition.getType().equals(IRubyPartitions.RUBY_COMMAND))
				return;
			// We are, and it's a pound symbol, so lets place pound in front of selection and surround it in braces!
			command.text = "#{" + textSelected + "}";
		}
		catch (BadLocationException x)
		{
		}
	}

	/**
	 * If we're not already inside a string, wrap selected text in quote that user hit!
	 * 
	 * @param document
	 * @param command
	 */
	private void doStringWrapping(IDocument document, DocumentCommand command, String textSelected)
	{
		String character = command.text;
		fLineTracker.set(command.text);
		try
		{
			ITypedRegion partition = TextUtilities.getPartition(document, IRubyPartitions.RUBY_PARTITIONING,
					command.offset, true);
			if (partition.getType().equals(IRubyPartitions.RUBY_STRING)
					|| partition.getType().equals(IRubyPartitions.RUBY_COMMAND))
				return;
			command.text = character + textSelected + character;
		}
		catch (BadLocationException x)
		{
		}
	}
}
