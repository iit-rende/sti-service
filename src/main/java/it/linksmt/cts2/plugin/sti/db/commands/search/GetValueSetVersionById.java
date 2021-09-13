package it.linksmt.cts2.plugin.sti.db.commands.search;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetValueSetVersionById extends HibernateCommand {

	private Long versionId;
	
	public GetValueSetVersionById(Long versionId) {
		this.versionId = versionId;
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
		return (ValueSetVersion) session.createCriteria(ValueSetVersion.class)
				.setFetchMode("valueSet", FetchMode.JOIN)
				.add(Restrictions.eq("versionId", versionId))
				.uniqueResult();
	}

}
