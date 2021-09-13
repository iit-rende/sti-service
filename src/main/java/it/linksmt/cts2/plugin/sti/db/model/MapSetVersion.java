package it.linksmt.cts2.plugin.sti.db.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "map_set_version" )
public class MapSetVersion implements java.io.Serializable {

	private Long versionId;
	private Date releaseDate;
	private Long previousVersionId;
	private Integer status;
	private Date statusDate;
	private String fullname;
	private String description;
	private String organization;

	private Set<CodeSystemEntityVersionAssociation> associations = new HashSet<CodeSystemEntityVersionAssociation>(0);

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "versionId", unique = true, nullable = false)
	public Long getVersionId()
	{
	  return this.versionId;
	}
	public void setVersionId(final Long versionId)
	{
	  this.versionId = versionId;
	}

	@Column(name = "previousVersionID")
	public Long getPreviousVersionId()
	{
	  return this.previousVersionId;
	}

	public void setPreviousVersionId(final Long previousVersionId)
	{
	  this.previousVersionId = previousVersionId;
	}

	@Column(name = "status")
	public Integer getStatus()
	{
	  return this.status;
	}

	public void setStatus(final Integer status)
	{
	  this.status = status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "statusDate", nullable = false, length = 19)
	public Date getStatusDate()
	{
	  return this.statusDate;
	}

	public void setStatusDate(final Date statusDate)
	{
	  this.statusDate = statusDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "releaseDate", length = 19)
	public Date getReleaseDate()
	{
	  return this.releaseDate;
	}

	public void setReleaseDate(final Date releaseDate)
	{
	  this.releaseDate = releaseDate;
	}

	
	
	@Column(name = "description", length = 65535)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "organization", length = 65535)

	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	
	
	
	@Column(name = "fullname", nullable = false, length = 100)
	public String getFullname()
	{
	  return this.fullname;
	}

	public void setFullname(final String fullname)
	{
	  this.fullname = fullname;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "mapSetVersion")
	public Set<CodeSystemEntityVersionAssociation> getCodeSystemEntityVersionAssociations()
	{
	  return this.associations;
	}

	public void setCodeSystemEntityVersionAssociations(final Set<CodeSystemEntityVersionAssociation> associations)
	{
	  this.associations = associations;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MapSetVersion [versionId=");
		builder.append(versionId);
		builder.append(", releaseDate=");
		builder.append(releaseDate);
		builder.append(", previousVersionId=");
		builder.append(previousVersionId);
		builder.append(", status=");
		builder.append(status);
		builder.append(", statusDate=");
		builder.append(statusDate);
		builder.append(", fullname=");
		builder.append(fullname);
		builder.append(", description=");
		builder.append(description);
		builder.append(", organization=");
		builder.append(organization);
		builder.append(", associations=");
		builder.append(associations);
		builder.append("]");
		return builder.toString();
	}
}
