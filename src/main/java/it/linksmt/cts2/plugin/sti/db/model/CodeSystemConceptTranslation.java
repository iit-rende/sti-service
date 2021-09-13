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
@Table(name = "code_system_concept_translation"
)
public class CodeSystemConceptTranslation implements java.io.Serializable
{

  private Long id;
  private CodeSystemConcept codeSystemConcept;
  private String term;
  private String termAbbrevation;
  private String languageCd;
  private String description;
  private String meaning;
  private String hints;

  public CodeSystemConceptTranslation()
  {
  }

  public CodeSystemConceptTranslation(final CodeSystemConcept codeSystemConcept, final String term, final String languageCd)
  {
    this.codeSystemConcept = codeSystemConcept;
    this.term = term;
    this.languageCd = languageCd;
  }

  public CodeSystemConceptTranslation(final CodeSystemConcept codeSystemConcept, final String term, final String termAbbrevation, final String languageCd, final String description)
  {
    this.codeSystemConcept = codeSystemConcept;
    this.term = term;
    this.termAbbrevation = termAbbrevation;
    this.languageCd = languageCd;
    this.description = description;
  }

  public CodeSystemConceptTranslation(final Long id, final String term, final String termAbbrevation, final String languageCd, final String description, final String meaning, final String hints)
  {
    this.id = id;
    this.term = term;
    this.termAbbrevation = termAbbrevation;
    this.languageCd = languageCd;
    this.description = description;
    this.meaning = meaning;
    this.hints = hints;
  }


  public CodeSystemConceptTranslation copyObject()
  {
    return new CodeSystemConceptTranslation(id, term, termAbbrevation, languageCd, description, meaning, hints);
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
  @JoinColumn(name = "codeSystemEntityVersionId", nullable = false)
  public CodeSystemConcept getCodeSystemConcept()
  {
    return this.codeSystemConcept;
  }

  public void setCodeSystemConcept(final CodeSystemConcept codeSystemConcept)
  {
    this.codeSystemConcept = codeSystemConcept;
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

  @Column(name = "languageCd", nullable = false)
  public String getLanguageCd()
  {
    return this.languageCd;
  }

  public void setLanguageCd(final String languageCd)
  {
    this.languageCd = languageCd;
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

}
