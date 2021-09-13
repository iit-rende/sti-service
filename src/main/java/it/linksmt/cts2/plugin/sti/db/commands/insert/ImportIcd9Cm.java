package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.ImportOwl;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportIcd9Cm extends HibernateCommand {

	private static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	static {
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_ID.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_CODE.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_CODE_RANGE.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_DESCRIPTION_it.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_DESCRIPTION_en.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_NAME_it.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_NAME_en.trim().toLowerCase());
	}

	private static Logger log = Logger.getLogger(ImportIcd9Cm.class);

	private String csVersionName = null;
	private String csVersionDescription = null;

	private String oid = null;
	private Date effectiveDate = null;

	private Map<String, JsonObject> icd9Map = null;

	private static int numInsert = 0;
	private static Session session = null;

	public ImportIcd9Cm(
			final String csVersionName, final String csVersionDescription,
			final String oid, final Date effectiveDate,
			final Map<String, JsonObject> icd9Map) {

		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.oid = oid;
		this.icd9Map = icd9Map;
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

		if ((icd9Map == null) || (icd9Map.size() < 1)) {
			throw new StiHibernateException("La lista degli elementi da importare risulta vuota.");
		}

		CodeSystemVersion newVers = ImportCsUtil.createCsVersion(
				originalSession, Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME,
				csVersionName, csVersionDescription,
				oid, effectiveDate,null,null);

		numInsert = 0;
		session = originalSession;

		String[] keyArr = icd9Map.keySet().toArray(new String[0]);
		for (String key : keyArr) {
			try {
				JsonObject entObj = icd9Map.get(key);
				insertIcd9Entity(newVers, key, entObj);
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

		log.info("Importazione ICD-9 CM terminata. Totale: " + String.valueOf(numInsert));
		return newVers;
	}

	private CodeSystemEntityVersion insertIcd9Entity(
			final CodeSystemVersion csVers,
			final String termAbbrevation,
			final JsonObject icd9Data)
			throws StiHibernateException, StiAuthorizationException, ImportException {

		String code = ImportOwl.getCode(icd9Data);

		// Verifico se la entry risulta inserita dall'algoritmo con le superclassi
		CodeSystemConcept existConc = new GetCodeSystemConcept(
				code, csVers.getVersionId().longValue()).execute(session);

		if (existConc != null) {
			return null;
		}

		String superClassID = null;
		if (icd9Data.get(Icd9CmFields.ICD9_CM_SUBCLASS_OF) != null) {
			superClassID = StiServiceUtil.trimStr(icd9Data.getAsJsonPrimitive(
					Icd9CmFields.ICD9_CM_SUBCLASS_OF).getAsString());
		}

		// Verifico se esiste la superclasse
		CodeSystemConcept superClass = null;
		if (!StiServiceUtil.isNull(superClassID)) {
			JsonObject superClassData = icd9Map.get(superClassID);
			if (superClassData == null) {
				throw new StiHibernateException("Impossibile leggere i dati della "
						+ "superclasse: " + superClassID);
			}

			String checkCode = ImportOwl.getCode(superClassData);
			superClass = new GetCodeSystemConcept(
					checkCode, csVers.getVersionId().longValue()).execute(session);

			if (superClass == null) {
				insertIcd9Entity(csVers, superClassID, superClassData);

				superClass = new GetCodeSystemConcept(checkCode,
						csVers.getVersionId().longValue()).execute(session);
			}
		}

		// Nuova Entity
		String term = null;
		String term_it = null;
		if (icd9Data.get(Icd9CmFields.ICD9_CM_NAME_en) != null) {
			term = StiServiceUtil.trimStr(icd9Data.getAsJsonPrimitive(
					Icd9CmFields.ICD9_CM_NAME_en).getAsString());
		}

		if (icd9Data.get(Icd9CmFields.ICD9_CM_NAME_it) != null) {
			term_it = StiServiceUtil.trimStr(icd9Data.getAsJsonPrimitive(
					Icd9CmFields.ICD9_CM_NAME_it).getAsString());
		}

		// Fix per qualche termine mancante
		if ( StiServiceUtil.isNull(term) && (!StiServiceUtil.isNull(term_it))) {
			term = new String(term_it);
		}

		String description = null;
		String description_it = null;

		JsonArray arrDescr = null;
		JsonArray arrDescr_it = null;

		if (icd9Data.getAsJsonArray(Icd9CmFields.ICD9_CM_DESCRIPTION_en) != null) {
			arrDescr = icd9Data.getAsJsonArray(Icd9CmFields.ICD9_CM_DESCRIPTION_en);
		}
		if (icd9Data.getAsJsonArray(Icd9CmFields.ICD9_CM_DESCRIPTION_it) != null) {
			arrDescr_it = icd9Data.getAsJsonArray(Icd9CmFields.ICD9_CM_DESCRIPTION_it);
		}

		if (arrDescr != null) {
			if (arrDescr.size() > 1) {
				log.warn("Il seguente codice ICD-9 CM possiede descrizioni multiple (EN): " + code);
			}

			for (int i = 0; i < arrDescr.size(); i++) {
				if (i > 0) {
					description += "; ";
				}
				description = StiServiceUtil.trimStr(
						arrDescr.get(i).getAsString());
			}
		}
		if (arrDescr_it != null) {
			if (arrDescr_it.size() > 1) {
				log.warn("Il seguente codice ICD-9 CM possiede descrizioni multiple (IT): " + code);
			}

			for (int i = 0; i < arrDescr_it.size(); i++) {
				if (i > 0) {
					description_it += "; ";
				}
				description_it = StiServiceUtil.trimStr(
						arrDescr_it.get(i).getAsString());
			}
		}

		// Fix per qualche termine mancante
		if (StiServiceUtil.isNull(description) && (!StiServiceUtil.isNull(description_it))) {
			description = new String(description_it);
		}

		if (StiServiceUtil.isNull(term) && (!StiServiceUtil.isNull(description))) {
			term = new String(description);
			description = "";
		}

		if (StiServiceUtil.isNull(term_it) && (!StiServiceUtil.isNull(description_it))) {
			term_it = new String(description_it);
			description_it = "";
		}

		CodeSystemEntityVersion csEntityVers = ImportCsUtil.insertEntity(
				session, csVers, code, term,
				term_it, termAbbrevation,
				description, description_it,
				effectiveDate, true,
				superClass,
				SKIP_FIELDS, icd9Data, null, "EN", "IT");

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
