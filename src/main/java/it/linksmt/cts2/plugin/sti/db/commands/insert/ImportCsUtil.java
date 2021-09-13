package it.linksmt.cts2.plugin.sti.db.commands.insert;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersions;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchCodeSystems;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConceptTranslation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntity;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemMetadataValue;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembershipId;
import it.linksmt.cts2.plugin.sti.db.model.MetadataParameter;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class ImportCsUtil {

	public static final int CHUNK_SIZE_IMPORT =  100;
	public static final int CHUNK_SIZE_INDEX  = 1000;

	private static Logger log = Logger.getLogger(ImportCsUtil.class);

	private ImportCsUtil() { }

	public static void setCurrentCodeSystemVersion(
			final Session session, CodeSystemVersion newVers)
		throws StiAuthorizationException, StiHibernateException {

		CodeSystemVersion nextVers = null;

		List<CodeSystemVersion> nextList = new GetCodeSystemVersions(
				newVers.getCodeSystem().getName(),
				StiConstants.STATUS_CODES.ACTIVE).execute(session);

		for (int i = 0; i < nextList.size(); i++) {
			CodeSystemVersion curVerNext = nextList.get(i);

			if ( curVerNext.getReleaseDate().after(newVers.getReleaseDate()) &&
				((nextVers == null) || (curVerNext.getReleaseDate().before(
						nextVers.getReleaseDate())) ) ) {
				nextVers = curVerNext;
			}
		}

		if (nextVers != null) {
			nextVers.setPreviousVersionId(newVers.getVersionId());

			session.save(nextVers);
			session.flush();
			session.refresh(nextVers);
		}
		else {
			CodeSystem curCs = (CodeSystem)session.get(CodeSystem.class,
					newVers.getCodeSystem().getId().longValue());
			curCs.setCurrentVersionId(newVers.getVersionId());

			session.save(curCs);
			session.flush();
			session.refresh(curCs);
		}

		// Imposto la nuova versione come attiva
		newVers = (CodeSystemVersion) session.get(
				CodeSystemVersion.class, newVers.getVersionId().longValue());

		newVers.setStatus(StiConstants.STATUS_CODES.ACTIVE.getCode());
		newVers.setStatusDate(new Date());
		newVers.setInsertTimestamp(new Date());
		newVers.setLastChangeDate(new Date());

		session.save(newVers);
		session.flush();
		session.refresh(newVers);
	}

	public static CodeSystemVersion createCsVersion(
			final Session session, final String codeSystemName,
			final String csVersionName, final String csDescription,
			final String oid, final Date releaseDate,final String languagePrimary, final String languageTranslate)
		throws StiHibernateException, StiAuthorizationException {

		// Individuo il code System
		List<CodeSystem> resCs = new SearchCodeSystems(
				codeSystemName, false, null).execute(session);

		if ((resCs == null) || (resCs.size() != 1)) {
			throw new StiHibernateException("Impossibile individuare il CS: " +
					codeSystemName);
		}
		if (codeSystemName.indexOf(':') > -1) {
			throw new StiHibernateException("Carattere non valido (:) "
					+ "nel nome del CodeSystem: " + codeSystemName);
		}

		CodeSystemVersion prevVers = null;
		List<CodeSystemVersion> prevList = new GetCodeSystemVersions(
				codeSystemName, StiConstants.STATUS_CODES.ACTIVE).execute(session);

		for (int i = 0; i < prevList.size(); i++) {
			CodeSystemVersion curVerPrev = prevList.get(i);

			if (StiServiceUtil.trimStr(csVersionName).equalsIgnoreCase(
					StiServiceUtil.trimStr(curVerPrev.getName()))) {
				throw new StiHibernateException("La versione risulta esistente "
						+ "all'interno del sistema: " + csVersionName);
			}

			if ( curVerPrev.getReleaseDate().before(releaseDate) &&
				((prevVers == null) || (curVerPrev.getReleaseDate().after(
						prevVers.getReleaseDate())) ) ) {
				prevVers = curVerPrev;
			}
		}

		CodeSystem codeSystem = resCs.get(0);

		CodeSystemVersion newVers = new CodeSystemVersion();
		newVers.setCodeSystem(codeSystem);
		newVers.setDescription(csDescription);

		newVers.setExpirationDate(null);
		newVers.setInsertTimestamp(new Date());
		newVers.setLastChangeDate(new Date());

		newVers.setName(csVersionName);
		newVers.setOid(oid);
//		newVers.setAvailableLanguages(StiConstants.LANG_IT+", "+StiConstants.LANG_EN);
//		newVers.setPreferredLanguageCd(StiConstants.LANG_IT);
		if((languagePrimary==null || "".equals(languagePrimary)) && (languageTranslate==null || "".equals(languageTranslate))){
			newVers.setAvailableLanguages(StiConstants.LANG_IT+", "+StiConstants.LANG_EN);
			newVers.setPreferredLanguageCd(StiConstants.LANG_IT);
		}else if((languagePrimary!=null && !"".equals(languagePrimary)) && (languageTranslate==null || "".equals(languageTranslate))){
			newVers.setAvailableLanguages(languagePrimary);
			newVers.setPreferredLanguageCd(languagePrimary);
		}else if((languagePrimary!=null && !"".equals(languagePrimary)) && (languageTranslate!=null && !"".equals(languageTranslate))){
			newVers.setAvailableLanguages(languagePrimary+", "+languageTranslate);
			newVers.setPreferredLanguageCd(languagePrimary);
		}
		
		

		Long preVersId = null;
		if (prevVers != null) {
			preVersId = prevVers.getVersionId();
		}
		newVers.setPreviousVersionId(preVersId);

		// TODO: ???
		newVers.setReleaseDate(releaseDate);
		newVers.setSource(null);
		newVers.setStatus(StiConstants.STATUS_CODES.INACTIVE.getCode());
		newVers.setStatusDate(new Date());

		newVers.setUnderLicence(false);
		newVers.setLicenceHolder(null);

		// TODO: ???
		newVers.setValidityRange(new Long(4));

		session.save(newVers);
		session.flush();

		session.refresh(newVers);
		log.info("Inserita nuova versione Code System. Id: " + newVers.getVersionId());

		return newVers;
	}

	public static CodeSystemEntityVersion insertEntity(
			final Session session, final CodeSystemVersion csVers,
			final String code, final String term,
			final String term_it, final String termAbbrevation,
			final String description, final String description_it,
			final Date releaseDate, final boolean isLeaf,
			final CodeSystemConcept superClass,
			final HashSet<String> skipFields, 
			final JsonObject metadataValPrimary,
			final JsonObject metadataValTranslation,
			final String languagePrimary,
			final String languageTranslate)
		throws StiHibernateException, StiAuthorizationException {
		
		CodeSystemEntity csEntity = new CodeSystemEntity();
		session.save(csEntity);
		session.flush();
		session.refresh(csEntity);

		// Entity Version
		CodeSystemEntityVersion csEntityVers = new CodeSystemEntityVersion();
		csEntityVers.setCodeSystemEntity(csEntity);

		Long previousVersionId = null;
		if (csVers.getPreviousVersionId() != null) {
			CodeSystemConcept prevConc = new GetCodeSystemConcept(
					code, csVers.getPreviousVersionId().longValue())
					.execute(session);

			if (prevConc != null) {
				previousVersionId = prevConc.getCodeSystemEntityVersionId();
			}
		}
		csEntityVers.setPreviousVersionId(previousVersionId);

		if (StiServiceUtil.isNull(term)) {
			log.error(  "Errore: Termine non specificato "+(languagePrimary!=null?"("+languagePrimary+")":"")+": " + metadataValPrimary.toString());
			throw new StiHibernateException(
						"Errore: Termine non specificato "+(languagePrimary!=null?"("+languagePrimary+")":"")+": " + metadataValPrimary.toString());
		}

		csEntityVers.setEffectiveDate(releaseDate);
		csEntityVers.setInsertTimestamp(new Date());

		csEntityVers.setIsLeaf(isLeaf);

		csEntityVers.setMajorRevision(1);
		csEntityVers.setMinorRevision(0);

		csEntityVers.setStatusDeactivated(0);
		csEntityVers.setStatusDeactivatedDate(null);

		csEntityVers.setStatusVisibility(1);
		csEntityVers.setStatusVisibilityDate(new Date());

		csEntityVers.setStatusWorkflow(0);
		csEntityVers.setStatusWorkflowDate(null);

		session.save(csEntityVers);
		session.flush();
		session.refresh(csEntityVers);

		// Code System Concept
		CodeSystemConcept csConc = new CodeSystemConcept();
		csConc.setCodeSystemEntityVersion(csEntityVers);
		csConc.setCodeSystemEntityVersionId(csEntityVers.getVersionId().longValue());

		csConc.setCode(StiServiceUtil.trimStr(code).toUpperCase());
		csConc.setTerm(term);

		if (termAbbrevation != null) {
			csConc.setTermAbbrevation(StiServiceUtil.trimStr(termAbbrevation).toUpperCase());
		}
		else {
			csConc.setTermAbbrevation(null);
		}

		csConc.setDescription(StiServiceUtil.trimStr(description));
		csConc.setMeaning(null);
		csConc.setHints(null);
		csConc.setIsPreferred(true);
		
		if(languagePrimary!=null){
			csConc.setLanguageCd(languagePrimary);	
		}

		session.save(csConc);
		session.flush();
		session.refresh(csConc);

		// Code System Concept Translation (IT)
		if (!StiServiceUtil.isNull(term_it)) {
			CodeSystemConceptTranslation concTr = new CodeSystemConceptTranslation();
			concTr.setCodeSystemConcept(csConc);
			concTr.setLanguageCd(languageTranslate);
			concTr.setTerm(term_it);
			concTr.setTermAbbrevation(null);

			concTr.setDescription(description_it);
			csConc.setMeaning(null);
			csConc.setHints(null);

			session.save(concTr);
			session.flush();
			session.refresh(concTr);
		}

		// Inserimento degli altri Meta-Dati
		insertMetadata(session, csVers.getCodeSystem(), csEntityVers, skipFields, metadataValPrimary, languagePrimary);
		
		if(metadataValTranslation!=null && languageTranslate!=null){
			// Inserimento degli altri Meta-Dati file secondario contenente le traduzioni
			insertMetadata(session, csVers.getCodeSystem(), csEntityVers, skipFields, metadataValTranslation, languageTranslate);
		}

		// Aggiornamento Entity
		csEntity.setCurrentVersionId(csEntityVers.getVersionId().longValue());

		session.save(csEntity);
		session.flush();
		session.refresh(csEntity);

		// Inserisco la relazione Tassonomica
		if (superClass != null) {

			// Fix nel caso la superclasse avesse settato isLeaf
			if (superClass.getCodeSystemEntityVersion().getIsLeaf()) {
				CodeSystemEntityVersion superEnt = superClass.getCodeSystemEntityVersion();
				superEnt.setIsLeaf(Boolean.FALSE);

				session.save(superEnt);
				session.flush();
				session.refresh(superEnt);
			}

			CodeSystemEntityVersionAssociation assoc = new CodeSystemEntityVersionAssociation();

			assoc.setLeftId(csEntityVers.getVersionId().longValue());
			assoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(csEntityVers);
			assoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(superClass.getCodeSystemEntityVersion());

			assoc.setForwardName(StiConstants.TAXONOMY_FORWARD_NAME);
			assoc.setReverseName(StiConstants.TAXONOMY_REVERSE_NAME);
			assoc.setAssociationKind(StiConstants.ASSOCIATION_KIND.TAXONOMY.getCode());

			assoc.setInsertTimestamp(new Date());
			assoc.setStatus(StiConstants.STATUS_CODES.ACTIVE.getCode());
			assoc.setStatusDate(new Date());

			assoc.setMapSetVersion(null);

			session.save(assoc);
			session.flush();
			session.refresh(assoc);
		}

		// Inserisco l'associazione con il Code System
		CodeSystemVersionEntityMembership memb = new CodeSystemVersionEntityMembership();
		CodeSystemVersionEntityMembershipId membId = new CodeSystemVersionEntityMembershipId();

		membId.setCodeSystemEntityId(csEntity.getId().longValue());
		membId.setCodeSystemVersionId(csVers.getVersionId().longValue());

		memb.setId(membId);

		// TODO: ???
		memb.setIsAxis(false);
		memb.setIsMainClass(false);
		memb.setOrderNr(null);

		session.save(memb);
		session.flush();
		session.refresh(memb);

		// Aggiorno le entità che attualmente puntano alla precedente con la nuova
		if ((previousVersionId != null) && (previousVersionId.longValue() > 0)) {

			List<CodeSystemEntityVersion> updList = session.createCriteria(CodeSystemEntityVersion.class)
			.add(Restrictions.eq("previousVersionId", previousVersionId.longValue())).list();

			if (updList != null) {
				for (int i = 0; i < updList.size(); i++) {
					CodeSystemEntityVersion nextVers = updList.get(i);
					if (nextVers.getVersionId().longValue() !=
							csEntityVers.getVersionId().longValue()) {

						nextVers.setPreviousVersionId(csEntityVers.getVersionId());

						session.save(nextVers);
						session.flush();
					}
				}
			}
		}
		else {
			// Si tratta della prima versione in ordine cronologico
			CodeSystemVersion firstVersion = (CodeSystemVersion) session.createCriteria(CodeSystemVersion.class)
				.add(Restrictions.eq("codeSystem.id", csVers.getCodeSystem().getId().longValue()))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode()))
				.add(Restrictions.isNull("previousVersionId")).uniqueResult();

			if (firstVersion != null) {
				CodeSystemConcept nextConc = new GetCodeSystemConcept(
						code, firstVersion.getVersionId().longValue())
						.execute(session);

				if (nextConc != null) {
					CodeSystemEntityVersion nextVers = nextConc.getCodeSystemEntityVersion();
					nextVers.setPreviousVersionId(csEntityVers.getVersionId());

					session.save(nextVers);
					session.flush();
				}
			}
		}

		return csEntityVers;
	}

	private static void insertMetadata(final Session session,
			final CodeSystem cs, final CodeSystemEntityVersion csEntityVers,
			final HashSet<String> skipFields,
			final JsonObject metadataVal, String language) throws StiHibernateException {

		Iterator<Entry<String, JsonElement>> members = metadataVal.entrySet().iterator();
		while (members.hasNext()) {
			Entry<String, JsonElement> jsonProp = members.next();
			if (skipFields.contains(jsonProp.getKey().trim().toLowerCase())) {
				continue;
			}

			String metaName = jsonProp.getKey().trim();
			String langCode = null;
			
			
			/*Il seguente flag 'oldCs' serve per mantenere la compatibilità con le vecchie importazioni*/
			boolean oldCs = false;
			if(cs.getName().equals(LoincFields.LOINC_CODE_SYSTEM_NAME) 
					|| !cs.getName().equals(Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME)
					|| !cs.getName().equals(AtcAicFields.ATC_CODE_SYSTEM_NAME)
					|| !cs.getName().equals(AtcAicFields.AIC_CODE_SYSTEM_NAME)){
				oldCs = true;
			}
			
			
			
			if(language!=null && !oldCs){
				langCode = language.toUpperCase();
			}
			else{
				if (metaName.toLowerCase().endsWith("_it")) {
					metaName = metaName.substring(0, metaName.length()-3);
					langCode = StiConstants.LANG_IT;
				}
				else if (metaName.toLowerCase().endsWith("_en")) {
					metaName = metaName.substring(0, metaName.length()-3);
					langCode = StiConstants.LANG_EN;
				}
			}
			
			Criteria critMeta = session.createCriteria(MetadataParameter.class)
					.add(Restrictions.eq("paramName", StiServiceUtil.paramNameToUpperCaseAndClean(metaName)))
					.add(Restrictions.eq("codeSystem.id", cs.getId().longValue()));


			if (!StiServiceUtil.isNull(langCode)) {
//				critMeta = critMeta.add(Restrictions.eq("languageCd", langCode));
				if(oldCs){
					critMeta = critMeta.add(Restrictions.or(Restrictions.eq("languageCd", langCode), Restrictions.isNull("languageCd")));
				}
				else{
					critMeta = critMeta.add(Restrictions.eq("languageCd", langCode));
				}
			}
			

			MetadataParameter metaParam = (MetadataParameter)critMeta.uniqueResult();
			if (metaParam == null) {
				throw new StiHibernateException("Impossibile trovare la definizione del metadato: " + metaName);
			}

			JsonElement propVal = jsonProp.getValue();
			if (propVal.isJsonArray()) {
				JsonArray valueArr = propVal.getAsJsonArray();
				for (int i = 0; i < valueArr.size(); i++) {

					String paramVal = null;
					if (valueArr.get(i) != null) {
						paramVal = valueArr.get(i).getAsString();
					}

					insertSingleValue(session, csEntityVers, metaParam, paramVal);
				}
			}
			else if (!propVal.isJsonNull()) {
				insertSingleValue(session, csEntityVers, metaParam, propVal.getAsString());
			}
		}
	}

	private static CodeSystemMetadataValue insertSingleValue(
			final Session session,
			final CodeSystemEntityVersion csEntityVers,
			final MetadataParameter metaParam, final String value) {

		CodeSystemMetadataValue metaVal = new CodeSystemMetadataValue();
		metaVal.setParameterValue(StiServiceUtil.trimStr(value));
		metaVal.setMetadataParameter(metaParam);
		metaVal.setCodeSystemEntityVersion(csEntityVers);

		session.save(metaVal);
		session.flush();
		session.refresh(metaVal);

		return metaVal;
	}
}
