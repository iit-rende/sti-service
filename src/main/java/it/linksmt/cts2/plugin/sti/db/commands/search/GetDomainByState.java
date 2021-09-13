package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.Domain;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class GetDomainByState extends HibernateCommand {

	private Integer state;
	
	public GetDomainByState(Integer state) {
		this.state = state;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Domain> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		
		if (state==null) {
			return null;
		}
		
		
		List<Domain> retVal = session.createCriteria(Domain.class)
				.add(Restrictions.eq("state", state)).addOrder(Order.asc("position"))
				.list();

		return retVal;
	}
}
