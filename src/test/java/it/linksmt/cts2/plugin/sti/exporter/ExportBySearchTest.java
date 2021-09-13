package it.linksmt.cts2.plugin.sti.exporter;


import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.search.util.SolrQueryUtil;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import scala.actors.threadpool.Arrays;

public class ExportBySearchTest {

	private static Logger log = Logger.getLogger(ExportBySearchTest.class);


	public static void main(final String[] args) {
		BasicConfigurator.configure();
		exportWithQuerySolr();
		
//		String indexSolr = "sti_local";		
//		String solrUrl = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_STI_SOLR_ADDRESS, "") +"/"+ indexSolr + "/update";
//		try {
//			SolrIndexerUtil.deleteByVersion(solrUrl + "?commit=true", "1.04");
//			SolrIndexerUtil.indexNewVersion("TestMatteo", solrUrl);
//		} catch (HttpException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	private static void exportWithQuerySolr(){
		try {
			
			String language = "it";
			String fileType = "csv";
			

			
//			String codeSystem = "ATC";
//			String indexSolr = "sti_atc";
//			String version = "2014";		
//			List<String> listFields = Arrays.asList(new String[]{"CODICE_ATC","DENOMINAZIONE","GRUPPO_ANATOMICO","VERSION"});

			
//			String codeSystem = "AIC";
//			String indexSolr = "sti_aic";
//			String version = "16.01.2017";
//			List<String> listFields = Arrays.asList(new String[]{"CODICE_AIC","DENOMINAZIONE","CONFEZIONE","TIPO_FARMACO","PRINCIPIO_ATTIVO","CLASSE","DITTA","VERSION"});


			String codeSystem = "ICD-9-CM";
			String indexSolr = "sti_icd9cm";
			String version = "2007";
			List<String> listFields = Arrays.asList(new String[]{"ICD9CM_ID","DESCRIPTION_"+language,"VERSION","NOTE_"+language});

//			String codeSystem = "LOINC";
//			String indexSolr = "sti_loinc";
//			String version = "2.58";
//			List<String> listFields = Arrays.asList(new String[]{"LOINC_NUM","COMPONENT_"+language,"PROPERTY_"+language,"TIME_ASPECT_"+language,"SYSTEM_"+language,"SCALE_TYP_"+language,"METHOD_TYP_"+language,"CLASS_"+language,"VERSION","STATUS"});


			
//			String codeSystem = "TestMatteo";
//			String indexSolr = "sti_local";		
//			String version = "1.05";
//			List<String> listFields = Arrays.asList(new String[]{"LOCAL_CODE","LOCAL_DESCRIPTION","NAME","DESCRIPTION","DOMAIN","VERSION_NAME","ORGANIZATION","CS_TYPE","CS_SUBTYPE","DF_S_COMPONENTE","DF_S_PROPRIETA","DF_S_VERSIONE","DF_N_CAMPO_NUMERICO","DF_D_CAMPO_DATA"});

			
			String fileName = codeSystem + "_" + language + "." + fileType;
			
//			String matchvalue = "indent=on&q=*&sort=LOINC_NUM%20ASC&fq=COMPONENT_it:[*%20TO%20*]&fq=STATUS:%22ACTIVE%22&fq=VERSION:%222.58%22&fq=CLASS_it:%22PROVOCAZIONE%22&fq=SYSTEM_it:%22Urine%22&fq=PROPERTY_it:%22MCnc%22&fq=METHOD_TYP_it:%22Test%20strip%22&fq=TIME_ASPECT_it:%22Pt%22&fq=SCALE_TYP_it:%22Qn%22";
//			String matchvalue = "indent=on&q=*&sort=LOINC_NUM%20ASC&fq=COMPONENT_it:[*%20TO%20*]&fq=STATUS:%22ACTIVE%22&fq=VERSION:%222.58%22&fq=CLASS_it:%22PROVOCAZIONE%22&fq=SYSTEM_it:%22Urine%22";
//			String matchvalue = "indent=on&q=*&sort=LOINC_NUM%20ASC&fq=COMPONENT_it:[*%20TO%20*]&fq=STATUS:%22ACTIVE%22&fq=VERSION:%222.58%22";
			String matchvalue = "indent=on&q=*&sort=id%20ASC&fq=VERSION:%22__VERSION__%22";

			matchvalue = matchvalue.replace("__VERSION__", version);
			
			
			
			String urlQuerySolr = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_STI_SOLR_ADDRESS, "") +"/"+ indexSolr + "/select?";
			
			
			String jsonObjectCount = SolrQueryUtil.executeRequest(urlQuerySolr + matchvalue, true);
			System.out.println(jsonObjectCount);
			
			JSONObject jsonObj = new JSONObject(jsonObjectCount);
			
			JSONObject response = jsonObj.getJSONObject("response");
			Integer numFound = response.getInt("numFound");
			System.out.println("numFound::"+numFound);
			
			Integer elemPerPage = ExportController.ELEM_PAGINATION_SOLR;
			Integer numPage = numFound/elemPerPage+1;
			System.out.println("numPage::"+numPage);
			Integer start = 0;
			
			File file = new File(ExportController.BASE_PATH + "/" + ExportController.CODE_SYSTEM, fileName);
//			File fileJson = new File(ExportController.BASE_PATH + "/" + ExportController.CODE_SYSTEM, codeSystem + "_" + language + "." + "json");
			
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
			
			OutputStream downloadStream = new FileOutputStream(file);

			
			for (float idx = 0; idx<=numPage; idx++) {
				DecimalFormat df = new DecimalFormat("#.#");
				float percentualeDownlaod = (float) (idx/numPage)*100;
//				if (percentualeDownlaod % 10 == 0) {
					System.out.println("downlaod ["+df.format(percentualeDownlaod)+"%]");
//				}
				
				String urlPaginato = matchvalue+"&start="+start+"&rows="+elemPerPage;
				
				String jsonObject = SolrQueryUtil.executeRequest(urlQuerySolr + urlPaginato, true);
				JSONObject jsonObjSearch = new JSONObject(jsonObject);
				JSONObject responseSearch = jsonObjSearch.getJSONObject("response");
				JSONArray docs =responseSearch.getJSONArray("docs");
				
				
				/*debug campi document*/
//				for(Object o: docs){
//				    if ( o instanceof JSONObject ) {
//				    	JSONObject document = (JSONObject)o;
//				    	String[] campi = document.getNames(document);
//				    	for (String campo : campi) {
//							System.out.println("campo::"+campo+" valore::"+document.get(campo));
//						}
//				    	System.out.println("\n################");
//				    }
//				}
				
				
				
				boolean firstElement = (idx==0);
				boolean lastElement = ((int)idx==numPage);
				
//				System.out.println("firstElement::"+firstElement);
//				System.out.println("lastElement::"+lastElement+" idx::"+idx+" numPage::"+numPage);

				
				try {
					File outFile = File.createTempFile(fileName, "_tmp"+idx);
					outFile.deleteOnExit();
					String localCode = "";
					outFile = ExporterSearchUtil.generaFileFromJSONArray(outFile, language, fileType, docs, firstElement, lastElement, listFields, localCode);
					
					int readBytes = 0;
					byte[] toDownload = new byte[(int)outFile.length()];

					InputStream targetStream = new FileInputStream(outFile);
					try {
						while (toDownload.length>0 && (readBytes = targetStream.read(toDownload)) != -1) {
							downloadStream.write(toDownload, 0, readBytes);
						}
					}catch (Exception e) {
						log.error("ERROR" + e.getLocalizedMessage(), e);
						e.printStackTrace();
						
						downloadStream.flush();
						downloadStream.close();
					} finally {
						targetStream.close();
					}
					
					outFile.delete();
				} catch (Exception e) {
					log.error("ERROR" + e.getLocalizedMessage(), e);
					e.printStackTrace();
				}
				
				start = start + elemPerPage;
			}
			
			downloadStream.flush();
			downloadStream.close();
			
		} catch (Exception ex) {
			log.error("Errore durante la ricerca.", ex);
		}
	}
	
	
}