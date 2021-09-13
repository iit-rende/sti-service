package it.linksmt.cts2.plugin.sti.service.changeset.impl;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.ImportStandardLocal;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
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

public class NewVersionValueSetWorkflow extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(NewVersionValueSetWorkflow.class);
	
	private static String CSV_BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	private String csvPathIt;
	private String csvPathEn;
	private String vsName;
	private String vsVersionName;
	private String vsDescription;
	private String versionDescription;
	private String oid;
	private Date releaseDate;
	
	private String domain;
	private String organization;
	
	private String hasOntology;
	private String ontologyName;
	
	private LinkedHashMap<String,String> parameterTypeMap;
	private HashMap<String, String> codificationMap;

	public NewVersionValueSetWorkflow(String csvPathIt,String csvPathEn, String localName,
			String vsVersionName, String vsDescription,
			String versionDescription, String oid, Date releaseDate,
			String domain, String organization, String hasOntology,String ontologyName,
			LinkedHashMap<String, String> parameterTypeMap,
			HashMap<String, String> codificationMap) {
		super();
		this.csvPathIt = csvPathIt;
		this.csvPathEn = csvPathEn;
		this.vsName = localName;
		this.vsVersionName = vsVersionName;
		this.vsDescription = vsDescription;
		this.versionDescription = versionDescription;
		this.oid = oid;
		this.releaseDate = releaseDate;
		this.domain = domain;
		this.organization = organization;
		this.parameterTypeMap = parameterTypeMap;
		this.codificationMap = codificationMap;
		this.hasOntology = hasOntology;
		this.ontologyName = ontologyName;
		
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
			
			ImportStandardLocal.importNewVersion(hibUtil, csvDataIt, csvDataEn, vsName, vsVersionName, vsDescription, releaseDate, oid, versionDescription,
					domain, organization, CodeSystemType.VALUE_SET.getKey(), null, hasOntology,ontologyName, csvPathIt, csvPathEn, parameterTypeMap, codificationMap); 
			
			/**
			 * INDICIZZAZIONE
			 */
			updateStatusWorkflow(ImportValues.REINDEX, "");
			log.info("NewVersionValueSetWorkflow::" + ImportValues.REINDEX);
			SolrIndexerUtil.indexNewVersion(vsName, StiServiceUtil.buildCsIndexPath(ValueSetFields.VALUESET_INDEX_SUFFIX_NAME) + "/update");
			/**
			 * indicizzazione
			 */
			
			updateStatusWorkflow(ImportValues.COMPLETE, "");
			
		}catch (Exception ex) {
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
		jsonObject.addProperty("IS_LEAF", true);
		jsonObject.addProperty("IS_LAST_VERSION", true);
		jsonObject.addProperty("HAS_ASSOCIATIONS", false);
		jsonObject.addProperty("VALUESET_CODE", "VALUESET_code");
		jsonObject.addProperty("VALUESET_DESCRIPTION", "VALUESET_description");


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
