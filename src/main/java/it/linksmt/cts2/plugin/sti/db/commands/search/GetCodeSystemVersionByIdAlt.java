package it.linksmt.cts2.plugin.sti.db.commands.search;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

/**
 * Metodo alternativo per recuperare 
 * CodeSystemVersion 
 * con join su CodeSystem
 * @author Luigi Pasca
 *
 */
public class GetCodeSystemVersionByIdAlt extends HibernateCommand {
	
	private long versionId = -1;
	
	public GetCodeSystemVersionByIdAlt(long versionId) {
		super();
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
	public CodeSystemVersion execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		
		return (CodeSystemVersion) session.createCriteria(CodeSystemVersion.class)
				.setFetchMode("codeSystem", FetchMode.JOIN)
				.add(Restrictions.eq("versionId", versionId))
				.uniqueResult();
		
	}

}
