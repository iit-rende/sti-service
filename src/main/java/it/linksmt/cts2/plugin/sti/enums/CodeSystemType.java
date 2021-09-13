package it.linksmt.cts2.plugin.sti.enums;

public enum CodeSystemType {
	
//	AIC("AIC"),
//	ATC("ATC"),
//	ICD9CM("ICD9CM"),
//	LOINC("LOINC"),
	STANDARD_NATIONAL_STATIC("STANDARD_NATIONAL_STATIC"),
	STANDARD_NATIONAL("STANDARD_NATIONAL"),
	LOCAL("LOCAL"),
	VALUE_SET("VALUE_SET");
	
	private String key;
	
	private CodeSystemType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
