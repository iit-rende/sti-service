package it.linksmt.cts2.plugin.sti.search.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntrySummary;
import edu.mayo.cts2.framework.model.core.Definition;
import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
import edu.mayo.cts2.framework.model.core.EntryDescription;
import edu.mayo.cts2.framework.model.core.LanguageReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.core.TsAnyType;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionsByCSIdAndStatus;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetConceptVersionList;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetEntityDirectoryEntries;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetParentList;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConceptTranslation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.importer.IndexDocumentBuilder;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class DbTransformUtil {

	private static Logger log = Logger.getLogger(DbTransformUtil.class);

	private static SimpleDateFormat VERS_FMT = new SimpleDateFormat("dd.MM.yyyy");

	private DbTransformUtil() {
	}

	public static Association entityVersionAssociationToAssociation(final Session session, final CodeSystemEntityVersionAssociation csAssoc, final AbstractStiService service)
			throws StiHibernateException {

		long sourceId = csAssoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId1().getVersionId().longValue();
		long targetId = csAssoc.getCodeSystemEntityVersionByCodeSystemEntityVersionId2().getVersionId().longValue();

		if ((csAssoc.getLeftId() != null) && (csAssoc.getLeftId().longValue() == targetId)) {
			targetId = sourceId;
			sourceId = csAssoc.getLeftId().longValue();
		}

		CodeSystemEntityVersion source = (CodeSystemEntityVersion) session.get(CodeSystemEntityVersion.class, sourceId);

		CodeSystemEntityVersion target = (CodeSystemEntityVersion) session.get(CodeSystemEntityVersion.class, targetId);

		// Source Code System
		Set<CodeSystemVersionEntityMembership> memSetSrc = source.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();

		if ((memSetSrc == null) || (memSetSrc.size() != 1)) {
			throw new StiHibernateException("Il sistema attualmente supporta una singola associazione tra la versione del CS e la Entity.");
		}

		CodeSystemVersion csVersSrc = memSetSrc.iterator().next().getCodeSystemVersion();

		String csVersionNameSrc = StiServiceUtil.trimStr(csVersSrc.getName());
		String csNameSrc = StiServiceUtil.trimStr(csVersSrc.getCodeSystem().getName());

		Association retVal = new Association();
		retVal.setAssociationID(String.valueOf(csAssoc.getId().longValue()));

		retVal.setAssertedBy(service.buildCodeSystemVersionReference(csNameSrc, csVersionNameSrc));

		// Target Code System
		Set<CodeSystemVersionEntityMembership> memSetTrg = target.getCodeSystemEntity().getCodeSystemVersionEntityMemberships();

		if ((memSetTrg == null) || (memSetTrg.size() != 1)) {
			throw new StiHibernateException("Il sistema attualmente supporta una singola associazione tra la versione del CS e la Entity.");
		}

		CodeSystemVersion csVersTrg = memSetTrg.iterator().next().getCodeSystemVersion();

		String csVersionNameTrg = StiServiceUtil.trimStr(csVersTrg.getName());
		String csNameTrg = StiServiceUtil.trimStr(csVersTrg.getCodeSystem().getName());

		// Imposto riferimenti
		CodeSystemConcept srcConc = source.getCodeSystemConcepts().iterator().next();
		CodeSystemConcept trgConc = target.getCodeSystemConcepts().iterator().next();

		String entityIdSrc = StiServiceUtil.trimStr(srcConc.getCode());
		String entityIdTrg = StiServiceUtil.trimStr(trgConc.getCode());

		// Source
		ScopedEntityName sNameSrc = ModelUtils.createScopedEntityName(entityIdSrc, csNameSrc);

		URIAndEntityName subjectRef = new URIAndEntityName();
		subjectRef.setNamespace(csNameSrc);
		subjectRef.setName(entityIdSrc);

		subjectRef.setUri(service.getUrlConstructor().createEntityUrl(sNameSrc));
		subjectRef.setHref(service.getUrlConstructor().createEntityUrl(csNameSrc, csVersionNameSrc, sNameSrc));

		retVal.setSubject(subjectRef);

		// Target
		ScopedEntityName sNameTrg = ModelUtils.createScopedEntityName(entityIdTrg, csNameTrg);

		URIAndEntityName targetRef = new URIAndEntityName();
		targetRef.setNamespace(csNameTrg);
		targetRef.setName(entityIdTrg);

		targetRef.setUri(service.getUrlConstructor().createEntityUrl(sNameTrg));
		targetRef.setHref(service.getUrlConstructor().createEntityUrl(csNameTrg, csVersionNameTrg, sNameTrg));

		StatementTarget statTrg = new StatementTarget();
		statTrg.setEntity(targetRef);

		retVal.addTarget(statTrg);

		// Predicate (uguale a target)
		PredicateReference predRef = new PredicateReference();
		predRef.setNamespace(csNameTrg);
		predRef.setName(entityIdTrg);

		predRef.setUri(service.getUrlConstructor().createEntityUrl(entityIdTrg));
		predRef.setHref(service.getUrlConstructor().createEntityUrl(csNameTrg, csVersionNameTrg, sNameTrg));

		retVal.setPredicate(predRef);

		// Altre informazioni sulla Associazione
		List<Property> propAssoc = new ArrayList<Property>();
		addPropertyToList(propAssoc, "forwardName", csAssoc.getForwardName());

		addPropertyToList(propAssoc, "reverseName", csAssoc.getReverseName());


		/***vecchia gestione*/
//		String sourceTit_en = StiServiceUtil.trimStr(srcConc.getTerm());
//		String targetTit_en = StiServiceUtil.trimStr(trgConc.getTerm());
//
//		String sourceTit_it = "";
//		String targetTit_it = "";
		/**************************/
		
		String sourceTit_en = "";
		String targetTit_en = "";

		String sourceTit_it = "";
		String targetTit_it = "";
		
		String langCdSrc = srcConc.getLanguageCd();
		String langCdTrg = trgConc.getLanguageCd();
		
		
		/*SRC*/
		if(langCdSrc!=null && langCdSrc.equalsIgnoreCase(StiConstants.LANG_IT)){
			sourceTit_it = StiServiceUtil.trimStr(srcConc.getTerm());
		}
		
		if(langCdSrc!=null && langCdSrc.equalsIgnoreCase(StiConstants.LANG_EN)){
			sourceTit_en = StiServiceUtil.trimStr(srcConc.getTerm());
		}
		
		
		/*TRG*/
		if(langCdTrg!=null && langCdTrg.equalsIgnoreCase(StiConstants.LANG_IT)){
			targetTit_it = StiServiceUtil.trimStr(trgConc.getTerm());
		}
		
		if(langCdTrg!=null && langCdTrg.equalsIgnoreCase(StiConstants.LANG_EN)){
			targetTit_en = StiServiceUtil.trimStr(trgConc.getTerm());
		}
		
		
		/*DEFAULT*/
		if(sourceTit_it.equals("") && sourceTit_en.equals("")){
			sourceTit_en = StiServiceUtil.trimStr(srcConc.getTerm());
			sourceTit_it = "";
		}
		if(targetTit_it.equals("") && targetTit_en.equals("")){
			targetTit_en = StiServiceUtil.trimStr(trgConc.getTerm());
			targetTit_it = "";
		}
		

		
		log.debug("srcConc.getLanguageCd()::"+srcConc.getLanguageCd()+" trgConc.getLanguageCd()::"+trgConc.getLanguageCd());
		log.debug(" -sourceTit_it::"+sourceTit_it+" -targetTit_it::"+targetTit_it);
		log.debug(" -sourceTit_en::"+sourceTit_en+" -targetTit_en::"+targetTit_en);
		log.debug("#################################################");
		

		Set<CodeSystemConceptTranslation> trSrc = srcConc.getCodeSystemConceptTranslations();
		if ((trSrc != null) && (trSrc.size() > 0)) {
			CodeSystemConceptTranslation translSrc = trSrc.iterator().next();
//			sourceTit_it = StiServiceUtil.trimStr(translSrc.getTerm()); /*vecchia gestione*/
			
			
			if(translSrc.getLanguageCd()==null && translSrc.getLanguageCd().equalsIgnoreCase(StiConstants.LANG_IT)){
				sourceTit_it = StiServiceUtil.trimStr(translSrc.getTerm());
			}
			else if(translSrc.getLanguageCd()==null && translSrc.getLanguageCd().equalsIgnoreCase(StiConstants.LANG_EN)){
				sourceTit_en = StiServiceUtil.trimStr(translSrc.getTerm());
			}
			
			if(sourceTit_it.equals("")){
				sourceTit_it = StiServiceUtil.trimStr(translSrc.getTerm());
			}
			if(sourceTit_en.equals("")){
				sourceTit_en = StiServiceUtil.trimStr(translSrc.getTerm());
			}
			
			log.debug("translSrc.getLanguageCd()::"+translSrc.getLanguageCd());
			log.debug(" -sourceTit_it::"+sourceTit_it);
			log.debug(" -sourceTit_en::"+sourceTit_en);
			log.debug("#################################################");
		}

		
		Set<CodeSystemConceptTranslation> trTrg = trgConc.getCodeSystemConceptTranslations();
		if ((trTrg != null) && (trTrg.size() > 0)) {
			CodeSystemConceptTranslation translTrg = trTrg.iterator().next();
//			targetTit_it = StiServiceUtil.trimStr(translTrg.getTerm()); /*vecchia gestione*/
			
			
			if(translTrg.getLanguageCd()!=null && translTrg.getLanguageCd().equalsIgnoreCase(StiConstants.LANG_IT)){
				targetTit_it =  StiServiceUtil.trimStr(translTrg.getTerm());
			}
			else if(translTrg.getLanguageCd()!=null && translTrg.getLanguageCd().equalsIgnoreCase(StiConstants.LANG_EN)){
				targetTit_en =  StiServiceUtil.trimStr(translTrg.getTerm());
			}
			
			if(targetTit_it.equals("")){
				targetTit_it = StiServiceUtil.trimStr(translTrg.getTerm());
			}
			if(targetTit_en.equals("")){
				targetTit_en = StiServiceUtil.trimStr(translTrg.getTerm());
			}
			
			log.debug("translTrg.getLanguageCd()::"+translTrg.getLanguageCd());
			log.debug(" -targetTit_it::"+targetTit_it);
			log.debug(" -targetTit_en::"+targetTit_en);
			log.debug("#################################################\n\n\n");
		}
		
		

		if (StiServiceUtil.isNull(sourceTit_it) && (!StiServiceUtil.isNull(sourceTit_en))) {
			sourceTit_it = sourceTit_en;
		}
		if (StiServiceUtil.isNull(targetTit_it) && (!StiServiceUtil.isNull(targetTit_en))) {
			targetTit_it = targetTit_en;
		}

		if (StiServiceUtil.isNull(sourceTit_en) && (!StiServiceUtil.isNull(sourceTit_it))) {
			sourceTit_en = sourceTit_it;
		}
		if (StiServiceUtil.isNull(targetTit_en) && (!StiServiceUtil.isNull(targetTit_it))) {
			targetTit_en = targetTit_it;
		}
		
		

		// FIX confezione AIC
		String confezSrc = "";
		String confezTrg = "";

		if (AtcAicFields.AIC_CODE_SYSTEM_NAME.equalsIgnoreCase(csNameSrc)) {
			confezSrc = " " + getConfezioneStr(source);
		}
		if (AtcAicFields.AIC_CODE_SYSTEM_NAME.equalsIgnoreCase(csNameTrg)) {
			confezTrg = " " + getConfezioneStr(target);
		}

		addPropertyToList(propAssoc, "sourceTitle_en", sourceTit_en + confezSrc);
		addPropertyToList(propAssoc, "sourceTitle_it", sourceTit_it + confezSrc);

		addPropertyToList(propAssoc, "targetTitle_en", targetTit_en + confezTrg);
		addPropertyToList(propAssoc, "targetTitle_it", targetTit_it + confezTrg);

		addPropertyToList(propAssoc, "associationKind", String.valueOf(csAssoc.getAssociationKind()));

		addPropertyToList(propAssoc, "status", String.valueOf(csAssoc.getStatus()));

		MapSetVersion msVers = csAssoc.getMapSetVersion();
		if (msVers != null) {
			addPropertyToList(propAssoc, "mapSetVersionName", String.valueOf(msVers.getFullname()));

			if (msVers.getReleaseDate() != null) {
				addPropertyToList(propAssoc, "releaseDate", VERS_FMT.format(msVers.getReleaseDate()));
			}
			if(msVers.getDescription()!=null){
				addPropertyToList(propAssoc, "description", msVers.getDescription());
			}
			if(msVers.getOrganization()!=null){
				addPropertyToList(propAssoc, "organization", msVers.getOrganization());
			}
		}

		retVal.setAssociationQualifier(propAssoc);

		return retVal;
	}

	public static void addPropertyToList(final List<Property> propList, final String name, final String value) {

		if (StiServiceUtil.isNull(name) || (StiServiceUtil.isNull(value))) {
			return;
		}

		Property prop = new Property();

		PredicateReference pred = new PredicateReference();
		pred.setName(StiServiceUtil.trimStr(name));
		pred.setUri(StiServiceUtil.trimStr(name));
		prop.setPredicate(pred);

		StatementTarget valPred = new StatementTarget();
		valPred.setLiteral(ModelUtils.createOpaqueData(StiServiceUtil.trimStr(value)));

		ArrayList<StatementTarget> arrVal = new ArrayList<StatementTarget>();
		arrVal.add(valPred);

		prop.setValue(arrVal);

		propList.add(prop);
	}

	public static CodeSystemCatalogEntrySummary codeSystemToCatalogEntry(final CodeSystem cs, final AbstractStiService service) throws StiHibernateException, StiAuthorizationException {

		// Versione Corrente
		HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
		CodeSystemVersion curVers = (CodeSystemVersion) hibUtil.executeBySystem(new GetCodeSystemVersionById(cs.getCurrentVersionId()));

		String csName = StiServiceUtil.trimStr(cs.getName());
		String csVersionName = StiServiceUtil.trimStr(curVers.getName());

		CodeSystemCatalogEntrySummary summary = new CodeSystemCatalogEntrySummary();

		summary.setFormalName(csName);
		summary.setCodeSystemName(csName);

		summary.setHref(service.buildCodeSystemReference(csName, csVersionName).getHref());
		summary.setAbout(summary.getHref());

		EntryDescription ed = new EntryDescription();
		ed.setValue(ModelUtils.toTsAnyType("Test implementazione servizio STI - " + cs.getName() + ": " + curVers.getDescription()));

		summary.setResourceSynopsis(ed);
		summary.setCurrentVersion(service.buildCodeSystemVersionReference(csName, csVersionName));

		summary.getCurrentVersion().getCodeSystem().setHref(summary.getHref());

		// TODO : summary.setVersions(getUrlConstructor().createVersionsOfCodeSystemUrl(resource.getCodeSystemName()));

		return summary;
	}

	public static EntityDirectoryEntry conceptToEntityEntry(final Session session, final String codeSystemName, final String codeSystemVersionName, 
			final CodeSystemConcept csConc,final AbstractStiService service) {

		EntityDirectoryEntry entry = new EntityDirectoryEntry();
		String entityId = StiServiceUtil.trimStr(csConc.getCode());

		ScopedEntityName sName = ModelUtils.createScopedEntityName(entityId, codeSystemName);

		entry.setName(sName);
		entry.setResourceName(entityId);

		// Entity URI
		entry.setAbout(service.getUrlConstructor().createEntityUrl(sName));
		entry.setHref(service.getUrlConstructor().createEntityUrl(codeSystemName, codeSystemVersionName, sName));

		DescriptionInCodeSystem descCS = new DescriptionInCodeSystem();
		descCS.setDescribingCodeSystemVersion(service.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName));

		JsonObject desObj = extractMetadataMin(csConc);

		// desObj = extractMetadata(csConc, IndexDocumentBuilder.SKIP_FIELDS);
		//
		// try {
		// List<Association> assList = new SearchAssociations(codeSystemVersionName, null, null,csConc.getCode(), service).execute(session);
		//
		// 	boolean hasCrossMap = (assList != null) && (assList.size() > 0);
		// 	desObj.addProperty(CommonFields.HAS_ASSOCIATIONS, hasCrossMap);
		// }
		// catch(Exception ex) {
		// 	log.error("Impossibile leggere le informazioni sulla presenza di Cross-Mapping: " + csConc.getCode());
		// }
		
		
		try{
			List<CodeSystem> csList = new GetCodeSystemByName(codeSystemName).execute(session);
			if(csList!=null && csList.size()>0){
				CodeSystem cs = csList.get(0);
				if(cs!=null && cs.getCodeSystemType()!=null){
					if(cs.getCodeSystemType().equals(CodeSystemType.LOCAL.getKey()) 
							|| cs.getCodeSystemType().equals(CodeSystemType.STANDARD_NATIONAL.getKey())
							|| cs.getCodeSystemType().equals(CodeSystemType.VALUE_SET.getKey())) {
						addMetadataValuesDinamicFields(csConc, desObj, IndexDocumentBuilder.SKIP_FIELDS);
					}
				}
				if(cs!=null){
					HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
					CodeSystemVersion curVers = (CodeSystemVersion) hibUtil.executeBySystem(new GetCodeSystemVersionById(cs.getCurrentVersionId()));
					if(curVers!=null && curVers.getReleaseDate()!=null){
						desObj.addProperty(CommonFields.RELEASE_DATE, new SimpleDateFormat("dd/MM/yyyy").format(curVers.getReleaseDate()));
					}
				}
			}
		}
		catch(Exception e){
			log.error("ERROR::"+e.getLocalizedMessage());
		}

		
		descCS.setDesignation(desObj.toString());

		ArrayList<DescriptionInCodeSystem> arrDes = new ArrayList<DescriptionInCodeSystem>();
		arrDes.add(descCS);

		entry.setKnownEntityDescription(arrDes);
		return entry;
	}

	public static NamedEntityDescription conceptToEntityDescription(final Session session, final String codeSystemName, final CodeSystemVersion codeSystemVersion, final CodeSystemConcept csConc,
			final AbstractStiService service) throws StiHibernateException, StiAuthorizationException {

		NamedEntityDescription retVal = new NamedEntityDescription();
		String entityId = StiServiceUtil.trimStr(csConc.getCode());

		ScopedEntityName sName = ModelUtils.createScopedEntityName(entityId, codeSystemName);

		retVal.setEntityID(sName);

		String codeSystemVersionName = codeSystemVersion.getName();

		// Entity URI
		retVal.setAbout(service.getUrlConstructor().createEntityUrl(sName));
		retVal.setDescribingCodeSystemVersion(service.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName));

		// EntityType
		URIAndEntityName eType = new URIAndEntityName();
		eType.setNamespace(codeSystemName);
		eType.setName(entityId);

		// EntityVersion URI
		eType.setUri(retVal.getAbout());
		eType.setHref(service.getUrlConstructor().createEntityUrl(codeSystemName, codeSystemVersionName, sName));

		retVal.setEntityType(new URIAndEntityName[] { eType });

		retVal.setParent(new GetParentList(csConc, service).execute(session));
		retVal.setAlternateEntityID(new GetConceptVersionList(codeSystemName, entityId).execute(session));

		// Children
		Gson gson = new Gson();
		List<EntityDirectoryEntry> entityDirectoryEntryList = new GetEntityDirectoryEntries(codeSystemVersionName, entityId, 0, 1000, service, codeSystemName).execute(session);
		retVal.setChildren(gson.toJson(entityDirectoryEntryList));

		JsonObject desObj = extractMetadata(csConc, null);

		List<Definition> vDefinitionList = new ArrayList<Definition>();
		Iterator<Entry<String, JsonElement>> jsonIt = desObj.entrySet().iterator();

		while (jsonIt.hasNext()) {
			Entry<String, JsonElement> jsonProp = jsonIt.next();

			String name = jsonProp.getKey().trim();
			JsonElement propVal = jsonProp.getValue();

			String language = null;
			if (name.toLowerCase().endsWith("_it")) {
				name = name.substring(0, name.length() - 3);
				language = "it";
			} else if (name.toLowerCase().endsWith("_en")) {
				name = name.substring(0, name.length() - 3);
				language = "en";
			}

			Definition def = buildDefinition(name, language, propVal);
			vDefinitionList.add(def);
		}

		addLocalMappings(csConc, vDefinitionList);

		// Informazione su Code System Oid
		vDefinitionList.add(buildDefinition(CommonFields.CODE_SYSTEM_OID, null, new JsonPrimitive(StiServiceUtil.trimStr(codeSystemVersion.getOid()))));
		
		
		

		retVal.setDefinition(vDefinitionList);
		return retVal;
	}

	private static JsonObject extractMetadataMin(final CodeSystemConcept csConc) {

		/* vecchio metodo valido per i codesystem inseriti prima della modifica dei codesystem standard/local/valueset */
		// JsonObject desObj = new JsonObject();
		//
		// // Translations
		// Set<CodeSystemConceptTranslation> trSet = csConc.getCodeSystemConceptTranslations();
		// if ((trSet != null) && (trSet.size() > 0)) {
		// // Essendo le versioni inglesi più complete, se esistono traduzioni la entry principale è inglese
		// desObj.addProperty(CommonFields.NAME + "_en", StiServiceUtil.trimStr(csConc.getTerm()));
		// desObj.addProperty(CommonFields.DESCRIPTION + "_en", StiServiceUtil.trimStr(csConc.getDescription()));
		//
		// Iterator<CodeSystemConceptTranslation> trSetIt = trSet.iterator();
		// while (trSetIt.hasNext()) {
		// CodeSystemConceptTranslation transl = trSetIt.next();
		// String lCod = StiServiceUtil.trimStr(transl.getLanguageCd()).toLowerCase();
		//
		// // Se il nome contiene "Capitolo", modificare il nome utilizzato
		// String name = StiServiceUtil.trimStr(transl.getTerm());
		// if (name.toLowerCase().startsWith("capitolo") || name.toLowerCase().startsWith("classificazione supplementare") || name.toLowerCase().startsWith("classificazion supplementare")) {
		// name = StiServiceUtil.trimStr(transl.getDescription());
		// }
		//
		// desObj.addProperty(CommonFields.NAME + "_" + lCod, name);
		// desObj.addProperty(CommonFields.DESCRIPTION + "_" + lCod, StiServiceUtil.trimStr(transl.getDescription()));
		// }
		// } else {
		// // Altrimenti faccio coincidere inglese e italiano
		// desObj.addProperty(CommonFields.NAME + "_it", StiServiceUtil.trimStr(csConc.getTerm()));
		// desObj.addProperty(CommonFields.DESCRIPTION + "_it", StiServiceUtil.trimStr(csConc.getDescription()));
		//
		// desObj.addProperty(CommonFields.NAME + "_en", StiServiceUtil.trimStr(csConc.getTerm()));
		// desObj.addProperty(CommonFields.DESCRIPTION + "_en", StiServiceUtil.trimStr(csConc.getDescription()));
		// }

		/* Nuovo metodo valido per i codesystem standard/local/valueset nel quale la lingua primaria puo essere sia l'italiano sia l'inglese in base all'inserimento del codesystem */
		/* di default setto la lingua primaria a "en" per mantenera la compatibilita con le vecchie versioni e tipologie di codesystem */
		String langPrimary = "en";

		JsonObject desObj = new JsonObject();
		if (null != csConc.getLanguageCd() && !"".equals(csConc.getLanguageCd()) && (csConc.getLanguageCd().equalsIgnoreCase("IT") || csConc.getLanguageCd().equalsIgnoreCase("IT"))) {
			langPrimary = csConc.getLanguageCd().toLowerCase();
		}

		// Translations
		Set<CodeSystemConceptTranslation> trSet = csConc.getCodeSystemConceptTranslations();
		if ((trSet != null) && (trSet.size() > 0)) {
			// Se ci sono traduzioni lingua della entry principale = langPrimary

			desObj.addProperty(CommonFields.NAME + "_" + langPrimary, StiServiceUtil.trimStr(csConc.getTerm()));
			desObj.addProperty(CommonFields.DESCRIPTION + "_" + langPrimary, StiServiceUtil.trimStr(csConc.getDescription()));

			Iterator<CodeSystemConceptTranslation> trSetIt = trSet.iterator();
			while (trSetIt.hasNext()) {
				CodeSystemConceptTranslation transl = trSetIt.next();
				// lingua della entry secondaria (di traduzione) = langPrimary
				String langTranslate = StiServiceUtil.trimStr(transl.getLanguageCd()).toLowerCase();

				// Se il nome contiene "Capitolo", modificare il nome utilizzato
				String name = StiServiceUtil.trimStr(transl.getTerm());
				if (name.toLowerCase().startsWith("capitolo") || name.toLowerCase().startsWith("classificazione supplementare") || name.toLowerCase().startsWith("classificazion supplementare")) {
					name = StiServiceUtil.trimStr(transl.getDescription());
				}

				desObj.addProperty(CommonFields.NAME + "_" + langTranslate, name);
				desObj.addProperty(CommonFields.DESCRIPTION + "_" + langTranslate, StiServiceUtil.trimStr(transl.getDescription()));
			}
		} else {
			// se non ci sono traduzioni faccio coincidere inglese e italiano
			desObj.addProperty(CommonFields.NAME + "_it", StiServiceUtil.trimStr(csConc.getTerm()));
			desObj.addProperty(CommonFields.DESCRIPTION + "_it", StiServiceUtil.trimStr(csConc.getDescription()));

			desObj.addProperty(CommonFields.NAME + "_en", StiServiceUtil.trimStr(csConc.getTerm()));
			desObj.addProperty(CommonFields.DESCRIPTION + "_en", StiServiceUtil.trimStr(csConc.getDescription()));
		}

		Boolean leafVal = csConc.getCodeSystemEntityVersion().getIsLeaf();
		if (leafVal == null) {
			leafVal = Boolean.FALSE;
		}

		desObj.addProperty(CommonFields.IS_LEAF, leafVal);
		return desObj;
	}

	private static JsonObject extractMetadata(final CodeSystemConcept csConc, final HashSet<String> skipFields) {

		JsonObject desObj = extractMetadataMin(csConc);
		addMetadataValues(csConc, desObj, skipFields, true);

		return desObj;
	}

	private static void addLocalMappings(final CodeSystemConcept csConc, final List<Definition> vDefinitionList) {
		Iterator<CodeSystemMetadataValue> propsIt = csConc.getCodeSystemEntityVersion().getCodeSystemMetadataValues().iterator();

		JsonArray availLocals = new JsonArray();

		while (propsIt.hasNext()) {
			CodeSystemMetadataValue metaVal = propsIt.next();
			String paramValue = StiServiceUtil.trimStr(metaVal.getParameterValue()).replace("\\\"", "\"");

			if (StiServiceUtil.isNull(paramValue)) {
				continue;
			}

			MetadataParameter metaDef = metaVal.getMetadataParameter();
			String paramKey = StiServiceUtil.trimStr(metaDef.getParamName());

			if (!StiConstants.LOCAL_LANGUAGE_CD.equalsIgnoreCase(StiServiceUtil.trimStr(metaDef.getLanguageCd()))) {
				continue;
			}

			String localCsName = null;
			int sepIdx = paramValue.indexOf(StiConstants.LOCAL_VALUE_SEPARATOR);
			if (sepIdx > 0) {
				localCsName = paramValue.substring(0, sepIdx);
				paramValue = paramValue.substring(sepIdx + StiConstants.LOCAL_VALUE_SEPARATOR.length());
			} else {
				log.warn("Impossibile estrarre il valore locale per il metadato: " + paramKey);
				continue;
			}

			if (!checkArrayLocals(availLocals, localCsName)) {
				availLocals.add(new JsonPrimitive(localCsName));
			}

			Definition def = buildDefinition(paramKey, StiConstants.LOCAL_LANGUAGE_CD + "_" + localCsName, new JsonPrimitive(paramValue));
			vDefinitionList.add(def);
		}

		Definition locList = buildDefinition(LoincFields.LOCAL_CODE_LIST, null, availLocals);

		vDefinitionList.add(locList);
	}

	private static boolean checkArrayLocals(final JsonArray availLocals, final String localCs) {
		if (StiServiceUtil.isNull(localCs) || (availLocals == null)) {
			return true;
		}

		String chkVal = StiServiceUtil.trimStr(localCs);
		for (int i = 0; i < availLocals.size(); i++) {
			if (chkVal.equalsIgnoreCase(StiServiceUtil.trimStr(availLocals.get(i).getAsString()))) {
				return true;
			}
		}

		return false;
	}

	private static Definition buildDefinition(final String identifier, final String language, final JsonElement propVal) {

		Definition def = new Definition();
		def.setExternalIdentifier(identifier);

		if (!StiServiceUtil.isNull(language)) {
			def.setLanguage(new LanguageReference(language));
		}

		TsAnyType any = new TsAnyType();
		if (propVal.isJsonArray()) {
			JsonArray arrVal = (JsonArray) propVal;

			String[] anyVal = new String[arrVal.size()];
			for (int i = 0; i < arrVal.size(); i++) {
				anyVal[i] = arrVal.get(i).getAsString();
			}
			any.setAnyObject(anyVal);
		} else {
			any.setContent(propVal.getAsString());
		}

		def.setValue(any);
		return def;
	}

	public static void addMetadataValues(final CodeSystemConcept csConc, final JsonObject desObj, final HashSet<String> skipFields, final boolean skipLocal) {

		Iterator<CodeSystemMetadataValue> propsIt = csConc.getCodeSystemEntityVersion().getCodeSystemMetadataValues().iterator();

		JsonArray availLocals = new JsonArray();

		while (propsIt.hasNext()) {
			CodeSystemMetadataValue metaVal = propsIt.next();
			String paramValue = StiServiceUtil.trimStr(metaVal.getParameterValue()).replace("\\\"", "\"");

			if (StiServiceUtil.isNull(paramValue)) {
				continue;
			}

			MetadataParameter metaDef = metaVal.getMetadataParameter();
			String paramKey = StiServiceUtil.trimStr(metaDef.getParamName());

			if (!StiServiceUtil.isNull(metaDef.getLanguageCd())) {

				if (StiConstants.LOCAL_LANGUAGE_CD.equalsIgnoreCase(StiServiceUtil.trimStr(metaDef.getLanguageCd()))) {
					if (skipLocal) {
						continue;
					}

					String localCsName = null;
					int sepIdx = paramValue.indexOf(StiConstants.LOCAL_VALUE_SEPARATOR);
					if (sepIdx > 0) {
						localCsName = paramValue.substring(0, sepIdx);
						// paramValue = paramValue.substring(sepIdx +
						// StiConstants.LOCAL_VALUE_SEPARATOR.length());
					} else {
						log.warn("Impossibile estrarre il valore locale per il metadato: " + paramKey);
						continue;
					}

					if (!checkArrayLocals(availLocals, localCsName)) {
						availLocals.add(new JsonPrimitive(localCsName));
					}
				} else {
					paramKey += "_" + StiServiceUtil.trimStr(metaDef.getLanguageCd()).toLowerCase();
				}
			}

			if ((skipFields != null) && (skipFields.contains(paramKey.toLowerCase()))) {
				continue;
			}

			if (desObj.get(paramKey) == null) {
				desObj.addProperty(paramKey, paramValue);
			} else if (desObj.get(paramKey) instanceof JsonArray) {
				desObj.getAsJsonArray(paramKey).add(new JsonPrimitive(paramValue));
			} else if (desObj.get(paramKey) instanceof JsonPrimitive) {
				JsonArray arr = new JsonArray();
				arr.add(desObj.get(paramKey));
				arr.add(new JsonPrimitive(paramValue));
				desObj.add(paramKey, arr);
			}
		}

		if (availLocals.size() > 0) {
			desObj.add(LoincFields.LOCAL_CODE_LIST, availLocals);
		}
	}

	public static void addMetadataValuesDinamicFields(final CodeSystemConcept csConc, final JsonObject desObj, final HashSet<String> skipFields) {

		Iterator<CodeSystemMetadataValue> propsIt = csConc.getCodeSystemEntityVersion().getCodeSystemMetadataValues().iterator();

		JsonArray availLocals = new JsonArray();

		while (propsIt.hasNext()) {
			CodeSystemMetadataValue metaVal = propsIt.next();
			String paramValue = StiServiceUtil.trimStr(metaVal.getParameterValue()).replace("\\\"", "\"");

			if (StiServiceUtil.isNull(paramValue)) {
				continue;
			}

			MetadataParameter metaDef = metaVal.getMetadataParameter();
			String paramKey = StiServiceUtil.trimStr(metaDef.getParamName());

			if ((skipFields != null) && (skipFields.contains(paramKey.toLowerCase()))) {
				continue;
			}
			

			/* Aggiunge il suffisso della lingua al campo dianmico */
			if (null != metaDef.getLanguageCd() && !"".equals(metaDef.getLanguageCd())
					&& (metaDef.getLanguageCd().equals(StiConstants.LANG_IT) || metaDef.getLanguageCd().equals(StiConstants.LANG_EN))) {
				if (!paramKey.endsWith("_" + metaDef.getLanguageCd().toLowerCase())) {
					paramKey = paramKey + "_" + metaDef.getLanguageCd().toLowerCase();
				}

			}

			if (desObj.get(paramKey) == null) {
				String metadataType = (metaDef.getParamDatatype() != null ? metaDef.getParamDatatype() : MetadataParameterType.STRING.getKey());
				String prefixDinamicField = "";

				if (metadataType.equalsIgnoreCase(MetadataParameterType.STRING.getKey())) {
					prefixDinamicField = MetadataParameterType.STRING.getPrefix();
					paramKey = prefixDinamicField + paramKey;
					desObj.addProperty(paramKey, paramValue);
				} else if (metadataType.equalsIgnoreCase(MetadataParameterType.DATE.getKey())) {
					prefixDinamicField = MetadataParameterType.DATE.getPrefix();
					paramKey = prefixDinamicField + paramKey;
					try {
						DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
						DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

						Date dataFormattata = formatter1.parse(paramValue);
						desObj.addProperty(paramKey, formatter2.format(dataFormattata));
					} catch (ParseException e) {
						log.error("ERROR::" + e.getLocalizedMessage());
					}
				} else if (metadataType.equalsIgnoreCase(MetadataParameterType.NUMBER.getKey())) {
					prefixDinamicField = MetadataParameterType.NUMBER.getPrefix();
					paramKey = prefixDinamicField + paramKey;
					try{
						Number num = NumberFormat.getInstance().parse(paramValue);
						desObj.addProperty(paramKey, Double.parseDouble(num.toString()));
					}
					catch(Exception e){
						log.equals("ERROR:: parser nell'elaborazione del valore:["+paramValue+"] del campo:["+paramKey+"] tipo:["+prefixDinamicField+"]  ");
					}
				} else if (metadataType.equalsIgnoreCase(MetadataParameterType.MAPPING.getKey())) {
					prefixDinamicField = MetadataParameterType.MAPPING.getPrefix();
					paramKey = prefixDinamicField + paramKey;
					desObj.addProperty(paramKey, paramValue);
				}

			}
		}
	}

	private static String getConfezioneStr(final CodeSystemEntityVersion entity) {

		Iterator<CodeSystemMetadataValue> propsIt = entity.getCodeSystemMetadataValues().iterator();

		while (propsIt.hasNext()) {
			CodeSystemMetadataValue metaVal = propsIt.next();
			String paramValue = StiServiceUtil.trimStr(metaVal.getParameterValue()).replace("\\\"", "\"");

			if (StiServiceUtil.isNull(paramValue)) {
				continue;
			}

			MetadataParameter metaDef = metaVal.getMetadataParameter();
			String paramKey = StiServiceUtil.trimStr(metaDef.getParamName());

			if (AtcAicFields.AIC_CONFEZIONE.equalsIgnoreCase(paramKey)) {
				return " " + paramValue;
			}
		}

		return "";
	}
}
