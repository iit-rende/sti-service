package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

import static javax.persistence.GenerationType.IDENTITY;

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

@Entity
@Table(name = "metadata_parameter")
public class MetadataParameter implements java.io.Serializable
{

  private Long id;
  private CodeSystem codeSystem;
  private ValueSet valueSet;
  private String paramName;
  private String paramDatatype;
  private String metadataParameterType;
  private String languageCd;
  private String description;
  private String paramNameDisplay;
  private Integer maxLength;
  private Integer position;

  private Set<CodeSystemMetadataValue> codeSystemMetadataValues = new HashSet<CodeSystemMetadataValue>(0);
  private Set<ValueSetMetadataValue> valueSetMetadataValues = new HashSet<ValueSetMetadataValue>(0);

  public MetadataParameter()
  {
  }

  public MetadataParameter(final String paramName)
  {
    this.paramName = paramName;
  }

  public MetadataParameter(final String paramName, final String paramDatatype, final String metadataParameterType, final Set<CodeSystemMetadataValue> codeSystemMetadataValues, final Set<ValueSetMetadataValue> valueSetMetadataValues)
  {
    this.paramName = paramName;
    this.paramDatatype = paramDatatype;
    this.metadataParameterType = metadataParameterType;
    this.codeSystemMetadataValues = codeSystemMetadataValues;
    this.valueSetMetadataValues = valueSetMetadataValues;
  }
  

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "codeSystemId")
  public CodeSystem getCodeSystem()
  {
    return this.codeSystem;
  }

  public void setCodeSystem(final CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "valueSetId")
  public ValueSet getValueSet()
  {
    return this.valueSet;
  }

  public void setValueSet(final ValueSet valueSet)
  {
    this.valueSet = valueSet;
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

  @Column(name = "paramName", nullable = false, length = 65535)
  public String getParamName()
  {
    return this.paramName;
  }

  public void setParamName(final String paramName)
  {
    this.paramName = paramName;
  }

  @Column(name = "paramDatatype", length = 65535)
  public String getParamDatatype()
  {
    return this.paramDatatype;
  }

  public void setParamDatatype(final String paramDatatype)
  {
    this.paramDatatype = paramDatatype;
  }

  @Column(name = "metadataParameterType", length = 30)
  public String getMetadataParameterType()
  {
    return this.metadataParameterType;
  }

  public void setMetadataParameterType(final String metadataParameterType)
  {
    this.metadataParameterType = metadataParameterType;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "metadataParameter")
  public Set<CodeSystemMetadataValue> getCodeSystemMetadataValues()
  {
    return this.codeSystemMetadataValues;
  }

  public void setCodeSystemMetadataValues(final Set<CodeSystemMetadataValue> codeSystemMetadataValues)
  {
    this.codeSystemMetadataValues = codeSystemMetadataValues;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  public Set<ValueSetMetadataValue> getValueSetMetadataValues()
  {
    return this.valueSetMetadataValues;
  }

  public void setValueSetMetadataValues(final Set<ValueSetMetadataValue> valueSetMetadataValues)
  {
    this.valueSetMetadataValues = valueSetMetadataValues;
  }

  @Column(name = "languageCd", nullable = true)
  public String getLanguageCd()
  {
    return this.languageCd;
  }

  public void setLanguageCd(final String languageCd)
  {
    this.languageCd = languageCd;
  }


  @Column(name = "maxLength", nullable = true)
  public Integer getMaxLength()
  {
    return this.maxLength;
  }

  public void setMaxLength(final Integer maxLength)
  {
    this.maxLength = maxLength;
  }

  @Column(name = "paramNameDisplay", nullable = true)
  public String getParamNameDisplay()
  {
    return this.paramNameDisplay;
  }

  public void setParamNameDisplay(final String paramNameDisplay)
  {
    this.paramNameDisplay = paramNameDisplay;
  }

  @Column(name = "description", nullable = true)
  public String getDescription()
  {
    return this.description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }
  
  
  @Column(name = "position", nullable = true)
  public Integer getPosition()
  {
    return this.position;
  }

  public void setPosition(final Integer position)
  {
    this.position = position;
  }
  


}
