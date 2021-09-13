package it.linksmt.cts2.plugin.sti.db.model;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA


import javax.persistence.Column;
import javax.persistence.Embeddable;
@Embeddable
public class ConceptValueSetMembershipId  implements java.io.Serializable {


     private Long codeSystemEntityVersionId;
     private Long valuesetVersionId;

    public ConceptValueSetMembershipId() {
    }

    public ConceptValueSetMembershipId(final Long codeSystemEntityVersionId, final Long valuesetVersionId) {
       this.codeSystemEntityVersionId = codeSystemEntityVersionId;
       this.valuesetVersionId = valuesetVersionId;
    }


    @Column(name="codeSystemEntityVersionId", nullable=false)
    public Long getCodeSystemEntityVersionId() {
        return this.codeSystemEntityVersionId;
    }

    public void setCodeSystemEntityVersionId(final Long codeSystemEntityVersionId) {
        this.codeSystemEntityVersionId = codeSystemEntityVersionId;
    }

    @Column(name="valuesetVersionId", nullable=false)
    public Long getValuesetVersionId() {
        return this.valuesetVersionId;
    }

    public void setValuesetVersionId(final Long valuesetVersionId) {
        this.valuesetVersionId = valuesetVersionId;
    }


     @Override
   public boolean equals(final Object other) {
         if ( (this == other ) ) {
			return true;
		}
		 if ( (other == null ) ) {
			return false;
		}
		 if ( !(other instanceof ConceptValueSetMembershipId) ) {
			return false;
		}
		 ConceptValueSetMembershipId castOther = ( ConceptValueSetMembershipId ) other;

		 return (this.getCodeSystemEntityVersionId()==castOther.getCodeSystemEntityVersionId())
 && (this.getValuesetVersionId()==castOther.getValuesetVersionId());
   }

     @Override
   public int hashCode() {
         int result = 17;

         result = 37 * result + (int) ((long)this.getCodeSystemEntityVersionId());
         result = 37 * result + (int) ((long)this.getValuesetVersionId());
         return result;
   }


}


