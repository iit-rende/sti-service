package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportLoinc extends HibernateCommand {

	private static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	static {
		SKIP_FIELDS.add(LoincFields.LOINC_ID.trim().toLowerCase());
		SKIP_FIELDS.add(LoincFields.LOINC_NUM.trim().toLowerCase());
		SKIP_FIELDS.add(LoincFields.COMPONENT_IT.trim().toLowerCase());
		SKIP_FIELDS.add(LoincFields.COMPONENT_EN.trim().toLowerCase());
		SKIP_FIELDS.add(LoincFields.DEFINITION_DESCRIPTION.trim().toLowerCase());
	}


	private static Logger log = Logger.getLogger(ImportLoinc.class);

	private String csVersionName = null;
	private String csVersionDescription = null;

	private String oid = null;
	private Date effectiveDate = null;
	private Map<String, JsonObject> loincMap = null;

	private static int numInsert = 0;
	private static Session session = null;

	public ImportLoinc(
			final String csVersionName, final String csVersionDescription,
			final String oid, final Date effectiveDate,
			final Map<String, JsonObject> loincMap) {

		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.oid = oid;
		this.loincMap = loincMap;
		this.effectiveDate = effectiveDate;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session originalSession) throws StiAuthorizationException, StiHibernateException {

		if ((loincMap == null) || (loincMap.size() < 1)) {
			throw new StiHibernateException("La lista degli elementi da importare risulta vuota.");
		}

		CodeSystemVersion newVers = ImportCsUtil.createCsVersion(
				originalSession, LoincFields.LOINC_CODE_SYSTEM_NAME,
				csVersionName, csVersionDescription,
				oid, effectiveDate,null,null);

		numInsert = 0;
		session = originalSession;

		String[] keyArr = loincMap.keySet().toArray(new String[0]);
		for (String key : keyArr) {
			try {
				JsonObject entObj = loincMap.get(key);
				insertLoincEntity(newVers, entObj);
			}
			catch(ImportException ex) {
				throw new StiHibernateException("Errore durante la scrittura sul DB.", ex);
			}
		}

		// Aggiorno il CS
		ImportCsUtil.setCurrentCodeSystemVersion(session, newVers);

		// Chiudo l'ultima transazione
		session.clear();
		session.getTransaction().commit();
		session.close();

		log.info("Importazione LOINC terminata. Totale: " + String.valueOf(numInsert));
		return newVers;
	}

	private CodeSystemEntityVersion insertLoincEntity(
			final CodeSystemVersion csVers,
			final JsonObject loincData)
			throws StiHibernateException, StiAuthorizationException, ImportException {

		String code = StiServiceUtil.trimStr(loincData.getAsJsonPrimitive(
				LoincFields.LOINC_NUM).getAsString());

		String term = StiServiceUtil.trimStr(loincData.getAsJsonPrimitive(
				LoincFields.COMPONENT_EN).getAsString());

		String term_it = null;
		JsonElement termObj = loincData.get(LoincFields.COMPONENT_IT);
		if ( (termObj != null) && (!termObj.isJsonNull())) {
			term_it = StiServiceUtil.trimStr(loincData.getAsJsonPrimitive(
					LoincFields.COMPONENT_IT).getAsString());
		}

		String description = null;
		JsonElement descObj = loincData.get(LoincFields.DEFINITION_DESCRIPTION);
		if ( (descObj != null) && (!descObj.isJsonNull())) {
			description = StiServiceUtil.trimStr(loincData.getAsJsonPrimitive(
					LoincFields.DEFINITION_DESCRIPTION).getAsString());
		}

		// Verifico se la entry risulta inserita e sollevo una eccezione
		CodeSystemConcept existConc = new GetCodeSystemConcept(
				code, csVers.getVersionId().longValue()).execute(session);

		if (existConc != null) {
			throw new ImportException("Il codice loinc risulta già presente: " + code);
		}

		CodeSystemEntityVersion csEntityVers = ImportCsUtil.insertEntity(
				session, csVers, code, term,
				term_it, null, description, null,
				effectiveDate, true, null,
				SKIP_FIELDS, loincData, null, StiConstants.LANG_IT, StiConstants.LANG_EN);
		
		
		

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
		}

		// Log avanzamento
		if ((numInsert % 100) == 0) {
			log.info("Numero elementi elaborati: " + numInsert);
		}

		return (CodeSystemEntityVersion)session.get(CodeSystemEntityVersion.class,
				csEntityVers.getVersionId().longValue());
	}
}
