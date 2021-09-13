package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class SearchAssociations extends HibernateCommand {

	private AbstractStiService service = null;
	private String codeSystemVersion = null;

	private String sourceEntityId = null;
	private String targetEntityId = null;
	private String sourceOrTargetEntity = null;

	public SearchAssociations(final String codeSystemVersion,
			final String sourceEntityId, final String targetEntityId,
			final String sourceOrTargetEntity,
			final AbstractStiService service) {

		this.codeSystemVersion = codeSystemVersion;
		this.sourceEntityId = sourceEntityId;
		this.targetEntityId = targetEntityId;
		this.sourceOrTargetEntity = sourceOrTargetEntity;
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<Association> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		CodeSystemVersion csVers = null;
		if (!StiServiceUtil.isNull(codeSystemVersion)) {
			List<CodeSystemVersion> csLst = new GetCodeSystemVersionByName(
					StiServiceUtil.trimStr(codeSystemVersion)).execute(session);

			if ((csLst != null) && (csLst.size() == 1)) {
				csVers = csLst.get(0);
			}
		}

		if (csVers == null) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + codeSystemVersion);
		}

		if (StiServiceUtil.isNull(sourceEntityId) && StiServiceUtil.isNull(targetEntityId) &&
				StiServiceUtil.isNull(sourceOrTargetEntity)) {
			throw new StiHibernateException("Occorre indicare un valore per "
					+ "l'entità sorgente o destinazione");
		}

		CodeSystemEntityVersion srcEntVersion = null;
		if (!StiServiceUtil.isNull(sourceEntityId)) {
			CodeSystemConcept foundSrc = new GetCodeSystemConcept(
					StiServiceUtil.trimStr(sourceEntityId),
					csVers.getVersionId().longValue()).execute(session);

			if (foundSrc != null) {
				srcEntVersion = foundSrc.getCodeSystemEntityVersion();
			}
		}

		CodeSystemEntityVersion trgEntVersion = null;
		if (!StiServiceUtil.isNull(targetEntityId)) {
			CodeSystemConcept foundSrc = new GetCodeSystemConcept(
					StiServiceUtil.trimStr(targetEntityId),
					csVers.getVersionId().longValue()).execute(session);

			if (foundSrc != null) {
				trgEntVersion = foundSrc.getCodeSystemEntityVersion();
			}
		}

		if (!StiServiceUtil.isNull(sourceOrTargetEntity)) {
			CodeSystemConcept foundSrc = new GetCodeSystemConcept(StiServiceUtil.trimStr(sourceOrTargetEntity),csVers.getVersionId().longValue()).execute(session);

			if (foundSrc != null) {
				srcEntVersion = foundSrc.getCodeSystemEntityVersion();
				trgEntVersion = foundSrc.getCodeSystemEntityVersion();
			}

		}
		
		LogicalExpression critSrc = null;
		if (srcEntVersion != null) {
			critSrc = Restrictions.or(
					Restrictions.and(
							Restrictions.isNull("leftId"),
							Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId1.versionId", srcEntVersion.getVersionId().longValue())),
					Restrictions.eq("leftId", srcEntVersion.getVersionId().longValue()) );
		}

		LogicalExpression critTrg = null;
		if (trgEntVersion != null) {
			critTrg = Restrictions.or(
					Restrictions.and(
							Restrictions.isNull("leftId"),
							Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId2.versionId", trgEntVersion.getVersionId().longValue())),
					Restrictions.and(
							Restrictions.isNotNull("leftId"),
							Restrictions.or(
									Restrictions.and(
											Restrictions.not(Restrictions.eqProperty("leftId", "codeSystemEntityVersionByCodeSystemEntityVersionId1.versionId")),
											Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId1.versionId", trgEntVersion.getVersionId().longValue())),
									Restrictions.and(
											Restrictions.eqProperty("leftId", "codeSystemEntityVersionByCodeSystemEntityVersionId1.versionId"),
											Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId2.versionId", trgEntVersion.getVersionId().longValue()))
									)));
		}

		List<Association> retVal = new ArrayList<Association>();
		Criteria assCrit = session.createCriteria(CodeSystemEntityVersionAssociation.class)
				.add(Restrictions.or(
						Restrictions.eq("associationKind", StiConstants.ASSOCIATION_KIND.CROSS_MAPPING.getCode()),
						Restrictions.eq("associationKind", StiConstants.ASSOCIATION_KIND.LINK.getCode())))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode()));

		if ( (critSrc != null) && (critTrg != null) ) {
			if (!StiServiceUtil.isNull(sourceOrTargetEntity)) {
				assCrit = assCrit.add(Restrictions.or(critSrc, critTrg));
			}
			else {
				assCrit = assCrit.add(Restrictions.and(critSrc, critTrg));
			}
		}
		else if (critSrc != null) {
			assCrit = assCrit.add(critSrc);
		}
		else if (critTrg != null) {
			assCrit = assCrit.add(critTrg);
		}
		else {
			return retVal;
		}

		List<CodeSystemEntityVersionAssociation> resList = assCrit.list();
		if (resList != null) {
			for (int i = 0; i < resList.size(); i++) {
				retVal.add(DbTransformUtil.entityVersionAssociationToAssociation(session, resList.get(i), service));
			}
		}

		return retVal;
	}

}
