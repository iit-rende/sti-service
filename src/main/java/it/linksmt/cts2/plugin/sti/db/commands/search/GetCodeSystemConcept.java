package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntity;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class GetCodeSystemConcept extends HibernateCommand {

	private String code = null;
	private long codeSystemVersionId = -1;

	/*
	private String SQL_QUERY_START = "select conc.* from code_system_version_entity_membership as memb"
			+ " inner join code_system_entity as ent on memb.codesystementityid = ent.id"
			+ " inner join code_system_entity_version as entVers on ent.currentversionid = entVers.versionId"
			+ " inner join code_system_concept as conc on entVers.versionId = conc.codesystementityversionid";
	*/

	public GetCodeSystemConcept(final String code, final long codeSystemVersionId) {
		this.code = code;
		this.codeSystemVersionId = codeSystemVersionId;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public CodeSystemConcept execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(code)) {
			return null;
		}

		CodeSystemVersion csVers = (CodeSystemVersion)session.get(
				CodeSystemVersion.class, codeSystemVersionId);

		if (csVers == null) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "del Code System - VersionId: " + codeSystemVersionId);
		}

		List<CodeSystemConcept> candConc = session.createCriteria(CodeSystemConcept.class).add(
				Restrictions.ilike("code", StiServiceUtil.trimStr(code))).list();

		if ( ((candConc == null) || (candConc.size() == 0)) &&
				Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME.equalsIgnoreCase(
					StiServiceUtil.trimStr(csVers.getCodeSystem().getName()))) {
			candConc = session.createCriteria(CodeSystemConcept.class).add(
					Restrictions.ilike("termAbbrevation", StiServiceUtil.trimStr(code))).list();
		}

		for (int i = 0; i < candConc.size(); i++) {
			CodeSystemConcept retVal = candConc.get(i);
			CodeSystemEntity entConc = retVal.getCodeSystemEntityVersion()
					.getCodeSystemEntity();

			CodeSystemVersionEntityMembership checkMemb = (CodeSystemVersionEntityMembership)
					session.createCriteria(CodeSystemVersionEntityMembership.class)
						.add(Restrictions.eq("id.codeSystemVersionId", codeSystemVersionId))
						.add(Restrictions.eq("id.codeSystemEntityId", entConc.getId().longValue()))
								.uniqueResult();

			if (checkMemb != null) {
				return retVal;
			}
		}

		return null;
	}
}
