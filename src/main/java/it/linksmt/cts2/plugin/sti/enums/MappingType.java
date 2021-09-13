package it.linksmt.cts2.plugin.sti.enums;


public enum MappingType {
	GENERIC("GENERIC"),
	ATC_AIC("ATC-AIC"),
	LOCAL_LOINC("LOCAL-LOINC");
	
	
	private String key;
	
	private MappingType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
