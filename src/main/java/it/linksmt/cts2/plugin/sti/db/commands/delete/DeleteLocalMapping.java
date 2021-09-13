package it.linksmt.cts2.plugin.sti.db.commands.delete;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;

import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.importer.IndexDocumentBuilder;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class DeleteLocalMapping extends HibernateCommand {

	private static Logger log = Logger.getLogger(DeleteLocalMapping.class);

	private String csVersionName = null;
	private String csLocalName = null;
	private String solrUrl = null;

	private String SQL_QUERY_META = "select entVers.* from code_system_version_entity_membership as memb"
			+ " inner join code_system_entity as ent on memb.codesystementityid = ent.id"
			+ " inner join code_system_entity_version as entVers on ent.currentversionid = entVers.versionId"
			+ " inner join code_system_metadata_value as metaval on entVers.versionId = metaval.codesystementityversionid"
			+ " inner join metadata_parameter as param on metaval.metadataparameterid = param.id "
			+ " where param.languagecd='" + StiConstants.LOCAL_LANGUAGE_CD + "' and memb.codesystemversionid = " ;

	public DeleteLocalMapping(final String csVersionName, final String csLocalName, final String solrUrl) {
		this.csVersionName = csVersionName;
		this.csLocalName = csLocalName;
		this.solrUrl = solrUrl;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		List<CodeSystemVersion> csList = new GetCodeSystemVersionByName(csVersionName).execute(session);
		if ( (csList == null) || (csList.size() != 1) ) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + csVersionName);
		}

		CodeSystemVersion csVersion = csList.get(0);

		List<CodeSystemEntityVersion> entList = session.createSQLQuery(
				SQL_QUERY_META + String.valueOf(csVersion.getVersionId().longValue()))
					.addEntity(CodeSystemEntityVersion.class)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

		try {
			String localPrefix = (StiServiceUtil.trimStr(csLocalName) +
					StiConstants.LOCAL_VALUE_SEPARATOR).toLowerCase();

			for (int i = 0; i < entList.size(); i++) {
				CodeSystemEntityVersion curEv = entList.get(i);

				CodeSystemMetadataValue[] metaList = curEv.getCodeSystemMetadataValues()
						.toArray(new CodeSystemMetadataValue[curEv.getCodeSystemMetadataValues().size()]);

				for (int j = 0; j < metaList.length; j++) {
					CodeSystemMetadataValue valCur = metaList[j];

					if (StiServiceUtil.trimStr(valCur.getMetadataParameter().getLanguageCd()).equalsIgnoreCase(StiConstants.LOCAL_LANGUAGE_CD) &&
							StiServiceUtil.trimStr(valCur.getParameterValue()).toLowerCase().startsWith(localPrefix)) {

						// Metadato locale da eliminare
						curEv.getCodeSystemMetadataValues().remove(valCur);

						session.delete(valCur);
						session.flush();
					}
				}

				JsonObject solrDoc = IndexDocumentBuilder.createByCodeSystemFields(
	        			session, csVersion.getCodeSystem().getName(),
	        			curEv.getCodeSystemConcepts().iterator().next());

	        	// Viene effettuato direttamente il commit
				// per fare un aggiornamento progressivo
	        	SolrIndexerUtil.indexSingleDocument(solrUrl+"?commit=true", solrDoc);
			}
		}
		catch(Exception dex) {
			throw new StiHibernateException("Errore durante l'eliminazione della Codifica Locale.", dex);
		}

		return csVersion;
	}
}
