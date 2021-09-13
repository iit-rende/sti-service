package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "code_system_concept"
)
public class CodeSystemConcept implements java.io.Serializable
{

  private Long codeSystemEntityVersionId;
  private CodeSystemEntityVersion codeSystemEntityVersion;
  private String code;
  private String term;
  private String termAbbrevation;
  private String description;
  private Boolean isPreferred;
  private String meaning;
  private String hints;
  private String languageCd;
  private Set<CodeSystemConceptTranslation> codeSystemConceptTranslations = new HashSet<CodeSystemConceptTranslation>(0);

  public CodeSystemConcept()
  {
  }

  public CodeSystemConcept(final Long codeSystemEntityVersionId, final CodeSystemEntityVersion codeSystemEntityVersion, final String code, final String term)
  {
    this.codeSystemEntityVersionId = codeSystemEntityVersionId;
    this.codeSystemEntityVersion = codeSystemEntityVersion;
    this.code = code;
    this.term = term;
  }

  public CodeSystemConcept(final Long codeSystemEntityVersionId, final CodeSystemEntityVersion codeSystemEntityVersion, final String code, final String term, final String termAbbrevation, final String description, final Boolean isPreferred, final Set<CodeSystemConceptTranslation> codeSystemConceptTranslations, final String meaning, final String hints)
  {
    this.codeSystemEntityVersionId = codeSystemEntityVersionId;
    this.codeSystemEntityVersion = codeSystemEntityVersion;
    this.code = code;
    this.term = term;
    this.termAbbrevation = termAbbrevation;
    this.description = description;
    this.isPreferred = isPreferred;
    this.codeSystemConceptTranslations = codeSystemConceptTranslations;
    this.meaning = meaning;
    this.hints = hints;
  }

  public CodeSystemConcept(final Long codeSystemEntityVersionId, final String code, final String term, final String termAbbrevation, final String description, final Boolean isPreferred, final String meaning, final String hints)
  {
    this.codeSystemEntityVersionId = codeSystemEntityVersionId;
    this.code = code;
    this.term = term;
    this.termAbbrevation = termAbbrevation;
    this.description = description;
    this.isPreferred = isPreferred;
    this.meaning = meaning;
    this.hints = hints;
  }



  public CodeSystemConcept copyObject()
  {
    return new CodeSystemConcept(codeSystemEntityVersionId, code, term, termAbbrevation, description, isPreferred, meaning, hints);
  }

  @Id

  @Column(name = "codeSystemEntityVersionId", unique = true, nullable = false)
  public Long getCodeSystemEntityVersionId()
  {
    return this.codeSystemEntityVersionId;
  }

  public void setCodeSystemEntityVersionId(final Long codeSystemEntityVersionId)
  {
    this.codeSystemEntityVersionId = codeSystemEntityVersionId;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "codeSystemEntityVersionId", unique = true, nullable = false, insertable = false, updatable = false)
  public CodeSystemEntityVersion getCodeSystemEntityVersion()
  {
    return this.codeSystemEntityVersion;
  }

  public void setCodeSystemEntityVersion(final CodeSystemEntityVersion codeSystemEntityVersion)
  {
    this.codeSystemEntityVersion = codeSystemEntityVersion;
  }

  @Column(name = "code", nullable = false, length = 100)
  public String getCode()
  {
    return this.code;
  }

  public void setCode(final String code)
  {
    this.code = code;
  }

  @Column(name = "term", nullable = false, length = 65535)
  public String getTerm()
  {
    return this.term;
  }

  public void setTerm(final String term)
  {
    this.term = term;
  }

  @Column(name = "termAbbrevation", length = 50)
  public String getTermAbbrevation()
  {
    return this.termAbbrevation;
  }

  public void setTermAbbrevation(final String termAbbrevation)
  {
    this.termAbbrevation = termAbbrevation;
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

  @Column(name = "isPreferred")
  public Boolean getIsPreferred()
  {
    return this.isPreferred;
  }

  public void setIsPreferred(final Boolean isPreferred)
  {
    this.isPreferred = isPreferred;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemConcept")
  public Set<CodeSystemConceptTranslation> getCodeSystemConceptTranslations()
  {
    return this.codeSystemConceptTranslations;
  }

  public void setCodeSystemConceptTranslations(final Set<CodeSystemConceptTranslation> codeSystemConceptTranslations)
  {
    this.codeSystemConceptTranslations = codeSystemConceptTranslations;
  }

  @Column(name = "meaning", length = 65535)
  public String getMeaning()
  {
    return meaning;
  }

  public void setMeaning(final String meaning)
  {
    this.meaning = meaning;
  }

  @Column(name = "hints", length = 65535)
  public String getHints()
  {
    return hints;
  }

  public void setHints(final String hints)
  {
    this.hints = hints;
  }
  @Column(name = "languageCd", nullable = false)
  public String getLanguageCd()
  {
    return this.languageCd;
  }

  public void setLanguageCd(final String languageCd)
  {
    this.languageCd = languageCd;
  }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CodeSystemConcept [codeSystemEntityVersionId=");
		builder.append(codeSystemEntityVersionId);
		builder.append(", codeSystemEntityVersion=");
		builder.append(codeSystemEntityVersion);
		builder.append(", code=");
		builder.append(code);
		builder.append(", term=");
		builder.append(term);
		builder.append(", termAbbrevation=");
		builder.append(termAbbrevation);
		builder.append(", description=");
		builder.append(description);
		builder.append(", isPreferred=");
		builder.append(isPreferred);
		builder.append(", meaning=");
		builder.append(meaning);
		builder.append(", hints=");
		builder.append(hints);
		builder.append(", languageCd=");
		builder.append(languageCd);
		builder.append(", codeSystemConceptTranslations=");
		builder.append(codeSystemConceptTranslations);
		builder.append("]");
		return builder.toString();
	}
  
}
