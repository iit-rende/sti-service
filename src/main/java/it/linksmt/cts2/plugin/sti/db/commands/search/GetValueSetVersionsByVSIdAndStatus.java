package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class GetValueSetVersionsByVSIdAndStatus extends HibernateCommand {

	private Long vsId;
	private Integer statusId;
	
	public GetValueSetVersionsByVSIdAndStatus(Long csId, Integer statusId) {
		this.vsId = csId;
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
	public List<ValueSetVersion> execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		List<ValueSetVersion> ret = null;
		
		if(null != vsId) {
			Criteria criteria = session.createCriteria(ValueSetVersion.class)
			.createAlias("valueSet", "vs")
			.add(Restrictions.eq("vs.id", vsId));
			
			if(null != statusId) {
				criteria.add(Restrictions.eq("status", statusId));
			}
			
			ret = (List<ValueSetVersion> ) criteria.addOrder(Order.desc("versionId")).list();
		}
		
		return ret;
	}

}
