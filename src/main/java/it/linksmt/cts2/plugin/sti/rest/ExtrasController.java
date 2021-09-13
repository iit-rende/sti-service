package it.linksmt.cts2.plugin.sti.rest;

import it.linksmt.cts2.plugin.sti.dtos.OutputDto;
import it.linksmt.cts2.plugin.sti.dtos.SearchInputDto;
import it.linksmt.cts2.plugin.sti.service.ExtrasService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;


@Controller("extrasControllerProvider")
@RequestMapping("/extras")
public class ExtrasController implements ControllerProvider  {

	@Autowired
	private ExtrasService extrasService;
	
	@RequestMapping(value={"/test"}, method = RequestMethod.GET)
	@ResponseBody public Date test() {
		return extrasService.getDate();
	}
	
//	@RequestMapping(value="/cs")
//	@ResponseBody JsonArray getCodeSystems(@RequestParam(required=false, defaultValue="false") boolean localsOnly) throws StiHibernateException, StiAuthorizationException {
//		JsonArray jsonArray = extrasService.getCodeSystems(localsOnly);
//		return jsonArray;
//	}
	
	@RequestMapping(value="/cs")
	@ResponseBody JsonArray getCodeSystems(@RequestParam(required=false) List<String> listCsType) throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = extrasService.getCodeSystems(listCsType);
		return jsonArray;
	}
	
	
	@RequestMapping(value="/cs/{codeSystemId}")
	@ResponseBody JsonObject getCodeSystem(@PathVariable Long codeSystemId) throws StiHibernateException, StiAuthorizationException {
		JsonObject jsonObject = extrasService.getCodeSystemById(codeSystemId);
		return jsonObject;
	}
	
	@RequestMapping(value="/cs/{codeSystemId}/typeMapping")	
	@ResponseBody HashMap<String, String> getCodeSystemTypeMapping(@PathVariable Long codeSystemId) throws StiHibernateException, StiAuthorizationException {
		LinkedHashMap<String, String> retVal = new LinkedHashMap<String, String>();
		Type typeMap = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
		String json = extrasService.getCodeSystemTypeMapping(codeSystemId);
		if(null != json ){
			retVal = new Gson().fromJson(json, typeMap);
		}
		return retVal;
	}
	
	@RequestMapping(value="/cs/{codeSystemIdOrName}/versions")	
	@ResponseBody JsonArray getCodeSystemVersions(@PathVariable String codeSystemIdOrName) throws StiHibernateException, StiAuthorizationException {
		Long codeSystemId = null;
		String codeSystemName = null;
		codeSystemIdOrName = codeSystemIdOrName.trim().replaceAll("\\s","_");
		if( StringUtils.isNumericSpace(codeSystemIdOrName)){
			codeSystemId = Long.parseLong(codeSystemIdOrName);
		}
		else if(!StringUtils.isNumericSpace(codeSystemIdOrName)){
			codeSystemName = codeSystemIdOrName;
		}
		JsonArray jsonArray = extrasService.getActiveCodeSystemVersionsByIdOrName(codeSystemId,codeSystemName);
		return jsonArray;
	}
	
	
	
	@RequestMapping(value="/valueSets")
	@ResponseBody JsonArray getValueSets() throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = extrasService.getValueSets();
		return jsonArray;
	}
	
	@RequestMapping(value="/valueSets/{valueSetId}/versions")
	@ResponseBody JsonArray getValueSetVersion(@PathVariable Long valueSetId) throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = extrasService.getActiveValueSetVersions(valueSetId);
		return jsonArray;
	}
	
	
	@RequestMapping(value="/vs/{valueSetId}")
	@ResponseBody JsonObject getValueSet(@PathVariable Long valueSetId) throws StiHibernateException, StiAuthorizationException {
		JsonObject jsonObject = extrasService.getValueSetById(valueSetId);
		return jsonObject;
	}
	
	/*
	 * http://web30.linksmt.it/cts2framework/extras/mapping/search?format=json
		&page=1
		&num=10
		&sourceOrTargetEntity=
		&mapping=LOINC (2.34) - ATC (2014)
	*/
	@RequestMapping(value="/mapping/search")
	@ResponseBody OutputDto searchMapping(@ModelAttribute final SearchInputDto filter) throws StiHibernateException, StiAuthorizationException {
		OutputDto jsonObject = extrasService.searchMapping(filter.getSourceOrTargetEntity(), filter.getMapping(), filter.getPage(), filter.getMaxtoreturn());
		return jsonObject;
	}
	
	@RequestMapping(value="/mapping/list")
	@ResponseBody OutputDto getListMapping(@ModelAttribute final SearchInputDto filter) throws StiHibernateException, StiAuthorizationException {
		OutputDto jsonObject = extrasService.getListMapping();
		return jsonObject;
	}
	
	/*Parameters*/
	@RequestMapping(value="/cs/{codeSystemIdOrName}/parameters")	
	@ResponseBody JsonArray getCodeSystemParameters(@PathVariable String codeSystemIdOrName) throws StiHibernateException, StiAuthorizationException {
		Long codeSystemId = null;
		String codeSystemName = null;
		codeSystemIdOrName = codeSystemIdOrName.trim().replaceAll("\\s","_");
		if( StringUtils.isNumericSpace(codeSystemIdOrName)){
			codeSystemId = Long.parseLong(codeSystemIdOrName);
		}
		else if(!StringUtils.isNumericSpace(codeSystemIdOrName)){
			codeSystemName = codeSystemIdOrName;
		}
		JsonArray jsonArray = extrasService.getCodeSystemParamsByIdOrName(codeSystemId,codeSystemName,false);
		return jsonArray;
	}
	
	
	@RequestMapping(value="/vs/{valueSetIdOrName}/parameters")	
	@ResponseBody JsonArray getValueSetParameters(@PathVariable String valueSetIdOrName) throws StiHibernateException, StiAuthorizationException {
		Long valueSetId = null;
		String valueSetName = null;
		valueSetIdOrName = valueSetIdOrName.trim().replaceAll("\\s","_");
		if( StringUtils.isNumericSpace(valueSetIdOrName)){
			valueSetId = Long.parseLong(valueSetIdOrName);
		}
		else if(!StringUtils.isNumericSpace(valueSetIdOrName)){
			valueSetName = valueSetIdOrName;
		}
		JsonArray jsonArray = extrasService.getCodeSystemParamsByIdOrName(valueSetId,valueSetName,true);
		return jsonArray;
	}
	/*Parameters*/
	
	
	/*Extra fields*/
	@RequestMapping(value="/cs/{codeSystemIdOrName}/solrextrafields")	
	@ResponseBody JsonArray getCodeSystemSolrExtraField(@PathVariable String codeSystemIdOrName) throws StiHibernateException, StiAuthorizationException {
		Long codeSystemId = null;
		String codeSystemName = null;
		codeSystemIdOrName = codeSystemIdOrName.trim().replaceAll("\\s","_");
		if( StringUtils.isNumericSpace(codeSystemIdOrName)){
			codeSystemId = Long.parseLong(codeSystemIdOrName);
		}
		else if(!StringUtils.isNumericSpace(codeSystemIdOrName)){
			codeSystemName = codeSystemIdOrName;
		}
		JsonArray jsonArray = extrasService.getCodeSolrExtraFieldsByIdOrName(codeSystemId,codeSystemName,false);
		return jsonArray;
	}
	
	@RequestMapping(value="/vs/{valueSetIdOrName}/solrextrafields")	
	@ResponseBody JsonArray getValueSetSolrExtraField(@PathVariable String valueSetIdOrName) throws StiHibernateException, StiAuthorizationException {
		Long valueSetId = null;
		String valueSetName = null;
		valueSetIdOrName = valueSetIdOrName.trim().replaceAll("\\s","_");
		if( StringUtils.isNumericSpace(valueSetIdOrName)){
			valueSetId = Long.parseLong(valueSetIdOrName);
		}
		else if(!StringUtils.isNumericSpace(valueSetIdOrName)){
			valueSetName = valueSetIdOrName;
		}
		JsonArray jsonArray = extrasService.getCodeSolrExtraFieldsByIdOrName(valueSetId,valueSetName,true);
		return jsonArray;
	}
	/*Extra fields*/
	
	
	@RequestMapping(value="/languages/{name}/{versionName}")	
	@ResponseBody List<String> getLanguagesFromCs(@PathVariable String name,@PathVariable String versionName) throws StiHibernateException, StiAuthorizationException {
		List<String>  languages = extrasService.getLanguagesFromCs(name,versionName);
		return languages;
	}
	
	@RequestMapping(value="/values/{name}/{versionName}/{paramName}/{lang}")	
	@ResponseBody List<String> getLanguagesFromCs(@PathVariable String name,@PathVariable String versionName,@PathVariable String paramName,@PathVariable String lang) throws StiHibernateException, StiAuthorizationException {
		List<String>  values = extrasService.getValueFromParamCodeSystemVersion(name,versionName,paramName,lang);
		return values;
	}
	
	
	
	@RequestMapping(value="/domains")	
	@ResponseBody JsonArray getDomains() throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = extrasService.getAllDomains();
		return jsonArray;
	}
	
	@Override
	public Object getController() {
		return this;
	}

}
