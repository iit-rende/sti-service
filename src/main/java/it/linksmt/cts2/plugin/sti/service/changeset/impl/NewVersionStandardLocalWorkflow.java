package it.linksmt.cts2.plugin.sti.service.changeset.impl;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.ImportStandardLocal;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

public class NewVersionStandardLocalWorkflow extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(NewVersionStandardLocalWorkflow.class);

	private static String CSV_BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	private String csvPathIt;
	private String csvPathEn;
	private String standardLocalName;
	private String csVersionName;
	private String csDescription;
	private String versionDescription;
	private String oid;
	private Date releaseDate;
	
	private String domain;
	private String organization;
	private String type;
	private String subType;
	
	private String hasOntology;
	private String ontologyName;
	
	private LinkedHashMap<String,String> parameterTypeMap;
	private HashMap<String, String> codificationMap;
 
	public NewVersionStandardLocalWorkflow(final String csvPathIt,final String csvPathEn, final String standardLocalName, final String csVersionName, final String csDescription, final String versionDescription,
			final String oid, final Date releaseDate, final String domain, final String organization,  final String type,  final String subType,
			 final String hasOntology,final String ontologyName,
			final LinkedHashMap<String,String> parameterTypeMap, HashMap<String, String> codificationMap) {

		this.csvPathIt = csvPathIt;
		this.csvPathEn = csvPathEn;
		this.standardLocalName = standardLocalName;
		this.csVersionName = csVersionName;

		this.csDescription = csDescription;
		this.versionDescription = versionDescription;
		this.oid = oid;
		this.releaseDate = releaseDate;
		
		this.domain = domain;
		this.organization = organization;
		this.type = type;
		this.subType = subType;
		this.hasOntology = hasOntology;
		this.ontologyName = ontologyName;
		
		this.parameterTypeMap = parameterTypeMap;
		this.codificationMap = codificationMap;
	}

	@Override
	public void run() {
		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {
			File csvDataIt = null;
			if(csvPathIt!=null && !"".equals(csvPathIt)){
				csvDataIt = new File(CSV_BASE_PATH + "/" + StiServiceUtil.trimStr(csvPathIt));
				if (!csvDataIt.isFile()) {
					throw new RuntimeException("ERRORE - impossibile accedere al file: " + csvDataIt.getAbsolutePath());
				}
				
			}
			
			File csvDataEn = null;
			if(csvPathEn!=null && !"".equals(csvPathEn)){
				csvDataEn = new File(CSV_BASE_PATH + "/" + StiServiceUtil.trimStr(csvPathEn));
				if (!csvDataEn.isFile()) {
					throw new RuntimeException("ERRORE - impossibile accedere al file: " + csvDataEn.getAbsolutePath());
				}
			}
			

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			
			ImportStandardLocal.importNewVersion(hibUtil, csvDataIt, csvDataEn, standardLocalName, csVersionName, csDescription, releaseDate, oid, versionDescription,
					domain, organization, type, subType, hasOntology, ontologyName, csvPathIt, csvPathEn, parameterTypeMap, codificationMap); 
			
			/******* INIZIO IMPORTAZIONE SOLR **************/

			// Reindicizza tutto il Code System
			updateStatusWorkflow(ImportValues.REINDEX, "");
			log.info("NewVersionLocalWorkflow::" + ImportValues.REINDEX);
			SolrIndexerUtil.indexNewVersion(standardLocalName, StiServiceUtil.buildCsIndexPath(StandardLocalFields.STANDARD_LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME) + "/update");

			/********* FINE IMPORTAZIONE SOLR ***********/
			
			updateStatusWorkflow(ImportValues.COMPLETE, "");

		} catch (Exception ex) {
			try {
				updateStatusWorkflow(ImportValues.ERROR, ex.getMessage());
			} catch (Exception uex) {
				log.error("Impossibile aggiornare lo stato della operazione di import LOCAL.");
			}

			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		} finally {
			NewCsVersionInfo.RUNNING = false;
		}

	}

	public static JsonObject getDocumentMock() {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("id", "4");
		jsonObject.addProperty("NAME", "m_nome");
		jsonObject.addProperty("DESCRIPTION", "m_descrizione");
		jsonObject.addProperty("VERSION", "1.1");
		jsonObject.addProperty("VERSION_NAME", "m_nome");
		jsonObject.addProperty("VERSION_DESCRIPTION", "m_descrizione");
		jsonObject.addProperty("RELEASE_DATE", formatter.format(new Date()));
		jsonObject.addProperty("CS_OID", "1");
		jsonObject.addProperty("DOMAIN", "www.test.it");
		jsonObject.addProperty("ORGANIZATION", "ACME");
		jsonObject.addProperty("CS_TYPE", "G");
		jsonObject.addProperty("CS_SUBTYPE", "G1");
		jsonObject.addProperty("IS_LEAF", true);
		jsonObject.addProperty("IS_LAST_VERSION", true);
		jsonObject.addProperty("HAS_ASSOCIATIONS", false);
		jsonObject.addProperty("LOCAL_CODE", "local_code");
		jsonObject.addProperty("LOCAL_DESCRIPTION", "local_description");

		// jsonObject.addProperty("DF_S_CAMPO1", "CAMPO DINAMICO 1");
		// jsonObject.addProperty("DF_S_CAMPO2", "CAMPO DINAMICO 2");
		//
		// jsonObject.addProperty("DF_N_CAMPO3", 10);
		// jsonObject.addProperty("DF_N_CAMPO4", 12.3);
		//
		// jsonObject.addProperty("DF_D_CAMPO5", formatter.format(new Date()));
		//
		// jsonObject.addProperty("DF_M_CAMPO6", "MAPPING-A");

		Random rn = new Random();
		int num = rn.nextInt(10) + 4;

		for (int idx = 1; idx <= num; idx++) {
			String nomeCampo = "CAMPO_" + idx;
			String tipoCampo = "";
			int tipo = rn.nextInt(4) + 1;

			switch (tipo) {
			case 1:
				tipoCampo = MetadataParameterType.STRING.getPrefix();// stringa
				nomeCampo = tipoCampo + nomeCampo;
				jsonObject.addProperty(nomeCampo, "STRINGA");
				break;

			case 2:
				tipoCampo = MetadataParameterType.DATE.getPrefix();// /data
				nomeCampo = tipoCampo + nomeCampo;
				jsonObject.addProperty(nomeCampo, formatter.format(new Date()));
				break;

			case 3:
				tipoCampo = MetadataParameterType.NUMBER.getPrefix();// numero
				nomeCampo = tipoCampo + nomeCampo;
				jsonObject.addProperty(nomeCampo, 10);
				break;

			case 4:
				tipoCampo = MetadataParameterType.MAPPING.getPrefix();// mapping
				nomeCampo = tipoCampo + nomeCampo;
				jsonObject.addProperty(nomeCampo, "MAPPING");
				break;

			default:
				break;
			}
		}
		return jsonObject;
	}
	
	public static HashMap<String, String> getParameterMapTypeMock() {
		HashMap<String,String> parameterTypeMap = new HashMap<String, String>();
		parameterTypeMap.put("versione", MetadataParameterType.STRING.getKey());
		parameterTypeMap.put("componente", MetadataParameterType.STRING.getKey());
		parameterTypeMap.put("proprieta", MetadataParameterType.STRING.getKey());
		parameterTypeMap.put("mapping", MetadataParameterType.MAPPING.getKey());
		parameterTypeMap.put("campo_numerico", MetadataParameterType.NUMBER.getKey());
		parameterTypeMap.put("campo_data", MetadataParameterType.DATE.getKey());
		return parameterTypeMap;
	}

}
