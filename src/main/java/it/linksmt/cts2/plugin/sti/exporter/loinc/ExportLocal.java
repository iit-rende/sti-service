package it.linksmt.cts2.plugin.sti.exporter.loinc;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.opencsv.CSVReader;

import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ExportLocal {

	private static Logger log = Logger.getLogger(ExportLocal.class);

	private ExportLocal() { }

	public static void exportNewVersion(
			final String csVersion, final String localName,
			final File csvData) throws Exception {

		String fileName = StiServiceUtil.trimStr(LoincFields.LOINC_CODE_SYSTEM_NAME).toUpperCase()
				+ "_" + StiServiceUtil.trimStr(csVersion).replace(".", "_").toUpperCase()
				+ "_" + StiServiceUtil.trimStr(localName).replace(".", "_").toLowerCase();

		File jsonLoc = new File(ExportController.BASE_PATH + "/" + ExportController.LOCAL, fileName + ".json");

		CSVReader readerLoc = null;
		FileWriter wJsonLoc  = null;

		try {
			readerLoc = new CSVReader(new FileReader(csvData));
			wJsonLoc  = new FileWriter(jsonLoc, false);

			log.info("Esportazione JSON Mapping Locale: " + localName + "-" + csVersion);
			wJsonLoc.append( "[\n");

			int cntLoc = 0;
			String[] lineVal = null;

        	while ((lineVal = readerLoc.readNext()) != null) {

        		String locCode = StiServiceUtil.trimStr(lineVal[0]);
            	String loincNum = StiServiceUtil.trimStr(lineVal[2]);
            	String locDescr = StiServiceUtil.trimStr(lineVal[1]);
            	String batteryCode = StiServiceUtil.trimStr(lineVal[3]);
            	String batteryDescription = StiServiceUtil.trimStr(lineVal[4]);
            	String locUnits = StiServiceUtil.trimStr(lineVal[5]);

            	// Salto intestazioni e righe vuote
	        	if ( (StiServiceUtil.isNull(locCode) && StiServiceUtil.isNull(loincNum)) ||
	        			"CODICE LOCALE".equalsIgnoreCase(StiServiceUtil.trimStr(locCode))) {
	        		continue;
	        	}

	        	JsonObject objLoc  = new JsonObject();

	        	if (!StiServiceUtil.isNull(locCode)) {
	        		objLoc.addProperty(LoincFields.LOCAL_CODE, locCode);
	        	}
	        	if (!StiServiceUtil.isNull(locDescr)) {
	        		objLoc.addProperty(LoincFields.LOCAL_DESCRIPTION, locDescr);
	        	}

	        	objLoc.addProperty(LoincFields.LOINC_NUM, loincNum);

	        	if (!StiServiceUtil.isNull(batteryCode)) {
	        		objLoc.addProperty(LoincFields.BATTERY_CODE, batteryCode);
	        	}
	        	if (!StiServiceUtil.isNull(batteryDescription)) {
	        		objLoc.addProperty(LoincFields.BATTERY_DESCRIPTION, batteryDescription);
	        	}
	        	if (!StiServiceUtil.isNull(locUnits)) {
	        		objLoc.addProperty(LoincFields.LOCAL_UNITS, locUnits);
	        	}

	        	if (cntLoc > 0) {
	        		wJsonLoc.append(",\n");
				}

	        	wJsonLoc.append("\t" + objLoc.toString());
	        	cntLoc++;
        	}

        	wJsonLoc.append( "\n]");
        	wJsonLoc.flush();
		}
		finally {

			try {
				readerLoc.close();
			}
			catch(Exception ee) {
				log.error("Errore durante la chiusura del file.", ee);
			}

			try {
				wJsonLoc.close();
			}
			catch(Exception ce) {
				log.error("Errore nella chiusura dello stream.", ce);
			}
		}
	}
}
