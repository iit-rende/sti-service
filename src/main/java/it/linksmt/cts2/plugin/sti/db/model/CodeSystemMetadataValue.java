package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "code_system_metadata_value"
)
public class CodeSystemMetadataValue implements java.io.Serializable
{

  private Long id;
  private CodeSystemEntityVersion codeSystemEntityVersion;
  private MetadataParameter metadataParameter;
  private String parameterValue;

  public CodeSystemMetadataValue()
  {
  }

  public CodeSystemMetadataValue(final String parameterValue)
  {
    this.parameterValue = parameterValue;
  }

  public CodeSystemMetadataValue(final CodeSystemEntityVersion codeSystemEntityVersion, final MetadataParameter metadataParameter, final String parameterValue)
  {
    this.codeSystemEntityVersion = codeSystemEntityVersion;
    this.metadataParameter = metadataParameter;
    this.parameterValue = parameterValue;
  }

  public CodeSystemMetadataValue(final Long id, final MetadataParameter metadataParameter, final String parameterValue)
  {
    this.id = id;
    this.metadataParameter = metadataParameter;
    this.parameterValue = parameterValue;
  }

  public CodeSystemMetadataValue copyObject()
  {
    return new CodeSystemMetadataValue(id, metadataParameter, parameterValue);
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
  @JoinColumn(name = "codeSystemEntityVersionId")
  public CodeSystemEntityVersion getCodeSystemEntityVersion()
  {
    return this.codeSystemEntityVersion;
  }

  public void setCodeSystemEntityVersion(final CodeSystemEntityVersion codeSystemEntityVersion)
  {
    this.codeSystemEntityVersion = codeSystemEntityVersion;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "metadataParameterId")
  public MetadataParameter getMetadataParameter()
  {
    return this.metadataParameter;
  }

  public void setMetadataParameter(final MetadataParameter metadataParameter)
  {
    this.metadataParameter = metadataParameter;
  }

  @Column(name = "parameterValue", nullable = false, length = 65535)
  public String getParameterValue()
  {
    return this.parameterValue;
  }

  public void setParameterValue(final String parameterValue)
  {
    this.parameterValue = parameterValue;
  }

}
