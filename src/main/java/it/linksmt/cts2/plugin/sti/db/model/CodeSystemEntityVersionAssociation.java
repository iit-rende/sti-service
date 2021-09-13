package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "code_system_entity_version_association"
)
public class CodeSystemEntityVersionAssociation implements java.io.Serializable
{

  private Long id;
  private CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2;
  private CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1;
  private Long leftId;
  private String forwardName;
  private String reverseName;
  private Integer associationKind;
  private Integer status;
  private Date statusDate;
  private Date insertTimestamp;
  private MapSetVersion mapSetVersion;

  public CodeSystemEntityVersionAssociation()
  {
  }

  public CodeSystemEntityVersionAssociation(final CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2, final CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1, final Date statusDate, final Date insertTimestamp)
  {
    this.codeSystemEntityVersionByCodeSystemEntityVersionId2 = codeSystemEntityVersionByCodeSystemEntityVersionId2;
    this.codeSystemEntityVersionByCodeSystemEntityVersionId1 = codeSystemEntityVersionByCodeSystemEntityVersionId1;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystemEntityVersionAssociation(final CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2, final CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1, final Long leftId, final Integer associationKind, final Integer status, final Date statusDate, final Date insertTimestamp)
  {
    this.codeSystemEntityVersionByCodeSystemEntityVersionId2 = codeSystemEntityVersionByCodeSystemEntityVersionId2;
    this.codeSystemEntityVersionByCodeSystemEntityVersionId1 = codeSystemEntityVersionByCodeSystemEntityVersionId1;
    this.leftId = leftId;
    this.associationKind = associationKind;
    this.status = status;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystemEntityVersionAssociation(final Long id, final Long leftId, final Integer associationKind, final Integer status, final Date statusDate, final Date insertTimestamp)
  {
    this.id = id;
    this.leftId = leftId;
    this.associationKind = associationKind;
    this.status = status;
    this.statusDate = statusDate;
    this.insertTimestamp = insertTimestamp;
  }

  public CodeSystemEntityVersionAssociation copyObject()
  {
    return new CodeSystemEntityVersionAssociation(id, leftId, associationKind, status, statusDate, insertTimestamp);
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "codeSystemEntityVersionId2", nullable = false)
  public CodeSystemEntityVersion getCodeSystemEntityVersionByCodeSystemEntityVersionId2()
  {
    return this.codeSystemEntityVersionByCodeSystemEntityVersionId2;
  }

  public void setCodeSystemEntityVersionByCodeSystemEntityVersionId2(final CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2)
  {
    this.codeSystemEntityVersionByCodeSystemEntityVersionId2 = codeSystemEntityVersionByCodeSystemEntityVersionId2;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "codeSystemEntityVersionId1", nullable = false)
  public CodeSystemEntityVersion getCodeSystemEntityVersionByCodeSystemEntityVersionId1()
  {
    return this.codeSystemEntityVersionByCodeSystemEntityVersionId1;
  }

  public void setCodeSystemEntityVersionByCodeSystemEntityVersionId1(final CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1)
  {
    this.codeSystemEntityVersionByCodeSystemEntityVersionId1 = codeSystemEntityVersionByCodeSystemEntityVersionId1;
  }

  @Column(name = "leftId")
  public Long getLeftId()
  {
    return this.leftId;
  }

  public void setLeftId(final Long leftId)
  {
    this.leftId = leftId;
  }

  @Column(name = "forwardName", nullable = false, length = 50)
  public String getForwardName()
  {
    return this.forwardName;
  }

  public void setForwardName(final String forwardName)
  {
    this.forwardName = forwardName;
  }

  @Column(name = "reverseName", nullable = false, length = 50)
  public String getReverseName()
  {
    return this.reverseName;
  }

  public void setReverseName(final String reverseName)
  {
    this.reverseName = reverseName;
  }

  @Column(name = "associationKind")
  public Integer getAssociationKind()
  {
    return this.associationKind;
  }

  public void setAssociationKind(final Integer associationKind)
  {
    this.associationKind = associationKind;
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
  @Column(name = "insertTimestamp", nullable = false, length = 19)
  public Date getInsertTimestamp()
  {
    return this.insertTimestamp;
  }

  public void setInsertTimestamp(final Date insertTimestamp)
  {
    this.insertTimestamp = insertTimestamp;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mapsetversionid", nullable = false)
  public MapSetVersion getMapSetVersion()
  {
    return this.mapSetVersion;
  }

  public void setMapSetVersion(final MapSetVersion mapSetVersion)
  {
    this.mapSetVersion = mapSetVersion;
  }
}
