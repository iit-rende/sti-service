package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA


import javax.persistence.Column;
import javax.persistence.Embeddable;
@Embeddable
public class CodeSystemVersionEntityMembershipId  implements java.io.Serializable {


     private Long codeSystemVersionId;
     private Long codeSystemEntityId;

    public CodeSystemVersionEntityMembershipId() {
    }

    public CodeSystemVersionEntityMembershipId(final Long codeSystemVersionId, final Long codeSystemEntityId) {
       this.codeSystemVersionId = codeSystemVersionId;
       this.codeSystemEntityId = codeSystemEntityId;
    }


    @Column(name="codeSystemVersionId", nullable=false)
    public Long getCodeSystemVersionId() {
        return this.codeSystemVersionId;
    }

    public void setCodeSystemVersionId(final Long codeSystemVersionId) {
        this.codeSystemVersionId = codeSystemVersionId;
    }

    @Column(name="codeSystemEntityId", nullable=false)
    public Long getCodeSystemEntityId() {
        return this.codeSystemEntityId;
    }

    public void setCodeSystemEntityId(final Long codeSystemEntityId) {
        this.codeSystemEntityId = codeSystemEntityId;
    }


     @Override
   public boolean equals(final Object other) {
         if ( (this == other ) ) {
			return true;
		}
		 if ( (other == null ) ) {
			return false;
		}
		 if ( !(other instanceof CodeSystemVersionEntityMembershipId) ) {
			return false;
		}
		 CodeSystemVersionEntityMembershipId castOther = ( CodeSystemVersionEntityMembershipId ) other;

		 return (this.getCodeSystemVersionId()==castOther.getCodeSystemVersionId())
 && (this.getCodeSystemEntityId()==castOther.getCodeSystemEntityId());
   }

     @Override
   public int hashCode() {
         int result = 17;

         result = 37 * result + (int)((long) this.getCodeSystemVersionId());
         result = 37 * result + (int)((long) this.getCodeSystemEntityId());
         return result;
   }


}


