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

public class ImportAic extends HibernateCommand {

	private static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	static {
		SKIP_FIELDS.add(AtcAicFields.AIC_CODICE.trim().toLowerCase());
		SKIP_FIELDS.add(AtcAicFields.AIC_DENOMINAZIONE.trim().toLowerCase());
		SKIP_FIELDS.add("EXPORT_ATC".trim().toLowerCase());
	}

	private String csVersionName = null;
	private String csVersionDescription = null;

	private String oid = null;
	private Date effectiveDate = null;

	private Map<String, JsonObject> aicMap = null;

	private static int numInsert = 0;
	private static Session session = null;

	private static Logger log = Logger.getLogger(ImportAic.class);

	public ImportAic(final String csVersionName, final String csVersionDescription,
			final String oid, final Date effectiveDate,
			final Map<String, JsonObject> aicMap) {

		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.oid = oid;
		this.effectiveDate = effectiveDate;
		this.aicMap = aicMap;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session originalSession) throws StiAuthorizationException, StiHibernateException {
		if ((aicMap == null) || (aicMap.size() < 1)) {
			throw new StiHibernateException("La lista degli elementi da importare risulta vuota.");
		}

		CodeSystemVersion newVers = ImportCsUtil.createCsVersion(
				originalSession, AtcAicFields.AIC_CODE_SYSTEM_NAME,
				csVersionName, csVersionDescription,
				oid, effectiveDate,null,null);

		numInsert = 0;
		session = originalSession;

		String[] keyArr = aicMap.keySet().toArray(new String[0]);
		for (String key : keyArr) {
			try {
				JsonObject entObj = aicMap.get(key);
				insertAicEntity(newVers, entObj);
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

		log.info("Importazione AIC terminata. Totale: " + String.valueOf(numInsert));
		return newVers;
	}

	private CodeSystemEntityVersion insertAicEntity(
			final CodeSystemVersion csVers,
			final JsonObject aicData)
			throws StiHibernateException, StiAuthorizationException, ImportException {

		String code = StiServiceUtil.trimStr(aicData.getAsJsonPrimitive(
				AtcAicFields.AIC_CODICE).getAsString());

		// Verifico se la entry risulta inserita e sollevo una eccezione
		CodeSystemConcept existConc = new GetCodeSystemConcept(
				code, csVers.getVersionId().longValue()).execute(session);

		if (existConc != null) {
			log.warn("Il codice AIC risulta già presente: " + code);
			return null;
		}

		String term = StiServiceUtil.trimStr(aicData.getAsJsonPrimitive(
				AtcAicFields.AIC_DENOMINAZIONE).getAsString());

		CodeSystemEntityVersion csEntityVers = ImportCsUtil.insertEntity(
				session, csVers, code, term, null, null, null, null,
				effectiveDate, true, null, SKIP_FIELDS, aicData, null, null, null);

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
