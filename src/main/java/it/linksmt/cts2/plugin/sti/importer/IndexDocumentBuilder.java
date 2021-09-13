package it.linksmt.cts2.plugin.sti.importer;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetConceptLastVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetExtraMetadataParameterValueByCs;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetParentConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConceptTranslation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.ExtraMetadataParameter;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.search.util.CommonFields;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.JsonObject;

public final class IndexDocumentBuilder {

	private static Logger log = Logger.getLogger(ChunkExtractor.class);

	private IndexDocumentBuilder() {
	}

	public static final String DOCUMENT_ID = "id";

	public static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	static {

		// Campi non indicizzati di LOINC
		SKIP_FIELDS.add(LoincFields.UNITS_REQUIRED.toLowerCase());
		SKIP_FIELDS.add(LoincFields.SUBMITTED_UNITS.toLowerCase());
		SKIP_FIELDS.add(LoincFields.UNITS_AND_RANGE.toLowerCase());
		SKIP_FIELDS.add(LoincFields.ORDER_OBS.toLowerCase());
		SKIP_FIELDS.add(LoincFields.EXAMPLE_UNITS.toLowerCase());
		// SKIP_FIELDS.add(LoincFields.LOCAL_UNITS.toLowerCase());

		SKIP_FIELDS.add(LoincFields.STATUS_REASON.toLowerCase());
		SKIP_FIELDS.add(LoincFields.STATUS_TEXT.toLowerCase());
		SKIP_FIELDS.add(LoincFields.CHANGE_REASON_PUBLIC.toLowerCase());
		SKIP_FIELDS.add(LoincFields.EXTERNAL_COPYRIGHT_NOTICE.toLowerCase());
		SKIP_FIELDS.add(LoincFields.MAP_TO_COMMENT.toLowerCase());

		SKIP_FIELDS.add(LoincFields.HL7_FIELD_SUBFIELD_ID.toLowerCase());
		SKIP_FIELDS.add(LoincFields.HL7_ATTACHMENT_STRUCTURE.toLowerCase());
		SKIP_FIELDS.add(LoincFields.VALID_HL7_ATTACHMENT_REQUEST.toLowerCase());

		// Campi non indicizzati di ICD-9
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_INCLUDE_it.toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_INCLUDE_en.toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_ESCLUDE_it.toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_ESCLUDE_en.toLowerCase());

		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_CODIFY_FIRST_it.toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_CODIFY_FIRST_en.toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_USE_ADD_CODE_it.toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_USE_ADD_CODE_en.toLowerCase());

		// Campi non indicizzati di ATC-AIC
		SKIP_FIELDS.add(AtcAicFields.AIC_METRI_CUBI_OSSIGENO.toLowerCase());
		//SKIP_FIELDS.add(AtcAicFields.AIC_NOTA.toLowerCase());

		SKIP_FIELDS.add(AtcAicFields.AIC_PREZZO_AL_PUBBLICO.toLowerCase());
		SKIP_FIELDS.add(AtcAicFields.AIC_PREZZO_EX_FACTORY.toLowerCase());
		SKIP_FIELDS.add(AtcAicFields.AIC_PREZZO_MASSIMO_CESSIONE.toLowerCase());

		SKIP_FIELDS.add(AtcAicFields.AIC_UNITA_POSOLOGICA.toLowerCase());
		SKIP_FIELDS.add(AtcAicFields.AIC_PREZZO_UNITA_POSOLOGICA.toLowerCase());
		
		// Campi non indicizzati di LOCAL
		SKIP_FIELDS.add(StandardLocalFields.CS_TYPE_MAPPING.toLowerCase());
		SKIP_FIELDS.add(StandardLocalFields.CS_CODIFICATION_MAPPING.toLowerCase());
		SKIP_FIELDS.add(StandardLocalFields.CS_FILE_NAME_VERSION.toLowerCase());
	}

	public static JsonObject createByCodeSystemFields(final Session session, final String codeSystemName, final CodeSystemConcept concept) throws StiHibernateException, StiAuthorizationException,
			IndexException {

		JsonObject docObj = new JsonObject();
		Boolean leafVal = null;
		String codeSystemType = null;

		String checkVal = StiServiceUtil.trimStr(codeSystemName);
		if (checkVal.equalsIgnoreCase(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME)) {

			String termStr = StiServiceUtil.trimStr(concept.getCode());
			docObj.addProperty(Icd9CmFields.ICD9_CM_ID, termStr);

			if (termStr.indexOf("-") > 0) {
				docObj.addProperty(Icd9CmFields.ICD9_CM_CODE_RANGE, termStr);
			} else {
				docObj.addProperty(Icd9CmFields.ICD9_CM_CODE, termStr);
			}

			// Translations
			Set<CodeSystemConceptTranslation> trSet = concept.getCodeSystemConceptTranslations();
			if ((trSet != null) && (trSet.size() > 0)) {
				// Essendo le versioni inglesi più complete, se esistono traduzioni la entry principale è inglese
				docObj.addProperty(Icd9CmFields.ICD9_CM_DESCRIPTION_en, getNameOrDescription(concept.getTerm(), concept.getDescription()));

				Iterator<CodeSystemConceptTranslation> trSetIt = trSet.iterator();
				while (trSetIt.hasNext()) {
					CodeSystemConceptTranslation transl = trSetIt.next();

					// ICD-9 ha solo italiano oltre a inglese
					// String lCod = StiServiceUtil.trimStr(transl.getLanguageCd()).toLowerCase();
					docObj.addProperty(Icd9CmFields.ICD9_CM_DESCRIPTION_it, getNameOrDescription(transl.getTerm(), transl.getDescription()));
				}

				String capVal = getCapitolo(session, concept);
				if (!StiServiceUtil.isNull(capVal)) {
					docObj.addProperty(Icd9CmFields.ICD9_CM_CAPITOLO, capVal);
				}
			} else {
				// Altrimenti assumo che ci sia solo italiano
				docObj.addProperty(Icd9CmFields.ICD9_CM_DESCRIPTION_it, getNameOrDescription(concept.getTerm(), concept.getDescription()));
			}

			leafVal = concept.getCodeSystemEntityVersion().getIsLeaf();
		} else if (checkVal.equalsIgnoreCase(LoincFields.LOINC_CODE_SYSTEM_NAME)) {
			docObj.addProperty(LoincFields.LOINC_NUM, StiServiceUtil.trimStr(concept.getCode()));

			docObj.addProperty(LoincFields.COMPONENT_EN, StiServiceUtil.trimStr(concept.getTerm()));

			// Translations
			Set<CodeSystemConceptTranslation> trSet = concept.getCodeSystemConceptTranslations();
			Iterator<CodeSystemConceptTranslation> trSetIt = trSet.iterator();

			if ((trSet != null) && (trSet.size() > 0)) {
				while (trSetIt.hasNext()) {
					CodeSystemConceptTranslation transl = trSetIt.next();

					// Non tutti i termini LOINC sono tradotti in italiano
					docObj.addProperty(LoincFields.COMPONENT_IT, StiServiceUtil.trimStr(transl.getTerm()));
				}
			}

			if (!StiServiceUtil.isNull(concept.getDescription())) {
				docObj.addProperty(LoincFields.DEFINITION_DESCRIPTION, StiServiceUtil.trimStr(concept.getDescription()));
			}

			// In LOINC non presente la tassonomia
			leafVal = Boolean.TRUE;
		} else if (checkVal.equalsIgnoreCase(AtcAicFields.ATC_CODE_SYSTEM_NAME)) {
			docObj.addProperty(AtcAicFields.ATC_CODICE, StiServiceUtil.trimStr(concept.getCode()));

			docObj.addProperty(AtcAicFields.ATC_DENOMINAZIONE, StiServiceUtil.trimStr(concept.getTerm()));

			leafVal = concept.getCodeSystemEntityVersion().getIsLeaf();
		} else if (checkVal.equalsIgnoreCase(AtcAicFields.AIC_CODE_SYSTEM_NAME)) {
			SKIP_FIELDS.add(AtcAicFields.AIC_NOTA.toLowerCase());
			docObj.addProperty(AtcAicFields.AIC_CODICE, StiServiceUtil.trimStr(concept.getCode()));

			docObj.addProperty(AtcAicFields.AIC_DENOMINAZIONE, StiServiceUtil.trimStr(concept.getTerm()));

			// In AIC non presente la tassonomia
			leafVal = Boolean.TRUE;
		} else {
			//Nuovi CodeSystem STANDARD/LOCAL e ValueSet
			List<CodeSystem> csList = new GetCodeSystemByName(codeSystemName).execute(session);
			if (null != csList && !csList.isEmpty()) {
				CodeSystem codeSystem = csList.get(0);
				codeSystemType = codeSystem.getCodeSystemType();
				
				String SUBCLASS_OF = getSubClassOf(session, concept, null);
				if(concept.getCode().equals(SUBCLASS_OF)){
					SUBCLASS_OF = null;
				}

				if (codeSystemType.equals(CodeSystemType.LOCAL.getKey()) || codeSystemType.equals(CodeSystemType.STANDARD_NATIONAL.getKey())) {
					buildMetadataLocalCodeSystem(session, concept, docObj, codeSystem);
					leafVal = concept.getCodeSystemEntityVersion().getIsLeaf();
					docObj.addProperty(StandardLocalFields.SUBCLASS_OF, SUBCLASS_OF);
				}
				
				if (codeSystemType.equals(CodeSystemType.VALUE_SET.getKey())) {
					buildMetadataValueSet(session, concept, docObj, codeSystem);
					leafVal = concept.getCodeSystemEntityVersion().getIsLeaf();
					docObj.addProperty(ValueSetFields.SUBCLASS_OF, SUBCLASS_OF);
				}
				
			}

		}

		if (leafVal == null) {
			leafVal = Boolean.FALSE;
		}
		docObj.addProperty(CommonFields.IS_LEAF, leafVal);

		if (codeSystemType != null && 
				( codeSystemType.equals(CodeSystemType.LOCAL.getKey()) || codeSystemType.equals(CodeSystemType.STANDARD_NATIONAL.getKey())
				|| codeSystemType.equals(CodeSystemType.VALUE_SET.getKey()) )) {
			DbTransformUtil.addMetadataValuesDinamicFields(concept, docObj, SKIP_FIELDS);
			IndexDocumentBuilder.addCsVersioneInfoLocal(concept, docObj);
		} else {
			DbTransformUtil.addMetadataValues(concept, docObj, SKIP_FIELDS, false);
		}
		
		IndexDocumentBuilder.addCsVersionInfo(session, docObj, concept);

		// Inserimento id univoco di SOLR
		String documentId = null;
		if (checkVal.equalsIgnoreCase(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME)) {
			documentId = docObj.getAsJsonPrimitive(Icd9CmFields.ICD9_CM_ID).getAsString();
		} else if (checkVal.equalsIgnoreCase(LoincFields.LOINC_CODE_SYSTEM_NAME)) {
			documentId = docObj.getAsJsonPrimitive(LoincFields.LOINC_NUM).getAsString();
		} else if (checkVal.equalsIgnoreCase(AtcAicFields.ATC_CODE_SYSTEM_NAME)) {
			documentId = docObj.getAsJsonPrimitive(AtcAicFields.ATC_CODICE).getAsString();
		} else if (checkVal.equalsIgnoreCase(AtcAicFields.AIC_CODE_SYSTEM_NAME)) {
			documentId = docObj.getAsJsonPrimitive(AtcAicFields.AIC_CODICE).getAsString();

			// FIX Versione ATC
			if (docObj.get(AtcAicFields.ATC_CODICE) != null) {
				if (docObj.get(AtcAicFields.VERSIONE_ATC) == null) {
					docObj.addProperty(AtcAicFields.VERSIONE_ATC, "2014");
				}
			}
		} else if (codeSystemType != null && 
				( codeSystemType.equals(CodeSystemType.LOCAL.getKey()) || codeSystemType.equals(CodeSystemType.STANDARD_NATIONAL.getKey()) 
				|| codeSystemType.equals(CodeSystemType.VALUE_SET.getKey()) )) {
			documentId = (StiServiceUtil.trimStr(codeSystemName) + "#" + StiServiceUtil.trimStr(concept.getCode()) + "#" + StiServiceUtil.trimStr(concept.getTerm())).toLowerCase().replaceAll("\\s", "");
		} else {
			throw new IndexException("Valore non valido per Code System: " + codeSystemName);
		}

		documentId += "#" + docObj.getAsJsonPrimitive(CommonFields.VERSION).getAsString();
		docObj.addProperty(DOCUMENT_ID, documentId);

		return docObj;
	}

	/*Recupera la gerarchia della classificazione a partire da un concept*/
	private static String getSubClassOf(final Session session, final CodeSystemConcept concept,final String SUBCLASS_OF) throws StiAuthorizationException, StiHibernateException {
		String result = SUBCLASS_OF;
		if(concept != null){
			if(result!=null){
				result=concept.getCode()+"_"+result;
			}
			else{
				/*FIRST STEP*/
				result=concept.getCode();
			}
			
			CodeSystemConcept parent = new GetParentConcept(concept).execute(session);
			if(parent != null){
				result = getSubClassOf(session, parent, result);
			}
			else{
				if(result!=null && result.endsWith("_")){
					/*LAST STEP*/
					result=result.substring(0,result.length()-1);
				}
			}
		}
		return result;
	}
	


	private static String getCapitolo(final Session session, final CodeSystemConcept concept) throws StiHibernateException, StiAuthorizationException {

		CodeSystemConcept parent = concept;
		while (parent != null) {
			parent = new GetParentConcept(parent).execute(session);

			if (parent == null) {
				return null;
			}

			if (parent.getCodeSystemConceptTranslations() != null) {
				Iterator<CodeSystemConceptTranslation> concIt = parent.getCodeSystemConceptTranslations().iterator();

				while (concIt.hasNext()) {
					CodeSystemConceptTranslation tras = concIt.next();
					if (StiServiceUtil.trimStr(tras.getTerm().toLowerCase()).startsWith("capitolo")) {
						return StiServiceUtil.trimStr(tras.getTerm()).replace(" ", "_");
					}
				}
			}
		}

		return null;
	}

	private static void addCsVersionInfo(final Session session, final JsonObject docObj, final CodeSystemConcept concept) throws StiHibernateException, StiAuthorizationException {
		Set<CodeSystemVersionEntityMembership> memSet = concept.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships();

		if ((memSet == null) || (memSet.size() != 1)) {
			throw new StiHibernateException("Il sistema attualmente supporta una singola associazione tra la versione del CS e la Entity.");
		}

		CodeSystemVersionEntityMembership mem = memSet.iterator().next();
		docObj.addProperty(CommonFields.VERSION, StiServiceUtil.trimStr(mem.getCodeSystemVersion().getName()));

		CodeSystemConcept lastVers = new GetConceptLastVersion(concept.getCode(), mem.getCodeSystemVersion().getCodeSystem().getName()).execute(session);

		boolean lastVerVal = (concept.getCodeSystemEntityVersion().getVersionId().longValue() == lastVers.getCodeSystemEntityVersion().getVersionId().longValue());

		docObj.addProperty(CommonFields.IS_LAST_VERSION, lastVerVal);
	}
	
	
	private static void addCsVersioneInfoLocal(final CodeSystemConcept concept, JsonObject docObj) throws StiHibernateException {
		Set<CodeSystemVersionEntityMembership> memSet = concept.getCodeSystemEntityVersion().getCodeSystemEntity().getCodeSystemVersionEntityMemberships();

		if (memSet != null && memSet.size() > 1) {
			throw new StiHibernateException("Il sistema attualmente supporta una singola associazione tra la versione del CS e la Entity.");
		}
		
		if (memSet != null && (memSet.size() > 0)) {
			CodeSystemVersionEntityMembership mem = memSet.iterator().next();
			docObj.addProperty(StandardLocalFields.VERSION_NAME, StiServiceUtil.trimStr(mem.getCodeSystemVersion().getName()));
			docObj.addProperty(StandardLocalFields.VERSION_DESCRIPTION, StiServiceUtil.trimStr(mem.getCodeSystemVersion().getDescription()));
		}
	}


	private static String getNameOrDescription(final String nameVal, final String descrVal) {
		if ((!StiServiceUtil.isNull(nameVal)) && (!StiServiceUtil.trimStr(nameVal).toLowerCase().startsWith("capitolo"))
				&& (!StiServiceUtil.trimStr(nameVal).toLowerCase().startsWith("classificazione supplementare"))
				&& (!StiServiceUtil.trimStr(nameVal).toLowerCase().startsWith("classificazion supplementare"))) {
			return StiServiceUtil.trimStr(nameVal);
		}
		return StiServiceUtil.trimStr(descrVal);
	}
	
	



	private static void buildMetadataLocalCodeSystem(final Session session, final CodeSystemConcept concept, JsonObject docObj, CodeSystem codeSystem) throws StiAuthorizationException,
			StiHibernateException {
		docObj.addProperty(StandardLocalFields.NAME, StiServiceUtil.trimStr(codeSystem.getName()));
		docObj.addProperty(StandardLocalFields.DESCRIPTION, StiServiceUtil.trimStr(codeSystem.getDescription()));
		
		
		docObj.addProperty(StandardLocalFields.CS_CODE, StiServiceUtil.trimStr(concept.getCode()));
		//docObj.addProperty(StandardLocalFields.CS_DESCRIPTION, StiServiceUtil.trimStr(concept.getTerm()));
		
		
		String langPrimary = StiServiceUtil.trimStr(concept.getLanguageCd());
		String descriptionPrimary = StiServiceUtil.trimStr(concept.getTerm());
		String descriptionTranslate = "";

		Set<CodeSystemConceptTranslation> trSrc = concept.getCodeSystemConceptTranslations();
		if ((trSrc != null) && (trSrc.size() > 0)) {
			CodeSystemConceptTranslation translSrc = trSrc.iterator().next();
			descriptionTranslate = StiServiceUtil.trimStr(translSrc.getTerm());
		}


		if(langPrimary.equalsIgnoreCase(StiConstants.LANG_IT)){
			if(!StiServiceUtil.isNull(descriptionPrimary)){
				docObj.addProperty(StandardLocalFields.CS_DESCRIPTION_it, StiServiceUtil.trimStr(descriptionPrimary));
			}
			if(!StiServiceUtil.isNull(descriptionTranslate)){
				docObj.addProperty(StandardLocalFields.CS_DESCRIPTION_en, StiServiceUtil.trimStr(descriptionTranslate));
			}
			
		}
		else if(langPrimary.equalsIgnoreCase(StiConstants.LANG_EN)){
			if(!StiServiceUtil.isNull(descriptionPrimary)){
				docObj.addProperty(StandardLocalFields.CS_DESCRIPTION_en, StiServiceUtil.trimStr(descriptionPrimary));
			}
			if(!StiServiceUtil.isNull(descriptionTranslate)){
				docObj.addProperty(StandardLocalFields.CS_DESCRIPTION_it, StiServiceUtil.trimStr(descriptionTranslate));
			}
		}


		List<ExtraMetadataParameter> emp = new GetExtraMetadataParameterValueByCs(codeSystem.getId()).execute(session);
		for (ExtraMetadataParameter extraMetadataParameter : emp) {
			String paramName = extraMetadataParameter.getParamName();
			if ((SKIP_FIELDS != null) && (SKIP_FIELDS.contains(paramName.toLowerCase()))) {
				continue;
			}
			if (paramName.toLowerCase().startsWith(StandardLocalFields.CS_FILE_NAME_VERSION.toLowerCase())) {
				continue;
			}
			docObj.addProperty(paramName.toUpperCase(), StiServiceUtil.trimStr(extraMetadataParameter.getParamValue()));
		}
	}
	
	
	private static void buildMetadataValueSet(final Session session, final CodeSystemConcept concept, JsonObject docObj, CodeSystem codeSystem) throws StiAuthorizationException, StiHibernateException {
		docObj.addProperty(ValueSetFields.NAME, StiServiceUtil.trimStr(codeSystem.getName()));
		docObj.addProperty(ValueSetFields.DESCRIPTION, StiServiceUtil.trimStr(codeSystem.getDescription()));
		docObj.addProperty(ValueSetFields.VALUESET_CODE, StiServiceUtil.trimStr(concept.getCode()));
		//docObj.addProperty(ValueSetFields.VALUESET_DESCRIPTION_it, StiServiceUtil.trimStr(concept.getTerm()));
		
		
		String langPrimary = StiServiceUtil.trimStr(concept.getLanguageCd());
		String descriptionPrimary = StiServiceUtil.trimStr(concept.getTerm());
		String descriptionTranslate = "";

		Set<CodeSystemConceptTranslation> trSrc = concept.getCodeSystemConceptTranslations();
		if ((trSrc != null) && (trSrc.size() > 0)) {
			CodeSystemConceptTranslation translSrc = trSrc.iterator().next();
			descriptionTranslate = StiServiceUtil.trimStr(translSrc.getTerm());
		}


		if(langPrimary.equalsIgnoreCase(StiConstants.LANG_IT)){
			if(!StiServiceUtil.isNull(descriptionPrimary)){
				docObj.addProperty(ValueSetFields.VALUESET_DESCRIPTION_it, StiServiceUtil.trimStr(descriptionPrimary));
			}
			if(!StiServiceUtil.isNull(descriptionTranslate)){
				docObj.addProperty(ValueSetFields.VALUESET_DESCRIPTION_en, StiServiceUtil.trimStr(descriptionTranslate));
			}
			
		}
		else if(langPrimary.equalsIgnoreCase(StiConstants.LANG_EN)){
			if(!StiServiceUtil.isNull(descriptionPrimary)){
				docObj.addProperty(ValueSetFields.VALUESET_DESCRIPTION_en, StiServiceUtil.trimStr(descriptionPrimary));
			}
			if(!StiServiceUtil.isNull(descriptionTranslate)){
				docObj.addProperty(ValueSetFields.VALUESET_DESCRIPTION_it, StiServiceUtil.trimStr(descriptionTranslate));
			}
		}
		
		
		List<ExtraMetadataParameter> emp = new GetExtraMetadataParameterValueByCs(codeSystem.getId()).execute(session);
		for (ExtraMetadataParameter extraMetadataParameter : emp) {
			String paramName = extraMetadataParameter.getParamName();
			if ((SKIP_FIELDS != null) && (SKIP_FIELDS.contains(paramName.toLowerCase()))) {
				continue;
			}
			if (paramName.toLowerCase().startsWith(StandardLocalFields.CS_FILE_NAME_VERSION.toLowerCase())) {
				continue;
			}
			docObj.addProperty(paramName.toUpperCase(), StiServiceUtil.trimStr(extraMetadataParameter.getParamValue()));
		}
	}


}
