package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA


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
@Table(name="value_set"

		)
public class ValueSet  implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	private Long currentVersionId;
	private String name;
	private String description;
	private String descriptionEng;
	private Integer status;
	private Date statusDate;
	private String website;
	private Set<ValueSetVersion> valueSetVersions = new HashSet<ValueSetVersion>(0);
	private Set<MetadataParameter> metadataParameters = new HashSet<MetadataParameter>(0);

	public ValueSet() {
	}

	public ValueSet(final Long currentVersionId, final String name, final String description, final String descriptionEng, final String website, final Integer status, final Date statusDate, final Set<ValueSetVersion> valueSetVersions) {
		this.currentVersionId = currentVersionId;
		this.name = name;
		this.description = description;
		this.status = status;
		this.statusDate = statusDate;
		this.valueSetVersions = valueSetVersions;
		this.website = website;
		this.descriptionEng = descriptionEng;
	}

	public ValueSet(final String name, final String description, 
			final Integer status, final Date statusDate) {
		this.name = name;
		this.description = description;
		this.status = status;
		this.statusDate = statusDate;
	}

	@Id @GeneratedValue(strategy=IDENTITY)

	@Column(name="id", unique=true, nullable=false)
	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Column(name="currentVersionId")
	public Long getCurrentVersionId() {
		return this.currentVersionId;
	}

	public void setCurrentVersionId(final Long currentVersionId) {
		this.currentVersionId = currentVersionId;
	}

	@Column(name="name", length=50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name="description", length=65535)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
	@Column(name="descriptionEng", length=65535)
	public String getDescriptionEng() {
		return this.descriptionEng;
	}

	public void setDescriptionEng(final String descriptionEng) {
		this.descriptionEng = descriptionEng;
	}

	@Column(name = "website", length = 65535)
	public String getWebsite() {
		return website;
	}

	public void setWebsite(final String website) {
		this.website = website;
	}

	@Column(name="status")
	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(final Integer status) {
		this.status = status;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="statusDate", length=19)
	public Date getStatusDate() {
		return this.statusDate;
	}

	public void setStatusDate(final Date statusDate) {
		this.statusDate = statusDate;
	}
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="valueSet")
	public Set<ValueSetVersion> getValueSetVersions() {
		return this.valueSetVersions;
	}

	public void setValueSetVersions(final Set<ValueSetVersion> valueSetVersions) {
		this.valueSetVersions = valueSetVersions;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "valueSet")
	public Set<MetadataParameter> getMetadataParameters()
	{
		return this.metadataParameters;
	}

	public void setMetadataParameters(final Set<MetadataParameter> metadataParameters)
	{
		this.metadataParameters = metadataParameters;
	}


}


