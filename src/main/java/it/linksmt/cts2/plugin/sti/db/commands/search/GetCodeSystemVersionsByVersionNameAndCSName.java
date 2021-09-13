package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class GetCodeSystemVersionsByVersionNameAndCSName extends HibernateCommand  {

	private String versionName = null;
	private String csName = null;
	
	public GetCodeSystemVersionsByVersionNameAndCSName(String versionName,
			String csName) {
		super();
		this.versionName = versionName;
		this.csName = csName;
	}


	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	
	@Override
	public List<CodeSystemVersion> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(versionName) || StiServiceUtil.isNull(csName)) {
			return null;
		}

		List<CodeSystemVersion> retVal = session.createCriteria(CodeSystemVersion.class)
				.add(Restrictions.ilike("name", StiServiceUtil.trimStr(versionName)))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode()))
				.list();

		// Precarico i dati del codeSystem per evitare Lazy-Loading
		for (int i = 0; i < retVal.size(); i++) {
			retVal.get(i).setCodeSystem((CodeSystem)session.get(
					CodeSystem.class, retVal.get(i).getCodeSystem().getId().longValue()));
		}

		return retVal;
	}
}
