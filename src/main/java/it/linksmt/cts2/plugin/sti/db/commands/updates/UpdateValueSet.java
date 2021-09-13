package it.linksmt.cts2.plugin.sti.db.commands.updates;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Session;

public class UpdateValueSet extends HibernateCommand {

	private ValueSet valueSet;
	
	public UpdateValueSet(ValueSet valueSet) {
		super();
		this.valueSet = valueSet;
	}

	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}
	
	@Override
	public Object execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		session.update(valueSet);
		session.flush();
		session.refresh(valueSet);
		return valueSet;
	}
}
