package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class GetExtraMetadataParameterByName extends HibernateCommand {

	private String paramName = null;

	public GetExtraMetadataParameterByName(final String paramName ) {
		this.paramName = paramName;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExtraMetadataParameter> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(paramName)) {
			return null;
		}

		List<ExtraMetadataParameter> retVal = session.createCriteria(ExtraMetadataParameter.class)
				.add(Restrictions.ilike("paramName", StiServiceUtil.trimStr(paramName), MatchMode.EXACT))
				.list();

		return retVal;
	}
}
