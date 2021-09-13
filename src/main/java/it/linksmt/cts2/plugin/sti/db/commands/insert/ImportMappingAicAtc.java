package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.importer.IndexDocumentBuilder;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportMappingAicAtc extends HibernateCommand {

	private static Logger log = Logger.getLogger(ImportMappingAicAtc.class);

	private String aicVersionName;
	private String atcVersionName;
	private String solrUrl;

	private Map<String, String> aicMap = null;

	private static int numInsert = 0;
	private static Session session = null;

	public ImportMappingAicAtc(
			final String aicVersionName, final String atcVersionName,
			final String solrUrl, final Map<String, String> aicMap) {

		this.aicVersionName = aicVersionName;
		this.atcVersionName = atcVersionName;
		this.aicMap = aicMap;
		this.solrUrl = solrUrl;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session originalSession)
			throws StiAuthorizationException, StiHibernateException {

		List<CodeSystemVersion> aicList = new GetCodeSystemVersionByName(aicVersionName).execute(originalSession);
		List<CodeSystemVersion> atcList = new GetCodeSystemVersionByName(atcVersionName).execute(originalSession);

		if ( (aicList == null) || (aicList.size() != 1) ) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + aicVersionName);
		}

		if ( (atcList == null) || (atcList.size() != 1) ) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + atcVersionName);
		}

		CodeSystemVersion aicVersion = aicList.get(0);
		CodeSystemVersion atcVersion = atcList.get(0);

		numInsert = 0;
		session = originalSession;

		String[] keyArr = aicMap.keySet().toArray(new String[0]);
		JsonArray docsArr = new JsonArray();

		for (String key : keyArr) {

			CodeSystemConcept aicConc = new GetCodeSystemConcept(
					StiServiceUtil.trimStr(key),
					aicVersion.getVersionId().longValue())
					.execute(session);

			String targetAtc = StiServiceUtil.trimStr(aicMap.get(key));
			CodeSystemConcept atcConc = new GetCodeSystemConcept(
					targetAtc,	atcVersion.getVersionId().longValue())
					.execute(session);

			if (aicConc == null) {
				log.warn("Impossibile inserire il mapping: " + key + " - " + targetAtc);
				continue;
			}

			String valMetaTrg = targetAtc;
			if (atcConc == null) {
				log.warn("Corrispondenza non trovata in ATC: " + key + " - " + targetAtc);
				valMetaTrg = targetAtc + " (NF)";
			}

			// Metadato per il codice ATC
			Criteria critMeta = session.createCriteria(MetadataParameter.class)
					.add(Restrictions.eq("paramName", AtcAicFields.ATC_CODICE))
					.add(Restrictions.eq("codeSystem.id", aicVersion.getCodeSystem()
							.getId().longValue()));

			MetadataParameter metaParam = (MetadataParameter)critMeta.uniqueResult();
			if (metaParam == null) {
				throw new StiHibernateException("Impossibile trovare la definizione del metadato: "
						+ AtcAicFields.ATC_CODICE);
			}

			inserOrUpdateMetadata(session, metaParam.getId().longValue(),
					aicConc.getCodeSystemEntityVersionId(), valMetaTrg);

			// Metadato per la versione del mapping ATC - AIC
			critMeta = session.createCriteria(MetadataParameter.class)
					.add(Restrictions.eq("paramName", AtcAicFields.VERSIONI_MAPPING))
					.add(Restrictions.eq("codeSystem.id", aicVersion.getCodeSystem()
							.getId().longValue()));

			metaParam = (MetadataParameter)critMeta.uniqueResult();
			if (metaParam == null) {
				throw new StiHibernateException("Impossibile trovare la definizione del metadato: "
						+ AtcAicFields.VERSIONI_MAPPING);
			}

			inserOrUpdateMetadata(session, metaParam.getId().longValue(),
					aicConc.getCodeSystemEntityVersionId(),
					"ATC (" + atcVersion.getName() + ") - AIC (" + aicVersion.getName() + ")");

			// Metadato per la versione di ATC
			critMeta = session.createCriteria(MetadataParameter.class)
					.add(Restrictions.eq("paramName", AtcAicFields.VERSIONE_ATC))
					.add(Restrictions.eq("codeSystem.id", aicVersion.getCodeSystem()
							.getId().longValue()));

			metaParam = (MetadataParameter)critMeta.uniqueResult();
			if (metaParam == null) {
				throw new StiHibernateException("Impossibile trovare la definizione del metadato: "
						+ AtcAicFields.VERSIONE_ATC);
			}

			inserOrUpdateMetadata(session, metaParam.getId().longValue(),
					aicConc.getCodeSystemEntityVersionId(), atcVersion.getName());

			// Inserimento della associazione
			if (atcConc != null) {
				new InsertAssociation(
						aicVersionName, StiServiceUtil.trimStr(key),
						atcVersionName, targetAtc,
						StiConstants.AIC_ATC_FORWARD_NAME,
						StiConstants.AIC_ATC_REVERSE_NAME,
						StiConstants.ASSOCIATION_KIND.CROSS_MAPPING,
						StiConstants.STATUS_CODES.ACTIVE, null).execute(session);
			}

			try {

				JsonObject solrDoc = IndexDocumentBuilder.createByCodeSystemFields(
	        			session, AtcAicFields.AIC_CODE_SYSTEM_NAME, aicConc);
				docsArr.add(solrDoc);

				numInsert++;
				if ((numInsert % ImportCsUtil.CHUNK_SIZE_IMPORT) == 0) {
					// Chiudo la transazione per il chunk
					SessionFactory sessFactory = session.getSessionFactory();

					session.clear();
					session.getTransaction().commit();
					session.close();

					// Apro una nuova transazione per il nuovo chunk
					session = sessFactory.openSession();
					session.beginTransaction();

					// Indicizzo i documenti
					SolrIndexerUtil.insertChunk(solrUrl+"?commit=true", docsArr);
					docsArr = new JsonArray();
				}

				// Log avanzamento
				if ((numInsert % 100) == 0) {
					log.info("Numero elementi elaborati: " + numInsert);
				}
			}
			catch(Exception ex) {
				log.error("Errore durante l'indicizzazione: " + key, ex);
			}
		}

		// Chiudo l'ultima transazione
		session.clear();
		session.getTransaction().commit();
		session.close();

		log.info("Importazione Mapping terminata. Totale: " + String.valueOf(numInsert));

		return aicVersion;
	}

	private void inserOrUpdateMetadata(final Session session,
			final long metaParameterId, final long csEntityVersionId,
			final String parameterValue) {

		CodeSystemMetadataValue metaVal = null;
		List<CodeSystemMetadataValue> checkList =
				session.createCriteria(CodeSystemMetadataValue.class)
				.add(Restrictions.eq("metadataParameter.id", metaParameterId))
				.add(Restrictions.eq("codeSystemEntityVersion.versionId", csEntityVersionId))
				.list();

		if ((checkList != null) && (checkList.size() > 0)) {
			metaVal = checkList.get(0);
		}

		if (metaVal == null) {
			metaVal = new CodeSystemMetadataValue();
			metaVal.setMetadataParameter((MetadataParameter)session
					.get(MetadataParameter.class, metaParameterId));

			metaVal.setCodeSystemEntityVersion((CodeSystemEntityVersion)session
					.get(CodeSystemEntityVersion.class, csEntityVersionId));
		}

		metaVal.setParameterValue(StiServiceUtil.trimStr(parameterValue));

		session.save(metaVal);
		session.flush();
		session.refresh(metaVal);
	}

}
