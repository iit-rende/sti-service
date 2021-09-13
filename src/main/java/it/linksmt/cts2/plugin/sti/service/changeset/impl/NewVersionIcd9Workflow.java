package it.linksmt.cts2.plugin.sti.service.changeset.impl;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.ImportOwl;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class NewVersionIcd9Workflow extends NewCsVersionInfo implements Runnable {

	private static Logger log = Logger.getLogger(NewVersionIcd9Workflow.class);

	private static String OWL_BASE_PATH = StiAppConfig.getProperty(
			StiServiceConfiguration.FILESYSTEM_IMPORT_BASE_PATH, "");

	private String owlItaPath;
	private String owlEnPath;

	private String csVersionName;
	private String csVersionDescription;
	private Date effectiveDate;
	private String oid;

	public NewVersionIcd9Workflow(
			final String owlItaPath, final String owlEnPath,
			final String csVersionName, final String csVersionDescription,
			final Date effectiveDate, final String oid) {

		this.owlItaPath = owlItaPath;
		this.owlEnPath = owlEnPath;
		this.csVersionName = csVersionName;
		this.csVersionDescription = csVersionDescription;
		this.effectiveDate = effectiveDate;
		this.oid = oid;
	}

	@Override
	public void run() {

		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {

			File icd9cmEngOwl = new File(OWL_BASE_PATH + "/" + StiServiceUtil.trimStr(owlEnPath));
			if (!icd9cmEngOwl.isFile()) {
				throw new RuntimeException("ERRORE - impossibile accedere al file: "
						+ icd9cmEngOwl.getAbsolutePath());
			}

			File icd9cmItaOwl = new File(OWL_BASE_PATH + "/" + StiServiceUtil.trimStr(owlItaPath));
			if (!icd9cmItaOwl.isFile()) {
				throw new RuntimeException("ERRORE - impossibile accedere al file: "
						+ icd9cmItaOwl.getAbsolutePath());
			}

			updateStatusWorkflow(ImportValues.CTS2_LOADING, "");

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			ImportOwl.importNewVersion(hibUtil,
					icd9cmEngOwl, icd9cmItaOwl,
					csVersionName, csVersionDescription,
					effectiveDate, oid);

			updateStatusWorkflow(ImportValues.REINDEX, "");

			SolrIndexerUtil.indexNewVersion(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME,
					StiServiceUtil.buildCsIndexPath(
							Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME) + "/update");

			updateStatusWorkflow(ImportValues.COMPLETE, "");
		}
		catch(Exception ex) {
			try {
				updateStatusWorkflow(ImportValues.ERROR, ex.getMessage());
			}
			catch(Exception uex) {
				log.error("Impossibile aggiornare lo stato della operazione di import ICD-9.");
			}

			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		}
		finally {
			NewCsVersionInfo.RUNNING = false;
		}
	}
}
