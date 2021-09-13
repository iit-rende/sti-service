package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class GetExtraMetadataParameterValueByCs extends HibernateCommand {

	private Long codeSystemId = null;

	public GetExtraMetadataParameterValueByCs(final Long codeSystemId ) {
		this.codeSystemId = codeSystemId;
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

		if (codeSystemId==null) {
			return null;
		}

		List<ExtraMetadataParameter> retVal = session.createCriteria(ExtraMetadataParameter.class)
				.add(Restrictions.eq("codeSystem.id", codeSystemId))
				.list();

		return retVal;
	}

}
