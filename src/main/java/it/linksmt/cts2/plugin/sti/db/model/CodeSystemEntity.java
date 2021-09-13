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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "code_system_entity"
)
public class CodeSystemEntity implements java.io.Serializable
{

  private Long id;
  private Long currentVersionId;
  private Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships = new HashSet<CodeSystemVersionEntityMembership>(0);
  private Set<CodeSystemEntityVersion> codeSystemEntityVersions = new HashSet<CodeSystemEntityVersion>(0);

  public CodeSystemEntity()
  {
  }

  public CodeSystemEntity(final Long currentVersionId, final Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships, final Set<CodeSystemEntityVersion> codeSystemEntityVersions)
  {
    this.currentVersionId = currentVersionId;
    this.codeSystemVersionEntityMemberships = codeSystemVersionEntityMemberships;
    this.codeSystemEntityVersions = codeSystemEntityVersions;
  }

  public CodeSystemEntity(final Long id, final Long currentVersionId)
  {
    this.id = id;
    this.currentVersionId = currentVersionId;
  }



  public CodeSystemEntity cloneObject()
  {
    return new CodeSystemEntity(id, currentVersionId);
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

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntity")
  public Set<CodeSystemVersionEntityMembership> getCodeSystemVersionEntityMemberships()
  {
    return this.codeSystemVersionEntityMemberships;
  }

  public void setCodeSystemVersionEntityMemberships(final Set<CodeSystemVersionEntityMembership> codeSystemVersionEntityMemberships)
  {
    this.codeSystemVersionEntityMemberships = codeSystemVersionEntityMemberships;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "codeSystemEntity")
  public Set<CodeSystemEntityVersion> getCodeSystemEntityVersions()
  {
    return this.codeSystemEntityVersions;
  }

  public void setCodeSystemEntityVersions(final Set<CodeSystemEntityVersion> codeSystemEntityVersions)
  {
    this.codeSystemEntityVersions = codeSystemEntityVersions;
  }

}
