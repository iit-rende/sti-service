package it.linksmt.cts2.plugin.sti.service.changeset.impl;

import java.io.File;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.exporter.ExportUtil;
import it.linksmt.cts2.plugin.sti.exporter.loinc.ExportLocal;
import it.linksmt.cts2.plugin.sti.importer.ChangeLogUtil;
import it.linksmt.cts2.plugin.sti.importer.loinc.ImportLocalCsv;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ImportLocalMapping extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(ImportLocalMapping.class);

	private static String CSV_BASE_PATH = StiAppConfig.getProperty(
			StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	private String csvPath;
	private String localName;
	private String csVersionName;

	public ImportLocalMapping(final String csvPath,
			final String localName, final String csVersionName) {

		this.csvPath = csvPath;
		this.localName = localName;
		this.csVersionName = csVersionName;
	}

	@Override
	public void run() {
		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {

			File csvData = new File(CSV_BASE_PATH + "/" + StiServiceUtil.trimStr(csvPath));
			if (!csvData.isFile()) {
				throw new RuntimeException("ERRORE - impossibile accedere al file: " + csvData.getAbsolutePath());
			}

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			String solrUrl = StiServiceUtil.buildCsIndexPath(LoincFields.LOINC_CODE_SYSTEM_NAME) + "/update";

			ImportLocalCsv.importLocalMapping(StiServiceProvider.getHibernateUtil(), solrUrl, csVersionName, localName, csvData);
			
			// Esporta in formato JSON
			ExportLocal.exportNewVersion(csVersionName, localName, csvData);

			// Copia il file in formato CSV per l'esportazione
			File mapset_csv = ExportUtil.getFileExport(ExportController.LOCAL, LoincFields.LOINC_CODE_SYSTEM_NAME, csVersionName, localName, ".csv");

			Files.move(csvData, mapset_csv);
		}
		catch(Exception ex) {
			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		}
		finally {

			// In questo caso il fallimento dell'import non è bloccante per le altre operazioni
			try {
				updateStatusWorkflow(ImportValues.COMPLETE, "");
			}
			catch(Exception ex) {
				log.error("Impossibile aggiornare lo stato della importazione locale");
			}

			NewCsVersionInfo.RUNNING = false;
		}
	}

}
