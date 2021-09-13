package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class GetCodeSystemMetadataParameters extends HibernateCommand {

	private Long codeSystemId;
	private String language;
	
	public GetCodeSystemMetadataParameters (Long id) {
		this.codeSystemId = id;
	}
	public GetCodeSystemMetadataParameters (Long id,String language) {
		this.codeSystemId = id;
		this.language = language;
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
	public List<MetadataParameter> execute(Session session) throws StiAuthorizationException,
			StiHibernateException {
		List<MetadataParameter> retVal = null;
		if(null != codeSystemId && null != language){
			Criteria cs = session.createCriteria(MetadataParameter.class)
						.createAlias("codeSystem", "cs")
						.add(Restrictions.eq("cs.id", codeSystemId))
						.add(Restrictions.eq("languageCd", language));
			
			cs.addOrder(Order.asc("position"));
			retVal = (List<MetadataParameter>)cs.list();
		}
		else{
			if(null != codeSystemId){
				Criteria cs = session.createCriteria(MetadataParameter.class).createAlias("codeSystem", "cs").add(Restrictions.eq("cs.id", codeSystemId));
				cs.addOrder(Order.asc("position"));
				retVal = (List<MetadataParameter>)cs.list();
			}
		}
		
		return retVal;
	}

}
