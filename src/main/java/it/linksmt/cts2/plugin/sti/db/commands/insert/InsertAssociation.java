package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants.ASSOCIATION_KIND;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants.STATUS_CODES;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class InsertAssociation extends HibernateCommand {

	private String sourceCsVersion = null;
	private String sourceEntityId  = null;

	private String targetCsVersion = null;
	private String targetEntityId  = null;

	private String forwardName = null;
	private String reverseName = null;

	private StiConstants.ASSOCIATION_KIND associationKind = null;
	private StiConstants.STATUS_CODES associationStatus = null;

	private AbstractStiService service = null;

	public InsertAssociation(
			final String sourceCsVersion, final String sourceEntityId,
			final String targetCsVersion, final String targetEntityId,
			final String forwardName, final String reverseName,
			final ASSOCIATION_KIND associationKind,
			final STATUS_CODES associationStatus,
			final AbstractStiService service) {

		this.sourceCsVersion = sourceCsVersion;
		this.sourceEntityId = sourceEntityId;
		this.targetCsVersion = targetCsVersion;
		this.targetEntityId = targetEntityId;
		this.forwardName = forwardName;
		this.reverseName = reverseName;
		this.associationKind = associationKind;
		this.service = service;
		this.associationStatus=associationStatus;
	}

	public InsertAssociation(
			final String sourceCsVersion, final String sourceEntityId,
			final String targetCsVersion, final String targetEntityId,
			final String forwardName, final String reverseName,
			final ASSOCIATION_KIND associationKind,
			final AbstractStiService service) {

		this(sourceCsVersion, sourceEntityId,
			targetCsVersion, targetEntityId,
			forwardName, reverseName,
			associationKind, StiConstants.STATUS_CODES.INACTIVE,
			service);
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public Association execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if ( (associationKind.getCode() != StiConstants.ASSOCIATION_KIND.CROSS_MAPPING.getCode()) &&
			(associationKind.getCode() != StiConstants.ASSOCIATION_KIND.LINK.getCode())) {
			throw new StiHibernateException("L'inserimento di associazioni di tipo " + associationKind.getCode()
					+ " non è attualmente supportato.");
		}

		List<CodeSystemVersion> srcCsVersList = new GetCodeSystemVersionByName(sourceCsVersion).execute(session);
		List<CodeSystemVersion> trgCsVersList = new GetCodeSystemVersionByName(targetCsVersion).execute(session);

		if ( (srcCsVersList == null) || (srcCsVersList.size() != 1) ) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + sourceCsVersion);
		}

		if ( (trgCsVersList == null) || (trgCsVersList.size() != 1) ) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + targetCsVersion);
		}

		CodeSystemConcept srcConcept = new GetCodeSystemConcept(
				sourceEntityId, srcCsVersList.get(0).getVersionId().longValue())
				.execute(session);

		CodeSystemConcept trgConcept = new GetCodeSystemConcept(
				targetEntityId, trgCsVersList.get(0).getVersionId().longValue())
				.execute(session);

		if (srcConcept == null) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della entità sorgente: " + sourceEntityId);
		}

		if (trgConcept == null) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della entità destinazione: " + targetEntityId);
		}

		// Check se l'associazione è già inserita
		List<CodeSystemEntityVersionAssociation> chkExist = session.createCriteria(
				CodeSystemEntityVersionAssociation.class)
				.add(Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId1.versionId",
						srcConcept.getCodeSystemEntityVersion().getVersionId().longValue()))
				.add(Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId2.versionId",
						trgConcept.getCodeSystemEntityVersion().getVersionId().longValue()))
				.list();

		if ( (chkExist != null) && (chkExist.size() > 0) ) {
			return null;
		}

		// Association
		CodeSystemEntityVersionAssociation newAssoc = new CodeSystemEntityVersionAssociation();

		newAssoc.setLeftId(srcConcept.getCodeSystemEntityVersion()
				.getVersionId().longValue());
		newAssoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(
				srcConcept.getCodeSystemEntityVersion());
		newAssoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(
				trgConcept.getCodeSystemEntityVersion());

		newAssoc.setForwardName(StiServiceUtil.trimStr(forwardName));
		newAssoc.setReverseName(StiServiceUtil.trimStr(reverseName));
		newAssoc.setAssociationKind(associationKind.getCode());

		newAssoc.setInsertTimestamp(new Date());
		newAssoc.setStatus(associationStatus.getCode());
		newAssoc.setStatusDate(new Date());

		newAssoc.setMapSetVersion(null);

		session.save(newAssoc);
		session.flush();
		session.refresh(newAssoc);

		if (service == null) {
			return null;
		}

		return DbTransformUtil.entityVersionAssociationToAssociation(session, newAssoc, service);
	}
}
