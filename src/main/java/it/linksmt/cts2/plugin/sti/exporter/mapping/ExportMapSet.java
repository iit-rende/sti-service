package it.linksmt.cts2.plugin.sti.exporter.mapping;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.opencsv.CSVReader;

import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ExportMapSet {

	public static String BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_EXPORT_BASE_PATH, "");

	private static Logger log = Logger.getLogger(ExportMapSet.class);

	private ExportMapSet() { }

	public static void exportMapSetVersion(
			final String mapsetFullname, final Date releaseDate,
			final File csvData) throws Exception {

		String fileName = StiServiceUtil.trimStr(mapsetFullname).replace(".", "_").replace(" - ", "_")
				.replace("(", "").replace(")", "").replace(" ", "_").toUpperCase() + ".json";

		File jsonMap = new File(BASE_PATH + "/" + ExportController.MAPSET, fileName);

		CSVReader readerMap = null;
		FileWriter wJsonMap  = null;

		try {
//			readerMap = new CSVReader(new FileReader(csvData));
			
			// Salto l'Header
			readerMap = new CSVReader(new FileReader(csvData), ',', '\'', 1);
			wJsonMap  = new FileWriter(jsonMap, false);

			String versione = new SimpleDateFormat("dd.MM.yyyy").format(releaseDate);

			log.info("Esportazione JSON Mapping Generico: " + mapsetFullname);
			wJsonMap.append( "[\n");

			int cntLoc = 0;
			String[] lineVal = null;

        	while ((lineVal = readerMap.readNext()) != null) {
        		if(lineVal.length == 4){
	        		String nomeSis1 = StiServiceUtil.trimStr(lineVal[0]);
		        	String nomeSis2 = StiServiceUtil.trimStr(lineVal[2]);
	
	        		String codSis1 = StiServiceUtil.trimStr(lineVal[1]);
		        	String codSis2 = StiServiceUtil.trimStr(lineVal[3]);
	
	        		JsonObject objMap  = new JsonObject();
	        		objMap.addProperty("Sistema di Codifica 1", nomeSis1);
	        		objMap.addProperty("Codice Sistema di Codifica 1", codSis1);
	        		objMap.addProperty("Sistema di Codifica 2", nomeSis2);
	        		objMap.addProperty("Codice Sistema di Codifica 2", codSis2);
	        		objMap.addProperty("Versione", versione);
	
		        	if (cntLoc > 0) {
		        		wJsonMap.append(",\n");
					}
	
		        	wJsonMap.append("\t" + objMap.toString());
		        	cntLoc++;
        		}
        	}

        	wJsonMap.append( "\n]");
        	wJsonMap.flush();
		}
		finally {

			try {
				readerMap.close();
			}
			catch(Exception ee) {
				log.error("Errore durante la chiusura del file.", ee);
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
