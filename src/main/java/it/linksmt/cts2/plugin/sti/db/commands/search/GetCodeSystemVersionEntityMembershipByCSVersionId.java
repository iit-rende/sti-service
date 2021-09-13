package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetCodeSystemVersionEntityMembershipByCSVersionId extends HibernateCommand {

	private Long codeSystemVersionId;
	
	public GetCodeSystemVersionEntityMembershipByCSVersionId(
			Long codeSystemVersionId) {
		this.codeSystemVersionId = codeSystemVersionId;
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
	public List<CodeSystemVersionEntityMembership> execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		
		List<CodeSystemVersionEntityMembership> retVal = session.createCriteria(CodeSystemVersionEntityMembership.class)
				.createAlias("codeSystemVersion", "csv")
				.add(Restrictions.eq("csv.id", codeSystemVersionId)).list();
		
		return retVal;
	}

}
