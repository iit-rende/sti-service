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
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.ImportAicData;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.ImportMappingData;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;
import it.linksmt.cts2.plugin.sti.transformer.KitchenLauncher;
import it.linksmt.cts2.plugin.sti.transformer.KitchenModel;
import it.linksmt.cts2.plugin.sti.transformer.KitchenParameter;

public class NewVersionAicWorkflow  extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(NewVersionAicWorkflow.class);

	private String csVersion;
	private String csDescription;
	private Date effectiveDate;
	private String codeSystemOid;
	private String aicClasseAFile;
	private String aicClasseHFile;
	private String aicClasseCFile;
	private String equivalentiAicAtcFile;
	// private String atcVersionName;

	private static String BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");
	private static String POPOLA_AIC = StiAppConfig.getProperty(StiServiceConfiguration.KITCHEN_JOB_AIC, "");
	private static String POPOLA_ATC_AIC = StiAppConfig.getProperty(StiServiceConfiguration.KITCHEN_JOB_ATC_AIC, "");

	// TODO: rendere dinamica
	private static String ATC_VERSION_NAME = "2014";

	public NewVersionAicWorkflow(
			final String csVersion,
			final String csDescription, final Date effectiveDate,
			final String codeSystemOid,
			final String aicClasseAFile,
			final String aicClasseHFile,
			final String aicClasseCFile,
			final String equivalentiAicAtcFile) {
			// final String atcVersionName

		this.csVersion = csVersion;
		this.csDescription = csDescription;
		this.effectiveDate = effectiveDate;
		this.codeSystemOid = codeSystemOid;
		this.aicClasseAFile = aicClasseAFile;
		this.aicClasseHFile = aicClasseHFile;
		this.aicClasseCFile = aicClasseCFile;
		this.equivalentiAicAtcFile = equivalentiAicAtcFile;
		// this.atcVersionName = atcVersionName;
	}

	@Override
	public void run() {
		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {

			String enableEtl = StiServiceUtil.trimStr(StiAppConfig.getProperty(
					StiServiceConfiguration.ETL_EXECUTION_ATC_AIC_ENABLE, "")).toLowerCase();

			if ("true".equals(enableEtl) || "yes".equals(enableEtl) || "si".equals(enableEtl)) {

				updateStatusWorkflow(ImportValues.ETL_LOADING, "");

				// Avviare ETL per AIC
				KitchenModel aicKitchenModel = new KitchenModel();
				aicKitchenModel.setFile(POPOLA_AIC);

				Map<String, String> params = new HashMap<String, String>();
				params.put(KitchenParameter.PATH_AIC_CLASSE_A.toString(), BASE_PATH + aicClasseAFile);
				params.put(KitchenParameter.PATH_AIC_CLASSE_H.toString(), BASE_PATH + aicClasseHFile);
				params.put(KitchenParameter.PATH_AIC_CLASSE_C.toString(), BASE_PATH + aicClasseCFile);
				params.put(KitchenParameter.CLASSE_A.toString(), "A");
				params.put(KitchenParameter.CLASSE_H.toString(), "H");
				params.put(KitchenParameter.CLASSE_C.toString(), "C");
				params.put(KitchenParameter.fk_temp_importazione_input.toString(), String.valueOf(getTempImportId()));
				aicKitchenModel.setParams(params);
				KitchenLauncher launcher = new KitchenLauncher(aicKitchenModel);
				launcher.execute();

				// Avviare ETL per Mapping ATC-AIC
				KitchenModel atcAicKitchenModel = new KitchenModel();
				atcAicKitchenModel.setFile(POPOLA_ATC_AIC);

				Map<String, String> atcAicParams = new HashMap<String, String>();
				atcAicParams.put(KitchenParameter.PATH_MAPPING_ATC.toString(), BASE_PATH + equivalentiAicAtcFile);
				atcAicParams.put(KitchenParameter.fk_temp_importazione_input.toString(), String.valueOf(getTempImportId()));
				aicKitchenModel.setParams(atcAicParams);
				KitchenLauncher atcAicLauncher = new KitchenLauncher(atcAicKitchenModel);
				atcAicLauncher.execute();
			}

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			ImportAicData.importNewVersion(
					SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					hibUtil, csVersion, csDescription,
					effectiveDate, codeSystemOid, getTempImportId());

			// Sposta i file csv per il download
			File classe_a_file =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					AtcAicFields.AIC_CODE_SYSTEM_NAME, csVersion, "classe_a", "csv");

			File classe_h_file =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					AtcAicFields.AIC_CODE_SYSTEM_NAME, csVersion, "classe_h", "csv");

			File classe_c_file =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					AtcAicFields.AIC_CODE_SYSTEM_NAME, csVersion, "classe_c", "csv");

			File farmacieq_file =  ExportUtil.getFileExport(ExportController.CODE_SYSTEM,
					AtcAicFields.AIC_CODE_SYSTEM_NAME, csVersion, "farmaci_equivalenti", "csv");

			Files.move(new File(BASE_PATH + aicClasseAFile), classe_a_file);
			Files.move(new File(BASE_PATH + aicClasseHFile), classe_h_file);
			Files.move(new File(BASE_PATH + aicClasseCFile), classe_c_file);
			Files.move(new File(BASE_PATH + equivalentiAicAtcFile), farmacieq_file);


			// Reindicizza tutto il Code System
			updateStatusWorkflow(ImportValues.REINDEX, "");

			String solrUrl = StiServiceUtil.buildCsIndexPath(
					AtcAicFields.AIC_CODE_SYSTEM_NAME) + "/update";

			SolrIndexerUtil.indexNewVersion(
					AtcAicFields.AIC_CODE_SYSTEM_NAME,
					solrUrl);

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			ImportMappingData.importMapping(
					SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
					hibUtil, csVersion, ATC_VERSION_NAME, solrUrl,
					getTempImportId());

			updateStatusWorkflow(ImportValues.COMPLETE, "");
		}
		catch(Exception ex) {
			try {
				updateStatusWorkflow(ImportValues.ERROR, ex.getMessage());
			}
			catch(Exception uex) {
				log.error("Impossibile aggiornare lo stato della operazione di import AIC.");
			}

			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		}
		finally {
			NewCsVersionInfo.RUNNING = false;
		}
	}

	public String getAicClasseAFile() {
		return aicClasseAFile;
	}

	public void setAicClasseAFile(final String aicClasseAFile) {
		this.aicClasseAFile = aicClasseAFile;
	}

	public String getAicClasseHFile() {
		return aicClasseHFile;
	}

	public void setAicClasseHFile(final String aicClasseHFile) {
		this.aicClasseHFile = aicClasseHFile;
	}

	public String getAicClasseCFile() {
		return aicClasseCFile;
	}

	public void setAicClasseCFile(final String aicClasseCFile) {
		this.aicClasseCFile = aicClasseCFile;
	}

	public String getEquivalentiAicAtcFile() {
		return equivalentiAicAtcFile;
	}

	public void setEquivalentiAicAtcFile(final String equivalentiAicAtcFile) {
		this.equivalentiAicAtcFile = equivalentiAicAtcFile;
	}

}