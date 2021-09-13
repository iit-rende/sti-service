package it.linksmt.cts2.plugin.sti.db.commands.updates;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Session;

public class UpdateValueSetVersion extends HibernateCommand {

	private ValueSetVersion vsVersion;
	
	public UpdateValueSetVersion(ValueSetVersion vsVersion) {
		this.vsVersion = vsVersion;
	}
	
	
	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public ValueSetVersion execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		session.update(vsVersion);
		session.flush();
		session.refresh(vsVersion);
		return vsVersion;
	}
}
