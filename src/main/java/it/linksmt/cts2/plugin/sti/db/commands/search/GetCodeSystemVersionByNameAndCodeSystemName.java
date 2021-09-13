package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetCodeSystemVersionByNameAndCodeSystemName extends HibernateCommand {

	private String versionName = null;
	private String codeSystemName = null;

	public GetCodeSystemVersionByNameAndCodeSystemName(final String versionName,final String codeSystemName) {
		this.versionName = versionName;
		this.codeSystemName = codeSystemName;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(versionName) || StiServiceUtil.isNull(codeSystemName) ) {
			return null;
		}

		CodeSystemVersion retVal = (CodeSystemVersion) session.createCriteria(CodeSystemVersion.class)
				.add(Restrictions.eq("name", StiServiceUtil.trimStr(versionName)))
				.createAlias("codeSystem", "cs")
				.add(Restrictions.eq("cs.name", StiServiceUtil.trimStr(codeSystemName)))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode()))
				.uniqueResult();

		// Precarico i dati del codeSystem per evitare Lazy-Loading
		if (retVal!=null) {
			retVal.setCodeSystem((CodeSystem)session.get(CodeSystem.class, retVal.getCodeSystem().getId().longValue()));
		}

		return retVal;
	}
}
