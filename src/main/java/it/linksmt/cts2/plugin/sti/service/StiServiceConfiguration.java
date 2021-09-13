package it.linksmt.cts2.plugin.sti.service;

public interface StiServiceConfiguration {

	String HIBERNANTE_CONFIGURATION_RESOURCE = "/it/linksmt/cts2/plugin/sti/db/hibernate.cfg.xml";

	String DB_STI_SERVER_ADDRESS = "db.sti.server.address";
	String DB_STI_USERNAME = "db.sti.username";
	String DB_STI_PASSWORD = "db.sti.password";

	String CTS2_STI_SERVER_ADDRESS 	= "cts2.sti.server.address";
	String CTS2_STI_SOLR_ADDRESS 	= "cts2.sti.solr.address";

	String CTS2_TEMP_IMPORT_ADDRESS = "cts2.sti.import.address";
	String CTS2_TEMP_IMPORT_USER = "cts2.sti.import.username";
	String CTS2_TEMP_IMPORT_PASS = "cts2.sti.import.password";

	String FILESYSTEM_IMPORT_BASE_PATH = "filesystem.import.base.path";
	String FILESYSTEM_EXPORT_BASE_PATH = "filesystem.export.base.path";

	String ETL_EXECUTION_LOINC_ENABLE = "etl.execution.loinc.enable";
	String ETL_EXECUTION_ATC_AIC_ENABLE = "etl.execution.atc-aic.enable";
	String KITCHEN_EXECUTABLE_PATH = "kitchen.executable.path";
	String KITCHEN_JOB_LOINC = "kitchen.job.loinc";
	String KITCHEN_JOB_ATC = "kitchen.job.atc";
	String KITCHEN_JOB_AIC = "kitchen.job.aic";
	String KITCHEN_JOB_ATC_AIC = "kitchen.job.mapping.atc.aic";
}
