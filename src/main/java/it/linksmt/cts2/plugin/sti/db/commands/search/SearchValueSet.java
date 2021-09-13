package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class SearchValueSet extends HibernateCommand {

	private String name;

	public SearchValueSet(String name) {
		this.name = name;
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
	public List<ValueSet> execute(Session session) throws StiAuthorizationException,
	StiHibernateException {

		List<ValueSet> retVal = null;

		Criteria c = session.createCriteria(ValueSet.class);

		if(StringUtils.isNotBlank(name)) {
			c.add(Restrictions.ilike("name", StiServiceUtil.trimStr(name), MatchMode.EXACT));
		}

		retVal = c.list();

		return retVal;
	}

}
