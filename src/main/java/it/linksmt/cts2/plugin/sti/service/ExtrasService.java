package it.linksmt.cts2.plugin.sti.service;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemLanguages;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemMetadataParameters;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByIdAlt;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionsByCSIdAndStatus;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetDomainByState;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetExtraMetadataParameterValueByCsAndParamName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetListValueFromParameterAndCodeSystemAndVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetMapSetVersionAlt;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetValueSetById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetValueSetVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetValueSetVersionsByVSIdAndStatus;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchCodeSystems;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchMapping;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchValueSet;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.Domain;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.dtos.OutputDto;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.service.core.DocumentedNamespaceReference;
import edu.mayo.cts2.framework.model.core.SourceReference;

@Component
public class ExtrasService extends AbstractStiService {
	
	private static Logger log = Logger.getLogger(ExtrasService.class);

	
	private JsonParser parser = new JsonParser();
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public Date getDate() {
		return new Date();
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public JsonArray getCodeSystems(List<String> listCsType) throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = new JsonArray();
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		String csType = null;
		List<CodeSystem> css = new ArrayList<CodeSystem>(0);
		if(listCsType!=null && listCsType.size()>0){
			for (String type : listCsType) {
				List<CodeSystem> cssTmp = (List<CodeSystem>) hibernateUtil.executeBySystem(new SearchCodeSystems(null, true, csType));
				css.addAll(cssTmp);
			}
		}

		if(null != css && css.size()>0) {
			for(CodeSystem cs: css) {
				//JsonObject jsonObject = new JsonObject();
				
				/**
				 * Purtroppo, non si può evitare
				 * di eseguire una query per ogni 
				 * code system.
				 */
				if(null != cs.getCurrentVersionId()){
					JsonObject jsonObject = populateCodeSystemObject(hibernateUtil, cs); 
					if(jsonObject!=null && jsonObject.get("codeSystemName")!=null){
						jsonArray.add(jsonObject);
					}
				}
			}
		}
		return jsonArray;
	}


	
	public JsonObject getCodeSystemById(Long codeSystemId) throws StiHibernateException, StiAuthorizationException {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		CodeSystem cs = (CodeSystem) hibernateUtil.executeBySystem(new GetCodeSystemById(codeSystemId));
		JsonObject jsonObject = new JsonObject();
		if(null != cs){
			jsonObject = populateCodeSystemObject(hibernateUtil, cs); 
		}
		return jsonObject;
	}
	
	
	public JsonObject getValueSetById(Long valueSetId) throws StiHibernateException, StiAuthorizationException {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		
		ValueSet vs = (ValueSet) hibernateUtil.executeBySystem(new GetValueSetById(valueSetId));
		JsonObject jsonObject = new JsonObject();
		if(null != vs){
			jsonObject = populateValueSetObject(hibernateUtil, vs); 
		}
		return jsonObject;
	}
	
	public String getCodeSystemTypeMapping(Long csId) throws StiHibernateException, StiAuthorizationException {
		String retVal = null;
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		ExtraMetadataParameter mp = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(csId, StandardLocalFields.CS_TYPE_MAPPING));
		if(null != mp) {
			retVal = mp.getParamValue();
		}
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	public JsonArray getActiveCodeSystemVersionsByIdOrName(Long csId, String csName) throws StiHibernateException, StiAuthorizationException {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		
		if(csId==null && csName!=null){
			List<CodeSystem> codeSystems = (List<CodeSystem>)hibernateUtil.executeBySystem(new GetCodeSystemByName(csName));
			if(codeSystems!=null && codeSystems.size()>0 && codeSystems.get(0)!=null && codeSystems.get(0).getId()!=null){
				CodeSystem cs = codeSystems.get(0);
				if(cs!=null && cs.getId()!=null){
					csId =  cs.getId();
				}
			}
		}
		
		List<CodeSystemVersion> versions = (List<CodeSystemVersion>) hibernateUtil.executeBySystem(new GetCodeSystemVersionsByCSIdAndStatus(csId, StiConstants.STATUS_CODES.ACTIVE.getCode()));
		JsonArray array = new JsonArray();
		if(null != versions){
			for(CodeSystemVersion version : versions){				
				JsonObject obj = new JsonObject();
				obj.addProperty("id", version.getVersionId());
				obj.addProperty("name", version.getName());
				obj.addProperty("namespace", version.getCodeSystem().getName());
				if(version.getReleaseDate()!=null){
					obj.addProperty("releaseDate", dateFormat.format(version.getReleaseDate()));
				}
				array.add(obj);
			}
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public JsonArray getValueSets() throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = new JsonArray();
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		List<ValueSet> valueSets = (List<ValueSet>) hibernateUtil.executeBySystem(new SearchValueSet(null));
		if(null != valueSets) {
			for(ValueSet vs : valueSets) {
				JsonObject jsonObject = populateValueSetObject(hibernateUtil, vs);
				if(jsonObject!=null && jsonObject.get("valueSetName")!=null){
					jsonArray.add(jsonObject);
				}
			}	
		}
		return jsonArray;
	}


	
	@SuppressWarnings("unchecked")
	public JsonArray getActiveValueSetVersions(Long vsId) throws StiHibernateException, StiAuthorizationException {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		List<ValueSetVersion> versions = (List<ValueSetVersion>) hibernateUtil.executeBySystem(new GetValueSetVersionsByVSIdAndStatus(vsId, StiConstants.STATUS_CODES.ACTIVE.getCode()));
		JsonArray array = new JsonArray();
		if(null != versions){
			for(ValueSetVersion version : versions){				
				JsonObject obj = new JsonObject();
				obj.addProperty("id", version.getVersionId());
				obj.addProperty("name", version.getName());
				obj.addProperty("namespace", version.getValueSet().getName());
				if(version.getReleaseDate()!=null){
					obj.addProperty("releaseDate", dateFormat.format(version.getReleaseDate()));
				}
				
				array.add(obj);
			}
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public OutputDto searchMapping(String sourceOrTargetEntity,String mapping,Integer page,Integer num) throws StiHibernateException, StiAuthorizationException {
		OutputDto outputDto = (OutputDto) StiServiceProvider.getHibernateUtil().executeBySystem(
					new SearchMapping(sourceOrTargetEntity, mapping, page, num, this));
		return outputDto;
	}
	
	@SuppressWarnings("unchecked")
	public OutputDto getListMapping() throws StiHibernateException, StiAuthorizationException {
		OutputDto outputDto = (OutputDto) StiServiceProvider.getHibernateUtil().executeBySystem(new GetMapSetVersionAlt());
		return outputDto;
	}
	
	public List<String> getValueFromParamCodeSystemVersion(String name,String version,String paramName,String lang) {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		List<String> valori = new ArrayList<String>(0);
		try {
			valori =  (List<String>) hibernateUtil.executeBySystem(new GetListValueFromParameterAndCodeSystemAndVersion(name,version,paramName,lang));
		} catch (StiHibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StiAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return valori;
	}
	
	
	
	/**
	 * Gte the codesystem metadata parameters
	 * @param name
	 * @param isValueSet
	 * @return
	 * @throws StiHibernateException
	 * @throws StiAuthorizationException
	 */
	@SuppressWarnings("unchecked")
	public JsonArray getCodeSystemParamsByIdOrName(Long id, String name, boolean isValueSet) throws StiHibernateException, StiAuthorizationException {
		List<MetadataParameter> metadataParameters = getAllMetadataParametersByIdOrName(id, name, isValueSet);
		JsonArray array = new JsonArray();
		if(null != metadataParameters){
			for(MetadataParameter parameter : metadataParameters){				
				JsonObject obj = new JsonObject();
				obj.addProperty("name", parameter.getParamName());
				obj.addProperty("lang", parameter.getLanguageCd());
				if(parameter.getPosition()!=null){
					obj.addProperty("position", parameter.getPosition());
				}
				array.add(obj);
			}
		}
		return array;
	}
	
	
	/**
	 * Format metadata parameters like solr
	 * @param name
	 * @param isValueSet
	 * @return
	 * @throws StiHibernateException
	 * @throws StiAuthorizationException
	 */
	@SuppressWarnings("unchecked")
	public JsonArray getCodeSolrExtraFieldsByIdOrName(Long id, String name, boolean isValueSet) throws StiHibernateException, StiAuthorizationException {
		List<MetadataParameter> metadataParameters = getAllMetadataParametersByIdOrName(id, name, isValueSet);
		
		JsonArray array = new JsonArray();
		
		if(null != metadataParameters){
			for(MetadataParameter parameter : metadataParameters){
				JsonObject obj = new JsonObject();
				String metadataType = (parameter.getParamDatatype() != null ? parameter.getParamDatatype() : MetadataParameterType.STRING.getKey());
				String paramName = parameter.getParamName();
				String lang = parameter.getLanguageCd().toLowerCase();
				
				if (metadataType.equalsIgnoreCase(MetadataParameterType.STRING.getKey())) {
					paramName = MetadataParameterType.STRING.getPrefix() + paramName;
				} else if (metadataType.equalsIgnoreCase(MetadataParameterType.DATE.getKey())) {
					paramName = MetadataParameterType.DATE.getPrefix() + paramName;
				} else if (metadataType.equalsIgnoreCase(MetadataParameterType.NUMBER.getKey())) {
					paramName = MetadataParameterType.NUMBER.getPrefix() + paramName;
				} else if (metadataType.equalsIgnoreCase(MetadataParameterType.MAPPING.getKey())) {
					paramName = MetadataParameterType.MAPPING.getPrefix() + paramName;
				}
				
				paramName = paramName + "_" +lang;
				obj.addProperty("name", paramName);
				obj.addProperty("lang", lang);
				if(parameter.getPosition()!=null){
					obj.addProperty("position", parameter.getPosition());
				}
				array.add(obj);
			}
		}
		
		return array;
	}




	@SuppressWarnings("unchecked")
	private List<MetadataParameter> getAllMetadataParametersByIdOrName(Long id, String name, boolean isValueSet) throws StiHibernateException, StiAuthorizationException {
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		List<MetadataParameter> metadataParameters = new ArrayList<MetadataParameter>(0);
		CodeSystem cs = null;
		
		if(name!=null){
			List<CodeSystem> codeSystems = (List<CodeSystem>)hibernateUtil.executeBySystem(new GetCodeSystemByName(name));
			if(codeSystems!=null && codeSystems.size()>0 && codeSystems.get(0)!=null && codeSystems.get(0).getId()!=null){
				cs = codeSystems.get(0);
			}
		}
		else {
			if(id != null){
				cs = (CodeSystem)hibernateUtil.executeBySystem(new GetCodeSystemById(id));
			}
		}
		
		
		if(!isValueSet && cs!=null && cs.getId()!=null){
			List<MetadataParameter> metadataParametersIt = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_IT));
			List<MetadataParameter> metadataParametersEn = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_EN));
			metadataParameters.addAll(metadataParametersIt);
			metadataParameters.addAll(metadataParametersEn);
		}
		else{
			if(cs!=null && cs.getId()!=null && cs.getCodeSystemType().equals(CodeSystemType.VALUE_SET.getKey())){
				List<MetadataParameter> metadataParametersIt = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_IT));
				List<MetadataParameter> metadataParametersEn = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_EN));
				metadataParameters.addAll(metadataParametersIt);
				metadataParameters.addAll(metadataParametersEn);
			}
		}
		return metadataParameters;
	}
	
	
	
	private JsonObject populateCodeSystemObject(HibernateUtil hibernateUtil, CodeSystem cs) throws StiHibernateException, StiAuthorizationException {
		JsonObject jsonObject =null;
		if(cs!=null){
			jsonObject = new JsonObject();
			CodeSystemVersion currentVersion = (CodeSystemVersion) hibernateUtil.executeBySystem(new GetCodeSystemVersionById(cs.getCurrentVersionId()));
			ExtraMetadataParameter mappingParam = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_TYPE_MAPPING));
			if(null != mappingParam) {
				JsonObject typeMappingJson = parser.parse(mappingParam.getParamValue()).getAsJsonObject();
				jsonObject.add("typeMapping", typeMappingJson);
			}
			
			boolean isClassification = false;
			ExtraMetadataParameter extraSubType = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_SUBTYPE));
			if(null != extraSubType) {
				isClassification = extraSubType.getParamValue().equals(StandardLocalFields.SUB_TYPE_CLASSIFICATION);
			}
			/*Per i codesystem già esistenti ICD9_CM e ATC il flag è isClassification è di default a true*/
			if(cs.getName().equals(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME) || cs.getName().equals(AtcAicFields.ATC_CODE_SYSTEM_NAME)){
				isClassification=true;
			}
			
			
			ExtraMetadataParameter organization = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.ORGANIZATION));
			ExtraMetadataParameter domain = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.DOMAIN));
			ExtraMetadataParameter hasOntology = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_HAS_ONTOLOGY));
			ExtraMetadataParameter ontologyName = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_ONTOLOGY_NAME));

			
			String releaseDate = "";
			 if( currentVersion.getReleaseDate()!=null){
				 try{
					 releaseDate = dateFormat.format( currentVersion.getReleaseDate());
				 }
				 catch(Exception e){
					 log.error("ERROR"+e.getLocalizedMessage());
				 }
			 }
			
			jsonObject.addProperty("codeSystemName", cs.getName());
			jsonObject.addProperty("codeSystemId", cs.getId());
			jsonObject.addProperty("currentVersion", currentVersion.getName());
			jsonObject.addProperty("isClassification", isClassification);
			jsonObject.addProperty("domain", (domain!=null?domain.getParamValue():""));
			jsonObject.addProperty("organization",  (organization!=null?organization.getParamValue():""));
			jsonObject.addProperty("type", (cs.getCodeSystemType()!=null)?cs.getCodeSystemType():"");
			jsonObject.addProperty("subType",(extraSubType!=null && extraSubType.getParamValue()!=null?extraSubType.getParamValue():""));
			jsonObject.addProperty("hasOntology",  (hasOntology!=null?hasOntology.getParamValue():""));
			jsonObject.addProperty("ontologyName",  (ontologyName!=null?ontologyName.getParamValue():""));
			jsonObject.addProperty("description", (cs.getDescription()!=null)?cs.getDescription():"" );
			jsonObject.addProperty("releaseDate", releaseDate );
		}
		
		return jsonObject;
	}
	
	




	private JsonObject populateValueSetObject(HibernateUtil hibernateUtil, ValueSet vs) throws StiHibernateException, StiAuthorizationException {
		JsonObject jsonObject = null;
		if(null != vs.getCurrentVersionId()){
			jsonObject = new JsonObject();
			ValueSetVersion vsVersion = (ValueSetVersion) hibernateUtil.executeBySystem(new GetValueSetVersionById(vs.getCurrentVersionId()));
			
			if(null != vsVersion && null != vsVersion.getVirtualCodeSystemVersionId()) {
			
				CodeSystemVersion csVersion = (CodeSystemVersion)hibernateUtil.executeBySystem(new GetCodeSystemVersionByIdAlt(vsVersion.getVirtualCodeSystemVersionId()));
				if(csVersion!=null && csVersion.getCodeSystem()!=null && csVersion.getCodeSystem().getId()!=null){
					CodeSystem cs = csVersion.getCodeSystem();
					ExtraMetadataParameter mp = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_TYPE_MAPPING));
					if(null != mp) {
						JsonObject j = parser.parse(mp.getParamValue()).getAsJsonObject();
						jsonObject.add("typeMapping", j);
					}
					
					ExtraMetadataParameter organization = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.ORGANIZATION));
					ExtraMetadataParameter domain = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.DOMAIN));
					
					
					String releaseDate = "";
					 if( vsVersion.getReleaseDate()!=null){
						 try{
							 releaseDate = dateFormat.format( vsVersion.getReleaseDate());
						 }
						 catch(Exception e){
							 log.error("ERROR"+e.getLocalizedMessage());
						 }
					 }
					 
					
					jsonObject.addProperty("valueSetName", vs.getName());
					jsonObject.addProperty("valueSetId", vs.getId());
					jsonObject.addProperty("currentVersion", vsVersion.getName());
					jsonObject.addProperty("domain", (domain!=null?domain.getParamValue():""));
					jsonObject.addProperty("organization",  (organization!=null?organization.getParamValue():""));
					jsonObject.addProperty("type", (cs.getCodeSystemType()!=null)?cs.getCodeSystemType():"");
					jsonObject.addProperty("description", (vs.getDescription()!=null)?vs.getDescription():"" );
					jsonObject.addProperty("releaseDate", releaseDate );
				}
				
			}
		}
		return jsonObject;
	}
	
	

	/**
	 * @return
	 * @throws StiHibernateException
	 * @throws StiAuthorizationException
	 */
	@SuppressWarnings("unchecked")
	public JsonArray getAllDomains() throws StiHibernateException, StiAuthorizationException {
		JsonArray jsonArray = new JsonArray();
		HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
		List<Domain> emp1 = (List<Domain>) hibernateUtil.executeBySystem(new GetDomainByState(StiConstants.STATUS_CODES.ACTIVE.getCode()));
		for (Domain domain : emp1) {
			JsonObject jsonObject = new JsonObject(); 
			jsonObject.addProperty("key", domain.getKey());
			jsonObject.addProperty("name", domain.getName());
			jsonObject.addProperty("position", domain.getPosition());
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<String>  getLanguagesFromCs(String name, String versionName){
		try{
			List<String> listaLingue = (List<String>) StiServiceProvider.getHibernateUtil().executeBySystem(new GetCodeSystemLanguages(name, versionName));
			return listaLingue;
		}
		catch(Exception e){
			log.error("ERROR::"+e.getLocalizedMessage());
			return null;
		}
	}

	

	
	@Override
	public SourceReference getServiceProvider() {
		return null;
	}
	@Override
	public List<DocumentedNamespaceReference> getKnownNamespaceList() {
		return null;
	}
	@Override
	public OpaqueData getServiceDescription() {
		return null;
	}
	
	
}
