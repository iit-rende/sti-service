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
import it.linksmt.cts2.plugin.sti.importer.atc_aic.ImportAtcData;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;
import it.linksmt.cts2.plugin.sti.transformer.KitchenLauncher;
import it.linksmt.cts2.plugin.sti.transformer.KitchenModel;
import it.linksmt.cts2.plugin.sti.transformer.KitchenParameter;

public class NewVersionAtcWorkflow extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(NewVersionAtcWorkflow.class);

	private static String BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");
	private static String POPOLA_ATC = StiAppConfig.getProperty(StiServiceConfiguration.KITCHEN_JOB_ATC, "");

	private String atcFile;
	private String csVersion;
	private String csDescription;
	private Date effectiveDate;
	private String codeSystemOid;

	public NewVersionAtcWorkflow(final String atcFile, final String csVersion, final String csDescription, final Date effectiveDate, final String codeSystemOid) {

		this.atcFile = atcFile;
		this.csVersion = csVersion;
		this.csDescription = csDescription;
		this.effectiveDate = effectiveDate;
		this.codeSystemOid = codeSystemOid;
	}

	@Override
	public void run() {
		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {

			String enableEtl = StiServiceUtil.trimStr(StiAppConfig.getProperty(StiServiceConfiguration.ETL_EXECUTION_ATC_AIC_ENABLE, "")).toLowerCase();


			if ("true".equals(enableEtl) || "yes".equals(enableEtl) || "si".equals(enableEtl)) {
				// aggiungere export KETTLE_HOME=/home/tomcat/
				updateStatusWorkflow(ImportValues.ETL_LOADING, "");

				// Avviare ETL per ATC
				KitchenModel kitchenModel = new KitchenModel();
				kitchenModel.setFile(POPOLA_ATC);

				Map<String, String> params = new HashMap<String, String>();
				params.put(KitchenParameter.PATH_ATC.toString(), BASE_PATH + atcFile);
				params.put(KitchenParameter.fk_temp_importazione_input.toString(), String.valueOf(getTempImportId()));
				kitchenModel.setParams(params);
				KitchenLauncher launcher = new KitchenLauncher(kitchenModel);
				launcher.execute();
			}

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			ImportAtcData.importNewVersion(SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS, hibUtil, csVersion, csDescription, effectiveDate, codeSystemOid, getTempImportId());

			// Sposta i file csv per il download
			File excsv_file = ExportUtil.getFileExport(ExportController.CODE_SYSTEM, AtcAicFields.ATC_CODE_SYSTEM_NAME, csVersion, null, "csv");

			Files.move(new File(BASE_PATH + atcFile), excsv_file);

			// Reindicizza tutto il code System
			updateStatusWorkflow(ImportValues.REINDEX, "");

			SolrIndexerUtil.indexNewVersion(AtcAicFields.ATC_CODE_SYSTEM_NAME, StiServiceUtil.buildCsIndexPath(AtcAicFields.ATC_CODE_SYSTEM_NAME) + "/update");

			updateStatusWorkflow(ImportValues.COMPLETE, "");
		} catch (Exception ex) {
			try {
				updateStatusWorkflow(ImportValues.ERROR, ex.getMessage());
			} catch (Exception uex) {
				log.error("Impossibile aggiornare lo stato della operazione di import ATC.");
			}

			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		} finally {
			NewCsVersionInfo.RUNNING = false;
		}
	}

	public String getAtcFile() {
		return atcFile;
	}

	public void setAtcFile(final String atcFile) {
		this.atcFile = atcFile;
	}

}
