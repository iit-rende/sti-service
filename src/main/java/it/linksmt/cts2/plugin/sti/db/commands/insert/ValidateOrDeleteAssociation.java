package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class ValidateOrDeleteAssociation extends HibernateCommand {

	private static Logger log = Logger.getLogger(ValidateOrDeleteAssociation.class);

	private long associationId = -1;
	private boolean setActive = false;

	private AbstractStiService service = null;

	public ValidateOrDeleteAssociation(
			final long associationId, final boolean setActive,
			final AbstractStiService service) {

		this.associationId = associationId;
		this.setActive = setActive;
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public Association execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		CodeSystemEntityVersionAssociation updVal = (CodeSystemEntityVersionAssociation)
				session.get(CodeSystemEntityVersionAssociation.class, associationId);

		if (updVal == null) {
			log.warn("Associazione inesistente: " + String.valueOf(associationId));
			return null;
		}

		if ((updVal.getStatus() == null) || (updVal.getStatus() !=
				StiConstants.STATUS_CODES.INACTIVE.getCode())) {
			log.warn("Lo stato dell'Associazione non risulta da validare: " + String.valueOf(associationId));
			return null;
		}

		int kindVal = -1;
		if (updVal.getAssociationKind() != null) {
			kindVal = updVal.getAssociationKind().intValue();
		}

		if ((kindVal != StiConstants.ASSOCIATION_KIND.CROSS_MAPPING.getCode()) &&
			(kindVal != StiConstants.ASSOCIATION_KIND.LINK.getCode())) {
			log.warn("Lo stato dell'Associazione non risulta compatibile con la validazione: " + String.valueOf(associationId));
			return null;
		}

		if (setActive) {
			updVal.setStatus(StiConstants.STATUS_CODES.ACTIVE.getCode());
			updVal.setStatusDate(new Date());

			session.save(updVal);
			session.flush();
			session.refresh(updVal);
		}
		else {
			session.delete(updVal);
			session.flush();
		}

		return DbTransformUtil.entityVersionAssociationToAssociation(session, updVal, service);
	}

}
