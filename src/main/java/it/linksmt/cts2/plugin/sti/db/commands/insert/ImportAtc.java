package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportAtc extends HibernateCommand {

	private static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	static {
		SKIP_FIELDS.add(AtcAicFields.ATC_CODICE.trim().toLowerCase());
		SKIP_FIELDS.add(AtcAicFields.ATC_DENOMINAZIONE.trim().toLowerCase());
	}

	private static Logger log = Logger.getLogger(ImportAtc.class);

	private String csVersionName = null;
	private String csVersionDescription = null;

	private String oid = null;
	private Date effectiveDate = null;

	private Map<String, JsonObject> atcMap = null;

	private static int numInsert = 0;
	private static Session session = null;

	public ImportAtc(final String csVersionName, final String csVersionDescription,
			final String oid, final Date effectiveDate,
			final Map<String, JsonObject> atcMap) {

		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.oid = oid;
		this.effectiveDate = effectiveDate;
		this.atcMap = atcMap;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session originalSession) throws StiAuthorizationException, StiHibernateException {
		if ((atcMap == null) || (atcMap.size() < 1)) {
			throw new StiHibernateException("La lista degli elementi da importare risulta vuota.");
		}

		CodeSystemVersion newVers = ImportCsUtil.createCsVersion(
				originalSession, AtcAicFields.ATC_CODE_SYSTEM_NAME,
				csVersionName, csVersionDescription,
				oid, effectiveDate,null,null);

		numInsert = 0;
		session = originalSession;

		String[] keyArr = atcMap.keySet().toArray(new String[0]);
		for (String key : keyArr) {
			try {
				JsonObject entObj = atcMap.get(key);
				insertAtcEntity(newVers, entObj);
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

		log.info("Importazione ATC terminata. Totale: " + String.valueOf(numInsert));
		return newVers;
	}

	private CodeSystemEntityVersion insertAtcEntity(
			final CodeSystemVersion csVers,
			final JsonObject atcData)
			throws StiHibernateException, StiAuthorizationException, ImportException {

		String code = StiServiceUtil.trimStr(atcData.getAsJsonPrimitive(
				AtcAicFields.ATC_CODICE).getAsString());

		// Verifico se la entry risulta inserita dall'algoritmo con le superclassi
		CodeSystemConcept existConc = new GetCodeSystemConcept(
				code, csVers.getVersionId().longValue()).execute(session);

		if (existConc != null) {
			return null;
		}

		String superClassID = null;
		if (atcData.get(AtcAicFields.ATC_SUBCLASS_OF) != null) {
			superClassID = StiServiceUtil.trimStr(atcData.getAsJsonPrimitive(
					AtcAicFields.ATC_SUBCLASS_OF).getAsString());
		}

		// Verifico se esiste la superclasse
		CodeSystemConcept superClass = null;
		if (!StiServiceUtil.isNull(superClassID)) {
			JsonObject superClassData = atcMap.get(superClassID);
			if (superClassData == null) {
				throw new StiHibernateException("Impossibile leggere i dati della "
						+ "superclasse: " + superClassID);
			}

			superClass = new GetCodeSystemConcept(superClassID,
					csVers.getVersionId().longValue()).execute(session);

			if (superClass == null) {
				insertAtcEntity(csVers, superClassData);

				superClass = new GetCodeSystemConcept(superClassID,
						csVers.getVersionId().longValue()).execute(session);
			}
		}

		String term = StiServiceUtil.trimStr(atcData.getAsJsonPrimitive(
				AtcAicFields.ATC_DENOMINAZIONE).getAsString());

		CodeSystemEntityVersion csEntityVers = ImportCsUtil.insertEntity(
				session, csVers, code, term, null, null, null, null,
				effectiveDate, (code.length() > 6),
				superClass, SKIP_FIELDS, atcData, null, null, null);

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
