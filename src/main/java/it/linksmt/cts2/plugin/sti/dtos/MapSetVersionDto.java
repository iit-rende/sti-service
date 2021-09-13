package it.linksmt.cts2.plugin.sti.dtos;

import java.util.Date;

public class MapSetVersionDto {
	
	private Long versionId;
	private Date releaseDate;
	private Long previousVersionId;
	private Integer status;
	private Date statusDate;
	private String fullname;
	private String description;
	private String organization;
	private String domainSrc;
	private String domainTrg;
	private String csSrc;
	private String csTrg;
	public Long getVersionId() {
		return versionId;
	}
	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}
	public Date getReleaseDate() {
		return releaseDate;
	}
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	public Long getPreviousVersionId() {
		return previousVersionId;
	}
	public void setPreviousVersionId(Long previousVersionId) {
		this.previousVersionId = previousVersionId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getStatusDate() {
		return statusDate;
	}
	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getDomainSrc() {
		return domainSrc;
	}
	public void setDomainSrc(String domainSrc) {
		this.domainSrc = domainSrc;
	}
	public String getDomainTrg() {
		return domainTrg;
	}
	public void setDomainTrg(String domainTrg) {
		this.domainTrg = domainTrg;
	}
	public String getCsSrc() {
		return csSrc;
	}
	public void setCsSrc(String csSrc) {
		this.csSrc = csSrc;
	}
	public String getCsTrg() {
		return csTrg;
	}
	public void setCsTrg(String csTrg) {
		this.csTrg = csTrg;
	}
}
