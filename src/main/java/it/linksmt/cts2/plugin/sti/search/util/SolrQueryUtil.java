package it.linksmt.cts2.plugin.sti.search.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class SolrQueryUtil {

	private SolrQueryUtil() { }
	
	private static final Logger log = LoggerFactory.getLogger(SolrQueryUtil.class);

	private static JsonParser JSON_PARSER = new JsonParser();

	public static JsonObject solrQueryResult(final String queryVal, final String solrUrl)
			throws HttpException, IOException {
		
		HttpClient httpclient = new HttpClient();
		String callUrl = solrUrl.trim();
		

		if (callUrl.contains("&")) {
			callUrl += "&" + queryVal.trim();
		}
		else {
			callUrl += "?" + queryVal.trim();
		}
		
		callUrl = callUrl.replaceAll("\\s+", "%20");
		log.info("solrQueryResult::"+callUrl);
		
		GetMethod httpGet = new GetMethod(callUrl);

		try {
			int statusCode = httpclient.executeMethod(httpGet);
			if (statusCode != HttpStatus.SC_OK) {
				throw new IOException("Chiamata HTTP fallita: " + httpGet.getStatusLine());
			}

			JsonObject respVal = (JsonObject) JSON_PARSER.parse(httpGet.getResponseBodyAsString()) ;
			return (JsonObject)respVal.get("response");
		}
		finally {
			// Release the connection.
			httpGet.releaseConnection();
		}
	}

	public static String encodeURIComponent(final String component) {
        String result = null;

        try {
            result = URLEncoder.encode(component, "UTF-8")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%7E", "~");
        }
        catch (UnsupportedEncodingException e) {
            result = component;
        }

        return result;
	}
	
	
	/**
	 *
	 * @param connectionUrl
	 * @return
	 * @throws IOException
	 */
	public static String executeRequest(String connectionUrl, boolean postFlag) throws IOException {
		String result = "";
		connectionUrl = connectionUrl.replaceAll("\\s+", "%20");
		log.info("solrQueryResult::"+connectionUrl);
		
		String[] urlArray = connectionUrl.split("\\?");
		String urlParameters ="";
		if(urlArray.length>1) {
			urlParameters=urlArray[1];
		}
		byte[] postData       = urlParameters.getBytes( "UTF-8" );
		int    postDataLength = postData.length;
		URL url = new URL(connectionUrl);
		if(postFlag) {
			url = new URL(urlArray[0]);
		}
		HttpURLConnection conn=null;
		BufferedReader br=null;
		try{
			conn =(HttpURLConnection) url.openConnection(Proxy.NO_PROXY);

			if(postFlag) {
				conn.setDoOutput( true );
				conn.setInstanceFollowRedirects( false );
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Accept", "application/json");
				conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
				conn.setRequestProperty( "charset", "UTF-8");
				conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
				conn.setUseCaches( false );
				DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
				wr.write( postData );
			}else {
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
			}

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			br = new BufferedReader(new InputStreamReader((conn.getInputStream()),"UTF-8"));


			String output;
			while ((output = br.readLine()) != null) {
				result += output;
			}

		}finally{
			if(conn!=null){
				conn.disconnect();
			}
			if(br!=null){
				br.close();
			}
		}

		result = result.replaceAll("&ldquo;", "'").replaceAll("&rdquo;", "'").replaceAll("&rsquo;", "'").replaceAll("&#39;", "'").replaceAll("&quot;", "'");
		return StringEscapeUtils.unescapeHtml(result);
	}

}
