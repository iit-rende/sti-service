package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class GetEntityDirectoryEntries extends HibernateCommand {

	private int start = -1;
	private int rows = -1;

	private String csVersionName = null;
	private String superClassId = null;

	private AbstractStiService service = null;

	private String csName = null;

	public GetEntityDirectoryEntries(final String csVersionName, final String superClassId, final int start, final int rows, final AbstractStiService service, String csName) {

		this.csVersionName = csVersionName;
		this.superClassId = superClassId;
		this.start = start;
		this.rows = rows;
		this.service = service;
		this.csName = csName;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<EntityDirectoryEntry> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (start < 0) {
			start = 0;
		}
		if (rows < 0) {
			rows = 100;
		}

		List<CodeSystemVersion> setVers = new GetCodeSystemVersionsByVersionNameAndCSName(csVersionName, csName).execute(session);
		if ((setVers == null) || (setVers.size() != 1)) {
			throw new StiHibernateException("Impossibile leggere i dati della Versione Code System: " + csVersionName);
		}

		CodeSystemVersion csVers = setVers.get(0);

		List<CodeSystemConcept> codeConcept = session.createSQLQuery("select conc.* " + getSqlQueryForConcepts(session) + " order by conc.code").addEntity(CodeSystemConcept.class)
				.setFirstResult(start).setMaxResults(rows).list();

		String codeSystemName = csVers.getCodeSystem().getName();
		List<EntityDirectoryEntry> retVal = new ArrayList<EntityDirectoryEntry>();

		if (codeConcept != null) {
			for (int i = 0; i < codeConcept.size(); i++) {
				retVal.add(DbTransformUtil.conceptToEntityEntry(session, codeSystemName, csVersionName, codeConcept.get(i), service));
			}
		}

		return retVal;
	}

	public String getSqlQueryForConcepts(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(csVersionName)) {
			throw new StiHibernateException("Occorre specificare la versione del Code System.");
		}

		List<CodeSystemVersion> setVers = new GetCodeSystemVersionByName(csVersionName).execute(session);
		if ((setVers == null) || (setVers.size() != 1)) {
			throw new StiHibernateException("Impossibile leggere i dati della Versione Code System: " + csVersionName);
		}

		CodeSystemVersion csVers = setVers.get(0);

		CodeSystemConcept superclass = null;
		if (!StiServiceUtil.isNull(superClassId)) {
			superclass = new GetCodeSystemConcept(StiServiceUtil.trimStr(superClassId), csVers.getVersionId().longValue()).execute(session);

			if (superclass == null) {
				throw new StiHibernateException("Impossibile leggere i dati della Superclasse: " + superClassId);
			}
		}

		String sqlQuery = " from code_system_version_entity_membership as memb" + " inner join code_system_entity as ent on memb.codesystementityid = ent.id"
				+ " inner join code_system_entity_version as entVers on ent.currentversionid = entVers.versionId"
				+ " inner join code_system_concept as conc on entVers.versionId = conc.codesystementityversionid";

		if (superclass != null) {
			sqlQuery += " inner join code_system_entity_version_association as taxon on ent.currentversionid = taxon.codesystementityversionid1 " + " where memb.codesystemversionid="
					+ csVers.getVersionId().longValue() + " and taxon.codesystementityversionid2=" + superclass.getCodeSystemEntityVersionId().longValue() + " and taxon.associationkind="
					+ StiConstants.ASSOCIATION_KIND.TAXONOMY.getCode() + " and taxon.status=" + StiConstants.STATUS_CODES.ACTIVE.getCode();
		} else {
			sqlQuery += " where memb.codesystemversionid=" + csVers.getVersionId().longValue() + " and entVers.versionId NOT IN "
					+ "(select distinct codesystementityversionid1 from code_system_entity_version_association as taxon " + " where taxon.associationkind="
					+ StiConstants.ASSOCIATION_KIND.TAXONOMY.getCode() + " and taxon.status=" + StiConstants.STATUS_CODES.ACTIVE.getCode() + ")";
		}

		return sqlQuery;
	}
}
