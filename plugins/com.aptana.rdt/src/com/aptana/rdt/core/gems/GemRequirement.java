package com.aptana.rdt.core.gems;



public class GemRequirement {

	private String name;
	private String versionDependency;

	public GemRequirement(String name, String versionDependency) {
		this.name = name;
		this.versionDependency = versionDependency;
	}
	
	private String getRule() {
		return versionDependency.split(" ")[0];
	}
	
	private Version getVersion() 
	{
		String raw = versionDependency.split(" ")[1];
		// FIXME When would it be using commas? Multiple version matches?
		if (raw.endsWith(","))
		{
			raw = raw.substring(0, raw.length() - 1);
		}
		return new Version(raw);
	}
	
	public String toString() {
		return name + " (" + versionDependency + ")";
	}

	public String getName() {
		return name;
	}

	public boolean meetsRequirements(String version) {
		Version gemVersion = new Version(version);
		if (getRule().equals("=")) {
			return gemVersion.equals(getVersion());
		} else if (getRule().equals(">=")) {
			return gemVersion.isGreaterThanOrEqualTo(getVersion());
		} else if (getRule().equals("<=")) {
			return gemVersion.isLessThanOrEqualTo(getVersion());
		} else if (getRule().equals(">")) {
			return gemVersion.isGreaterThan(getVersion());
		} else if (getRule().equals("<")) {
			return gemVersion.isLessThan(getVersion());
		}
		return false;
	}
}
