package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class CountCodeSystemEntities extends HibernateCommand {

	private String csVersionName = null;
	private String superClassId  = null;
	
	private String csName = null;

	public CountCodeSystemEntities(final String csVersionName, final String superClassId, String csName) {
		this.csVersionName = csVersionName;
		this.superClassId = superClassId;
		this.csName = csName;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public Integer execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		SQLQuery qEntity = session.createSQLQuery("select count (distinct entVers.versionId) " +
				new GetEntityDirectoryEntries(csVersionName, superClassId, -1, -1, null, csName)
					.getSqlQueryForConcepts(session));

		return ((Number)qEntity.uniqueResult()).intValue();
	}
}
