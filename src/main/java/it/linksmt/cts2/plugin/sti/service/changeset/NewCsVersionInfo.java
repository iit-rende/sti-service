package it.linksmt.cts2.plugin.sti.service.changeset;

import it.linksmt.cts2.plugin.sti.db.util.DbUtil;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

public class NewCsVersionInfo {

	protected static String SOURCE_TABLE_URL = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_ADDRESS, "");
	protected static String SOURCE_TABLE_USER = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_USER, "");
	protected static String SOURCE_TABLE_PASS = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_PASS, "");

	public static boolean RUNNING = false;

	private long tempImportId = -1;

	public long getTempImportId() {
		return tempImportId;
	}
	public void setTempImportId(final long tempImportId) {
		this.tempImportId = tempImportId;
	}

	protected void updateStatusWorkflow(
			final String status, final String statusMessage)
		throws ImportException {

		DbUtil.updateStatusWorkflow(tempImportId, status, statusMessage);
	}
}
