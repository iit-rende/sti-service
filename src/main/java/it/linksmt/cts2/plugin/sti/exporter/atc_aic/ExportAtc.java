package it.linksmt.cts2.plugin.sti.exporter.atc_aic;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ExportAtc {

	private static Logger log = Logger.getLogger(ExportAtc.class);

	private ExportAtc() { }
	
	private static List<String> codiceCapitoli = Arrays.asList(new String []{"A","B","C","D","G","H","J","L","M","N","P","R","S","V"});
	
	
	public static void exportNewVersion(final String csVersionName, final Map<String, JsonObject> atcMap) throws Exception {

		String fileName = StiServiceUtil.trimStr(AtcAicFields.ATC_CODE_SYSTEM_NAME).toUpperCase() + "_" + StiServiceUtil.trimStr(csVersionName).replace(".", "_").toUpperCase();

		File jsonFile = new File(ExportController.BASE_PATH + "/" + ExportController.CODE_SYSTEM, fileName + ".json");

		Map<String,String> capitoli = new HashMap<String, String>();
		String[] kArr = atcMap.keySet().toArray(new String[0]);
		for (String key : kArr) {
			JsonObject entObj = atcMap.get(key);
			String codice = entObj.get(AtcAicFields.ATC_CODICE).getAsString();
			if(codiceCapitoli.contains(codice)){
				capitoli.put(key, entObj.get(AtcAicFields.ATC_DENOMINAZIONE).getAsString());
			}
		}
		
		
		int count = 0;
		FileWriter wJson = null;

		try {
			wJson = new FileWriter(jsonFile, false);
			wJson.append("[\n");

			String[] keyArr = atcMap.keySet().toArray(new String[0]);
			for (String key : keyArr) {
				JsonObject entObj = atcMap.get(key);
				JsonObject expJson = new JsonObject();
				

				//expJson.addProperty(AtcAicFields.ATC_CODICE,entObj.get(AtcAicFields.ATC_CODICE).getAsString());
				//expJson.addProperty(AtcAicFields.ATC_DENOMINAZIONE,entObj.get(AtcAicFields.ATC_DENOMINAZIONE).getAsString());
				//expJson.addProperty(AtcAicFields.ATC_GRUPPO_ANATOMICO,entObj.get(AtcAicFields.ATC_GRUPPO_ANATOMICO).getAsString());
				
				
				String codice = entObj.get(AtcAicFields.ATC_CODICE).getAsString();
				String denominazione = entObj.get(AtcAicFields.ATC_DENOMINAZIONE).getAsString();
				String gruppoAnatomico = "";
				if(codice.length()>1){
					String radice = codice.substring(0, 1);
					if(capitoli.get(radice)!=null){
						gruppoAnatomico = capitoli.get(radice);
					}
					
				}
				else{
					if(capitoli.get(codice)!=null){
						gruppoAnatomico = capitoli.get(codice);
					}
				}
				
				expJson.addProperty(AtcAicFields.ATC_CODICE,codice);
				expJson.addProperty(AtcAicFields.ATC_DENOMINAZIONE,denominazione);
				expJson.addProperty(AtcAicFields.ATC_GRUPPO_ANATOMICO,gruppoAnatomico);

				if (count > 0) {
					wJson.append(",\n");
				}
				wJson.append("\t" + expJson.toString());

				// Riversamento su file
				count++;
				if ((count % ImportCsUtil.CHUNK_SIZE_IMPORT) == 0) {
					wJson.flush();
				}
			}

			wJson.append("\n]");
		}
		finally {
			try {
				wJson.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
		}
	}
}
