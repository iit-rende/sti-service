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
@Table(name = "code_system_entity_version"
)
public class CodeSystemEntityVersion implements java.io.Serializable
{

  private Long versionId;
  private CodeSystemEntity codeSystemEntity;
  private Long previousVersionId;
  private Integer statusVisibility;
  private Date statusVisibilityDate;
  private Integer statusDeactivated;
  private Date statusDeactivatedDate;
  private Integer statusWorkflow;
  private Date statusWorkflowDate;
  private Date effectiveDate;
  private Integer majorRevision;
  private Integer minorRevision;
  private Date insertTimestamp;
  private Boolean isLeaf;
  private Set<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 = new HashSet<CodeSystemEntityVersionAssociation>(0);
  private Set<CodeSystemConcept> codeSystemConcepts = new HashSet<CodeSystemConcept>(0);
  private Set<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 = new HashSet<CodeSystemEntityVersionAssociation>(0);
  private Set<ConceptValueSetMembership> conceptValueSetMemberships = new HashSet<ConceptValueSetMembership>(0);
  private Set<CodeSystemMetadataValue> codeSystemMetadataValues = new HashSet<CodeSystemMetadataValue>(0);
  private Set<ValueSetMetadataValue> valueSetMetadataValues = new HashSet<ValueSetMetadataValue>(0);

  public CodeSystemEntityVersion()
  {
  }

  public CodeSystemEntityVersion cloneObject()
  {
    CodeSystemEntityVersion csev = new CodeSystemEntityVersion(versionId, previousVersionId, statusVisibility, statusVisibilityDate, statusDeactivated, statusDeactivatedDate, statusWorkflow, statusWorkflowDate, effectiveDate, majorRevision, minorRevision, insertTimestamp, isLeaf);
    if(csev.getStatusDeactivatedDate() == null) {
		csev.setStatusDeactivatedDate(new Date());
	}
    if(csev.getStatusWorkflowDate()== null) {
		csev.setStatusWorkflowDate(new Date());
	}

    return csev;
  }

  public CodeSystemEntityVersion(final Long versionId, final Long previousVersionId, final Integer statusVisibility, final Date statusVisibilityDate, final Integer statusDeactivated, final Date statusDeactivatedDate, final Integer statusWorkflow, final Date statusWorkflowDate, final Date effectiveDate, final Integer majorRevision, final Integer minorRevision, final Date insertTimestamp, final Boolean isLeaf)
  {
    this.versionId = versionId;
    this.previousVersionId = previousVersionId;
    this.statusVisibility = statusVisibility;
    this.statusVisibilityDate = statusVisibilityDate;
    this.statusDeactivated = statusDeactivated;
    this.statusDeactivatedDate = statusDeactivatedDate;
    this.statusWorkflow = statusWorkflow;
    this.statusWorkflowDate = statusWorkflowDate;
    this.effectiveDate = effectiveDate;
    this.majorRevision = majorRevision;
    this.minorRevision = minorRevision;
    this.insertTimestamp = insertTimestamp;
    this.isLeaf = isLeaf;
  }



  public CodeSystemEntityVersion(final CodeSystemEntity codeSystemEntity, final Integer status, final Date statusDate, final Date insertTimestamp)
  {
    this.codeSystemEntity = codeSystemEntity;
    this.statusVisibility = status;
    this.statusVisibilityDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystemEntityVersion(final CodeSystemEntity codeSystemEntity, final Long previousVersionId, final Integer status, final Date statusDate, final Date effectiveDate, final Integer majorRevision, final Integer minorRevision, final Date insertTimestamp, final Set<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1, final Set<CodeSystemConcept> codeSystemConcepts, final Set<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2, final Set<ConceptValueSetMembership> conceptValueSetMemberships, final Set<CodeSystemMetadataValue> codeSystemMetadataValues)
  {
    this.codeSystemEntity = codeSystemEntity;
    this.previousVersionId = previousVersionId;
    this.statusVisibility = status;
    this.statusVisibilityDate = statusDate;
    this.effectiveDate = effectiveDate;
    this.majorRevision = majorRevision;
    this.minorRevision = minorRevision;
    this.insertTimestamp = insertTimestamp;
    this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 = codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1;
    this.codeSystemConcepts = codeSystemConcepts;
    this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 = codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2;
    this.conceptValueSetMemberships = conceptValueSetMemberships;
    this.codeSystemMetadataValues = codeSystemMetadataValues;
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
  @JoinColumn(name = "codeSystemEntityId", nullable = false)
  public CodeSystemEntity getCodeSystemEntity()
  {
    return this.codeSystemEntity;
  }

  public void setCodeSystemEntity(final CodeSystemEntity codeSystemEntity)
  {
    this.codeSystemEntity = codeSystemEntity;
  }

  @Column(name = "isLeaf")
  public Boolean getIsLeaf()
  {
    return this.isLeaf;
  }

  public void setIsLeaf(final Boolean isLeaf)
  {
    this.isLeaf = isLeaf;
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

  @Column(name = "statusVisibility", nullable = false)
  public Integer getStatusVisibility()
  {
    return this.statusVisibility;
  }

  public void setStatusVisibility(final Integer status)
  {
    this.statusVisibility = status;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "statusVisibilityDate", nullable = false, length = 19)
  public Date getStatusVisibilityDate()
  {
    return this.statusVisibilityDate;
  }

  public void setStatusVisibilityDate(final Date statusDate)
  {
    this.statusVisibilityDate = statusDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "effectiveDate", length = 19)
  public Date getEffectiveDate()
  {
    return this.effectiveDate;
  }

  public void setEffectiveDate(final Date effectiveDate)
  {
    this.effectiveDate = effectiveDate;
  }

  @Column(name = "majorRevision")
  public Integer getMajorRevision()
  {
    return this.majorRevision;
  }

  public void setMajorRevision(final Integer majorRevision)
  {
    this.majorRevision = majorRevision;
  }

  @Column(name = "minorRevision")
  public Integer getMinorRevision()
  {
    return this.minorRevision;
  }

  public void setMinorRevision(final Integer minorRevision)
  {
    this.minorRevision = minorRevision;
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

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntityVersionByCodeSystemEntityVersionId1")
  public Set<CodeSystemEntityVersionAssociation> getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1()
  {
    return this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1;
  }

  public void setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1(final Set<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1)
  {
    this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 = codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntityVersion")
  public Set<CodeSystemConcept> getCodeSystemConcepts()
  {
    return this.codeSystemConcepts;
  }

  public void setCodeSystemConcepts(final Set<CodeSystemConcept> codeSystemConcepts)
  {
    this.codeSystemConcepts = codeSystemConcepts;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntityVersionByCodeSystemEntityVersionId2")
  public Set<CodeSystemEntityVersionAssociation> getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2()
  {
    return this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2;
  }

  public void setCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2(final Set<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2)
  {
    this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 = codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntityVersion")
  public Set<ConceptValueSetMembership> getConceptValueSetMemberships()
  {
    return this.conceptValueSetMemberships;
  }

  public void setConceptValueSetMemberships(final Set<ConceptValueSetMembership> conceptValueSetMemberships)
  {
    this.conceptValueSetMemberships = conceptValueSetMemberships;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntityVersion")
  public Set<CodeSystemMetadataValue> getCodeSystemMetadataValues()
  {
    return this.codeSystemMetadataValues;
  }

  public void setCodeSystemMetadataValues(final Set<CodeSystemMetadataValue> codeSystemMetadataValues)
  {
    this.codeSystemMetadataValues = codeSystemMetadataValues;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntityVersion")
  public Set<ValueSetMetadataValue> getValueSetMetadataValues()
  {
    return this.valueSetMetadataValues;
  }

  public void setValueSetMetadataValues(final Set<ValueSetMetadataValue> valueSetMetadataValues)
  {
    this.valueSetMetadataValues = valueSetMetadataValues;
  }


  @Column(name = "statusDeactivated", nullable = false)
  public Integer getStatusDeactivated()
  {
    return this.statusDeactivated;
  }

  public void setStatusDeactivated(final Integer status)
  {
    this.statusDeactivated = status;
  }

  @Column(name = "statusWorkflow", nullable = false)
  public Integer getStatusWorkflow()
  {
    return this.statusWorkflow;
  }

  public void setStatusWorkflow(final Integer status)
  {
    this.statusWorkflow = status;
  }


  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "statusDeactivatedDate", nullable = false, length = 19)
  public Date getStatusDeactivatedDate()
  {
    return this.statusDeactivatedDate;
  }

  public void setStatusDeactivatedDate(final Date statusDate)
  {
    this.statusDeactivatedDate = statusDate;
  }

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "statusWorkflowDate", nullable = false, length = 19)
  public Date getStatusWorkflowDate()
  {
    return this.statusWorkflowDate;
  }

  public void setStatusWorkflowDate(final Date statusDate)
  {
    this.statusWorkflowDate = statusDate;
  }
}
