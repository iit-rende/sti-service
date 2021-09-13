package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;

import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

public class GetEntityDescriptionEntry extends HibernateCommand {

	private String code = null;
	private String csVersionName = null;

	private AbstractStiService service = null;

	public GetEntityDescriptionEntry(
			final String code, final String csVersionName,
			final AbstractStiService service) {

		this.code = code;
		this.csVersionName = csVersionName;
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public NamedEntityDescription execute(final Session session)
			throws StiAuthorizationException, StiHibernateException {

		List<CodeSystemVersion> setVers = new GetCodeSystemVersionByName(csVersionName).execute(session);
		if ((setVers == null) || (setVers.size() != 1)) {
			throw new StiHibernateException("Impossibile leggere i dati della Versione Code System: " + csVersionName);
		}

		CodeSystemVersion csVers = setVers.get(0);

		CodeSystemConcept csConc = new GetCodeSystemConcept(
				code, csVers.getVersionId().longValue()).execute(session);

		return DbTransformUtil.conceptToEntityDescription(session,
				csVers.getCodeSystem().getName(), csVers, csConc, service);
	}
}
