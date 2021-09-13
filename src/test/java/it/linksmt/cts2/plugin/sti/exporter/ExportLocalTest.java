package it.linksmt.cts2.plugin.sti.exporter;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.exporter.loinc.ExportLocal;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

public class ExportLocalTest {

	private static String CSV_BASE_PATH = StiAppConfig.getProperty(
			StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	private static Logger log = Logger.getLogger(ExportLocalTest.class);

	public static void main(final String[] args) {
		// Logging configuration for Test
		BasicConfigurator.configure();

		try {
			File csvData = new File(CSV_BASE_PATH + "/local/LOINC/Mapping_catalogoLocale_VS_LOINC_16012017.csv");
			if (!csvData.isFile()) {
				throw new RuntimeException("ERRORE - impossibile accedere al file: "
						+ csvData.getAbsolutePath());
			}

			ExportLocal.exportNewVersion("2.54", "Umbria", csvData);
		}
		catch(Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}
}
