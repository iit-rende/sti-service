package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SearchCodeSystems extends HibernateCommand {

	private String csName = null;
	private boolean excludeValueSets = true;
	private String csType = null;

	public SearchCodeSystems(final String csName, boolean excludeValueSets, String csType ) {
		this.csName = csName;
		this.excludeValueSets = excludeValueSets;
		this.csType = csType;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CodeSystem> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		Criteria critCs = session.createCriteria(CodeSystem.class);
		
		if(excludeValueSets) {
			critCs.add(Restrictions.or(Restrictions.not(Restrictions.eq("codeSystemType", CodeSystemType.VALUE_SET.getKey())), Restrictions.isNull("codeSystemType")));
		}
		
		if (!StiServiceUtil.isNull(csName)) {
			critCs = critCs.add(Restrictions.ilike("name", StiServiceUtil.trimStr(csName), MatchMode.EXACT));
		}
		
		if(!StiServiceUtil.isNull(csType)){
			critCs = critCs.add(Restrictions.ilike("codeSystemType", StiServiceUtil.trimStr(csType), MatchMode.EXACT));
		}

		return critCs.addOrder(Order.asc("name")).list();
	}
}
