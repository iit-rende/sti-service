package it.linksmt.cts2.plugin.sti.db.commands.updates;

import org.hibernate.Session;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionChangelog;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class UpdateChangelog extends HibernateCommand {

	private CodeSystemVersionChangelog changelog;
	
	public UpdateChangelog(CodeSystemVersionChangelog changelog) {
		this.changelog = changelog;
	}
	
	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemVersionChangelog execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		session.update(changelog);
		session.flush();
		session.refresh(changelog);
		return changelog;
	}

}
