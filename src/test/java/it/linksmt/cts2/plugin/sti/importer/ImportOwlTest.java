package it.linksmt.cts2.plugin.sti.importer;

import java.io.File;
import java.util.Calendar;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.ImportOwl;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

public final class ImportOwlTest {

	private static Logger log = Logger.getLogger(ImportOwlTest.class);

	private static final String FILE_ENG = "/opt/dev/STI_CTS2/Files_DATI/ICD9-CM/2007_ita/ICD-9_v9.xml";
	private static final String FILE_ITA = "/opt/dev/STI_CTS2/Files_DATI/ICD9-CM/2007_ita/ICD9CM-ita-2007_v1.0.owl";

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

			Calendar effectiveDate = Calendar.getInstance();
	        effectiveDate.set(Calendar.YEAR, 2009);
	        effectiveDate.set(Calendar.MONTH, Calendar.JANUARY);
	        effectiveDate.set(Calendar.DATE, 1);
	        effectiveDate.set(Calendar.HOUR, 0);
	        effectiveDate.set(Calendar.MINUTE, 0);
	        effectiveDate.set(Calendar.MILLISECOND, 0);

			ImportOwl.importNewVersion(
					hibernateUtil, icd9cmEngOwl, icd9cmItaOwl,
					 "2007_ita", "ICD-9 CM Code System, importato da Ontologia.",
					effectiveDate.getTime(), "000111000 - TEST");
		}
		catch(Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}
}
