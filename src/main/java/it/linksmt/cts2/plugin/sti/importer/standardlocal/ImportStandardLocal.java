package it.linksmt.cts2.plugin.sti.importer.standardlocal;

import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateCodeSystem;
import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateCodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateMetadataParameter;
import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateStandardLocal;
import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateValueSet;
import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateValueSetVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.CountCodeSystemEntityVersionAssociationByMapSetVersionId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcepts;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemEntityVersionByEntityId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemMetadataParameters;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionEntityMembershipByCSVersionId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetExtraMetadataParameterValueByCsAndParamName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetMetadataParameterValue;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchValueSet;
import it.linksmt.cts2.plugin.sti.db.commands.updates.UpdateValueSet;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.dtos.ChangelogDto;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.importer.ChangeLogUtil;
import it.linksmt.cts2.plugin.sti.importer.mapset.ImportAutomaticMapSet;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

public final class ImportStandardLocal {

	private static Logger log = Logger.getLogger(ImportStandardLocal.class);

	private ImportStandardLocal() {

	}

	/**
	 * TODO Differenziare parametri del codesystem (nome, descrizione, ecc.) da parametri di versione (descrizione ecc.)
	 * @param hibernateUtil
	 * @param csvFileIt
	 * @param csvFileEn
	 * @param name
	 * @param version
	 * @param csDescription
	 * @param releaseDate
	 * @param oid
	 */
	@SuppressWarnings("unchecked")
	public static void importNewVersion(final HibernateUtil hibernateUtil, final File csvFileIt, final File csvFileEn, 
			final String name, final String version, final String csDescription, final Date releaseDate, final String oid, 
			final String versionDescription, 
			final String domain, final String organization,  final String type, final String subType,
			final String hasOntology, final String ontologyName, final String fileNameIt,final String fileNameEn,
			LinkedHashMap<String,String> parameterTypeMap, HashMap<String, String> codificationMap) throws ImportStandardLocalException {
		
			try {
				
				
				
				String FIELD_CODE = null;
				String FIELD_DESCRIPTION = null;
				String FIELD_CODE_TRANSLATE = null;
				String FIELD_DESCRIPTION_TRANSLATE = null;
				
				
				Iterator<Entry<String, String>> parameterTypeIterator = parameterTypeMap.entrySet().iterator();
				while (parameterTypeIterator.hasNext()) {
					Entry<String, String> entry = parameterTypeIterator.next();
					String key = entry.getKey();
					String value = entry.getValue();
					switch (value.toLowerCase()) {
					case "code":
						FIELD_CODE = key;
						break;
					case "description":
						FIELD_DESCRIPTION =  key;
						break;
					default:
						break;
					}
				}
	
				
				if(FIELD_CODE!=null || FIELD_DESCRIPTION!=null){
					
				
				
				/**
				 * Lettura del CSV
				 */
				
				String langPrimary = StiConstants.LANG_IT;
				String langTraslate = StiConstants.LANG_EN;
				File csvFilePrimary = csvFileIt;
				File csvFileTranslate = csvFileEn;
				if(csvFileIt==null && csvFileEn!=null){
					 langPrimary = StiConstants.LANG_EN;
					 langTraslate = null;
					 csvFilePrimary = csvFileEn;
					 csvFileTranslate = null;
				}
				
				
				CSVReader readerCsvPrimary = null;
				CSVReader readerCsvTranslate = null;
				List<String[]> recordsPrimary = new ArrayList<String[]>(0);
				List<String[]> recordsTranslate = new ArrayList<String[]>(0);
				
		
				if(csvFilePrimary!=null){
					readerCsvPrimary = new CSVReader(new InputStreamReader(new FileInputStream(csvFilePrimary),   "ISO-8859-1"), ';', CSVParser.DEFAULT_QUOTE_CHARACTER);
					recordsPrimary = readerCsvPrimary.readAll();
				}
				if(csvFileTranslate!=null){
					readerCsvTranslate = new CSVReader(new InputStreamReader(new FileInputStream(csvFileTranslate),  "ISO-8859-1"), ';', CSVParser.DEFAULT_QUOTE_CHARACTER);
					recordsTranslate = readerCsvTranslate.readAll();
				}	
				
				
				int rowsIndex = 0;
				String[] headerPrimary = null;
				LinkedHashMap<String, JsonObject> csvValues = new LinkedHashMap<String, JsonObject>();
				
				int rowsIndexTranslate = 0;
				String[] headerTranslate = null;
				LinkedHashMap<String, JsonObject> csvValuesTranslate = new LinkedHashMap<String, JsonObject>();
				
				
				/*Dati CSV Primary*/
				for (String[] record : recordsPrimary) {
					if(record[0]!=null && !"".equals(record[0]) && record.length>1){
						if(0 == rowsIndex) {
							headerPrimary = record;
						}else {
							String[] values = record;
							int valuesIndex = 0;
							JsonObject obj = new JsonObject();
							String codificationCode = "";
							for (String v : values){
								if(valuesIndex<headerPrimary.length){
									String headerCol = headerPrimary[valuesIndex];
									if(headerCol.equalsIgnoreCase(FIELD_CODE)) {
										codificationCode = v;
									}
									obj.addProperty(headerCol, StiServiceUtil.cleanValCsv(v));
									valuesIndex ++;
								}
								
							}
							csvValues.put(codificationCode, obj);
						}
						rowsIndex++;
					}
				}
				
				
				int positionCode = 0;
				int positionDescription = 0;
				int count = 0;
				for (String campo : headerPrimary) {
					if(campo.equalsIgnoreCase(FIELD_CODE)){
						positionCode=count;
					}
					if(campo.equalsIgnoreCase(FIELD_DESCRIPTION)){
						positionDescription=count;
					}
					count++;
				}
				log.debug("positionCode::"+positionCode);
				log.debug("positionDescription::"+positionDescription);
				
				
				if(recordsTranslate!=null && recordsTranslate.size()>0 && recordsTranslate.get(0)!=null){
					headerTranslate = recordsTranslate.get(0);
					FIELD_CODE_TRANSLATE = headerTranslate[positionCode];
					FIELD_DESCRIPTION_TRANSLATE = headerTranslate[positionDescription];
				}
				
			
				
				log.debug("FIELD_CODE::"+FIELD_CODE);
				log.debug("FIELD_DESCRIPTION::"+FIELD_DESCRIPTION);
				log.debug("FIELD_CODE_TRANSLATE::"+FIELD_CODE_TRANSLATE);
				log.debug("FIELD_DESCRIPTION_TRANSLATE::"+FIELD_DESCRIPTION_TRANSLATE);
	
				
				/*Dati Csv di Translate*/
				for (String[] recordTranslate : recordsTranslate) {
					if(recordTranslate[0]!=null && !"".equals(recordTranslate[0]) && recordTranslate.length>1){
						if(rowsIndexTranslate > 0) {
							String[] values = recordTranslate;
							int valuesIndex = 0;
							JsonObject obj = new JsonObject();
							String codificationCode = "";
							for (String v : values){
								if(valuesIndex<headerTranslate.length){
									String headerCol = headerTranslate[valuesIndex];
									if(headerCol.equalsIgnoreCase(FIELD_CODE_TRANSLATE)) {
										codificationCode = v;
									}
									obj.addProperty(headerCol, StiServiceUtil.cleanValCsv(v));
									valuesIndex ++;
								}
							}
							csvValuesTranslate.put(codificationCode, obj);
							
						}
						rowsIndexTranslate++;
					}
				}
				
				
				LinkedHashMap<Integer, String> mappingPositionsTypologiesFieldHeader = new LinkedHashMap<Integer, String>();
				int countType = 0;
				for (String h : headerPrimary) {
					if(!h.equalsIgnoreCase(FIELD_CODE) && !h.equalsIgnoreCase(FIELD_DESCRIPTION)) {
						mappingPositionsTypologiesFieldHeader.put(countType, parameterTypeMap.get(h));
						//log.info(countType+") "+parameterTypeMap.get(h)+ " campo:"+h);
						countType++;
					}
				}
				
				
				if(readerCsvPrimary!=null){
					readerCsvPrimary.close();
				}
				if(readerCsvTranslate!=null){
					readerCsvTranslate.close();
				}
				
				
				CodeSystem cs = null;
				List<CodeSystem> css = (List<CodeSystem>) hibernateUtil.executeBySystem(new GetCodeSystemByName(name));
				
				boolean isClassification = false;	
				
				
				/*Inserimento del CodeSystem*/
				if(null == css || css.isEmpty()) {
					log.info("No versions for " + name + ". Adding code system");
					cs = (CodeSystem) hibernateUtil.executeBySystem(new CreateCodeSystem(name, csDescription, releaseDate, type));
					
					int metadataParametersCount = addParameter(hibernateUtil, parameterTypeMap, FIELD_CODE, FIELD_DESCRIPTION, langPrimary, headerPrimary, cs);
					
					if(headerTranslate!=null){
						int metadataTraslateParametersIndex = 0;
						for(String h : headerTranslate) {
							if(!h.equalsIgnoreCase(FIELD_CODE_TRANSLATE) && !h.equalsIgnoreCase(FIELD_DESCRIPTION_TRANSLATE)) {
								String parameterType = mappingPositionsTypologiesFieldHeader.get(metadataTraslateParametersIndex);
								//log.info("param translate::"+h+" parameterType::"+parameterType);
								hibernateUtil.executeBySystem(new CreateMetadataParameter(h, cs, parameterType, null, langTraslate, null, null, null, metadataTraslateParametersIndex));
								metadataParametersCount++;
								metadataTraslateParametersIndex++;
							}
						}
					}
					
					
					log.info("Inseriti " + metadataParametersCount + " nuovi metadata ");
					
					Gson gson = new Gson();
					
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.DOMAIN, cs, "string", null, domain));
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.ORGANIZATION, cs, "string", null, organization));
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_TYPE_MAPPING, cs, "string", null, gson.toJson(parameterTypeMap)));
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_CODIFICATION_MAPPING, cs, "string", null, gson.toJson(codificationMap)));
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_HAS_ONTOLOGY, cs, "string", null, hasOntology!=null && !"".equals(hasOntology)?hasOntology:"N" ));
					if(ontologyName!=null && !"".equals(ontologyName)){
						hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_ONTOLOGY_NAME, cs, "string", null, ontologyName));
					}
	
					if(!type.equals(CodeSystemType.VALUE_SET.getKey())) {
						hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_TYPE, cs, "string", null, type));
						hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_SUBTYPE, cs, "string", null, subType));
					}
					
					if(subType!=null){
						isClassification = subType.equals(StandardLocalFields.SUB_TYPE_CLASSIFICATION);
					}
				}else {
					
					cs = css.get(0);
					//previousVersionId = cs.getCurrentVersionId();
					
					/**
					 * TODO 
					 * Verificare che la versione del codeSystem inserito
					 * sia diversa da tutte le altre versioni del codeSystem
					 */
	
					List<MetadataParameter> csMetadataParametersIt = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_IT));
					List<MetadataParameter> csMetadataParametersEn = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_EN));
	
					/*controllo se nella versione precedente sono stati inseriti parametri in italiano o in inglese*/
					if(null != csMetadataParametersIt || null != csMetadataParametersEn) {
						/**
						 * Controllo che il numero di metadati della versione precedente (+2 = codice e descrizione, salvati nella tabella codeSystemEntity)
						 * sia uguale al numero di metadati (incluso codice e descrizione) della nuova versione
						 */
						List<String> unmatchedColumns = new ArrayList<String>();
						
						
						List<MetadataParameter> csMetadataParameters = null;
						
						/*controllo se sto inserendo come file primario l'italiano e se sono presenti i parametri in italiano nella versione precedente*/
						if(langPrimary.equals(StiConstants.LANG_IT) && csMetadataParametersIt!=null && csMetadataParametersIt.size()>0){
							csMetadataParameters = csMetadataParametersIt;
							
							/*se nelle versioni precedenti non ho mai isertito la lingua inglese inserisco ora i parametri per la traduzione*/
							if(csMetadataParametersEn==null || csMetadataParametersEn.isEmpty()){
								if(headerTranslate!=null){
									int metadataTraslateParametersIndex = 0;
									for(String h : headerTranslate) {
										if(!h.equalsIgnoreCase(FIELD_CODE_TRANSLATE) && !h.equalsIgnoreCase(FIELD_DESCRIPTION_TRANSLATE)) {
											String parameterType = mappingPositionsTypologiesFieldHeader.get(metadataTraslateParametersIndex);
											//log.info("param translate::"+h+" parameterType::"+parameterType);
											hibernateUtil.executeBySystem(new CreateMetadataParameter(h, cs, parameterType, null, langTraslate, null, null, null, metadataTraslateParametersIndex));
											metadataTraslateParametersIndex++;
										}
									}
								}
							}
						}
						/*Se sto inserendo come file primario l'italiano e non ho trovato parametri per la lingua italiana
						 * controllo se ci sono parametri in lingua inglese, se sono presenti in lingua inglese dovrò
						 * controllare che il numero di parametri in italiana (che sto inserendo in questo momento) sia uguale a quello della linguia inglese 
						 * inserire i nuovi per la lingua italiana */
						else if(langPrimary.equals(StiConstants.LANG_IT) && csMetadataParametersEn!=null && csMetadataParametersEn.size()>0){
							int numFieldPrevieusVersion = csMetadataParametersEn.size();
							if(numFieldPrevieusVersion + 2 == headerPrimary.length) {
								int metadataParametersCount = addParameter(hibernateUtil, parameterTypeMap, FIELD_CODE, FIELD_DESCRIPTION, langPrimary, headerPrimary, cs);
								log.info("Inseriti " + metadataParametersCount + " nuovi metadata per la lingua ("+langPrimary+")");
								/*ricarico la lista dei parametri appena inseriti*/
								csMetadataParametersIt = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_IT));
							}
							else{
								throw new ImportStandardLocalException("Il numero di colonne della nuova versione non corrisponde a quello della precedente versione");
							}
							
						}
						
						
						
						
						/*controllo se sto inserendo come file primario l'inglese e se sono presenti i parametri in inglese nella versione precedente*/
						if(langPrimary.equals(StiConstants.LANG_EN)&& csMetadataParametersEn!=null && csMetadataParametersEn.size()>0){
							csMetadataParameters = csMetadataParametersEn;
							
							/*se nelle versioni precedenti non ho mai isertito la lingua inglese inserisco ora i parametri per la traduzione*/
							if(csMetadataParametersIt==null || csMetadataParametersIt.isEmpty()){
								if(headerTranslate!=null){
									int metadataTraslateParametersIndex = 0;
									for(String h : headerTranslate) {
										if(!h.equalsIgnoreCase(FIELD_CODE_TRANSLATE) && !h.equalsIgnoreCase(FIELD_DESCRIPTION_TRANSLATE)) {
											String parameterType = mappingPositionsTypologiesFieldHeader.get(metadataTraslateParametersIndex);
											//log.info("param translate::"+h+" parameterType::"+parameterType);
											hibernateUtil.executeBySystem(new CreateMetadataParameter(h, cs, parameterType, null, langTraslate, null, null, null, metadataTraslateParametersIndex));
											metadataTraslateParametersIndex++;
										}
									}
								}
							}
						}
						/*Se sto inserendo come file primario l'inglese e non ho trovato parametri per la lingua inglese
						 * controllo se ci sono parametri in lingua italiana, se sono presenti in lingua italiana dovrò
						 * controllare che il numero di parametri in inglsese (che sto inserendo in questo momento) sia uguale a quello della linguia italiana 
						 * e inserire i nuovi per la lingia inglese */
						else if(langPrimary.equals(StiConstants.LANG_EN) && csMetadataParametersIt!=null && csMetadataParametersIt.size()>0){
							int numFieldPrevieusVersion = csMetadataParametersIt.size();
							if(numFieldPrevieusVersion + 2 == headerPrimary.length) {
								int metadataParametersCount = addParameter(hibernateUtil, parameterTypeMap, FIELD_CODE, FIELD_DESCRIPTION, langPrimary, headerPrimary, cs);
								log.info("Inseriti " + metadataParametersCount + " nuovi metadata per la lingua ("+langPrimary+")");
								/*ricarico la lista dei parametri appena inseriti*/
								csMetadataParametersEn = (List<MetadataParameter>) hibernateUtil.executeBySystem(new GetCodeSystemMetadataParameters(cs.getId(),StiConstants.LANG_EN));
							}
							else{
								throw new ImportStandardLocalException("Il numero di colonne della nuova versione non corrisponde a quello della precedente versione");
							}
						}
						
						
						
						/*se csMetadataParameters è ancora null sto inserendo una versione con una sola lingua e i nomi delle le colonne potrebbero 
						 * cambiare rispetto a quelli salvati precedentemente, quindi controllo solo che la lunghezza sia la stessa */
						if(csMetadataParameters==null){
							int numFieldPrevieusVersion = 0;
							if(null != csMetadataParametersIt && csMetadataParametersIt.size()>0) {
								numFieldPrevieusVersion = csMetadataParametersIt.size();
							}
							if(null != csMetadataParametersEn && csMetadataParametersEn.size()>0) {
								numFieldPrevieusVersion = csMetadataParametersEn.size();
							}
							
							if(numFieldPrevieusVersion + 2 != headerPrimary.length) {
								throw new ImportStandardLocalException("Il numero di colonne della nuova versione non corrisponde a quello della precedente versione");
							}
						}
						else{
							if(csMetadataParameters.size() + 2 == headerPrimary.length) {
								boolean headerOk = true;
								for(MetadataParameter mp : csMetadataParameters) {
									if(!containsIgnoreCase(headerPrimary, mp.getParamName())) {
										headerOk = false;
										unmatchedColumns.add(mp.getParamName());
									}
								}
								if(!headerOk) {
									throw new ImportStandardLocalException("Le colonne " + unmatchedColumns.toString() + " non corrispondono alle colonne della precedente versione");
								}
							}else {
								throw new ImportStandardLocalException("Il numero di colonne della nuova versione non corrisponde a quello della precedente versione");
							}
						}
					}
	
					ExtraMetadataParameter extraSubType = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_SUBTYPE));
					if(null != extraSubType) {
						isClassification = extraSubType.getParamValue().equals(StandardLocalFields.SUB_TYPE_CLASSIFICATION);
					}
				}
				
				
				/*TODO: workaround per bug sulle versioni con nome duplicato, in questo modo la versione è unica per codesystem
				 * in visualizzazione bisogna eliminare la prima parte del nome della versione fino al separatore "__" */
				String prefixVersion = cs.getName().replaceAll("[^\\p{Alpha}]+","");
				if(prefixVersion == null || "".equals(prefixVersion)){
					prefixVersion = System.currentTimeMillis()+"";
				}
				String uniqueVersion = prefixVersion.replaceAll("[^\\p{Alpha}]+","")+StiConstants.NAME_VERSION_SEPARATOR+version;
	//			String uniqueVersion = version;
				
				
				if(fileNameIt!=null && !"".equals(fileNameIt)){
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_FILE_NAME_VERSION+"_IT_"+uniqueVersion, cs, "string", null, fileNameIt));
				}
				if(fileNameEn!=null && !"".equals(fileNameEn)){
					hibernateUtil.executeBySystem(new CreateExtraMetadataParameter(StandardLocalFields.CS_FILE_NAME_VERSION+"_EN_"+uniqueVersion, cs, "string", null, fileNameEn));
				}
				
				
				log.info("ImportStandardLocal::Starting CreateStandardLocal at " + new Date());
				CodeSystemVersion csVer = (CodeSystemVersion)hibernateUtil.executeBySystem(new CreateStandardLocal(name, uniqueVersion, versionDescription, oid, releaseDate, 
						csvValues, csvValuesTranslate, FIELD_CODE, FIELD_DESCRIPTION, FIELD_CODE_TRANSLATE, FIELD_DESCRIPTION_TRANSLATE,
						isClassification,langPrimary, langTraslate ));
				log.info("ImportStandardLocal::Ending CreateStandardLocal at " + new Date());
				Long previousVersionId = csVer.getPreviousVersionId();
				
				
				/*Inserimento del ValueSet*/
				if(type.equals(CodeSystemType.VALUE_SET.getKey())) {
					addValueSet(hibernateUtil, name, uniqueVersion, csDescription, releaseDate, oid, csVer);
				}
				
				/*Inserimento del mapping*/
				addMapping(hibernateUtil, name, cs);
	
				/**
				 * TODO
				 * Generare ChangeLog
				 */
				String languages = "";
				if(csvFileIt!=null && csvFileEn!=null){
					languages = "it, en";
				}
				else {
					if(csvFileIt!=null){
						languages = "it";
					}
					if(csvFileEn!=null){
						languages = "en";
					}
				}
				
				
				if(null != previousVersionId){
					/*Inserimento del ChangeLog*/
					ChangeLogUtil.addChangeLogCs(hibernateUtil, previousVersionId, csvValues, cs, csVer, languages);
				}
				else{
					ChangeLogUtil.addChangeLogCs(hibernateUtil, null, csvValues, cs, csVer, languages);
				}
			}
				
		}catch(Exception e) {
			log.error("ImportStandardLocal::ERROR - ", e);
			throw new ImportStandardLocalException(e);
		}
		
	}

	private static void addValueSet(final HibernateUtil hibernateUtil, final String name, final String version, final String csDescription, final Date releaseDate, final String oid,
			CodeSystemVersion csVer) throws StiHibernateException, StiAuthorizationException {
		ValueSet valueSet = null;
		
		List<ValueSet> valueSets = (List<ValueSet>) hibernateUtil.executeBySystem(new SearchValueSet(name));
		
		if(null == valueSets || valueSets.isEmpty()) {
			 //Creare il valueSet
			valueSet = (ValueSet) hibernateUtil.executeBySystem(new CreateValueSet(name, csDescription, new Date(), StiConstants.STATUS_CODES.ACTIVE.getCode()));
			
		}else {
			valueSet = valueSets.get(0);
		}
		
		ValueSetVersion vsVersion = (ValueSetVersion) hibernateUtil.executeBySystem(new CreateValueSetVersion(valueSet, StiConstants.STATUS_CODES.ACTIVE.getCode(), new Date(), new Date(), releaseDate, 
		valueSet.getCurrentVersionId(), oid, version, csVer.getVersionId(), new Date()));
		valueSet.setCurrentVersionId(vsVersion.getVersionId());
		hibernateUtil.executeBySystem(new UpdateValueSet(valueSet));
	}

	private static int addParameter(final HibernateUtil hibernateUtil, HashMap<String, String> parameterTypeMap, String FIELD_CODE, String FIELD_DESCRIPTION, String langPrimary,
			String[] headerPrimary, CodeSystem cs) throws StiHibernateException, StiAuthorizationException {
		int metadataParametersCount = 0;
		for(String h : headerPrimary) {
			if(!h.equalsIgnoreCase(FIELD_CODE) && !h.equalsIgnoreCase(FIELD_DESCRIPTION) && !h.equalsIgnoreCase(FIELD_CODE)) {
				String parameterType = parameterTypeMap.get(h);
//				log.info("param::"+h+" parameterType::"+parameterType);
				hibernateUtil.executeBySystem(new CreateMetadataParameter(h, cs, parameterType, null, langPrimary, null, null, null,metadataParametersCount));
				metadataParametersCount++;
			}
		}
		return metadataParametersCount;
	}

	

	
	@SuppressWarnings("unchecked")
	private static void addMapping(final HibernateUtil hibernateUtil, final String name, CodeSystem codeSystem) throws StiHibernateException, StiAuthorizationException, ImportStandardLocalException {
		log.debug("ImportStandardLocal::addMapping::start");
		CodeSystem cs = (CodeSystem) hibernateUtil.executeBySystem(new GetCodeSystemById(codeSystem.getId()));
		
		List<CodeSystemVersionEntityMembership> csvems = (List<CodeSystemVersionEntityMembership>) hibernateUtil.executeBySystem(new  GetCodeSystemVersionEntityMembershipByCSVersionId(cs.getCurrentVersionId()));
		List<Long> csevIds = new ArrayList<Long>();
		for(CodeSystemVersionEntityMembership cvem : csvems) {
			//log.info("codesystementityid::"+cvem.getId().getCodeSystemEntityId());
			CodeSystemEntityVersion codeSystemEntityVersion = (CodeSystemEntityVersion) hibernateUtil.executeBySystem(new GetCodeSystemEntityVersionByEntityId(cvem.getId().getCodeSystemEntityId()));
			csevIds.add(codeSystemEntityVersion.getVersionId());
		}
		
		List<CodeSystemMetadataValue> paramValues = (List<CodeSystemMetadataValue>) hibernateUtil.executeBySystem(new GetMetadataParameterValue(csevIds)); 
		
		MapSetVersion mapSetVersion = null;
		String trgCsName = null;
		for (CodeSystemMetadataValue codeSystemMetadataValue : paramValues) {
			if(codeSystemMetadataValue!=null 
					&& codeSystemMetadataValue.getMetadataParameter()!=null 
					&& codeSystemMetadataValue.getMetadataParameter().getParamDatatype()!=null
					&& codeSystemMetadataValue.getMetadataParameter().getParamDatatype().equals(MetadataParameterType.MAPPING.getKey())){
				
				csevIds = new ArrayList<Long>();
				csevIds.add(codeSystemMetadataValue.getCodeSystemEntityVersion().getVersionId());
				List<CodeSystemConcept> concepts = (List<CodeSystemConcept>) hibernateUtil.executeBySystem(new GetCodeSystemConcepts(csevIds));

				if(concepts!=null && concepts.size()==1){ 
					ExtraMetadataParameter mp = (ExtraMetadataParameter) hibernateUtil.executeBySystem(new GetExtraMetadataParameterValueByCsAndParamName(cs.getId(), StandardLocalFields.CS_CODIFICATION_MAPPING));
					if(mp!=null && mp.getParamValue()!=null){
						try{
							JSONObject jsonObj = new JSONObject(mp.getParamValue());
							Iterator<String> keys = jsonObj.keys();
							if(keys.hasNext()){
								String srcCsName = name;
								trgCsName = jsonObj.getString(jsonObj.keys().next());
								
								String srcValElem = concepts.get(0).getCode();
								String trgValElem = codeSystemMetadataValue.getParameterValue();
								
								log.info("-srcCsName::"+srcCsName+" -trgCsName::"+trgCsName+" -srcValElem::"+srcValElem+" -trgValElem::"+trgValElem);
								mapSetVersion = ImportAutomaticMapSet.addMapping(srcCsName, trgCsName, srcValElem, trgValElem);
							}
							
						}
						catch(Exception e){
							log.error("ERROR::"+e.getLocalizedMessage(),e);
						}
					}
				}
				
			}
		}
		
		if(mapSetVersion!=null){
			try {
				CodeSystemVersion csVersion = null;
				if(cs!=null){
					csVersion =  (CodeSystemVersion) hibernateUtil.executeByUser(new GetCodeSystemVersionById(cs.getCurrentVersionId()), SessionUtil.getLoggedUser());	
				}
				
				List<CodeSystem> csListTo = (List<CodeSystem>) hibernateUtil.executeBySystem(new GetCodeSystemByName(trgCsName));
				CodeSystem csTo = null;
				CodeSystemVersion csVersionTo = null;
				if(csListTo!=null && csListTo.size()>0){
					csTo = csListTo.get(0);
					csVersionTo =  (CodeSystemVersion) hibernateUtil.executeByUser(new GetCodeSystemVersionById(csTo.getCurrentVersionId()), SessionUtil.getLoggedUser());	
				}
				
				String fullname = mapSetVersion.getFullname();
				Integer importedRow = (Integer) hibernateUtil.executeBySystem(new CountCodeSystemEntityVersionAssociationByMapSetVersionId(mapSetVersion.getVersionId()));
				
				ChangelogDto changelogDto = new ChangelogDto();
				changelogDto.setTitle(fullname);
				changelogDto.setCodeSystem(cs!=null?cs.getName():"");
				changelogDto.setVersion(csVersion!=null?csVersion.getName():"");
				changelogDto.setCodeSystemTo(csTo!=null?csTo.getName():"");
				changelogDto.setVersionTo(csVersionTo!=null?csVersionTo.getName():"");
				changelogDto.setType(ChangeLogUtil.TYPE_MAPPING);
				changelogDto.setImportedRow(importedRow);
				changelogDto.setDateCreate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
				
				
				ChangeLogUtil.addChangeLogMapping(hibernateUtil, changelogDto, true);
			} catch (Exception e) {
				log.error("ImportStandardLocal::addMapping::ERROR - ", e);
				throw new ImportStandardLocalException(e);
			}
		}
		else{
			log.debug("ImportStandardLocal::no mapping create");
		}
		
		log.debug("ImportStandardLocal::addMapping::end");
	}

	
	
	private static boolean containsIgnoreCase(String[] array, String search) {
		boolean found = false;
		for(String s : array) {
			if(StiServiceUtil.paramNameToUpperCaseAndClean(s).equalsIgnoreCase(search)) found = true;
		}
		return found;
	}
}
