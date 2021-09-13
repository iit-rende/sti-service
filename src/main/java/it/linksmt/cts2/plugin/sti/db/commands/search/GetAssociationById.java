package it.linksmt.cts2.plugin.sti.db.commands.search;

import org.hibernate.Session;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetAssociationById extends HibernateCommand {

	private long entityVerisionAssociationId = -1;
	private AbstractStiService service = null;

	public GetAssociationById(
			final long entityVerisionAssociationId,
			final AbstractStiService service) {

		this.entityVerisionAssociationId = entityVerisionAssociationId;
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public Association execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		CodeSystemEntityVersionAssociation csAssoc = (CodeSystemEntityVersionAssociation) session.get(
				CodeSystemEntityVersionAssociation.class, entityVerisionAssociationId);

		if (csAssoc == null) {
			return null;
		}
		return DbTransformUtil.entityVersionAssociationToAssociation(session, csAssoc, service);
	}
}
