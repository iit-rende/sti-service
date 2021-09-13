package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class GetAllMapVersion extends HibernateCommand {

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<MapSetVersion> execute(final Session session) throws StiAuthorizationException, StiHibernateException {
		return session.createCriteria(MapSetVersion.class).add(Restrictions.eq("status",
				StiConstants.STATUS_CODES.ACTIVE.getCode())).addOrder(Order.asc("fullname")).list();
	}
}
