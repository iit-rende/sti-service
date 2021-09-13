package it.linksmt.cts2.plugin.sti.importer.loinc;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.JsonObject;
import com.opencsv.CSVReader;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportLocalMetadata;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.dtos.ChangelogDto;
import it.linksmt.cts2.plugin.sti.importer.ChangeLogUtil;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.IndexDocumentBuilder;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ImportLocalCsv {

	private static Logger log = Logger.getLogger(ImportLocalCsv.class);

	private ImportLocalCsv() {
	}

	public static void importLocalMapping(final HibernateUtil hibernateUtil, final String solrUrl, final String csVersion, final String localName, final File csvData) throws StiHibernateException,
			StiAuthorizationException, ImportException {

		List<CodeSystemVersion> setVers = (List<CodeSystemVersion>) hibernateUtil.executeByUser(new GetCodeSystemVersionByName(csVersion), SessionUtil.getLoggedUser());

		if ((setVers == null) || (setVers.size() != 1)) {
			throw new StiHibernateException("Impossibile leggere i dati della Versione Code System: " + csVersion);
		}

		CodeSystemVersion csVers = setVers.get(0);
		String csName = StiServiceUtil.trimStr(csVers.getCodeSystem().getName());

		if (!LoincFields.LOINC_CODE_SYSTEM_NAME.equalsIgnoreCase(csName)) {
			throw new ImportException("Il supporto delle codifiche locali è previso solo per LOINC.");
		}

		if (StiServiceUtil.isNull(localName)) {
			throw new ImportException("Specificare il nome della codifica locale.");
		}

		CSVReader readerLoc = null;

		Session session = null;
		HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

		try {
			session = hibUtil.getSessionFactory().openSession();
			session.beginTransaction();

			readerLoc = new CSVReader(new FileReader(csvData));
			log.info("Importazione Mapping Locale: " + localName + "-" + csVersion);

			String[] lineVal = null;
			SolrIndexerUtil.rollback(solrUrl);

			int numInsert = 0;
			while ((lineVal = readerLoc.readNext()) != null) {
				String locCode = StiServiceUtil.trimStr(lineVal[0]);
				String loincNum = StiServiceUtil.trimStr(lineVal[2]);
				String locDescr = StiServiceUtil.trimStr(lineVal[1]);
				String batteryCode = StiServiceUtil.trimStr(lineVal[3]);
				String batteryDescription = StiServiceUtil.trimStr(lineVal[4]);
				String locUnits = StiServiceUtil.trimStr(lineVal[5]);

				// Salto intestazioni e righe vuote
				if ((StiServiceUtil.isNull(locCode) && StiServiceUtil.isNull(loincNum)) || "CODICE LOCALE".equalsIgnoreCase(StiServiceUtil.trimStr(locCode))) {
					continue;
				}

				Map<String, String> localData = new HashMap<String, String>();
				if (!StiServiceUtil.isNull(locCode)) {
					localData.put(LoincFields.LOCAL_CODE, locCode);
				}
				if (!StiServiceUtil.isNull(locDescr)) {
					localData.put(LoincFields.LOCAL_DESCRIPTION, locDescr);
				}
				if (!StiServiceUtil.isNull(batteryCode)) {
					localData.put(LoincFields.BATTERY_CODE, batteryCode);
				}
				if (!StiServiceUtil.isNull(batteryDescription)) {
					localData.put(LoincFields.BATTERY_DESCRIPTION, batteryDescription);
				}
				if (!StiServiceUtil.isNull(locUnits)) {
					localData.put(LoincFields.LOCAL_UNITS, locUnits);
				}

				ImportLocalMetadata iCmd = new ImportLocalMetadata(csVers.getVersionId().longValue(), localName, loincNum, localData);

				CodeSystemConcept updConc = iCmd.execute(session);

				if (updConc == null) {
					log.warn("Impossibile importare il codice locale: " + locCode);
				}

				JsonObject solrDoc = IndexDocumentBuilder.createByCodeSystemFields(session, csName, updConc);

				// Viene effettuato direttamente il commit
				// per fare un aggiornamento progressivo
				SolrIndexerUtil.indexSingleDocument(solrUrl + "?commit=true", solrDoc);

				numInsert++;
			}

			log.info("Importazione Mapping terminata. Totale: " + String.valueOf(numInsert));

			
			
			ChangelogDto changelogDto = new ChangelogDto();
			changelogDto.setTitle(localName+" - LOINC("+csVersion+")");
			changelogDto.setType(ChangeLogUtil.TYPE_MAPPING);
			changelogDto.setImportedRow(numInsert);
			changelogDto.setCodeSystem(csVers.getCodeSystem().getName());
			changelogDto.setVersion(csVers.getName());
			changelogDto.setDateCreate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
			
			ChangeLogUtil.addChangeLogMapping(hibUtil, changelogDto, false);
			
			session.clear();
			session.getTransaction().commit();

			SolrIndexerUtil.optimize(solrUrl);
		} catch (Exception rex) {

			try {
				if (session != null) {
					session.clear();
				}
			} catch (Exception ex) {
			}

			if (rex instanceof ImportException) {
				throw (ImportException) rex;
			}
			if (rex instanceof StiHibernateException) {
				throw (StiHibernateException) rex;
			}
			if (rex instanceof StiAuthorizationException) {
				throw (StiAuthorizationException) rex;
			}

			throw new ImportException("Errore durante l'importazione della Codifica Locale.", rex);
		} finally {

			try {
				readerLoc.close();
			} catch (Exception ee) {
				log.error("Errore durante la chiusura del file.", ee);
			}

			if ((session != null) && (session.isOpen())) {
				session.close();
			}
		}
	}
}
