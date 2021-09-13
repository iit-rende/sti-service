package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetCodeSystemEntityVersionByEntityId extends HibernateCommand {

	private long codeSystemEntityId = -1;

	public GetCodeSystemEntityVersionByEntityId(
			final long codeSystemEntityId) {

		this.codeSystemEntityId = codeSystemEntityId;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemEntityVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {
		
		CodeSystemEntityVersion result = (CodeSystemEntityVersion) session.createCriteria(CodeSystemEntityVersion.class).add(Restrictions.eq("codeSystemEntity.id", codeSystemEntityId)).uniqueResult();

		if (result == null) {
			return null;
		}
		return result;
	}
}
