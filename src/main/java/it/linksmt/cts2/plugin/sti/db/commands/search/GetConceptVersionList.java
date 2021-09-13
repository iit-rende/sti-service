package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class GetConceptVersionList extends HibernateCommand {

	private String codeSystemName;
	private String code;

	public GetConceptVersionList(final String codeSystemName, final String code) {
		this.codeSystemName = codeSystemName;
		this.code = code;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<ScopedEntityName> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		List<ScopedEntityName> retVal = new ArrayList<ScopedEntityName>();
		if (StiServiceUtil.isNull(codeSystemName) || StiServiceUtil.isNull(code)) {
			return retVal;
		}

		List<CodeSystemVersion> versionList = new GetCodeSystemVersions(
				codeSystemName, StiConstants.STATUS_CODES.ACTIVE).execute(session);

		if (versionList == null) {
			return retVal;
		}

		for (int i = 0; i < versionList.size(); i++) {
			CodeSystemVersion csVers = versionList.get(i);

			CodeSystemConcept prevConc = new GetCodeSystemConcept(
				code, csVers.getVersionId().longValue()).execute(session);

			if (prevConc != null) {
				retVal.add(ModelUtils.createScopedEntityName(
						StiServiceUtil.trimStr(csVers.getName()),
						StiServiceUtil.trimStr(csVers.getCodeSystem().getName())));
			}
		}

		return retVal;
	}

}
