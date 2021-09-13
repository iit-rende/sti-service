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
@Table(name = "value_set_metadata_value")
public class ValueSetMetadataValue implements java.io.Serializable
{

  private Long id;
     private CodeSystemEntityVersion codeSystemEntityVersion;
     private MetadataParameter metadataParameter;
     private String parameterValue;
     private Long valuesetVersionId;

    public ValueSetMetadataValue() {
    }


    public ValueSetMetadataValue(final String parameterValue) {
        this.parameterValue = parameterValue;
    }
    public ValueSetMetadataValue(final CodeSystemEntityVersion codeSystemEntityVersion, final MetadataParameter metadataParameter, final String parameterValue, final Long valuesetVersionId) {
       this.codeSystemEntityVersion = codeSystemEntityVersion;
       this.metadataParameter = metadataParameter;
       this.parameterValue = parameterValue;
       this.valuesetVersionId = valuesetVersionId;
    }

     @Id @GeneratedValue(strategy=IDENTITY)

    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemEntityVersionId")
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return this.codeSystemEntityVersion;
    }

    public void setCodeSystemEntityVersion(final CodeSystemEntityVersion codeSystemEntityVersion) {
        this.codeSystemEntityVersion = codeSystemEntityVersion;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="metadataParameterId")
    public MetadataParameter getMetadataParameter() {
        return this.metadataParameter;
    }

    public void setMetadataParameter(final MetadataParameter metadataParameter) {
        this.metadataParameter = metadataParameter;
    }

    @Column(name="parameterValue", nullable=false, length=65535)
    public String getParameterValue() {
        return this.parameterValue;
    }

    public void setParameterValue(final String parameterValue) {
        this.parameterValue = parameterValue;
    }

    @Column(name="valuesetVersionId")
    public Long getValuesetVersionId() {
        return this.valuesetVersionId;
    }

    public void setValuesetVersionId(final Long valuesetVersionId) {
        this.valuesetVersionId = valuesetVersionId;
    }
}
