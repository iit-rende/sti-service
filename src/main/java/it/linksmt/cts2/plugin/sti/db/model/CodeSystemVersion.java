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
@Table(name = "code_system_version"
)
public class CodeSystemVersion implements java.io.Serializable
{
  private Long versionId;
  private CodeSystem codeSystem;
  private Long previousVersionId;
  private String name;
  private Integer status;
  private Date statusDate;
  private Date releaseDate;
  private Date expirationDate;
  private String source;
  private String description;
  private String preferredLanguageCd;
  private String oid;
  private String licenceHolder;
  private String availableLanguages;
  private Boolean underLicence;
  private Date insertTimestamp;
  private Long validityRange;
  private Date lastChangeDate;
  private Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships = new HashSet<CodeSystemVersionEntityMembership>(0);

  // TODO: Utenti e License gestiti in LR
  // private Set<LicencedUser> licencedUsers = new HashSet<LicencedUser>(0);
  // private Set<LicenceType> licenceTypes = new HashSet<LicenceType>(0);

  public CodeSystemVersion()
  {
  }

  public CodeSystemVersion(final CodeSystem codeSystem, final String name, final Date statusDate, final Date insertTimestamp)
  {
    this.codeSystem = codeSystem;
    this.name = name;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystemVersion(final CodeSystem codeSystem, final Long previousVersionId, final String name, final Integer status, final Date statusDate, final Date releaseDate, final Date expirationDate, final String source, final String description, final String preferredLanguageCd, final String oid, final String licenceHolder, final Boolean underLicence, final Date insertTimestamp, final Long validityRange, final Date lastChangeDate, final Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMembership)
  {
	// TODO: Utenti e License gestiti in LR
	//  final Set<LicencedUser> licencedUsers, final Set<LicenceType> licenceTypes
    this.codeSystem = codeSystem;
    this.previousVersionId = previousVersionId;
    this.name = name;
    this.status = status;
    this.statusDate = statusDate;
    this.releaseDate = releaseDate;
    this.expirationDate = expirationDate;
    this.source = source;
    this.description = description;
    this.preferredLanguageCd = preferredLanguageCd;
    this.oid = oid;
    this.licenceHolder = licenceHolder;
    this.underLicence = underLicence;
    this.insertTimestamp = insertTimestamp;
    this.codeSystemVersionEntityMemberships = codeSystemVersionEntityMemberships;
    // this.licencedUsers = licencedUsers;
    // this.licenceTypes = licenceTypes;
    this.validityRange = validityRange;
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
  @JoinColumn(name = "codeSystemId", nullable = false)
  public CodeSystem getCodeSystem()
  {
    return this.codeSystem;
  }

  public void setCodeSystem(final CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
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
  @Column(name = "releaseDate", length = 19)
  public Date getReleaseDate()
  {
    return this.releaseDate;
  }

  public void setReleaseDate(final Date releaseDate)
  {
    this.releaseDate = releaseDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "expirationDate", length = 19)
  public Date getExpirationDate()
  {
    return this.expirationDate;
  }

  public void setExpirationDate(final Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }

  @Column(name = "source", length = 65535)
  public String getSource()
  {
    return this.source;
  }

  public void setSource(final String source)
  {
    this.source = source;
  }

  @Column(name = "description", length = 65535)
  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
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

  @Column(name = "licenceHolder", length = 65535)
  public String getLicenceHolder()
  {
    return this.licenceHolder;
  }

  public void setLicenceHolder(final String licenceHolder)
  {
    this.licenceHolder = licenceHolder;
  }

  @Column(name = "underLicence")
  public Boolean getUnderLicence()
  {
    return this.underLicence;
  }

  public void setUnderLicence(final Boolean underLicence)
  {
    this.underLicence = underLicence;
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

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemVersion")
  public Set<CodeSystemVersionEntityMembership> getCodeSystemVersionEntityMemberships()
  {
    return this.codeSystemVersionEntityMemberships;
  }

  public void setCodeSystemVersionEntityMemberships(final Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships)
  {
    this.codeSystemVersionEntityMemberships = codeSystemVersionEntityMemberships;
  }

  /*
   * TODO: Utenti e License gestiti in LR
   *
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemVersion")
  public Set<LicencedUser> getLicencedUsers()
  {
    return this.licencedUsers;
  }

  public void setLicencedUsers(final Set<LicencedUser> licencedUsers)
  {
    this.licencedUsers = licencedUsers;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemVersion")
  public Set<LicenceType> getLicenceTypes()
  {
    return this.licenceTypes;
  }

  public void setLicenceTypes(final Set<LicenceType> licenceTypes)
  {
    this.licenceTypes = licenceTypes;
  }
  */

  @Column(name = "validityRange")
  public Long getValidityRange()
  {
    return this.validityRange;
  }

  public void setValidityRange(final Long validityRange)
  {
    this.validityRange = validityRange;
  }

  /**
   * @return the availableLanguages
   */
  @Column(name = "availableLanguages")
  public String getAvailableLanguages()
  {
    return availableLanguages;
  }

  /**
   * @param availableLanguages the availableLanguages to set
   */
  public void setAvailableLanguages(final String availableLanguages)
  {
    this.availableLanguages = availableLanguages;
  }
}
