package it.linksmt.cts2.plugin.sti.exporter;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.importer.icd9cm.ImportOwl;

public class ExportIcd9Test {

	private static Logger log = Logger.getLogger(ExportIcd9Test.class);

	private static final String FILE_ENG = "/opt/dev/STI_CTS2/Files_DATI/ICD9-CM/2007_ita/ICD-9_v9.xml";
	private static final String FILE_ITA = "/opt/dev/STI_CTS2/Files_DATI/ICD9-CM/2007_ita/ICD9CM-ita-2007_v1.0.owl";

	public static void main(final String[] args) {
		// Logging configuration for Test
		BasicConfigurator.configure();

		File icd9cmEngOwl = new File(FILE_ENG);
		if (!icd9cmEngOwl.isFile()) {
			log.error("ERRORE - impossibile accedere al file: " + FILE_ENG);
			System.exit(1);
		}

		File icd9cmItaOwl = new File(FILE_ITA);
		if (!icd9cmItaOwl.isFile()) {
			log.error("ERRORE - impossibile accedere al file: " + FILE_ITA);
			System.exit(1);
		}

		try {
			ImportOwl.testExportVersion(icd9cmEngOwl, icd9cmItaOwl, "2007");
		}
		catch(Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}
}
