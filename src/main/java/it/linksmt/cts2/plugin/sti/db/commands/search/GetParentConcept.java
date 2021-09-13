package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class GetParentConcept extends HibernateCommand {

	private CodeSystemConcept csConcept;

	public GetParentConcept(final CodeSystemConcept csConcept) {
		this.csConcept = csConcept;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemConcept execute(final Session session) throws StiAuthorizationException, StiHibernateException {


		CodeSystemEntityVersionAssociation parAssoc = (CodeSystemEntityVersionAssociation)
				session.createCriteria(CodeSystemEntityVersionAssociation.class)
				.add(Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId1.id",
						csConcept.getCodeSystemEntityVersion().getVersionId().longValue()))
				.add(Restrictions.eq("associationKind", StiConstants.ASSOCIATION_KIND.TAXONOMY.getCode()))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode()))
				.uniqueResult();

		if (parAssoc == null) {
			return null;
		}

		Set<CodeSystemConcept> setConc = parAssoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId2()
				.getCodeSystemConcepts();

		if ((setConc == null) || (setConc.size() != 1)) {
			throw new StiHibernateException("Il sistema attualmente supporta "
					+ "una singola associazione tra la versione del CS e la Entity.");
		}

		return setConc.iterator().next();
	}
}
