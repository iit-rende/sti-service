package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetCodeSystemVersionsByCSIdAndStatus extends HibernateCommand {

	private Long csId;
	private Integer statusId;
	
	public GetCodeSystemVersionsByCSIdAndStatus(Long csId, Integer statusId) {
		this.csId = csId;
		this.statusId = statusId;
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
	public List<CodeSystemVersion> execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		List<CodeSystemVersion> ret = null;
		
		if(null != csId) {
			Criteria criteria = session.createCriteria(CodeSystemVersion.class)
			.createAlias("codeSystem", "cs")
			.add(Restrictions.eq("cs.id", csId));
			
			if(null != statusId) {
				criteria.add(Restrictions.eq("status", statusId));
			}
			
			ret = (List<CodeSystemVersion> ) criteria.addOrder(Order.asc("versionId")).list();
		}
		
		return ret;
	}

}
