package it.linksmt.cts2.plugin.sti.exporter.icd9cm;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.exporter.ExportUtil;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ExportIcd9Map {

	public static final String EXPORT_CSV_SEP = " | ";

	private static Logger log = Logger.getLogger(ExportIcd9Map.class);

	private ExportIcd9Map() { }

	public static void exportNewVersion(final String csVersionName,
			final Map<String, JsonObject> icd9Map) throws Exception {

		String fileName = StiServiceUtil.trimStr(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME).toUpperCase()
				+ "_" + StiServiceUtil.trimStr(csVersionName).replace(".", "_").toUpperCase();

		File jsonIt = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_it.json");

		File jsonEn = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_en.json");

		File csvIt = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_it.csv");

		File csvEn = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_en.csv");

		FileWriter wJsonIt = null;
		FileWriter wJsonEn = null;

		FileWriter wCsvIt = null;
		FileWriter wCsvEn = null;

		CSVWriter csvWriterIt = null;
		CSVWriter csvWriterEn = null;

		try {
			wJsonIt = new FileWriter(jsonIt, false);
			wJsonEn = new FileWriter(jsonEn, false);

			wJsonIt.append("[\n");
			wJsonEn.append("[\n");

			wCsvIt = new FileWriter(csvIt, false);
			wCsvEn = new FileWriter(csvEn, false);

			csvWriterIt = new CSVWriter(wCsvIt);
			csvWriterEn = new CSVWriter(wCsvEn);

			String[] headerIt = { "CODICE", "CODE_RANGE", "TIPO", "NOME", "DESCRIZIONE", "SUBCLASS_OF", "ALTRE_DESCRIZIONI", "INCLUSIONI", "ESCLUSIONI", "CODIFY_FIRST", "USE_ADD_CODE" };
			csvWriterIt.writeNext(headerIt);

			String[] headerEn = { "CODE", "CODE_RANGE", "TYPE", "NAME", "DESCRIPTION", "SUBCLASS_OF", "OTHER_DESCRIPTION", "INCLUDE", "EXCLUDE", "CODIFY_FIRST", "USE_ADD_CODE" };
			csvWriterEn.writeNext(headerEn);

			int count = 0;
			String[] keyArr = icd9Map.keySet().toArray(new String[0]);

			for (String key : keyArr) {
				JsonObject entObj = icd9Map.get(key);

				JsonObject objIt = new JsonObject();
				JsonObject objEn = new JsonObject();

				// Codice o Code Range
				String icd9Code = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_CODE);
				String icd9CodeRange = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_CODE_RANGE);

				String superclassId = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_SUBCLASS_OF);

				// Tipo
				String type = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_TYPE);

				// Nome e Descrizione
				String name_it = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_NAME_it);
				String name_en = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_NAME_en);

				String description_it = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_DESCRIPTION_it, EXPORT_CSV_SEP);
				String description_en = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_DESCRIPTION_en, EXPORT_CSV_SEP);

				// Altri campi
				String codify_it = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_CODIFY_FIRST_it);
				String codify_en = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_CODIFY_FIRST_en);

				String use_add_it = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_USE_ADD_CODE_it);
				String use_add_en = ExportUtil.getAsString(entObj, Icd9CmFields.ICD9_CM_USE_ADD_CODE_en);


				// JSON IT
				if (!StiServiceUtil.isNull(icd9CodeRange)) {
					objIt.addProperty("CODE_RANGE", icd9CodeRange);
				}
				else {
					objIt.addProperty("CODICE", icd9Code);
				}

				ExportUtil.addJsonString(objIt, "SUBCLASS_OF", superclassId);
				ExportUtil.addJsonString(objIt, "TIPO", type);
				ExportUtil.addJsonString(objIt, "NOME", name_it);
				ExportUtil.addJsonString(objIt, "DESCRIZIONE", description_it);

				if ( (entObj.get(Icd9CmFields.ICD9_CM_OTHER_DESCR_it) != null ) &&
					 (entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_OTHER_DESCR_it).size() > 0)) {
					objIt.add("ALTRE_DESCRIZIONI", entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_OTHER_DESCR_it));
				}
				if ( (entObj.get(Icd9CmFields.ICD9_CM_INCLUDE_it) != null ) &&
					 (entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_INCLUDE_it).size() > 0)) {
					objIt.add("INCLUSIONI", entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_INCLUDE_it));
				}
				if ( (entObj.get(Icd9CmFields.ICD9_CM_ESCLUDE_it) != null ) &&
					 (entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_ESCLUDE_it).size() > 0)) {
					objIt.add("ESCLUSIONI", entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_ESCLUDE_it));
				}

				ExportUtil.addJsonString(objIt, "CODIFY_FIRST", codify_it);
				ExportUtil.addJsonString(objIt, "USE_ADD_CODE", use_add_it);

				if (count > 0) {
					wJsonIt.append(",\n");
				}
				wJsonIt.append("\t" + objIt.toString());

				// JSON EN
				if (!StiServiceUtil.isNull(icd9CodeRange)) {
					objEn.addProperty("CODE_RANGE", icd9CodeRange);
				}
				else {
					objEn.addProperty("CODE", icd9Code);
				}

				ExportUtil.addJsonString(objEn, "SUBCLASS_OF", superclassId);
				ExportUtil.addJsonString(objEn, "TYPE", type);
				ExportUtil.addJsonString(objEn, "NAME", name_en);
				ExportUtil.addJsonString(objEn, "DESCRIPTION", description_en);

				if ( (entObj.get(Icd9CmFields.ICD9_CM_OTHER_DESCR_en) != null ) &&
					 (entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_OTHER_DESCR_en).size() > 0)) {
					objEn.add("OTHER_DESCRIPTION", entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_OTHER_DESCR_en));
				}
				if ( (entObj.get(Icd9CmFields.ICD9_CM_INCLUDE_en) != null ) &&
					 (entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_INCLUDE_en).size() > 0)) {
					objEn.add("INCLUDE", entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_INCLUDE_en));
				}
				if ( (entObj.get(Icd9CmFields.ICD9_CM_ESCLUDE_en) != null ) &&
					 (entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_ESCLUDE_en).size() > 0)) {
					objEn.add("EXCLUDE", entObj.getAsJsonArray(Icd9CmFields.ICD9_CM_ESCLUDE_en));
				}

				ExportUtil.addJsonString(objEn, "CODIFY_FIRST", codify_en);
				ExportUtil.addJsonString(objEn, "USE_ADD_CODE", use_add_en);

				if (count > 0) {
					wJsonEn.append(",\n");
				}
				wJsonEn.append("\t" + objEn.toString());

				// CSV IT
				String other_description_it = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_OTHER_DESCR_it, EXPORT_CSV_SEP);
				String include_it = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_INCLUDE_it, EXPORT_CSV_SEP);
				String exclude_it = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_ESCLUDE_it, EXPORT_CSV_SEP);

				String[] recordIt = { icd9Code, icd9CodeRange, type,
						name_it, description_it, superclassId, other_description_it,
						include_it, exclude_it, codify_it, use_add_it };
				csvWriterIt.writeNext(recordIt);

				// CSV EN
				String other_description_en = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_OTHER_DESCR_en, EXPORT_CSV_SEP);
				String include_en = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_INCLUDE_en, EXPORT_CSV_SEP);
				String exclude_en = ExportUtil.jsonArrayToString(
						entObj, Icd9CmFields.ICD9_CM_ESCLUDE_en, EXPORT_CSV_SEP);

				String[] recordEn = { icd9Code, icd9CodeRange, type,
						name_en, description_en, superclassId, other_description_en,
						include_en, exclude_en, codify_en, use_add_en };
				csvWriterEn.writeNext(recordEn);

				// Riversamento su file
				count++;
				if ((count % ImportCsUtil.CHUNK_SIZE_IMPORT) == 0) {
					wJsonIt.flush();
					wJsonEn.flush();

					csvWriterIt.flush();
					csvWriterEn.flush();
				}
			}

			wJsonIt.append("\n]");
			wJsonEn.append("\n]");
		}
		finally {
			try {
				wJsonIt.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
			try {
				wJsonEn.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
			try {
				csvWriterIt.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
			try {
				csvWriterEn.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
		}
	}
}
