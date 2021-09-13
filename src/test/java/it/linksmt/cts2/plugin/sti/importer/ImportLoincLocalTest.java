package it.linksmt.cts2.plugin.sti.importer;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.importer.loinc.ImportLocalCsv;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

public final class ImportLoincLocalTest {

	private static Logger log = Logger.getLogger(ImportLoincLocalTest.class);

	private static final String SOLR_URL = "http://localhost:8983/solr/sti_loinc/update";
	private static final String FILE_LOCAL = "/opt/dev/STI_CTS2/Files_DATI/LOINC_DATA/Mapping_catalogoLocale_VS_LOINC_16012017.csv";

	private static HibernateUtil hibernateUtil;
	static {
		try {
			hibernateUtil = HibernateUtil.create(
					StiAppConfig.getProperty(StiServiceConfiguration.DB_STI_SERVER_ADDRESS),
					StiAppConfig.getProperty(StiServiceConfiguration.DB_STI_USERNAME),
					StiAppConfig.getProperty(StiServiceConfiguration.DB_STI_PASSWORD),
					StiServiceConfiguration.HIBERNANTE_CONFIGURATION_RESOURCE, false);
		}
		catch (StiHibernateException e) {
			log.error("Errore di accesso al database!!!", e);
		}
	}

	public static void main(final String[] args) {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);
		// LogManager.getLogger("").setLevel(Level.WARN);

		try {
			ImportLocalCsv.importLocalMapping(
					hibernateUtil, SOLR_URL,
					"2.58", "Umbria", new File(FILE_LOCAL));
		}
		catch(Exception ex) {
			log.error("Errore durante la scrittura dei dati.", ex);
		}

	}

}
