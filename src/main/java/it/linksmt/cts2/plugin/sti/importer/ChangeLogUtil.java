package it.linksmt.cts2.plugin.sti.importer;

import it.linksmt.cts2.plugin.sti.db.commands.insert.InsertChangelog;
import it.linksmt.cts2.plugin.sti.db.commands.search.CountCodeSystemEntityVersionAssociationByMapSetVersionId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcepts;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemEntityVersionByEntityId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionEntityMembershipByCSVersionId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetMetadataParameterValue;
import it.linksmt.cts2.plugin.sti.db.commands.updates.UpdateChangelog;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionChangelog;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.dtos.ChangelogDto;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class ChangeLogUtil {
	private static Logger log = Logger.getLogger(ChangeLogUtil.class);
	
	
	public static String TYPE_MAPPING = "MAPPING";
	
	
	public static void addChangeLogCs(final HibernateUtil hibernateUtil, Long previousVersionId, LinkedHashMap<String, JsonObject> csvValues, CodeSystem cs, 
			CodeSystemVersion csVer,String languages)
			throws StiHibernateException, StiAuthorizationException, MalformedURLException, IOException, ProtocolException, ImportException {
		log.info("ChangeLogUtil::addChangeLogCs::start");
		
		List<CodeSystemVersionEntityMembership> csvems = new ArrayList<CodeSystemVersionEntityMembership>(0);

		Long versionId = null;
		if(previousVersionId!=null){
			log.debug("previousVersionId::not null");
			versionId = previousVersionId;
		}
		else{
			log.debug("previousVersionId::null and get current version");
			cs = (CodeSystem) hibernateUtil.executeBySystem(new  GetCodeSystemById(cs.getId()));
			versionId = cs.getCurrentVersionId();
		}
		log.debug("versionId::"+versionId);
		csvems = (List<CodeSystemVersionEntityMembership>) hibernateUtil.executeBySystem(new  GetCodeSystemVersionEntityMembershipByCSVersionId(versionId));
		
		if(null != csvems) {
		
			int newRows = 0;
			int deletedRows = 0;
			
			
			List<Long> codeSystemEntityVersionIds = new ArrayList<Long>();
			for(CodeSystemVersionEntityMembership cvem : csvems) {
				CodeSystemEntityVersion codeSystemEntityVersion = (CodeSystemEntityVersion) hibernateUtil.executeBySystem(new GetCodeSystemEntityVersionByEntityId(cvem.getId().getCodeSystemEntityId()));
				codeSystemEntityVersionIds.add(codeSystemEntityVersion.getVersionId());
				log.debug("codeSystemEntityVersion.getVersionId::"+codeSystemEntityVersion.getVersionId());
			}
			
			
			List<CodeSystemMetadataValue> paramValues = (List<CodeSystemMetadataValue>) hibernateUtil.executeBySystem(new GetMetadataParameterValue(codeSystemEntityVersionIds)); 
			List<CodeSystemConcept> concepts = (List<CodeSystemConcept>) hibernateUtil.executeBySystem(new GetCodeSystemConcepts(codeSystemEntityVersionIds));
			
			List<String> changedCodes = new ArrayList<String>();
			if(concepts!=null && concepts.size()>0){
				for (CodeSystemConcept concept: concepts) {
					
					//Codice esistente
					String codice = concept.getCode();
					
//					if(!csvValues.containsKey(codice.toUpperCase())) {
//						deletedRows ++ ;	
//					}
//					JsonObject currentVersionRow = csvValues.get(codice.toUpperCase());
					
					if(!csvValues.containsKey(codice)) {
						deletedRows ++ ;	
					}
					JsonObject currentVersionRow = csvValues.get(codice);
					JsonObject currentVersionRowCleanKey = new JsonObject();
					
					if(null != currentVersionRow) {
						/*Questa operazione serve ad uniformare le chiavi della mappa (nomi dei campi) passati in input con il formato standardizzato con in quale sono stati salvati nel DB*/
						Set<Entry<String, JsonElement>> entries = currentVersionRow.entrySet();
						for (Map.Entry<String, JsonElement> entry: entries) {
							if(entry!=null && entry.getKey()!=null && currentVersionRow!=null && currentVersionRow.get(entry.getKey())!=null){
								JsonElement element = currentVersionRow.get(entry.getKey());
								if(element!=null && !element.isJsonNull() && !(element instanceof JsonNull)) {
								    currentVersionRowCleanKey.addProperty(StiServiceUtil.paramNameToUpperCaseAndClean(entry.getKey()), element.getAsString());
								}
							}
						}
					}
					
					
					//log.debug("currentVersionRow::"+currentVersionRow.toString());
					//log.debug("currentVersionRowCleanKey::"+currentVersionRowCleanKey.toString());
					
					for(CodeSystemMetadataValue paramValue : paramValues) {
						if(concept.getCodeSystemEntityVersionId().compareTo(paramValue.getCodeSystemEntityVersion().getVersionId()) == 0){								
							MetadataParameter param =  paramValue.getMetadataParameter();
							if(null != param && currentVersionRowCleanKey.has(param.getParamName()) ) {
								String value = currentVersionRowCleanKey.get(param.getParamName()).getAsString();
								log.debug("paramName::"+param.getParamName()+" valueOld::"+paramValue.getParameterValue()+" valueNew::"+value);
								if(!value.equalsIgnoreCase(paramValue.getParameterValue())) {
									if(!changedCodes.contains(codice)){
										changedCodes.add(codice);
									}
								}
							}
						}
					}
					
				}
				
				
				Iterator<Entry<String, JsonObject>> it = csvValues.entrySet().iterator();
				while (it.hasNext()) {
					String csvCode = it.next().getKey();
					boolean found = false;
					for (CodeSystemConcept concept: concepts) {
						if(csvCode.equalsIgnoreCase(concept.getCode())) {
							found = true;
						}
					}
					if(!found) {
						newRows++;
					}
				}
				
			}
			
			//TODO verificare la correttezza dei dati contenuti in changedCodes
			//changedCodes = new ArrayList<String>(new LinkedHashSet<String>(changedCodes));
			log.info("ChangeLogUtil::changelog - I seguenti codici hanno subito dei cambiamenti " + changedCodes );
			
			CodeSystemVersion previousVersion = null;
			String previousVersionName = "";
			if(previousVersionId!=null){
				previousVersion = (CodeSystemVersion) hibernateUtil.executeBySystem(new GetCodeSystemVersionById(previousVersionId));
				previousVersionName = previousVersion.getName();
				
			}
			
			CodeSystemVersionChangelog changelog = (CodeSystemVersionChangelog) hibernateUtil.executeBySystem(new InsertChangelog(newRows, deletedRows, new Gson().toJson(changedCodes), cs ,csVer, previousVersion, new Date(), cs.getCodeSystemType()));
			log.info("ChangeLogUtil::changelog - Creato il changelog " + changelog.getId());
			
			ChangelogDto changelogDto = new ChangelogDto();
			changelogDto.setCodeSystem(cs.getName());
			changelogDto.setVersion(csVer.getName());
			changelogDto.setPreviousVersion(previousVersionName);
			changelogDto.setDateCreate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
			changelogDto.setDeletedCodes(deletedRows);
			changelogDto.setNewCodes(newRows);
			changelogDto.setImportedRow(csvValues.size());
			changelogDto.setChangedCodes(StringUtils.join(changedCodes, ';'));
			changelogDto.setType(cs.getCodeSystemType());
			changelogDto.setLanguages(languages);
			
			sendChangelog(hibernateUtil, changelog, changelogDto);
		}
	}


	
	public static void addChangeLogMapping(final HibernateUtil hibernateUtil,ChangelogDto changelogDto,boolean flagMappingGenerico)
			throws StiHibernateException, StiAuthorizationException, MalformedURLException, IOException, ProtocolException {
		
		CodeSystemVersionChangelog changelog = null;
		
		if(changelogDto.getVersion()!=null && !"".equals(changelogDto.getVersion())){
			String csVersion = changelogDto.getVersion();
			List<CodeSystemVersion> setVers = (List<CodeSystemVersion>) hibernateUtil.executeByUser(new GetCodeSystemVersionByName(csVersion), SessionUtil.getLoggedUser());	
		
			if ((setVers == null) || (setVers.size() != 1)) {
				throw new StiHibernateException("Impossibile leggere i dati della Versione Code System: " + csVersion);
			}
			
			CodeSystemVersion csVers = setVers.get(0);
			CodeSystem cs = csVers.getCodeSystem();
			
			
			
			if(changelogDto.getVersionTo()!=null && !"".equals(changelogDto.getVersionTo())){ 
				String csVersionTo = changelogDto.getVersionTo();
				List<CodeSystemVersion> setVersTo = (List<CodeSystemVersion>) hibernateUtil.executeByUser(new GetCodeSystemVersionByName(csVersionTo), SessionUtil.getLoggedUser());	

				if ((setVersTo == null) || (setVersTo.size() != 1)) {
					throw new StiHibernateException("Impossibile leggere i dati della Versione Code System: " + csVersionTo);
				}
				
				CodeSystemVersion csVersTo = setVersTo.get(0);
				CodeSystem csTo = csVersTo.getCodeSystem();
				
				changelog = (CodeSystemVersionChangelog) hibernateUtil.executeBySystem(new InsertChangelog(0, 0, "", cs ,csVers, null , new Date(), changelogDto.getType(), csTo, csVersTo));
			}
			else{
				changelog = (CodeSystemVersionChangelog) hibernateUtil.executeBySystem(new InsertChangelog(0, 0, "", cs ,csVers, null , new Date(), changelogDto.getType(),null,null));
			}
		}
		else{
			changelog = (CodeSystemVersionChangelog) hibernateUtil.executeBySystem(new InsertChangelog(0, 0, "", null ,null, null, new Date(), changelogDto.getType(), null, null));
		}
		log.info("ChangeLogUtil::changelog - Creato il changelog " + changelog.getId());
		
		if(changelogDto.getTitle()!=null && flagMappingGenerico){
			changelogDto.setTitle(StiServiceUtil.makeMappingName(changelogDto.getTitle()));
		}
		
		sendChangelog(hibernateUtil, changelog, changelogDto);
	}


	


	private static void sendChangelog(final HibernateUtil hibernateUtil, CodeSystemVersionChangelog changelog, ChangelogDto changelogDto) throws MalformedURLException, IOException, ProtocolException,
			StiHibernateException, StiAuthorizationException {
		log.info("sendChangelog::"+changelogDto.getType());
		String changecodeUrl = StiServiceUtil.SERVER_URL + "/sti-gestione-portlet/api/manage/changelogs";
		
		try {
			
			URL url = new URL(changecodeUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Accept", "application/json");
			
			con.setDoOutput(true);
			
			OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());
			wr.write(new Gson().toJson(changelogDto));
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			
			//log.info("ChangeLogUtil::changelog - Creazione Journal Article = " + responseCode);
			
			if(responseCode == HttpStatus.SC_CREATED) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				log.info("ChangeLogUtil::changelog - Creazione Journal Article = " + response);
				changelog.setArticleId(Long.valueOf(response.toString()));
				changelog = (CodeSystemVersionChangelog) hibernateUtil.executeBySystem(new UpdateChangelog(changelog));
			}
			con.disconnect();
		}
		finally { }
	}
	
	

}
