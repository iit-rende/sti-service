package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetExtraMetadataParameterValueByCsAndParamName extends HibernateCommand {

	private Long codeSystemId = null;
	
	private String paramName = null;

	public GetExtraMetadataParameterValueByCsAndParamName(final Long codeSystemId, String paramName ) {
		this.codeSystemId = codeSystemId;
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
	public ExtraMetadataParameter execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (codeSystemId==null) {
			return null;
		}

		ExtraMetadataParameter retVal = (ExtraMetadataParameter) session.createCriteria(ExtraMetadataParameter.class)
				.add(Restrictions.eq("codeSystem.id", codeSystemId))
				.add(Restrictions.eq("paramName", this.paramName)).uniqueResult();

		return retVal;
	}

}
