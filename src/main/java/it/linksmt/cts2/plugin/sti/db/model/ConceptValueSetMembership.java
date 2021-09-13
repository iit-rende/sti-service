package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA


import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
@Entity
@Table(name="concept_value_set_membership")
public class ConceptValueSetMembership  implements java.io.Serializable {


     private ConceptValueSetMembershipId id;
     private CodeSystemEntityVersion codeSystemEntityVersion;
     private ValueSetVersion valueSetVersion;
     private String valueOverride;
     private Integer status;
     private Date statusDate;
     private Boolean isStructureEntry;
     private Long orderNr;
     private String description;
     private String meaning;
     private String hints;

    public ConceptValueSetMembership() {
    }


    public ConceptValueSetMembership(final ConceptValueSetMembershipId id, final CodeSystemEntityVersion codeSystemEntityVersion, final ValueSetVersion valueSetVersion) {
        this.id = id;
        this.codeSystemEntityVersion = codeSystemEntityVersion;
        this.valueSetVersion = valueSetVersion;
    }
    public ConceptValueSetMembership(final ConceptValueSetMembershipId id, final CodeSystemEntityVersion codeSystemEntityVersion, final ValueSetVersion valueSetVersion, final String valueOverride, final Integer status, final Date statusDate, final Boolean isStructureEntry, final Long orderNr) {
       this.id = id;
       this.codeSystemEntityVersion = codeSystemEntityVersion;
       this.valueSetVersion = valueSetVersion;
       this.valueOverride = valueOverride;
       this.status = status;
       this.statusDate = statusDate;
       this.isStructureEntry = isStructureEntry;
       this.orderNr = orderNr;
    }

     @EmbeddedId

    @AttributeOverrides( {
        @AttributeOverride(name="codeSystemEntityVersionId", column=@Column(name="codeSystemEntityVersionId", nullable=false) ),
        @AttributeOverride(name="valuesetVersionId", column=@Column(name="valuesetVersionId", nullable=false) ) } )
    public ConceptValueSetMembershipId getId() {
        return this.id;
    }

    public void setId(final ConceptValueSetMembershipId id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemEntityVersionId", nullable=false, insertable=false, updatable=false)
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return this.codeSystemEntityVersion;
    }

    public void setCodeSystemEntityVersion(final CodeSystemEntityVersion codeSystemEntityVersion) {
        this.codeSystemEntityVersion = codeSystemEntityVersion;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="valuesetVersionId", nullable=false, insertable=false, updatable=false)
    public ValueSetVersion getValueSetVersion() {
        return this.valueSetVersion;
    }

    public void setValueSetVersion(final ValueSetVersion valueSetVersion) {
        this.valueSetVersion = valueSetVersion;
    }

    @Column(name="valueOverride", length=100)
    public String getValueOverride() {
        return this.valueOverride;
    }

    public void setValueOverride(final String valueOverride) {
        this.valueOverride = valueOverride;
    }

    @Column(name="status", nullable=false)
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="statusDate", nullable=false, length=19)
    public Date getStatusDate() {
        return this.statusDate;
    }

    public void setStatusDate(final Date statusDate) {
        this.statusDate = statusDate;
    }
    @Column(name="isStructureEntry")
    public Boolean getIsStructureEntry() {
        return isStructureEntry;
    }

    public void setIsStructureEntry(final Boolean isStructureEntry) {
        this.isStructureEntry = isStructureEntry;
    }
    @Column(name="orderNr")
    public Long getOrderNr() {
        return orderNr;
    }

    public void setOrderNr(final Long orderNr) {
        this.orderNr = orderNr;
    }

    @Column(name = "description", length = 65535)
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Column(name = "meaning", length = 65535)
    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(final String meaning) {
        this.meaning = meaning;
    }

    @Column(name = "hints", length = 65535)
    public String getHints() {
        return hints;
    }

    public void setHints(final String hints) {
        this.hints = hints;
    }
}


