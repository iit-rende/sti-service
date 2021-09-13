package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.math.BigInteger;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class CountCodeSystemEntityVersionAssociationByMapSetVersionId extends HibernateCommand {

	private Long mapSetVersionId  = null;

	public CountCodeSystemEntityVersionAssociationByMapSetVersionId(final Long mapSetVersionId) {
		this.mapSetVersionId = mapSetVersionId;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public Integer execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		
		SQLQuery queryCount = session.createSQLQuery("SELECT COUNT(distinct id) FROM code_system_entity_version_association where mapSetVersionId = :MAPSETVERSIONID");
		queryCount.setLong("MAPSETVERSIONID",mapSetVersionId);
		
		Integer numFound = ((BigInteger) queryCount.uniqueResult()).intValue();
		return numFound;
	}
}
