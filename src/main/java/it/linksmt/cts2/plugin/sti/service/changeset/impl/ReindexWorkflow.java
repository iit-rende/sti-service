package it.linksmt.cts2.plugin.sti.service.changeset.impl;

import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class ReindexWorkflow implements Runnable {

	private String csName;

	public ReindexWorkflow(final String csName) {
		this.csName = csName;
	}

	@Override
	public void run() {

		if (NewCsVersionInfo.RUNNING) {
			return;
		}
		try {
			SolrIndexerUtil.indexNewVersion(csName, StiServiceUtil.buildCsIndexPath(csName) + "/update");
		}
		catch(Exception ex) {
			throw new RuntimeException("Impossibile eseguire l'importazone del CS.", ex);
		}
		finally {
			NewCsVersionInfo.RUNNING = false;
		}
	}
}
