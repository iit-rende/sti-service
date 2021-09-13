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
@Table(name = "code_system")
public class CodeSystem implements java.io.Serializable
{

  private Long id;
  private Long currentVersionId;
  private String name;
  private String description;
  private String descriptionEng;
  private Date insertTimestamp;
  private String codeSystemType;
  private String website;
  private Set<CodeSystemVersion> codeSystemVersions = new HashSet<CodeSystemVersion>(0);
  // TODO: domini non gestiti per STI
  // private Set<DomainValue> domainValues = new HashSet<DomainValue>(0);
  private Set<MetadataParameter> metadataParameters = new HashSet<MetadataParameter>(0);

  public CodeSystem()
  {
  }

  public CodeSystem(final String name, final Date insertTimestamp)
  {
    this.name = name;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystem(final Long currentVersionId, final String name, final String description, final Date insertTimestamp, final String descriptionEng, final String codeSystemType, final String website, final Set<CodeSystemVersion> codeSystemVersions)
  {
  	// , final Set<DomainValue> domainValues
    this.currentVersionId = currentVersionId;
    this.name = name;
    this.description = description;
    this.descriptionEng = descriptionEng;
    this.insertTimestamp = insertTimestamp;
    this.codeSystemType = codeSystemType;
    this.website = website;
    this.codeSystemVersions = codeSystemVersions;
    // this.domainValues = domainValues;
  }

  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  public Long getId()
  {
    return this.id;
  }

  public void setId(final Long id)
  {
    this.id = id;
  }

  @Column(name = "currentVersionId")
  public Long getCurrentVersionId()
  {
    return this.currentVersionId;
  }

  public void setCurrentVersionId(final Long currentVersionId)
  {
    this.currentVersionId = currentVersionId;
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

  @Column(name = "description", length = 65535)
  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  @Column(name = "descriptionEng", length = 65535)
  public String getDescriptionEng()
  {
    return this.descriptionEng;
  }

  public void setDescriptionEng(final String descriptionEng)
  {
    this.descriptionEng = descriptionEng;
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

  @Column(name = "codeSystemType", length = 30)
  public String getCodeSystemType()
  {
    return this.codeSystemType;
  }

  public void setCodeSystemType(final String codeSystemType)
  {
    this.codeSystemType = codeSystemType;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystem")
  public Set<CodeSystemVersion> getCodeSystemVersions()
  {
    return this.codeSystemVersions;
  }

  public void setCodeSystemVersions(final Set<CodeSystemVersion> codeSystemVersions)
  {
    this.codeSystemVersions = codeSystemVersions;
  }


  @Column(name = "website", length = 65535)
  public String getWebsite() {
      return website;
  }

  public void setWebsite(final String website) {
      this.website = website;
  }

  /*
   * TODO: domini non gestiti per STI
   *
  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(name = "domain_value_has_code_system", joinColumns =
  {
    @JoinColumn(name = "code_system_id", nullable = false, updatable = false)
  }, inverseJoinColumns =
  {
    @JoinColumn(name = "domain_value_domainValueId", nullable = false, updatable = false)
  })
  public Set<DomainValue> getDomainValues()
  {
    return this.domainValues;
  }

  public void setDomainValues(final Set<DomainValue> domainValues)
  {
    this.domainValues = domainValues;
  }
  */

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystem")
  public Set<MetadataParameter> getMetadataParameters()
  {
    return this.metadataParameters;
  }

  public void setMetadataParameters(final Set<MetadataParameter> metadataParameters)
  {
    this.metadataParameters = metadataParameters;
  }

  @Override
    public String toString(){
        String s = super.toString();

        return "CS: " + name + "("+s+")";
    }
}
