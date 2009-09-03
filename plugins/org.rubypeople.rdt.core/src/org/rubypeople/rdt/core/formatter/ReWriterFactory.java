package org.rubypeople.rdt.core.formatter;

import org.rubypeople.rdt.internal.formatter.rewriter.DRegxReWriteVisitor;
import org.rubypeople.rdt.internal.formatter.rewriter.HereDocReWriteVisitor;
import org.rubypeople.rdt.internal.formatter.rewriter.IgnoreCommentsReWriteVisitor;
import org.rubypeople.rdt.internal.formatter.rewriter.MultipleAssignmentReWriteVisitor;
import org.rubypeople.rdt.internal.formatter.rewriter.ShortIfNodeReWriteVisitor;

public class ReWriterFactory {
	
	private ReWriterContext config;

	public ReWriterFactory(ReWriterContext config) {
		this.config = config;
	}
	
	public ReWriteVisitor createShortIfNodeReWriteVisitor() {
		return new ShortIfNodeReWriteVisitor(config);
	}
	
	public ReWriteVisitor createMultipleAssignmentReWriteVisitor() {
		return new MultipleAssignmentReWriteVisitor(config);
	}
	
	public ReWriteVisitor createDRegxReWriteVisitor() {
		return new DRegxReWriteVisitor(config);
	}
	
	public ReWriteVisitor createHereDocReWriteVisitor() {
		return new HereDocReWriteVisitor(config);
	}
	
	public ReWriteVisitor createIgnoreCommentsReWriteVisitor() {
		return new IgnoreCommentsReWriteVisitor(config);
	}
	
	public ReWriteVisitor createReWriteVisitor() {
		return new ReWriteVisitor(config);
	}
}
