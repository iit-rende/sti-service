package it.linksmt.cts2.plugin.sti.exporter;

import java.io.File;
import java.util.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.exporter.mapping.ExportMapSet;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

public class ExportMapSetTest {

	private static String CSV_BASE_PATH = StiAppConfig.getProperty(
			StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	private static Logger log = Logger.getLogger(ExportMapSetTest.class);

	public static void main(final String[] args) {

		// Logging configuration for Test
		BasicConfigurator.configure();

		try {
			File csvData = new File(CSV_BASE_PATH + "/LOINC_2_54_ATC_2014.csv");
			if (!csvData.isFile()) {
				throw new RuntimeException("ERRORE - impossibile accedere al file: "
						+ csvData.getAbsolutePath());
			}

			ExportMapSet.exportMapSetVersion("LOINC (2.54) - ATC (2014)", new Date(), csvData);
		}
		catch(Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}
}
