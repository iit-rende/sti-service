package it.linksmt.cts2.plugin.sti.importer;

import java.util.Calendar;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.importer.loinc.ImportJsonData;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

public final class ImportLoincTest {

	private static Logger log = Logger.getLogger(ImportLoincTest.class);

	private static final String SOURCE_TABLE_URL = "jdbc:postgresql://10.0.6.16/sti_etltest";
	private static final String SOURCE_TABLE_USER = "sti_etltest";
	private static final String SOURCE_TABLE_PASS = "annalisa";


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

			Calendar effectiveDate = Calendar.getInstance();
	        effectiveDate.set(Calendar.YEAR, 2016);
	        effectiveDate.set(Calendar.MONTH, Calendar.JUNE);
	        effectiveDate.set(Calendar.DATE, 24);
	        effectiveDate.set(Calendar.HOUR, 0);
	        effectiveDate.set(Calendar.MINUTE, 0);
	        effectiveDate.set(Calendar.MILLISECOND, 0);

			ImportJsonData.importNewVersion(
					SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					hibernateUtil, "2.56", "LOINC - Importazione da CSV",
					effectiveDate.getTime(), "000222056 - TEST", 1);
		}
		catch(Exception ex) {
			log.error("Errore durante la scrittura dei dati.", ex);
		}
	}
}
