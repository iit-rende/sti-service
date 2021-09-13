package it.linksmt.cts2.plugin.sti.db.commands.search;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetCodeSystemById extends HibernateCommand {

	private Long csId;
	
	public GetCodeSystemById(Long csId) {
		this.csId = csId;
	}
	
	@Override
	public void checkPermission(Session session)
			throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystem execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		CodeSystem ret = null;
		
		if(null != csId ) {
			ret = (CodeSystem) session.createCriteria(CodeSystem.class).add(Restrictions.eq("id", csId)).uniqueResult();
		}
		
		return ret;
		
	}

}
