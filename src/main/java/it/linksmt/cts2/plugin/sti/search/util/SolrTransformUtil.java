package it.linksmt.cts2.plugin.sti.search.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.SearchAssociations;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.atc_aic.AtcAicFields;
import it.linksmt.cts2.plugin.sti.importer.icd9cm.Icd9CmFields;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public final class SolrTransformUtil {

	private static Logger log = Logger.getLogger(SolrTransformUtil.class);

	private SolrTransformUtil() { }

	// Campi non inseriti nella griglia dei risultati
	private static HashSet<String> SKIP_FIELDS = new HashSet<String>();
	static {
		// Campi utilizzati per ragioni tecniche di SOLR
		SKIP_FIELDS.add("id");
		SKIP_FIELDS.add("_version_");
		SKIP_FIELDS.add("text");

		// Campi già disponibili nel formato del risultato previsto da CTS2
		SKIP_FIELDS.add(LoincFields.LOINC_NUM.trim().toLowerCase());
		SKIP_FIELDS.add(Icd9CmFields.ICD9_CM_ID.trim().toLowerCase());
	}

	public static EntityDirectoryEntry solrDocToEntityEntry(
			final HibernateUtil hibUtil,
			final String codeSystemName,
			final JsonObject solrDoc,
			final AbstractStiService service) {

		String entityId = null;

		if (Icd9CmFields.ICD9_CM_CODE_SYSTEM_NAME.equalsIgnoreCase(
				StiServiceUtil.trimStr(codeSystemName))) {

			entityId = solrDoc.getAsJsonPrimitive(
					Icd9CmFields.ICD9_CM_ID).getAsString();
		}
		else if (LoincFields.LOINC_CODE_SYSTEM_NAME.equalsIgnoreCase(
				StiServiceUtil.trimStr(codeSystemName))) {

			entityId = solrDoc.getAsJsonPrimitive(
					LoincFields.LOINC_NUM).getAsString();
		}
		else if (AtcAicFields.ATC_CODE_SYSTEM_NAME.equalsIgnoreCase(
				StiServiceUtil.trimStr(codeSystemName))) {

			entityId = solrDoc.getAsJsonPrimitive(
					AtcAicFields.ATC_CODICE).getAsString();
		}
		else if (AtcAicFields.AIC_CODE_SYSTEM_NAME.equalsIgnoreCase(
				StiServiceUtil.trimStr(codeSystemName))) {

			entityId = solrDoc.getAsJsonPrimitive(
					AtcAicFields.AIC_CODICE).getAsString();
		}else{
			try {
				List<CodeSystem> csl = (List<CodeSystem>) hibUtil.executeBySystem(new GetCodeSystemByName(codeSystemName));
				if(csl!=null && csl.size()>0){
					CodeSystem cs = csl.get(0);
					if(cs!=null && cs.getCodeSystemType()!=null){
						if(cs.getCodeSystemType().equals(CodeSystemType.LOCAL.getKey()) || cs.getCodeSystemType().equals(CodeSystemType.STANDARD_NATIONAL.getKey())) {
							entityId = solrDoc.getAsJsonPrimitive(StandardLocalFields.CS_CODE).getAsString();
						}else if(cs.getCodeSystemType().equals(CodeSystemType.VALUE_SET.getKey())) {
							entityId = solrDoc.getAsJsonPrimitive(ValueSetFields.VALUESET_CODE).getAsString();
						}
					}
				}
			}catch(Exception e){
				log.info("Impossibile creare l'uri del record");
			}
		}

		ScopedEntityName sName = ModelUtils.createScopedEntityName(
				entityId, codeSystemName);

		EntityDirectoryEntry entry = new EntityDirectoryEntry();

		entry.setName(sName);
		entry.setResourceName(entityId);

		// Entity URI
		try {
			entry.setAbout(service.getUrlConstructor().createEntityUrl(sName));
		}catch(Exception e){
			log.info("Impossibile creare l'uri del record");
		}
		String codeSystemVersion = solrDoc.getAsJsonPrimitive(
				CommonFields.VERSION).getAsString();

		try {
		entry.setHref(service.getUrlConstructor().createEntityUrl(
				codeSystemName, codeSystemVersion, sName));
		}catch(Exception e){
			log.info("Impossibile creare l'href del record");
		}
		
		DescriptionInCodeSystem descCS = new DescriptionInCodeSystem();
		descCS.setDescribingCodeSystemVersion(service.buildCodeSystemVersionReference(codeSystemName, codeSystemVersion));

		JsonObject desObj = new JsonObject();
		Iterator<Entry<String, JsonElement>> jsonIt = solrDoc.entrySet().iterator();

		while (jsonIt.hasNext()) {
			Entry<String, JsonElement> jsonProp = jsonIt.next();
			if (SKIP_FIELDS.contains(jsonProp.getKey().trim().toLowerCase())) {
				continue;
			}
			desObj.add(jsonProp.getKey().trim(), jsonProp.getValue());			
		}
		
		//Set correctly the name in the ATC case
		if (AtcAicFields.ATC_CODE_SYSTEM_NAME.equalsIgnoreCase(
				StiServiceUtil.trimStr(codeSystemName))) {
			desObj.add(CommonFields.NAME + "_it", solrDoc.get(AtcAicFields.ATC_DENOMINAZIONE));
		}

		try {
			SearchAssociations assCmd = new SearchAssociations(codeSystemVersion, null, null, entityId, service);

			List<Association> assList = (List<Association>)hibUtil.executeBySystem(assCmd);

			boolean hasCrossMap = (assList != null) && (assList.size() > 0);
			desObj.addProperty(CommonFields.HAS_ASSOCIATIONS, hasCrossMap);
		}
		catch(Exception ex) {
			log.error("Impossibile leggere le informazioni sulla presenza di Cross-Mapping: " + entityId);
		}

		descCS.setDesignation(desObj.toString());

		ArrayList<DescriptionInCodeSystem> arrDes = new ArrayList<DescriptionInCodeSystem>();
		arrDes.add(descCS);

		entry.setKnownEntityDescription(arrDes);
		return entry;
	}
}
