package it.linksmt.cts2.plugin.sti.service.changeset;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.core.SourceReference;
import edu.mayo.cts2.framework.model.core.types.FinalizableState;
import edu.mayo.cts2.framework.model.updates.ChangeSet;
import edu.mayo.cts2.framework.service.profile.update.ChangeSetService;
import it.linksmt.cts2.plugin.sti.db.commands.delete.DeleteLocalMapping;
import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportMappingAicAtc;
import it.linksmt.cts2.plugin.sti.db.commands.insert.ValidateOrDeleteAssociation;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.TempImportazione;
import it.linksmt.cts2.plugin.sti.db.util.DbUtil;
import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.enums.MetadataParameterType;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.loinc.LoincFields;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.ImportLocalMapping;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionAicWorkflow;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionAtcWorkflow;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionIcd9Workflow;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionStandardLocalWorkflow;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionLoincWorkflow;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.NewVersionValueSetWorkflow;
import it.linksmt.cts2.plugin.sti.service.changeset.impl.ReindexWorkflow;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiChangeSetService extends AbstractStiService implements
		ChangeSetService {

	private static Logger log = Logger.getLogger(StiChangeSetService.class);

	private static String SOURCE_TABLE_URL = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_TEMP_IMPORT_ADDRESS, "");
	private static String SOURCE_TABLE_USER = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_TEMP_IMPORT_USER, "");
	private static String SOURCE_TABLE_PASS = StiAppConfig.getProperty(StiServiceConfiguration.CTS2_TEMP_IMPORT_PASS, "");

	@Override
	public ChangeSet readChangeSet(final String changeSetUri) {
		try {
			TempImportazione last = DbUtil.readLast(SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS);

			FinalizableState state = FinalizableState.OPEN;
			if (canImport()) {
				state = FinalizableState.FINAL;
			}

			ChangeSet retVal = new ChangeSet();

			if (last != null) {
				retVal.setChangeSetURI(last.getNameCodeSystem() + ":"
						+ last.getVersionCodeSystem());
			}

			retVal.setState(state);
			return retVal;
		}
		catch (Exception e) {
			throw new StiQueryServiceException("Errore durante"
					+ " l'esecuzione della richiesta.", e);
		}
	}

	@Override
	public String importChangeSet(final ChangeSet changeSet) {

		String chUri = StiServiceUtil.trimStr(changeSet.getChangeSetURI());
		Date effectiveDate = changeSet.getOfficialEffectiveDate();

		int sepIdx = chUri.indexOf(":");
		if (sepIdx < 1) {
			throw new StiQueryServiceException("Impossibile leggere "
					+ "i valori per Code System e Version: " + chUri);
		}

		String retVal = null;

		OpaqueData instrOp = changeSet.getChangeSetElementGroup().getChangeInstructions();
		Object[] chInst = instrOp.getValue().getAnyObject();

		Map<String, String> instrucMap = buildInstructionMap(chInst);

		if (chUri.toUpperCase().startsWith("CROSS-MAPPING:")) {
			String associationIdStr = chUri.substring(sepIdx+1);
			String action = StiServiceUtil.trimStr(instrucMap.get("ACTION"));

			return validateCrossMapping(chUri, associationIdStr, action);
		}

		try {
			// Check if the element can be inserted
			if (!canImport()) {
				throw new ImportException("Importazione in corso. Riprovare in seguito.");
			}

			String csVersion = chUri.substring(sepIdx + 1);
			String codeSystemName = chUri.substring(0, sepIdx);

			String codeSystemOid = instrucMap.get("CODE_SYSTEM_OID");
			String csDescription = instrucMap.get("CODE_SYSTEM_DESCRIPTION");
			

			Runnable impRun = null;
			if (codeSystemName.equalsIgnoreCase("DO-REINDEX")) {
				impRun = new ReindexWorkflow(csVersion);
			}
			else {
				impRun = getImportWorkflowManager(csVersion, csDescription,
						effectiveDate, codeSystemOid, instrucMap);
			}

			if (impRun != null) {
				if (NewCsVersionInfo.RUNNING) {
					throw new RuntimeException("Importazione in corso. Riprovare in seguito.");
				}

				if (impRun instanceof NewCsVersionInfo) {

					if (impRun instanceof ImportLocalMapping) {
						csDescription = "Import Codifica Locale: " + instrucMap.get("LOCAL_CS_NAME");
					}
					if (impRun instanceof ImportMappingAicAtc) {
						csDescription = "Import Mapping ATC - AIC";
					}

					long tempImportId = startImport(
							codeSystemName, csVersion, csDescription,
							effectiveDate, codeSystemOid);

					((NewCsVersionInfo)impRun).setTempImportId(tempImportId);
				}

				Thread impThread = new Thread(impRun);
				impThread.start();

				retVal = chUri;
			}
		}
		catch (Exception e) {
			throw new StiQueryServiceException("Errore durante"
					+ " l'esecuzione della richiesta.", e);
		}

		return retVal;
	}

	private static Map<String, String> buildInstructionMap(final Object[] valAny) {

		if (valAny == null) {
			return null;
		}

		Map<String, String> retVal = new HashMap<String, String>();
		for (int i = 0; i < valAny.length; i++) {

			String valInst = String.valueOf(valAny[i]);
			int sepIdx = valInst.indexOf(":");

			if ((!StiServiceUtil.isNull(valInst)) && (sepIdx > 0)) {
				retVal.put(StiServiceUtil.trimStr(valInst.substring(0,
						sepIdx).toUpperCase()), StiServiceUtil
						.trimStr(valInst.substring(sepIdx + 1)));
			}
		}

		return retVal;
	}

	@Override
	public ChangeSet createChangeSet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateChangeSetMetadata(final String changeSetUri,
			final SourceReference creator, final OpaqueData changeInstructions,
			final Date officialEffectiveDate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rollbackChangeSet(final String changeSetUri) {
		String[] rollbackData = changeSetUri.split(":");

		// Formato <localname>:<version>
		if(rollbackData.length < 2){
			throw new UnsupportedOperationException("Informazioni insufficienti: <localname>:<version>");
		}

		try {
			String solrUrl = StiServiceUtil.buildCsIndexPath(
					LoincFields.LOINC_CODE_SYSTEM_NAME) + "/update";

			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
			hibUtil.executeByUser(new DeleteLocalMapping(
					rollbackData[1], rollbackData[0], solrUrl),
					SessionUtil.getLoggedUser());
		}
		catch(Exception re) {
			log.error("Errore durante l'eliminazione del mapping locale.", re);
			throw new RuntimeException("Errore durante l'eliminazione del mapping locale.", re);
		}
	}

	@Override
	public void commitChangeSet(final String changeSetUri) {
		// In questa implementazione le modifiche sono committate direttamente
	}

	/**
	 * Returns true if the system can import a file, false otherwise.
	 *
	 * @return Returns true if the system can import a file, false otherwise.
	 * @throws ImportException
	 */
	private boolean canImport() throws ImportException {

		TempImportazione last = DbUtil.readLast(SOURCE_TABLE_URL,
				SOURCE_TABLE_USER, SOURCE_TABLE_PASS);

		if (last == null) {
			return true;
		}

		return last.getStatus().equals(ImportValues.COMPLETE);
	}

	private String validateCrossMapping(final String chUri,
			final String associationIdStr, final String action) {

		long associationId = -1;
		try {
			associationId = Long.parseLong(StiServiceUtil.trimStr(associationIdStr));
		}
		catch(Exception ex) {
			throw new StiQueryServiceException("Impossibile leggere l'identificativo "
					+ "dell'associazione da approvare: " + chUri);
		}

		boolean setActive = false;
		if ("APPROVE".equalsIgnoreCase(action)) {
			setActive = true;
		}

		HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();
		ValidateOrDeleteAssociation valCmd = new ValidateOrDeleteAssociation(
				associationId, setActive, this);

		try {
			Association inserted = (Association)hibUtil.executeByUser(valCmd, SessionUtil.getLoggedUser());
			if (inserted == null) {
				return null;
			}
		}
		catch (Exception e) {
			throw new StiQueryServiceException("Errore durante"
					+ " l'esecuzione della richiesta.", e);
		}

		return chUri;
	}


	private Runnable getImportWorkflowManager(
			final String csVersion, final String csDescription,
			final Date effectiveDate, final String codeSystemOid,
			final Map<String, String> instrucMap) {

		Runnable impRun = null;
		if (!StiServiceUtil.isNull(instrucMap.get("LOCAL_MAPPING_FILE"))) {
			impRun = new ImportLocalMapping(instrucMap.get("LOCAL_MAPPING_FILE"),instrucMap.get("LOCAL_CS_NAME"), csVersion);
		}
		else if(!StiServiceUtil.isNull(instrucMap.get("STANDARD_LOCAL_CSV_FILE_IT")) || !StiServiceUtil.isNull(instrucMap.get("STANDARD_LOCAL_CSV_FILE_EN"))) {
			Gson gson = new Gson();
			
			String domain = instrucMap.get("DOMAIN");
			String organization = instrucMap.get("ORGANIZATION");
			String type = instrucMap.get("TYPE");
			String subType = instrucMap.get("SUBTYPE");
			String hasOntology = instrucMap.get("HAS_ONTOLOGY");
			String ontologyName = instrucMap.get("ONTOLOGY_NAME");
			
			
			LinkedHashMap<String, String> parameterNameTypeMap = new LinkedHashMap<String, String>();
			String parameterNameTypeMapJson = instrucMap.get("TYPE_MAPPING");
			Type typeMap = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
			parameterNameTypeMap = gson.fromJson(parameterNameTypeMapJson, typeMap);
			
			HashMap<String, String> codificationMap = new HashMap<String, String>();
			String codificationMapJson = instrucMap.get("CODIFICATION_MAPPING");
			codificationMap = gson.fromJson(codificationMapJson, typeMap);
			
			impRun = new NewVersionStandardLocalWorkflow(
					instrucMap.get("STANDARD_LOCAL_CSV_FILE_IT"), 
					instrucMap.get("STANDARD_LOCAL_CSV_FILE_EN"), 
					instrucMap.get("NAME"), 
					csVersion,
					csDescription,
					instrucMap.get("VERSION_DESCRIPTION"),
					codeSystemOid,
					effectiveDate,
					domain, 
					organization, 
					type, 
					subType,
					hasOntology,
					ontologyName,
					parameterNameTypeMap,
					codificationMap);
		}
		else if(!StiServiceUtil.isNull(instrucMap.get("VALUESET_CSV_FILE_IT")) || !StiServiceUtil.isNull(instrucMap.get("VALUESET_CSV_FILE_EN"))) {
			Gson gson = new Gson();
			
			String domain = instrucMap.get("DOMAIN");
			String organization = instrucMap.get("ORGANIZATION");
			String hasOntology = instrucMap.get("HAS_ONTOLOGY");
			String ontologyName = instrucMap.get("ONTOLOGY_NAME");
			//String type = instrucMap.get("TYPE");
			//String subType = instrucMap.get("SUBTYPE");
			
			LinkedHashMap<String, String> parameterNameTypeMap = new LinkedHashMap<String, String>();
			String parameterNameTypeMapJson = instrucMap.get("TYPE_MAPPING");
			Type typeMap = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
			parameterNameTypeMap = gson.fromJson(parameterNameTypeMapJson, typeMap);
			
			HashMap<String, String> codificationMap = new HashMap<String, String>();
			String codificationMapJson = instrucMap.get("CODIFICATION_MAPPING");
			codificationMap = gson.fromJson(codificationMapJson, typeMap);
			
			impRun = new NewVersionValueSetWorkflow(
					instrucMap.get("VALUESET_CSV_FILE_IT"), 
					instrucMap.get("VALUESET_CSV_FILE_EN"), 
					instrucMap.get("NAME"), 
					csVersion,
					csDescription,
					instrucMap.get("VERSION_DESCRIPTION"),
					codeSystemOid,
					effectiveDate,
					domain, 
					organization, 
					hasOntology,
					ontologyName,
					parameterNameTypeMap,
					codificationMap);
		}
		else if (!StiServiceUtil.isNull(instrucMap.get("ICD9_CM_OWL_EN")) && !StiServiceUtil.isNull(instrucMap.get("ICD9_CM_OWL_IT"))) {

			impRun = new NewVersionIcd9Workflow(
					instrucMap.get("ICD9_CM_OWL_IT"),
					instrucMap.get("ICD9_CM_OWL_EN"), csVersion,
					csDescription, effectiveDate, codeSystemOid);
		}
		else if (!StiServiceUtil.isNull(instrucMap.get("ATC_CSV_FILE"))) {
			impRun = new NewVersionAtcWorkflow(instrucMap.get("ATC_CSV_FILE"), csVersion,csDescription, effectiveDate, codeSystemOid);
		}
		else if (!StiServiceUtil.isNull(instrucMap.get("AIC_CLASSE_A_FILE")) && !StiServiceUtil.isNull(instrucMap.get("AIC_CLASSE_H_FILE")) &&
				 !StiServiceUtil.isNull(instrucMap.get("AIC_CLASSE_C_FILE")) && !StiServiceUtil.isNull(instrucMap.get("EQUIVALENTI_AIC_ATC_FILE"))
				 // && !StiServiceUtil.isNull(instrucMap.get("VERSIONE_ATC"))
				 ) {

			String aicClasseAFile = instrucMap.get("AIC_CLASSE_A_FILE");
			String aicClasseHFile = instrucMap.get("AIC_CLASSE_H_FILE");
			String aicClasseCFile = instrucMap.get("AIC_CLASSE_C_FILE");
			String equivalentiAicAtcFile = instrucMap.get("EQUIVALENTI_AIC_ATC_FILE");
			// String atcVersionName = StiServiceUtil.trimStr(instrucMap.get("VERSIONE_ATC"));
			impRun = new NewVersionAicWorkflow(
					csVersion, csDescription, effectiveDate, codeSystemOid,
					aicClasseAFile, aicClasseHFile, aicClasseCFile, equivalentiAicAtcFile);
					// atcVersionName);
		}
		else {

			// TODO: introdurre controlli per LOINC.
			String enFile = instrucMap.get("LOINC_EN_FILE");
			String itFile = instrucMap.get("LOINC_IT_FILE");
			String mapToFile = instrucMap.get("LOINC_MAPTO_FILE");
			impRun = new NewVersionLoincWorkflow(csVersion, csDescription, effectiveDate, codeSystemOid, enFile, itFile, mapToFile);
		}

		return impRun;
	}


	private long startImport(final String codeSystemName,
			final String csVersion, final String csDescription,
			final Date effectiveDate, final String codeSystemOid)
		throws ImportException {

		//Add the record in the db
		TempImportazione tempImportazione = new TempImportazione();
		tempImportazione.setDescription(csDescription);
		if (effectiveDate != null) {
			tempImportazione.setEffectiveDate(new java.sql.Date(effectiveDate.getTime()));
		}

		tempImportazione.setNameCodeSystem(codeSystemName);
		tempImportazione.setOid(codeSystemOid);
		tempImportazione.setStatus(ImportValues.START);
		tempImportazione.setVersionCodeSystem(csVersion);

		return DbUtil.create(SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS,
				tempImportazione).longValue();
	}
}