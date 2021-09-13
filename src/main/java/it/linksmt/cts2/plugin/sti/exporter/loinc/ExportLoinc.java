package it.linksmt.cts2.plugin.sti.exporter.loinc;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ExportLoinc {

	private static Logger log = Logger.getLogger(ExportLoinc.class);

	private ExportLoinc() { }

	public static void exportNewVersion(final String csVersionName,
			final Map<String, JsonObject> loincMap) throws Exception {

		String fileName = StiServiceUtil.trimStr(LoincFields.LOINC_CODE_SYSTEM_NAME).toUpperCase()
				+ "_" + StiServiceUtil.trimStr(csVersionName).replace(".", "_").toUpperCase();

		File jsonIt = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_it.json");

		File jsonEn = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_en.json");

		File mapto = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + "_mapto.json");

		FileWriter wJsonIt  = null;
		FileWriter wJsonEn  = null;
		FileWriter wJsonMap = null;

		try {
			wJsonIt  = new FileWriter(jsonIt, false);
			wJsonEn  = new FileWriter(jsonEn, false);
			wJsonMap = new FileWriter( mapto, false);

			wJsonIt.append( "[\n");
			wJsonEn.append( "[\n");
			wJsonMap.append("[\n");

			int count_it = 0;
			int count_en = 0;
			int count_map = 0;

			String[] keyArr = loincMap.keySet().toArray(new String[0]);

			for (String key : keyArr) {
				JsonObject entObj = loincMap.get(key);

				JsonObject objIt  = new JsonObject();
				JsonObject objEn  = new JsonObject();
				JsonObject objMap = new JsonObject();

				Set<Entry<String, JsonElement>> jsonData = entObj.entrySet();
				Iterator<Entry<String, JsonElement>> entIt = jsonData.iterator();

				while (entIt.hasNext()) {
					Entry<String, JsonElement> entry = entIt.next();
					String entryKey = entry.getKey();

					JsonElement value = entry.getValue();

					if ((value == null) || (value instanceof JsonNull)) {
						continue;
					}

					if (entryKey.equalsIgnoreCase(LoincFields.MAP_TO)) {
						objMap.addProperty(LoincFields.LOINC_NUM, key);
						objMap.add(LoincFields.MAP_TO, value);
					}
					else if (entryKey.equalsIgnoreCase(LoincFields.MAP_TO_COMMENT)) {
						objMap.add(LoincFields.MAP_TO_COMMENT, value);
					}
					if (entryKey.toLowerCase().endsWith("_it")) {
						objIt.add(entryKey.substring(0, entryKey.length()-3), value);
					}
					else if (entryKey.toLowerCase().endsWith("_en")) {
						objEn.add(entryKey.substring(0, entryKey.length()-3), value);
					}
					else {
						objIt.add(entryKey, value);
						objEn.add(entryKey, value);
					}
				}

				if (count_en > 0) {
					wJsonEn.append(",\n");
				}

				wJsonEn.append("\t" + objEn.toString());
				count_en++;

				if (objIt.get("COMPONENT") != null) {
					if (count_it > 0) {
						wJsonIt.append(",\n");
					}

					wJsonIt.append("\t" + objIt.toString());
					count_it++;
				}
				if (objMap.entrySet().size() > 0) {
					if (count_map > 0) {
						wJsonMap.append(",\n");
					}

					wJsonMap.append("\t" + objMap.toString());
					count_map++;
				}


				// Riversamento su file
				if ((count_en % ImportCsUtil.CHUNK_SIZE_IMPORT) == 0) {
					wJsonIt.flush();
					wJsonEn.flush();
					wJsonMap.flush();
				}
			}

			wJsonIt.append( "\n]");
			wJsonEn.append( "\n]");
			wJsonMap.append("\n]");
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
				wJsonMap.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
		}
	}
}
