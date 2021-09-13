package it.linksmt.cts2.plugin.sti.db.commands.search;

import org.hibernate.Session;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetCodeSystemVersionById extends HibernateCommand {

	private long versionId = -1;

	public GetCodeSystemVersionById(final long versionId) {
		this.versionId = versionId;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {
		return (CodeSystemVersion)session.get(CodeSystemVersion.class, versionId);
	}

}
