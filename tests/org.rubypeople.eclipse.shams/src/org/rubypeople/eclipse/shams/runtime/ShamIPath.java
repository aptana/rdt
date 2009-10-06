package org.rubypeople.eclipse.shams.runtime;

import java.io.File;

import org.eclipse.core.runtime.IPath;

public class ShamIPath implements IPath {
	protected String path;

	public ShamIPath(String thePath) {
		path = thePath;
	}

	public IPath addFileExtension(String extension) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath addTrailingSeparator() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath append(String path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath append(IPath path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public Object clone() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String getDevice() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String getFileExtension() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean hasTrailingSeparator() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isAbsolute() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isEmpty() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isPrefixOf(IPath anotherPath) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isRoot() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isUNC() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isValidPath(String path) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public boolean isValidSegment(String segment) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String lastSegment() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath makeAbsolute() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath makeRelative() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath makeUNC(boolean toUNC) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public int matchingFirstSegments(IPath anotherPath) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath removeFileExtension() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath removeFirstSegments(int count) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath removeLastSegments(int count) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath removeTrailingSeparator() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String segment(int index) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public int segmentCount() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String[] segments() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath setDevice(String device) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public File toFile() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String toOSString() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath uptoSegment(int count) {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public String toString() {
		return path;
	}

	public String toPortableString() {
		throw new RuntimeException("Unimplemented method in sham");
	}

	public IPath makeRelativeTo(IPath base)
	{
		return null;
	}

}
