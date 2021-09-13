package it.linksmt.cts2.plugin.sti.enums;

public enum MetadataParameterType {
	
	STRING("java.lang.String","DF_S_"),
	NUMBER("java.lang.Double","DF_N_"),
	DATE("java.util.Date","DF_D_"),
	MAPPING("it.linksmt.cts2.portlet.search.rest.model.localcodification.HeaderFieldOption","DF_M_");
	
	private String key;
	private String prefix;
	
	private MetadataParameterType(String key,String prefix) {
		this.key = key;
		this.prefix = prefix;
	}

	public String getKey() {
		return key;
	}

	public String getPrefix() {
		return prefix;
	}
	
}
