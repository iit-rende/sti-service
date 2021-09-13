package it.linksmt.cts2.plugin.sti.service.changeset.impl;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.exporter.ExportController;
import it.linksmt.cts2.plugin.sti.exporter.ExportUtil;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.importer.loinc.ImportJsonData;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;
import it.linksmt.cts2.plugin.sti.transformer.KitchenLauncher;
import it.linksmt.cts2.plugin.sti.transformer.KitchenModel;
import it.linksmt.cts2.plugin.sti.transformer.KitchenParameter;

public class NewVersionLoincWorkflow extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(NewVersionLoincWorkflow.class);

	private String csVersionName;
	private String csVersionDescription;
	private Date effectiveDate;
	private String oid;
	private String enFile;
	private String itFile;
	private String mapToFile;

	private static String BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");
	private static String POPOLA_LOINC = StiAppConfig.getProperty(StiServiceConfiguration.KITCHEN_JOB_LOINC, "");

	public NewVersionLoincWorkflow(
			final String csVersionName, final String csVersionDescription,
			final Date effectiveDate, final String oid, final String enFile, final String itFile, final String mapToFile) {

		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.effectiveDate = effectiveDate;
		this.oid = oid;
		this.enFile = enFile;
		this.itFile = itFile;
		this.mapToFile = mapToFile;
	}

	@Override
	public void run() {

		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {

			String enableEtl = StiServiceUtil.trimStr(StiAppConfig.getProperty(
					StiServiceConfiguration.ETL_EXECUTION_LOINC_ENABLE, "")).toLowerCase();

			if ("true".equals(enableEtl) || "yes".equals(enableEtl) || "si".equals(enableEtl)) {

				updateStatusWorkflow(ImportValues.ETL_LOADING, "");

				// Avviare ETL per LOINC
				KitchenModel kitchenModel = new KitchenModel();
				kitchenModel.setFile(POPOLA_LOINC);


				//EN file normalizzato
				String enFileNormalizzatoDirectory = BASE_PATH + "/LOINC_" + csVersionName + "/normalizzati/";
				String enFileNormalizzato = enFileNormalizzatoDirectory + "normalizzato_en.csv";
				File enFileNormalizzatoDirectoryFile = new File(enFileNormalizzatoDirectory);
				enFileNormalizzatoDirectoryFile.mkdirs();

				//IT file normalizzato
				String itFileNormalizzatoDirectory = BASE_PATH + "/LOINC_" + csVersionName + "/normalizzati/";
				String itFileNormalizzato = itFileNormalizzatoDirectory + "normalizzato_it.csv";
				File itFileNormalizzatoDirectoryFile = new File(itFileNormalizzatoDirectory);
				itFileNormalizzatoDirectoryFile.mkdirs();

				//MAP TO file normalizzato
				String mapToFileNormalizzatoDirectory = BASE_PATH + "/LOINC_" + csVersionName + "/normalizzati/";
				String mapToFileNormalizzato = mapToFileNormalizzatoDirectory + "normalizzato_mapto.csv";
				File mapToFileNormalizzatoDirectoryFile = new File(mapToFileNormalizzatoDirectory);
				mapToFileNormalizzatoDirectoryFile.mkdirs();

				Map<String, String> params = new HashMap<String, String>();
				params.put(KitchenParameter.PATH_LOINC_EN_INPUT.toString(), BASE_PATH + enFile);
				params.put(KitchenParameter.PATH_LOINC_EN_NORMALIZZATO.toString(), enFileNormalizzato);
				params.put(KitchenParameter.PATH_LOINC_IT_INPUT.toString(), BASE_PATH + itFile);
				params.put(KitchenParameter.PATH_LOINC_IT_NORMALIZZATO.toString(), itFileNormalizzato);
				params.put(KitchenParameter.PATH_INPUT_MAP_TO.toString(), BASE_PATH + mapToFile);
				params.put(KitchenParameter.PATH_MAP_TO_NORMALIZZATO.toString(), mapToFileNormalizzato);
				params.put(KitchenParameter.fk_temp_importazione_input.toString(), String.valueOf(getTempImportId()));
				kitchenModel.setParams(params);
				KitchenLauncher launcher = new KitchenLauncher(kitchenModel);
				launcher.execute();
			}

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			ImportJsonData.importNewVersion(
					SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					hibUtil, csVersionName, csVersionDescription,
					effectiveDate, oid, getTempImportId());

			// Sposta i file csv per il download
			File loinc_it_csv =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					LoincFields.LOINC_CODE_SYSTEM_NAME, csVersionName, "it", "csv");

			File loinc_en_csv =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					LoincFields.LOINC_CODE_SYSTEM_NAME, csVersionName, "en", "csv");

			File loinc_mapto_csv =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					LoincFields.LOINC_CODE_SYSTEM_NAME, csVersionName, "mapto", "csv");

			Files.move(new File(BASE_PATH + itFile), loinc_it_csv);
			Files.move(new File(BASE_PATH + enFile), loinc_en_csv);
			Files.move(new File(BASE_PATH + mapToFile), loinc_mapto_csv);


			// Reindicizza tutto il Code System
			updateStatusWorkflow(ImportValues.REINDEX, "");

			SolrIndexerUtil.indexNewVersion(LoincFields.LOINC_CODE_SYSTEM_NAME,
					StiServiceUtil.buildCsIndexPath(
							LoincFields.LOINC_CODE_SYSTEM_NAME) + "/update");

			updateStatusWorkflow(ImportValues.COMPLETE, "");
		}
		catch(Exception ex) {
			try {
				updateStatusWorkflow(ImportValues.ERROR, ex.getMessage());
			}
			catch(Exception uex) {
				log.error("Impossibile aggiornare lo stato della operazione di import LOINC.");
			}

			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		}
		finally {
			NewCsVersionInfo.RUNNING = false;
		}
	}

	public String getEnFile() {
		return enFile;
	}

	public void setEnFile(final String enFile) {
		this.enFile = enFile;
	}

	public String getItFile() {
		return itFile;
	}

	public void setItFile(final String itFile) {
		this.itFile = itFile;
	}

	public String getMapToFile() {
		return mapToFile;
	}

	public void setMapToFile(final String mapToFile) {
		this.mapToFile = mapToFile;
	}
}
