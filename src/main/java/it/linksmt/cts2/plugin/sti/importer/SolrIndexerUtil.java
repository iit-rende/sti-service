package it.linksmt.cts2.plugin.sti.importer;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class SolrIndexerUtil {

	private static Logger log = Logger.getLogger(SolrIndexerUtil.class);

	private SolrIndexerUtil() { }

	public static void indexSingleDocument(
			final String solrUrl, final JsonObject indexDoc)
		throws Exception {

		if (indexDoc == null) {
			log.warn("L'elemento da indicizzare risulta nullo.");
		}

		JsonArray chunkDoc = new JsonArray();
		chunkDoc.add(indexDoc);

		insertChunk(solrUrl, chunkDoc);
	}

	public static void indexNewVersion(
			final String codeSystemName, final String solrUrl)
		throws Exception {

		int totalCnt = 0;
		ChunkExtractor chEx = new ChunkExtractor(codeSystemName);

		 // Annullo eventuali modifiche rimaste incomplete
        rollback(solrUrl);

        // Ripulitura e aggiornamento indici
        // deleteAll(solrUrl);

		boolean terminated = false;
		while(!terminated) {
			try {
				JsonObject curChunk = chEx.nextChunk();

				JsonArray docsArr = curChunk.getAsJsonArray(
						ChunkExtractor.DOCS_ARRAY_FIELD);

				if ((docsArr != null) && (docsArr.size() > 0)) {

					// Viene effettuato direttamente il commit
					// per fare un aggiornamento progressivo
					insertChunk(solrUrl+"?commit=true", docsArr);

					for (int i = 0; i < docsArr.size(); i++) {
						totalCnt++;
						if ((totalCnt % 100) == 0) {
							log.info("Numero elementi elaborati: " + totalCnt);
						}
					}
				}

				terminated = StiServiceUtil.trimStr(String.valueOf(
						curChunk.get(ChunkExtractor.AT_END_FIELD)))
							.equalsIgnoreCase("true");
			}
			catch(Exception cex) {
				terminated = true;
				throw cex;
			}
		}

		// commit(solrUrl);
        optimize(solrUrl);
	}

	public static void insertChunk(final String solrUrl, final JsonArray chunkDocs)
			throws HttpException, IOException {

		HttpClient httpclient = new HttpClient();
		PostMethod httpPost = new PostMethod(solrUrl);

		try {

			StringRequestEntity params = new StringRequestEntity(
					chunkDocs.toString(), "application/json", "UTF-8");
			httpPost.setRequestEntity(params);

			int statusCode = httpclient.executeMethod(httpPost);
			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("Chiamata HTTP fallita: " + httpPost.getStatusLine());
			}
		}
		finally {
			// Release the connection.
			httpPost.releaseConnection();
		}
	}


	public static void deleteAll(final String solrUrl) throws HttpException, IOException {
		doXmlRequest(solrUrl, "<delete><query>*:*</query></delete>");
	}
	
	public static void deleteByVersion(final String solrUrl, final String version) throws HttpException, IOException {
		doXmlRequest(solrUrl, "<delete><query>VERSION:"+version+"</query></delete>");
	}
	
	/*Solo per indici LOCAL e VALUESET*/
	public static void deleteByVersionAndName(final String solrUrl, final String version, final String name) throws HttpException, IOException {
		doXmlRequest(solrUrl, "<delete><query>VERSION:"+version+" AND NAME:"+name+"</query></delete>");
	}

	public static void optimize(final String solrUrl) throws HttpException, IOException {
		doXmlRequest(solrUrl, "<optimize />");
	}

	/*
	private static void commit(final String solrUrl) throws HttpException, IOException {
		doXmlRequest(solrUrl, "<commit />");
	}
	*/

	public static void rollback(final String solrUrl) throws HttpException, IOException {
		doXmlRequest(solrUrl, "<rollback/>");
	}

	private static void doXmlRequest(final String solrUrl, final String xmlContent)
			throws HttpException, IOException {

		HttpClient httpclient = new HttpClient();
		PostMethod httpPost = new PostMethod(solrUrl);

		try {

			StringRequestEntity params = new StringRequestEntity(
					xmlContent, "text/xml", "UTF-8");

			httpPost.setRequestEntity(params);

			int statusCode = httpclient.executeMethod(httpPost);
			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("Chiamata HTTP fallita: " + httpPost.getStatusLine());
			}
		}
		finally {
			// Release the connection.
			httpPost.releaseConnection();
		}
	}
}
