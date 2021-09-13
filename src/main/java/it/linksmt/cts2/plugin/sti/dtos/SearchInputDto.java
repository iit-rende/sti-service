package it.linksmt.cts2.plugin.sti.dtos;


public class SearchInputDto{
	
	private Integer page;
	private Integer maxtoreturn;
	private String sourceOrTargetEntity;
	private String mapping;
	private String lang;
	
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getMaxtoreturn() {
		return maxtoreturn;
	}
	public void setMaxtoreturn(Integer maxtoreturn) {
		this.maxtoreturn = maxtoreturn;
	}
	public String getSourceOrTargetEntity() {
		return sourceOrTargetEntity;
	}
	public void setSourceOrTargetEntity(String sourceOrTargetEntity) {
		this.sourceOrTargetEntity = sourceOrTargetEntity;
	}
	public String getMapping() {
		return mapping;
	}
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	
}
