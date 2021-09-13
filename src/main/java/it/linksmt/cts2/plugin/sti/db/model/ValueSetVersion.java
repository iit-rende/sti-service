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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "value_set_version"
)
public class ValueSetVersion implements java.io.Serializable
{

  private Long versionId;
  private ValueSet valueSet;
  private Integer status;
  private Date statusDate;
  private Date insertTimestamp;
  private Date releaseDate;
  private Long previousVersionId;
  private String preferredLanguageCd;
  private String oid;
  private String name;
  private Long validityRange;
  private Long virtualCodeSystemVersionId;
  private Date lastChangeDate;
  private Set<ConceptValueSetMembership> conceptValueSetMemberships = new HashSet<ConceptValueSetMembership>(0);

  public ValueSetVersion()
  {
  }

  public ValueSetVersion(final ValueSet valueSet, final Date statusDate, final Date insertTimestamp)
  {
    this.valueSet = valueSet;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public ValueSetVersion(final ValueSet valueSet, final Integer status, final Date statusDate, final Date insertTimestamp, final Date releaseDate, final String name, final Long previousVersionId, final String preferredLanguageCd, final String oid, final Long validityRange, final Date lastChangeDate, final Set<ConceptValueSetMembership> conceptValueSetMemberships)
  {
    this.valueSet = valueSet;
    this.status = status;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
    this.releaseDate = releaseDate;
    this.previousVersionId = previousVersionId;
    this.preferredLanguageCd = preferredLanguageCd;
    this.oid = oid;
    this.validityRange = validityRange;
    this.lastChangeDate = lastChangeDate;
    this.conceptValueSetMemberships = conceptValueSetMemberships;
    this.name = name;
  }
  
  
  public ValueSetVersion(ValueSet valueSet, Integer status, Date statusDate, Date insertTimestamp, Date releaseDate,
		  Long previousVersionId, String oid, String name, Long virtualCodeSystemVersionId, Date lastChangeDate  ) {
	  this.valueSet = valueSet;
	  this.status = status;
	  this.statusDate = statusDate;
	  this.insertTimestamp = insertTimestamp;
	  this.releaseDate = releaseDate;
	  this.previousVersionId = previousVersionId;
	  this.oid = oid;
	  this.name = name;
	  this.virtualCodeSystemVersionId = virtualCodeSystemVersionId;
	  this.lastChangeDate = lastChangeDate;
  }

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "valueSetId", nullable = false)
  public ValueSet getValueSet()
  {
    return this.valueSet;
  }

  public void setValueSet(final ValueSet valueSet)
  {
    this.valueSet = valueSet;
  }

  @Column(name = "name", nullable = false, length = 100)
  public String getName()
  {
    return this.name;
  }

  public void setName(final String name)
  {
    this.name = name;
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
  @Column(name = "lastChangeDate", length = 19)
  public Date getLastChangeDate()
  {
    return this.lastChangeDate;
  }

  public void setLastChangeDate(final Date lastChangeDate)
  {
    this.lastChangeDate = lastChangeDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "insertTimestamp", nullable = false, length = 19)
  public Date getInsertTimestamp()
  {
    return this.insertTimestamp;
  }

  public void setInsertTimestamp(final Date insertTimestamp)
  {
    this.insertTimestamp = insertTimestamp;
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

  @Column(name = "previousVersionId")
  public Long getPreviousVersionId()
  {
    return this.previousVersionId;
  }

  public void setPreviousVersionId(final Long previousVersionId)
  {
    this.previousVersionId = previousVersionId;
  }

  @Column(name = "preferredLanguageCd")
  public String getPreferredLanguageCd()
  {
    return this.preferredLanguageCd;
  }

  public void setPreferredLanguageCd(final String preferredLanguageCd)
  {
    this.preferredLanguageCd = preferredLanguageCd;
  }

  @Column(name = "oid", length = 100)
  public String getOid()
  {
    return this.oid;
  }

  public void setOid(final String oid)
  {
    this.oid = oid;
  }

  @Column(name = "validityRange")
  public Long getValidityRange()
  {
    return this.validityRange;
  }

  public void setValidityRange(final Long validityRange)
  {
    this.validityRange = validityRange;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "valueSetVersion")
  public Set<ConceptValueSetMembership> getConceptValueSetMemberships()
  {
    return this.conceptValueSetMemberships;
  }

  public void setConceptValueSetMemberships(final Set<ConceptValueSetMembership> conceptValueSetMemberships)
  {
    this.conceptValueSetMemberships = conceptValueSetMemberships;
  }


  @Column(name = "virtualCodeSystemVersionId")
  public Long getVirtualCodeSystemVersionId()
  {
    return this.virtualCodeSystemVersionId;
  }

  public void setVirtualCodeSystemVersionId(final Long virtualCodeSystemVersionId)
  {
    this.virtualCodeSystemVersionId = virtualCodeSystemVersionId;
  }

}
