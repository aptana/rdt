package com.aptana.rdt.core.gems;

public class Version extends org.osgi.framework.Version {

	public Version(String version) {
		super(version);
	}

	public boolean isGreaterThan(Version other) {
		int result = compareTo(other);
		return result > 0;
	}

	public boolean isEqualTo(Version other) {
		int result = compareTo(other);
		return result == 0;
	}

	public boolean isGreaterThanOrEqualTo(Version other) {
		int result = compareTo(other);
		return result >= 0;
	}

	public boolean isLessThan(Version other) {
		int result = compareTo(other);
		return result < 0;
	}

	public boolean isLessThanOrEqualTo(Version other) {
		int result = compareTo(other);
		return result <= 0;
	}

	public int getBugfix() {
		return getMicro();
	}

	public int getRevision() {
		String qualifier = getQualifier();
		if (qualifier == null)
			return 0;
		return Integer.parseInt(qualifier);
	}

	public boolean isGreaterThanOrEqualTo(String string) {
		return isGreaterThanOrEqualTo(new Version(string));
	}

	public boolean isLessThanOrEqualTo(String string) {
		return isLessThanOrEqualTo(new Version(string));
	}

	public boolean isLessThan(String string) {
		return isLessThan(new Version(string));
	}
}
