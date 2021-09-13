package it.linksmt.cts2.plugin.sti.exporter;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;

public class ExporterSearchUtil {

	private static Logger log = Logger.getLogger(ExporterSearchUtil.class);
	

	public static String getBaseUrlIndexSolr(String codesystem) throws Exception {
		String indexSolr = "sti_";
		String checkVal = StiServiceUtil.trimStr(codesystem);
		String codeSystemType = null;
		String subQuery = "";

		if (checkVal.equalsIgnoreCase(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME) || checkVal.equalsIgnoreCase(LoincFields.LOINC_CODE_SYSTEM_NAME)
				|| checkVal.equalsIgnoreCase(AtcAicFields.ATC_CODE_SYSTEM_NAME) || checkVal.equalsIgnoreCase(AtcAicFields.AIC_CODE_SYSTEM_NAME)) {
			indexSolr = indexSolr + codesystem.toLowerCase().replace("-", "");
		} else {

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			Session session = null;
			try {
				session = hibUtil.getSessionFactory().openSession();
				List<CodeSystem> csList = new GetCodeSystemByName(codesystem).execute(session);
				if (null != csList && !csList.isEmpty()) {
					CodeSystem codeSystem = csList.get(0);
					codeSystemType = codeSystem.getCodeSystemType();

					if (codeSystemType.equals(CodeSystemType.LOCAL.getKey())  || codeSystemType.equals(CodeSystemType.STANDARD_NATIONAL.getKey())) {
						indexSolr = indexSolr + StandardLocalFields.STANDARD_LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME.toLowerCase();
						subQuery = "fq=NAME:"+codesystem;
					}
					else if (codeSystemType.equals(CodeSystemType.VALUE_SET.getKey())) {
						indexSolr = indexSolr + ValueSetFields.VALUESET_INDEX_SUFFIX_NAME.toLowerCase();
						subQuery = "fq=NAME:"+codesystem;
					}
				}
			} catch (Exception e) {
				try {
					if (session != null) {
						session.clear();
					}
				} catch (Exception ex) {
					log.error("ERROR::" + e.getLocalizedMessage());
				}

				throw new Exception(e.getMessage(), e);
			} finally {

				if ((session != null) && (session.isOpen())) {
					session.close();
				}
			}
		}
		
		return StiServiceUtil.SOLR_URL +"/"+ indexSolr + "/query?indent=on&wt=json&"+subQuery+"&";
	}
	
	
	
	
	
	
	
	
	

	public static File generaFileFromJSONArray(File file, final String language, final String fileType, final JSONArray docs, boolean firstElement, boolean lastElement, List<String> listFields, String localCode) throws Exception {

		FileWriter wFile = null;
		try {
			wFile = new FileWriter(file, true);

			if (fileType.equals("csv")) {
				makeRowCsv(language, docs, file, wFile, firstElement, listFields, localCode);
			}
			if (fileType.equals("json")) {
				makeRowJson(language, docs, file, wFile, firstElement, lastElement, listFields, localCode);
			}

			wFile.flush();
		} finally {
			try {
				if (wFile != null) {
					wFile.close();
				}

			} catch (Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
		}

		return file;
	}
	
	private static void makeRowCsv(final String language, final JSONArray docs, File file, FileWriter wFile, boolean firstElement, List<String> listFields, String localCode) throws Exception {

		if (docs != null && docs.length() > 0 && docs.get(0) != null) {
			/* Intestazione del file */
			if (firstElement) {
				for (String key : listFields) {
					wFile.append(replaceField(key) + ";");
				}
				wFile.append("\n");
			}

			/* dati */
			for (Object o : docs) {
				if (o instanceof JSONObject) {
					JSONObject document = (JSONObject) o;
					for (String key : listFields) {
						if (!document.isNull(key) && !"".equals(document.get(key))) {
							wFile.append(cleanLocalCode(document.get(key).toString(),localCode) + ";");
						} else {
							wFile.append(";");
						}
					}
					wFile.append("\n");
					wFile.flush();
				}
			}
		}
	}

	private static void makeRowJson(final String language, final JSONArray docs, File file, FileWriter wFile, boolean firstElement, boolean lastElement, List<String> listFields,String localCode) throws Exception {
		if (firstElement && file.length() == 0) {
			wFile.append("[\n");
		}

		JsonObject expJson = new JsonObject();

		if (docs != null && docs.length() > 0 && docs.get(0) != null) {
			for (Object o : docs) {
				if (o instanceof JSONObject) {
					JSONObject document = (JSONObject) o;
					for (String key : listFields) {
						if (!document.isNull(key) && !"".equals(document.get(key))) {
							expJson.addProperty(replaceField(key), cleanLocalCode(document.get(key).toString(),localCode));
						} else {
							expJson.addProperty(replaceField(key), "");
						}
					}

					if (firstElement && file.length() == 0) {
						wFile.append("\t" + expJson.toString());
					} else {
						wFile.append(",\n \t" + expJson.toString());

					}
					wFile.flush();
				}
			}
		}

		if (lastElement) {
			wFile.append("\n]");
			wFile.flush();
		}
	}

	public static String makeFilters(String matchvalue, String codesystemversion) {
		if (matchvalue.equals(StiConstants.ALL_ENTITIES_FILTER)) {
			matchvalue = matchvalue.replace(StiConstants.ALL_ENTITIES_FILTER, "q=*");
		} else if (matchvalue.indexOf("q=*") == -1 && matchvalue.indexOf("q=%2B") == -1) {
			matchvalue = "q=*" + matchvalue;
		}

		if (matchvalue.indexOf("TYPE%3Achapter") != -1) {
			matchvalue = matchvalue.replace("TYPE%3Achapter", "&fq=TYPE%3Achapter");
		}

		if (codesystemversion != null && !"".equals(codesystemversion)) {
			matchvalue = matchvalue + "&fq=VERSION:%22" + codesystemversion + "%22";
		} else {
			matchvalue = matchvalue + "&fq=IS_LAST_VERSION:%22" + true + "%22";
		}
		return matchvalue;
	}

	public static String makeFileName(final String codesystemversion, final String codesystem, final String fileType, final String language) {
		String fileName = "";
		
		String codesystemversionTmp = "";
		if(codesystemversion.indexOf(StiConstants.NAME_VERSION_SEPARATOR)!=-1){
			String[] tmp = codesystemversion.split(StiConstants.NAME_VERSION_SEPARATOR);
			codesystemversionTmp = tmp[1];
		}
		else{
			codesystemversionTmp = codesystemversion;
		}
		
		if (codesystemversion != null && !"".equals(codesystemversion) && !"".equals(codesystemversionTmp)) {
			fileName = codesystem + "_" + codesystemversionTmp + "_" + language + "." + fileType;
		} else {
			fileName = codesystem + "_" + language + "." + fileType;
		}
		return fileName;
	}
	
	public static String makeMappingFileName(final String mappingName,final String fileType) {
		String mappingNameTml = mappingName;
		return StiServiceUtil.makeMappingName(mappingNameTml) + "." + fileType;
	}
	
	
	
	

	private static String replaceField(String field) {
		return field.replace("DF_S_", "").replace("DF_N_", "").replace("DF_M_", "").replace("DF_D_", "").replace("_it", "").replace("_en", "");
	}
	
	/*ripulisce i dati dell'export dei mapping loinc/codifiche-locali */
	private static String cleanLocalCode(String value,String localCode) {
		if(localCode!=null){
			value = value.replace(localCode+"_#_V#_: ", "");
		}
		return value;
	}
	
}
