package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class GetCodeSystemLastVersion extends HibernateCommand {

	private String codeSystemName = null;

	public GetCodeSystemLastVersion(final String codeSystemName) {
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
		CodeSystemVersion retVal = null;

		List<CodeSystemVersion> versList = new GetCodeSystemVersions(
				codeSystemName, StiConstants.STATUS_CODES.ACTIVE).execute(session);

		if ((versList != null) && (versList.size() > 0)) {
			retVal = versList.get(versList.size()-1);

			// Carico i dati per evitare lazy loading Exception
			retVal.setCodeSystem((CodeSystem)session.get(
					CodeSystem.class, retVal.getCodeSystem().getId().longValue()));
		}

		return retVal;
	}

}
