package it.linksmt.cts2.plugin.sti.indexer;

import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionStandardLocalWorkflow;

import java.text.ParseException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

public final class IndexCodeSystemTest {

	private static Logger log = Logger.getLogger(IndexCodeSystemTest.class);

	// private static final String SOLR_URL = "http://10.0.6.16/solr/sti_icd9cm/update";
	// private static final String SOLR_URL = "http://localhost:8983/solr/sti_loinc/update";
	private static final String SOLR_URL = "http://web30.linksmt.it/solr/sti_local/update";

	// private static final String CODE_SYSTEM = LoincFields.LOINC_CODE_SYSTEM_NAME;
	// private static final String CODE_SYSTEM = Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME;
	// private static final String CODE_SYSTEM = LocalFields.LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME;
	private static final String CODE_SYSTEM = "TestMatteo2";

	public static void main(final String[] args) throws ParseException {

		// Logging configuration for Test
		BasicConfigurator.configure();
		LogManager.getLogger("httpclient.wire").setLevel(Level.WARN);
		LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.WARN);
		LogManager.getLogger("org.hibernate").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.c3p0").setLevel(Level.WARN);
		LogManager.getLogger("com.mchange.v2.resourcepool").setLevel(Level.WARN);
		// LogManager.getLogger("").setLevel(Level.WARN);

		addSingleDocumentTest();

	}

	private static void addSingleDocumentTest() {

		try {
			// SolrIndexerUtil.indexNewVersion(CODE_SYSTEM, SOLR_URL);

			// id
			// NAME
			// DESCRIPTION
			// VERSION_NAME
			// VERSION_DESCRIPTION
			// VERSION
			// RELEASE_DATE
			// CS_OID
			// DOMAIN
			// ORGANIZATION
			// CS_TYPE
			// CS_SUBTYPE
			// IS_LEAF
			// IS_LAST_VERSION
			// HAS_ASSOCIATIONS
			// ----- campi base del csv ----
			// LOCAL_CODE
			// LOCAL_DESCRIPTION
			// ----- campi del csv dinamici ----
			// DF_S_*
			// DF_D_*
			// DF_N_*
			// DF_M_*

			JsonObject jsonObject = NewVersionStandardLocalWorkflow.getDocumentMock();

			SolrIndexerUtil.indexSingleDocument(SOLR_URL + "?commit=true", jsonObject);

			System.out.println("DONE");
		} catch (Exception ex) {
			log.error("Errore durante l'importazione dei files.", ex);
		}
	}


}
