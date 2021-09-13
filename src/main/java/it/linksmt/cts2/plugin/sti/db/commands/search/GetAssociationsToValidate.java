package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class GetAssociationsToValidate extends HibernateCommand {

	private AbstractStiService service = null;

	public GetAssociationsToValidate(final AbstractStiService service) {
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ( (userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<Association> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		List<Association> retVal = new ArrayList<Association>();

		List<CodeSystemEntityVersionAssociation> resList =
				session.createCriteria(CodeSystemEntityVersionAssociation.class)
				.add(Restrictions.not(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode())))
				.addOrder(Order.asc("id")).list();

		if (resList != null) {
			for (int i = 0; i < resList.size(); i++) {
				retVal.add(DbTransformUtil.entityVersionAssociationToAssociation(
						session, resList.get(i), service));
			}
		}

		return retVal;
	}
}
