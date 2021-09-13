package it.linksmt.cts2.plugin.sti.exporter.atc_aic;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.exporter.ExportUtil;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ExportAic {

	private static Logger log = Logger.getLogger(ExportAic.class);

	private ExportAic() { }

	public static void exportNewVersion(final String csVersionName,
			final Map<String, JsonObject> aicMap) throws Exception {

		String fileName = StiServiceUtil.trimStr(AtcAicFields.AIC_CODE_SYSTEM_NAME).toUpperCase()
				+ "_" + StiServiceUtil.trimStr(csVersionName).replace(".", "_").toUpperCase();

		File jsonFile = new File(ExportController.BASE_PATH + "/"
				+ ExportController.CODE_SYSTEM, fileName + ".json");

		int count = 0;
		FileWriter wJson = null;

		try {
			wJson = new FileWriter(jsonFile, false);
			wJson.append("[\n");

			String[] keyArr = aicMap.keySet().toArray(new String[0]);
			for (String key : keyArr) {
				JsonObject entObj = aicMap.get(key);
				JsonObject expJson = new JsonObject();

				expJson.addProperty("Codice AIC", ExportUtil.getAsString(entObj, AtcAicFields.AIC_CODICE));

				ExportUtil.addJsonString(expJson, "Denominazione", ExportUtil.getAsString(entObj, AtcAicFields.AIC_DENOMINAZIONE));
				ExportUtil.addJsonString(expJson, "Confezione", ExportUtil.getAsString(entObj, AtcAicFields.AIC_CONFEZIONE));
				ExportUtil.addJsonString(expJson, "Principio Attivo", ExportUtil.getAsString(entObj, AtcAicFields.AIC_PRINCIPIO_ATTIVO));

				ExportUtil.addJsonString(expJson, "Codice ATC", ExportUtil.getAsString(entObj, "EXPORT_ATC"));

				ExportUtil.addJsonString(expJson, "Ditta", ExportUtil.getAsString(entObj, AtcAicFields.AIC_DITTA));
				ExportUtil.addJsonString(expJson, "Classe di rimborsabilità", ExportUtil.getAsString(entObj, AtcAicFields.AIC_CLASSE));
				ExportUtil.addJsonString(expJson, "Tipo di Farmaco", ExportUtil.getAsString(entObj, AtcAicFields.AIC_TIPO_FARMACO));

				ExportUtil.addJsonString(expJson, "Codice Gruppo Equivalenza", ExportUtil.getAsString(entObj, AtcAicFields.AIC_CODICE_GRUPPO_EQ));
				ExportUtil.addJsonString(expJson, "Descrizione Gruppo Equivalenza", ExportUtil.getAsString(entObj, AtcAicFields.AIC_DESCR_GRUPPO_EQ));

				ExportUtil.addJsonString(expJson, "Metri Cubi Ossigeno", ExportUtil.getAsString(entObj, AtcAicFields.AIC_METRI_CUBI_OSSIGENO));
				ExportUtil.addJsonString(expJson, "In Lista di Trasparenza Aifa", ExportUtil.getAsString(entObj, AtcAicFields.AIC_IN_LISTA_TRASPARENZA_AIFA));
				ExportUtil.addJsonString(expJson, "Solo In Lista Regione", ExportUtil.getAsString(entObj, AtcAicFields.AIC_IN_LISTA_TRASPARENZA_AIFA));
				ExportUtil.addJsonString(expJson, "Unità Posologica", ExportUtil.getAsString(entObj, AtcAicFields.AIC_UNITA_POSOLOGICA));

				ExportUtil.addJsonString(expJson, "Prezzo Per Unità Posologica", ExportUtil.getAsString(entObj, AtcAicFields.AIC_PREZZO_UNITA_POSOLOGICA));
				ExportUtil.addJsonString(expJson, "Prezzo Al Pubblico", ExportUtil.getAsString(entObj, AtcAicFields.AIC_PREZZO_AL_PUBBLICO));
				ExportUtil.addJsonString(expJson, "Prezzo Ex-factory", ExportUtil.getAsString(entObj, AtcAicFields.AIC_PREZZO_EX_FACTORY));
				ExportUtil.addJsonString(expJson, "Prezzo massimo di cessione", ExportUtil.getAsString(entObj, AtcAicFields.AIC_PREZZO_MASSIMO_CESSIONE));

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
