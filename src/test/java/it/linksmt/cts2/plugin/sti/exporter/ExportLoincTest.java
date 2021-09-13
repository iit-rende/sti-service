package it.linksmt.cts2.plugin.sti.exporter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.importer.loinc.ImportJsonData;

public class ExportLoincTest {

	private static Logger log = Logger.getLogger(ExportLoincTest.class);

	private static final String SOURCE_TABLE_URL = "jdbc:postgresql://localhost/sti_import";
	private static final String SOURCE_TABLE_USER = "sti_cts2";
	private static final String SOURCE_TABLE_PASS = "sti_local";

	public static void main(final String[] args) {
		// Logging configuration for Test
		BasicConfigurator.configure();

		try {
			ImportJsonData.testExportVersion(SOURCE_TABLE_URL,
					SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					"2.58", 36);
		}
		catch(Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}
}
