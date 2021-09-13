package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class GetCodeSystemVersions extends HibernateCommand {

	private String codeSystemName = null;
	private StiConstants.STATUS_CODES status = null;

	public GetCodeSystemVersions(final String codeSystemName, final StiConstants.STATUS_CODES status) {
		this.codeSystemName = codeSystemName;
		this.status = status;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<CodeSystemVersion> execute(final Session session)
			throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(codeSystemName)) {
			throw new StiHibernateException("Occorre specificare il nome del Code System.");
		}

		CodeSystem cs = (CodeSystem)session.createCriteria(CodeSystem.class).add(
				Restrictions.ilike("name", StiServiceUtil.trimStr(codeSystemName),
						MatchMode.EXACT)).uniqueResult();

		if (cs == null) {
			throw new StiHibernateException("Nessuna occorrenza trovata per: " + codeSystemName);
		}

		return session.createCriteria(CodeSystemVersion.class)
				.add(Restrictions.eq("codeSystem.id", cs.getId().longValue()))
				.add(Restrictions.eq("status", status.getCode()))
				.addOrder(Order.asc("releaseDate")).list();
	}
}
