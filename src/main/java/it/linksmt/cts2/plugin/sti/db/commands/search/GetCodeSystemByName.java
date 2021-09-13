package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class GetCodeSystemByName extends HibernateCommand {

	private String codeSystemName = null;

	public GetCodeSystemByName(final String codeSystemName ) {
		this.codeSystemName = codeSystemName;
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

		if (StiServiceUtil.isNull(codeSystemName)) {
			return null;
		}

		List<CodeSystem> retVal = session.createCriteria(CodeSystem.class)
				.add(Restrictions.ilike("name", StiServiceUtil.trimStr(codeSystemName), MatchMode.EXACT))
				.list();

		return retVal;
	}
}
