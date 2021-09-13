package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetMetadataParameterValue extends HibernateCommand {

	private List<Long> codeSystemEntityVersionIds;
	
	
	
	public GetMetadataParameterValue(List<Long> codeSystemEntityVersionIds) {
		this.codeSystemEntityVersionIds = codeSystemEntityVersionIds;
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
	public List<CodeSystemMetadataValue> execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		List<CodeSystemMetadataValue> retVal = null;
		
		Criteria criteria = session.createCriteria(CodeSystemMetadataValue.class);
		
		if(null != codeSystemEntityVersionIds && codeSystemEntityVersionIds.size()>0) {
			criteria.setFetchMode("metadataParameter", FetchMode.JOIN)
			.setFetchMode("codeSystemEntityVersion", FetchMode.JOIN)
			.createAlias("codeSystemEntityVersion", "codeSystemEntityVersion")
			.add(Restrictions.in("codeSystemEntityVersion.id", codeSystemEntityVersionIds ));
			retVal = criteria.list();
		}
		
		
		return retVal;
	}

}
