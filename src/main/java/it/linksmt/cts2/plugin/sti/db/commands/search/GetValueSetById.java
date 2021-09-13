package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetValueSetById extends HibernateCommand {

	private Long id;

	public GetValueSetById(Long id) {
		this.id = id;
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
	public ValueSet execute(Session session) throws StiAuthorizationException,
	StiHibernateException {

		ValueSet retVal = null;

		if(id!=null) {
			Criteria c = session.createCriteria(ValueSet.class);
			c.add(Restrictions.eq("id", id));
			retVal = (ValueSet) c.uniqueResult();
		}

		return retVal;
	}

}
