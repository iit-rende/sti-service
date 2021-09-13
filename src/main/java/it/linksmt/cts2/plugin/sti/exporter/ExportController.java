package it.linksmt.cts2.plugin.sti.exporter;


import it.linksmt.cts2.plugin.sti.dtos.OutputDto;
import it.linksmt.cts2.plugin.sti.enums.MappingType;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.search.util.SolrQueryUtil;
import it.linksmt.cts2.plugin.sti.service.ExtrasService;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import scala.collection.mutable.HashMap;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;

import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;

@Controller("exportControllerProvider")
public class ExportController implements ControllerProvider {
	
	private static Logger log = Logger.getLogger(ExportController.class);


	public static String BASE_PATH = StiAppConfig.getProperty(StiServiceConfiguration.FILESYSTEM_EXPORT_BASE_PATH, "");
	
	@Autowired
	private ExtrasService extrasService;

	public static final String LOCAL = "local";
	public static final String CODE_SYSTEM = "codesystem";
	public static final String MAPSET = "mapset";


	public static final String PARAM_CODESYSTEM = "codesystem";
	public static final String PARAM_LANGUAGE = "language";

	public static final String PARAM_AIC_TYPE = "aictype";
	public static final String PARAM_LOCAL_MAPPING = "localmapping";
	public static final String PARAM_MAPSET_FULLNAME = "mapsetfullname";

	public static final String PARAM_FORMAT = "format";
	public static final String PARAM_RESOURCE_TYPE = "resourceType";
	
	
	
	public static final Integer ELEM_PAGINATION_SOLR = 100;
	

	@Override
	public Object getController() {
		return this;
	}

	@RequestMapping(value = { "/exporter" }, method = RequestMethod.GET)
	public void export(@RequestParam(value = PARAM_CODESYSTEM, required = false) 
				final String codeSystem, 
				@RequestParam(value = PARAM_LANGUAGE, required = false) 
				final String language,
				@RequestParam(value = PARAM_LOCAL_MAPPING, required = false) final String localMapping, 
				@RequestParam(value = PARAM_AIC_TYPE, required = false) final String aicType,
				@RequestParam(value = PARAM_MAPSET_FULLNAME, required = false) final String mapsetFullname, 
				@RequestParam(value = PARAM_FORMAT, required = false) final String format,
				@RequestParam(value = PARAM_RESOURCE_TYPE, required = false) final String resourceType,
				
				final HttpServletResponse response) {

		File outFile = null;
		String fileOutputName = "";
		boolean omitVersion = false;
		
		String fileExtension = "";
		if (StiServiceUtil.trimStr(format).equalsIgnoreCase("json")) {
			fileExtension = ".json";
		} else {
			fileExtension = ".csv";
		}

		
		try {
			String codeSystemName = null;
			String codeSystemVersion = null;
			
			/*Download codesystem/valueset*/
			if (!StiServiceUtil.isNull(codeSystem)) {
				int idxp = codeSystem.indexOf(':');

				codeSystemName = StiServiceUtil.trimStr(codeSystem.substring(0, idxp));
				codeSystemVersion = StiServiceUtil.trimStr(codeSystem.substring(idxp + 1));
				omitVersion = codeSystemName.equalsIgnoreCase(AtcAicFields.AIC_CODE_SYSTEM_NAME);

				if (!StiServiceUtil.isNull(localMapping)) {
					outFile = getDownloadFile(LOCAL, codeSystemName, codeSystemVersion, localMapping, fileExtension);
					
					fileOutputName = localMapping
										+"_"+StiServiceUtil.trimStr(codeSystemName).toUpperCase() 
										+ "_"+ StiServiceUtil.trimStr(codeSystemVersion).replace(".", "_").toUpperCase()
										+fileExtension;
					
				} else if (!StiServiceUtil.isNull(aicType)) {
					outFile = getDownloadFile(CODE_SYSTEM, codeSystemName, codeSystemVersion, aicType, fileExtension);
					fileOutputName = outFile.getName();
				} else {
					outFile = getDownloadFile(CODE_SYSTEM, codeSystemName, codeSystemVersion, language, fileExtension);
					fileOutputName = outFile.getName();
				}
			} 
			/*download mapping*/
			else if (!StiServiceUtil.isNull(mapsetFullname)) {

				String fileName = StiServiceUtil.trimStr(mapsetFullname).replace(".", "_").replace(" - ", "_").replace("(", "").replace(")", "").replace(" ", "_").toUpperCase();
				fileName += fileExtension;

				File retVal = new File(BASE_PATH + "/" + MAPSET, fileName);
				if (retVal.exists() && retVal.isFile()) {
					outFile = retVal;
					fileOutputName = outFile.getName();
				} else {
					throw new ExportException("Impossibile accedere al file: " + retVal.getAbsolutePath());
				}
			} else {
				throw new RuntimeException("I parametri per l'esportazione della risorsa non sono corretti.");
			}

			if (StiServiceUtil.trimStr(format).equalsIgnoreCase("json")) {
				response.setContentType("application/json");
			} else {
				response.setContentType("text/csv");
			}

			if (omitVersion) {
				String versStrVal = "_" + StiServiceUtil.trimStr(codeSystemVersion).replace(".", "_").toUpperCase();
				fileOutputName = fileOutputName.replace(versStrVal, "");
			}

			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileOutputName + "\"");
			Files.copy(Paths.get(outFile.toURI()), response.getOutputStream());

			response.flushBuffer();
		} catch (Exception ex) {
			log.error("Errore durante la lettura del file.", ex);
			throw new RuntimeException("Errore durante la lettura del file.");
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 * 
	 * 
	 */
	@RequestMapping(value = { "/exporter/search" }, method = RequestMethod.GET)
	public void exportSearch(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		log.info("ExportController::exportSearch start");

		String matchvalue = request.getParameter("matchvalue");
		String codesystemversion = request.getParameter("codesystemversion");
		String codesystem = request.getParameter("codesystem");
		String fileType = request.getParameter("fileType");
		String language = request.getParameter("language");
		String fields = request.getParameter("fields");
		String exportType = request.getParameter("exportType");
		String mappingName = request.getParameter("mappingName");
		String filterMapping = request.getParameter("filterMapping");
		String mappingType = request.getParameter("mappingType");
		
		List<String> listFields = Arrays.asList(fields.split(","));
		
		/*correzioni filtri*/
		matchvalue = ExporterSearchUtil.makeFilters(matchvalue,codesystemversion);
		
		if(fileType==null || "".equals(fileType)){
			fileType="csv";
		}
		
		if(exportType.equals("mapping") && mappingName!=null && !mappingName.equals("")){
			exportMapping(response, fileType, language, mappingName, filterMapping, mappingType);
		}
		else if(exportType.equals("cs") || exportType.equals("vs")){
			/*generazione del fileName*/
			String fileName = ExporterSearchUtil.makeFileName(codesystemversion, codesystem, fileType, language);
			exportFromSolr(response, matchvalue, codesystem, fileType, language, listFields, fileName, null);
		}
		
		log.info("ExportController::exportSearch end");
	}


	private void exportMapping(final HttpServletResponse response, final String fileType, final String language, final String mappingName, String filterMapping, String mappingType) throws IOException {
		
		if(mappingType.equals(MappingType.ATC_AIC.getKey()) || mappingType.equals(MappingType.LOCAL_LOINC.getKey())){
			if(mappingType.equals(MappingType.ATC_AIC.getKey()) ){
				String fields = "CODICE_ATC,PRINCIPIO_ATTIVO,CODICE_AIC,DENOMINAZIONE,CONFEZIONE,TIPO_FARMACO";
				List<String> listFields = Arrays.asList(fields.split(","));
				String codeSystem = AtcAicFields.AIC_CODE_SYSTEM_NAME;	
				String fileName = mappingName+"."+fileType;
				//exportMappingFromSolr(response, fileType, language, mappingName, filterMapping, listFields, codeSystem, fileName);
				exportFromSolr(response, filterMapping,  codeSystem, fileType, language, listFields, fileName, mappingName);
			}
			if(mappingType.equals(MappingType.LOCAL_LOINC.getKey()) ){
				String fields = "LOCAL_CODE,LOCAL_DESCRIPTION,BATTERY_CODE,BATTERY_DESCRIPTION,LOCAL_UNITS";
				List<String> listFields = Arrays.asList(fields.split(","));
				String codeSystem = LoincFields.LOINC_CODE_SYSTEM_NAME;	
				
				String version = "";
				if(filterMapping.indexOf("VERSION:")!=-1){
					version = filterMapping.split("VERSION:")[1];
				}
				String fileName = mappingName+"_"+codeSystem+"_"+version+"."+fileType;
				
//				exportMappingFromSolr(response, fileType, language, mappingName, filterMapping, listFields, codeSystem, fileName);
				exportFromSolr(response, filterMapping, codeSystem, fileType, language, listFields, fileName, mappingName);
			}
		}
		else if(mappingType.equals(MappingType.GENERIC.getKey())){
			exportGenericMappingFromDB(response, fileType, language, mappingName, filterMapping);
		}
	}
	

	private void exportFromSolr(final HttpServletResponse response,final  String matchvalue,  
			final String codesystem,final  String fileType,final  String language, final List<String> listFields, final String fileName, final String mappingName) {
		
		/*Recupero indice solr */
		String solrQueryBase = "";
		try {
			solrQueryBase = ExporterSearchUtil.getBaseUrlIndexSolr(codesystem);
		} catch (Exception e) {
			log.error("Errore durante il recupero del nome dell'indice SOLR.", e);
			throw new RuntimeException("Errore durante il recupero del nome dell'indice SOLR.");
		}
		
		try {
			if(!"".equals(solrQueryBase)){
	
				log.info("solrQueryBase::"+solrQueryBase);
				log.info("matchvalue::"+matchvalue);
				
				/* Queries snippet */
				String jsonObjectCount = SolrQueryUtil.executeRequest(solrQueryBase + matchvalue, true);
				JSONObject jsonObj = new JSONObject(jsonObjectCount);
				JSONObject responseJson = jsonObj.getJSONObject("response");
				Integer numFound = responseJson.getInt("numFound");
				Integer elemPerPage = ELEM_PAGINATION_SOLR;
				Integer numPage = numFound / elemPerPage + 1;
				Integer start = 0;
				
				log.info("elementi trovati ["+numFound+"] per ["+codesystem+"]");
				OutputStream downloadStream = response.getOutputStream();
				
				try {
					response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
					
					for (float idx = 0; idx <= numPage; idx++) {
						DecimalFormat df = new DecimalFormat("#.#");
						float percentualeDownlaod = (float) (idx/numPage)*100;
						if (percentualeDownlaod % 10 == 0) {
							log.info("downlaod ["+df.format(percentualeDownlaod)+"%]");
						}
						
						String urlPaginato = matchvalue + "&start=" + start + "&rows=" + elemPerPage;
						String responseSolr = SolrQueryUtil.executeRequest(solrQueryBase + urlPaginato, true);
						JSONArray docs = new JSONObject(responseSolr).getJSONObject("response").getJSONArray("docs");
	
						boolean firstElement = (idx==0);
						boolean lastElement = ((int)idx==numPage);
						try {
							File outFile = File.createTempFile(fileName, "_tmp"+idx);
							outFile.deleteOnExit();
							
							outFile = ExporterSearchUtil.generaFileFromJSONArray(outFile, language, fileType, docs, firstElement, lastElement, listFields, null);
	
							InputStream targetStream = new FileInputStream(outFile);
							try {
								ByteStreams.copy(targetStream,downloadStream);
							}catch (Exception e) {
								log.error("ERROR" + e.getLocalizedMessage(), e);
								
								downloadStream.flush();
								downloadStream.close();
							} finally {
								
								targetStream.close();
							}
							
							try { 
								/*Eliminqa il file temporaneo*/
								outFile.delete();
							} catch (Exception e) {
								log.error("ERROR" + e.getLocalizedMessage(), e);
							}
						} catch (Exception e) {
							log.error("ERROR" + e.getLocalizedMessage(), e);
						}
						
						/*valore paginazione query solr*/
						start = start + elemPerPage;
					}
				} finally {
					downloadStream.flush();
					downloadStream.close();
				}
			}
		} catch (Exception e) {
			log.error("Errore durante il download file.", e);
			throw new RuntimeException("Errore durante il download file.");
		}
	}
	
	

	
	
	
	
	
	
	
	

	private void exportMappingFromSolr(final HttpServletResponse response, final String fileType, final String language, 
			final String mappingName, final String filterMapping, final List<String> listFields, final String codeSystem, final String fileName) throws IOException {
		

		/*Recupero indice solr */
		String solrQueryBase = "";
		try {
			solrQueryBase = ExporterSearchUtil.getBaseUrlIndexSolr(codeSystem);
		} catch (Exception e) {
			log.error("Errore durante il recupero del nome dell'indice SOLR.", e);
			throw new RuntimeException("Errore durante il recupero del nome dell'indice SOLR.");
		}
		
		try {
			if(!"".equals(solrQueryBase)){
	
				log.info("solrQueryBase::"+solrQueryBase);
				log.info("filterMapping::"+filterMapping);
				
				/* Queries snippet */
				String jsonObjectCount = SolrQueryUtil.executeRequest(solrQueryBase + filterMapping, true);
				JSONObject jsonObj = new JSONObject(jsonObjectCount);
				JSONObject responseJson = jsonObj.getJSONObject("response");
				Integer numFound = responseJson.getInt("numFound");
				Integer elemPerPage = ELEM_PAGINATION_SOLR;
				Integer numPage = numFound / elemPerPage + 1;
				Integer start = 0;
				
				log.info("elementi trovati ["+numFound+"] per ["+mappingName+"]");
				OutputStream downloadStream = response.getOutputStream();
				
				try {
					response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
					
					for (float idx = 0; idx <= numPage; idx++) {
						DecimalFormat df = new DecimalFormat("#.#");
						float percentualeDownlaod = (float) (idx/numPage)*100;
						if (percentualeDownlaod % 10 == 0) {
							log.info("downlaod ["+df.format(percentualeDownlaod)+"%]");
						}
						
						String urlPaginato = filterMapping + "&start=" + start + "&rows=" + elemPerPage;
						String responseSolr = SolrQueryUtil.executeRequest(solrQueryBase + urlPaginato, true);
						JSONArray docs = new JSONObject(responseSolr).getJSONObject("response").getJSONArray("docs");
	
						boolean firstElement = (idx==0);
						boolean lastElement = ((int)idx==numPage);
						try {
							File outFile = File.createTempFile(fileName, "_tmp"+idx);
							outFile.deleteOnExit();
							
							outFile = ExporterSearchUtil.generaFileFromJSONArray(outFile, language, fileType, docs, firstElement, lastElement, listFields, mappingName);
	
							InputStream targetStream = new FileInputStream(outFile);
							try {
								ByteStreams.copy(targetStream,downloadStream);
							}catch (Exception e) {
								log.error("ERROR" + e.getLocalizedMessage(), e);
								
								downloadStream.flush();
								downloadStream.close();
							} finally {
								
								targetStream.close();
							}
							
							try { 
								/*Eliminqa il file temporaneo*/
								outFile.delete();
							} catch (Exception e) {
								log.error("ERROR" + e.getLocalizedMessage(), e);
							}
						} catch (Exception e) {
							log.error("ERROR" + e.getLocalizedMessage(), e);
						}
						
						/*valore paginazione query solr*/
						start = start + elemPerPage;
					}
				} finally {
					downloadStream.flush();
					downloadStream.close();
				}
			}
		} catch (Exception e) {
			log.error("Errore durante il download file.", e);
			throw new RuntimeException("Errore durante il download file.");
		}
		
	}

	
	
	private void exportGenericMappingFromDB(final HttpServletResponse response, final String fileType, final String language, final String mappingName, String filterMapping) throws IOException {
		String fields = "CODE_SYSTEM_SRC_NAME,CODE_SYSTEM_SRC_CODE,CODE_SYSTEM_SRC_DESCRIPTION,CODE_SYSTEM_TRG_NAME,CODE_SYSTEM_TRG_CODE,CODE_SYSTEM_TRG_DESCRIPTION,VERSION";
		List<String> listFields = Arrays.asList(fields.split(","));
		
		String fileName = ExporterSearchUtil.makeMappingFileName(mappingName,fileType);
		
		
		if(filterMapping==null){
			filterMapping="";
		}
		
		try {
			
			OutputDto countElement = extrasService.searchMapping(filterMapping,mappingName, 0, ELEM_PAGINATION_SOLR);
			Integer numFound = countElement.getNumFound().intValue();
			
			log.info("elementi trovati ["+numFound+"] per mapping ["+mappingName+"]");
			OutputStream downloadStream = response.getOutputStream();
			
			Integer elemPerPage = ELEM_PAGINATION_SOLR;
			Integer numPage = numFound / elemPerPage + 1;
			Integer start = 0;
			
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			for (int idx = 0; idx <= numPage; idx++) {
				
				
				OutputDto result = extrasService.searchMapping(filterMapping,mappingName, idx, ELEM_PAGINATION_SOLR);
				

				boolean firstElement = (idx==0);
				boolean lastElement = ((int)idx==numPage);
				try {
					String jsonObject = new Gson().toJson(result);
					JSONArray entry = new JSONObject(jsonObject).getJSONArray("entry");				
					
					

					JSONArray docs = new JSONArray();
					
					for (Object o : entry) {
						if (o instanceof JSONObject) {
							JSONObject document = (JSONObject) o;
							String srcNamespace = (String) document.getJSONObject("_subject").get("_namespace");
							String srcCode = (String) document.getJSONObject("_subject").get("_name");
							
							String trcNamespace = (String) document.getJSONObject("_predicate").get("_namespace");
							String trcCode = (String) document.getJSONObject("_predicate").get("_name");
							
							JSONObject jsonobject = new JSONObject();
							
							jsonobject.put("CODE_SYSTEM_SRC_NAME", srcNamespace);
							jsonobject.put("CODE_SYSTEM_SRC_CODE", srcCode);
							jsonobject.put("CODE_SYSTEM_TRG_NAME", trcNamespace);
							jsonobject.put("CODE_SYSTEM_TRG_CODE", trcCode);
							
							
							JSONArray associationQualifierList = document.getJSONArray("_associationQualifierList");
							for (Object aq : associationQualifierList) {
								if (aq instanceof JSONObject) {
									JSONObject associationQualifier = (JSONObject) aq;
									
									String predicateName = (String) associationQualifier.getJSONObject("_predicate").get("_name");

									if(predicateName.equals("sourceTitle_"+language)){
										String value = getValueByPredicateName(associationQualifier, predicateName);
										jsonobject.put("CODE_SYSTEM_SRC_DESCRIPTION", value);
									}

									 
									if(predicateName.equals("targetTitle_"+language)){
										String value = getValueByPredicateName(associationQualifier, predicateName);
										jsonobject.put("CODE_SYSTEM_TRG_DESCRIPTION", value);
									}
									
									if(predicateName.equals("releaseDate")){
										String value = getValueByPredicateName(associationQualifier, predicateName);
										jsonobject.put("VERSION", value);
									}
								}
							}
							docs.put(jsonobject);
						}
					}
					
					File outFile = File.createTempFile(fileName, "_tmp"+idx);
					outFile.deleteOnExit();

					outFile = ExporterSearchUtil.generaFileFromJSONArray(outFile, language, fileType, docs, firstElement, lastElement, listFields, null);

					InputStream targetStream = new FileInputStream(outFile);
					try {
						ByteStreams.copy(targetStream,downloadStream);
					}catch (Exception e) {
						log.error("ERROR" + e.getLocalizedMessage(), e);
						
						downloadStream.flush();
						downloadStream.close();
					} finally {
						
						targetStream.close();
					}
					
					try { 
						/*Eliminqa il file temporaneo*/
						outFile.delete();
					} catch (Exception e) {
						log.error("ERROR" + e.getLocalizedMessage(), e);
					}
				} catch (Exception e) {
					log.error("ERROR" + e.getLocalizedMessage(), e);
				}
				
				/*valore paginazione query*/
				start = start + elemPerPage;
			}
			
		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private File getDownloadFile(final String folder, final String codeSystemName, final String codeSystemVersion, final String variant, final String fileExtension) throws ExportException {

		File retVal = ExportUtil.getFileExport(folder, codeSystemName, codeSystemVersion, variant, fileExtension);

		if (retVal.exists() && retVal.isFile()) {
			return retVal;
		} else {
			throw new ExportException("Impossibile accedere al file: " + retVal.getAbsolutePath());
		}
	}
	
	
	private String getValueByPredicateName(JSONObject associationQualifier, String predicateName) {
		JSONArray values = associationQualifier.getJSONArray("_valueList");
		String value = "";
		for (Object v : values) {
			JSONObject oValue = (JSONObject) v;
			value = (String) oValue.getJSONObject("_literal").getJSONObject("_value").get("_content"); 
			log.debug("predicateName::"+predicateName+" -valueStr::"+value);
		}
		return value;
	}

	

	
}
