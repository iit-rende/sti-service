package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetCodeSystemConceptByCSEntityVersionId extends HibernateCommand {

	private Long codeSystemEntityVersionId;
	
	public GetCodeSystemConceptByCSEntityVersionId(Long codeSystemEntityVersionId) {
		this.codeSystemEntityVersionId = codeSystemEntityVersionId;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CodeSystemConcept> execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		
		List<CodeSystemConcept> retVal = (List<CodeSystemConcept>) session.createCriteria(CodeSystemConcept.class)
				.add(Restrictions.eq("codeSystemEntityVersionId", codeSystemEntityVersionId));
		
		return retVal;
		
	}

}
