package it.linksmt.cts2.plugin.sti.importer;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.ImportMappingData;
import it.linksmt.cts2.plugin.sti.importer.mapset.ImportAutomaticMapSet;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ImportMappingTest {

	private static Logger log = Logger.getLogger(ImportMappingTest.class);

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
		
//		testMapping();
		
		
		
		
		
		//FIXME CREARE ASSOCIAZIONE /VEDI IN 
		//ImportMapSetCsv.importNewVersion(fromVersionName, toVersionName, releaseDate, csvData);
		//VEDI ANCHE ImportMappingAicAtc RIGA 157 PIù PRECISAMENTE QUESTO PEZZO DI CODICE
		
		
		
		/*********************** INPUT **********************/
		/*codesystem sorgente*/
		String srcCsName = "TestMatteo";
//		String srcCsName = "LOINC";  	//solo per test
//		String srcCsName = "ATC"; 	 	//solo per test
//		String srcCsName = "AIC"; 		//solo per test
//		String srcCsName = "ICD9-CM"; 	//solo per test
//
		/*codesystem  target*/
//		String trgCsName = "TestMatteo";
		String trgCsName = "LOINC"; 
//		String trgCsName = "ATC"; 
//		String trgCsName = "AIC"; 
//		String trgCsName = "ICD9-CM"; 
		
		
//		String srcVersionName = "2.58";
//		String trgVersionName = "2007"; 
		
		/*valore del campo mapping i-esimo*/
		String srcValElem = StiServiceUtil.trimStr("VALORE CODICE 0 0"); //<- recuperato dal campo LOCAL_CODE	
//		String srcValElem = StiServiceUtil.trimStr("10000-8"); 			//<-  solo per test
//		String srcValElem = StiServiceUtil.trimStr("B02BD04"); 			//<-  solo per test
//		String srcValElem = StiServiceUtil.trimStr("06035063"); 		//<-  solo per test
//		String srcValElem = StiServiceUtil.trimStr("001-139"); 			//<-  solo per test
		
		
		
		/*valore elemento target*/
//		String trgValElem = StiServiceUtil.trimStr("VALORE CODICE 0 0"); //<- recuperato dal campo LOCAL_CODE	
		String trgValElem = StiServiceUtil.trimStr("10000-8"); 			//<- recuperato dal campo dianmico MAPPING per LOINC
//		String trgValElem = StiServiceUtil.trimStr("B02BD04"); 			//<- recuperato dal campo dianmico MAPPING per ATC
//		String trgValElem = StiServiceUtil.trimStr("06035063"); 		//<- recuperato dal campo dianmico MAPPING per AIC
//		String trgValElem = StiServiceUtil.trimStr("001-139"); 			//<- recuperato dal campo dianmico MAPPING per ICD9-CM
//		String trgValElem = StiServiceUtil.trimStr("290-319"); 			//<- recuperato dal campo dianmico MAPPING per ICD9-CM
		
		ImportAutomaticMapSet.addMapping(srcCsName,trgCsName,srcValElem,trgValElem);
		/************************************************************/
		
		
		
	}
	
	
//	private static void testMapping(){
//		try {
//
//		ImportMappingData.importMapping( SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS, hibernateUtil, "16.01.2017", "2014", SOLR_URL, 41);
//		}
//		catch(Exception ex) {
//			log.error("Errore durante la scrittura dei dati.", ex);
//		}
//	}
	
}
