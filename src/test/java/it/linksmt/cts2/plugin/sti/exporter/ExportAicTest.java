package it.linksmt.cts2.plugin.sti.exporter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.importer.atc_aic.ImportAicData;

public class ExportAicTest {

	private static Logger log = Logger.getLogger(ExportAicTest.class);

	private static final String SOURCE_TABLE_URL = "jdbc:postgresql://localhost/sti_import";
	private static final String SOURCE_TABLE_USER = "sti_cts2";
	private static final String SOURCE_TABLE_PASS = "sti_local";

	public static void main(final String[] args) {
		// Logging configuration for Test
		BasicConfigurator.configure();

		try {
			ImportAicData.testExportVersion(SOURCE_TABLE_URL,
					SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					"16.01.2017", 33);
		}
		catch(Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}
}
