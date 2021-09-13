package it.linksmt.cts2.plugin.sti.service.codesystem;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionMaintenanceService;
import it.linksmt.cts2.plugin.sti.db.commands.delete.DeleteCodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.commands.delete.DeleteCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.delete.DeleteMapSetVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByIdAlt;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionEntityMembershipByCSVersionId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetMapSetVersionByCodeSystemEntityVersionId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetValueSetVersionById;
import it.linksmt.cts2.plugin.sti.db.commands.updates.UpdateValueSet;
import it.linksmt.cts2.plugin.sti.db.commands.updates.UpdateValueSetVersion;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntity;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.db.model.TempImportazione;
import it.linksmt.cts2.plugin.sti.db.model.ValueSet;
import it.linksmt.cts2.plugin.sti.db.model.ValueSetVersion;
import it.linksmt.cts2.plugin.sti.db.util.DbUtil;
import it.linksmt.cts2.plugin.sti.db.util.ImportValues;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.importer.SolrIndexerUtil;
import it.linksmt.cts2.plugin.sti.importer.standardlocal.StandardLocalFields;
import it.linksmt.cts2.plugin.sti.importer.valueset.ValueSetFields;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.StiServiceConfiguration;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.changeset.NewCsVersionInfo;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.exception.StiQueryServiceException;
import it.linksmt.cts2.plugin.sti.service.util.SessionUtil;
import it.linksmt.cts2.plugin.sti.service.util.StiAppConfig;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

@Component
public class StiCodeSystemVersionMaintenanceService
	extends AbstractStiService
	implements CodeSystemVersionMaintenanceService {

	private static Logger log = Logger.getLogger(StiCodeSystemVersionMaintenanceService.class);

	private static String SOURCE_TABLE_URL = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_ADDRESS, "");
	private static String SOURCE_TABLE_USER = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_USER, "");
	private static String SOURCE_TABLE_PASS = StiAppConfig.getProperty(
			StiServiceConfiguration.CTS2_TEMP_IMPORT_PASS, "");
	
	private static final String VALUESET_IDENTIFIER_PREFIX = "vs__";

	@Override
	public void updateChangeableMetadata(final NameOrURI identifier, final UpdateChangeableMetadataRequest request) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateResource(final CodeSystemVersionCatalogEntry resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CodeSystemVersionCatalogEntry createResource(final CodeSystemVersionCatalogEntry resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteResource(final NameOrURI identifier, final String changeSetUri) {

		if (!isValidToken(changeSetUri)) {
			throw new StiQueryServiceException("Il token per l'eliminazione del Code System non risulta valido.");
		}

		if (NewCsVersionInfo.RUNNING) {
			return;
		}

		long impId = -1;
		try {
			HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

			/**
			 * Per utilizzare il metodo di eliminazione code system, anche sui valueSet
			 * in caso di valueset l'identifier della versione verrà passato come
			 * vs__idVersioneValue set.
			 * Il seguente blocco di codice serve a distinguere le casistiche
			 * codeSystem vs valueSet
			 */
			boolean isValueSet = false;
			
//			String csVersion = StiServiceUtil.trimStr(identifier.getName());
			String csVersion = null;
			String trimmedIdentifier = StiServiceUtil.trimStr(identifier.getName());
			
			if(StringUtils.contains(trimmedIdentifier, VALUESET_IDENTIFIER_PREFIX)){
				isValueSet = true;
				String vsVersion = StringUtils.substring(trimmedIdentifier, VALUESET_IDENTIFIER_PREFIX.length());
				ValueSetVersion vsv = (ValueSetVersion) hibUtil.executeByUser(new GetValueSetVersionById(Long.valueOf(vsVersion)), SessionUtil.getLoggedUser());
				
				if( null != vsv) {
					csVersion = String.valueOf(vsv.getVirtualCodeSystemVersionId());
					ValueSet vs = vsv.getValueSet();
					vsv.setStatus(StiConstants.STATUS_CODES.DELETED.getCode());
					hibUtil.executeByUser(new UpdateValueSetVersion(vsv), SessionUtil.getLoggedUser());
					if(vs.getCurrentVersionId().compareTo(vsv.getVersionId())==0) {
						vs.setCurrentVersionId(vsv.getPreviousVersionId());
						hibUtil.executeByUser(new UpdateValueSet(vs), SessionUtil.getLoggedUser());
					}
				}
				
			}else {
				csVersion = trimmedIdentifier;
			}
			
			String csName = null;
			
			/**
			 * Vecchia modalità di cancellazione versione, attraverso il nome.
			 * 
			 */

			/*List<CodeSystemVersion> setVers = (List<CodeSystemVersion>) hibUtil
//					.executeByUser(new GetCodeSystemVersionByName(csVersion),
							SessionUtil.getLoggedUser());
			
			if ((setVers == null) || (setVers.size() != 1)) {
				throw new StiHibernateException("Impossibile leggere i dati "
						+ "della Versione Code System: " + csVersion);
			}
			
			CodeSystemVersion csVers = setVers.get(0);*/
			
			CodeSystemVersion csVers = (CodeSystemVersion) hibUtil
					.executeByUser(new GetCodeSystemVersionByIdAlt(Long.valueOf(csVersion)),
							SessionUtil.getLoggedUser());
			
			csName = csVers.getCodeSystem().getName();

			//Add the record in the db
			TempImportazione tempImportazione = new TempImportazione();
			tempImportazione.setDescription("Eliminazione versione del CS.");

			tempImportazione.setNameCodeSystem(csName);
			tempImportazione.setStatus(ImportValues.START);
			tempImportazione.setVersionCodeSystem(csVersion);

			impId = DbUtil.create(SOURCE_TABLE_URL, SOURCE_TABLE_USER, SOURCE_TABLE_PASS, tempImportazione).longValue();

			DbUtil.updateStatusWorkflow(impId, ImportValues.CTS2_LOADING, "");
			
//			hibUtil.executeByUser(new DeleteCodeSystemVersion(csVersion), SessionUtil.getLoggedUser());
			
			CodeSystemVersion codeSystemVersion = (CodeSystemVersion) hibUtil.executeByUser(new DeleteCodeSystemVersionById(Long.valueOf(csVersion)), SessionUtil.getLoggedUser());
			
			DbUtil.updateStatusWorkflow(impId, ImportValues.REINDEX, "");

			String solrUrl = null;
			
			if(csVers.getCodeSystem().getCodeSystemType().equals(CodeSystemType.LOCAL.getKey()) 
					|| csVers.getCodeSystem().getCodeSystemType().equals(CodeSystemType.STANDARD_NATIONAL.getKey())) {
				solrUrl = StiServiceUtil.buildCsIndexPath(StandardLocalFields.STANDARD_LOACL_CODE_SYSTEM_INDEX_SUFFIX_NAME) + "/update";
				SolrIndexerUtil.deleteByVersionAndName(solrUrl + "?commit=true", csVers.getName(), csVers.getCodeSystem().getName());
			}else if (csVers.getCodeSystem().getCodeSystemType().equals(CodeSystemType.VALUE_SET.getKey())) {
				solrUrl = StiServiceUtil.buildCsIndexPath(ValueSetFields.VALUESET_INDEX_SUFFIX_NAME) + "/update";
				SolrIndexerUtil.deleteByVersionAndName(solrUrl + "?commit=true", csVers.getName(), csVers.getCodeSystem().getName());
			}else {				
				solrUrl = StiServiceUtil.buildCsIndexPath(csName) + "/update";
				SolrIndexerUtil.deleteByVersion(solrUrl + "?commit=true", csVers.getName());
			}
			
			//SolrIndexerUtil.deleteAll(solrUrl + "?commit=true");
			if(codeSystemVersion!=null){
//				SolrIndexerUtil.indexNewVersion(codeSystemVersion.getCodeSystem().getName(), solrUrl);
				SolrIndexerUtil.indexNewVersion(csName, solrUrl);
			}
			

			DbUtil.updateStatusWorkflow(impId, ImportValues.COMPLETE, "");
		}
		catch (Exception ex) {

			try {
				DbUtil.updateStatusWorkflow(impId, ImportValues.ERROR, ex.getMessage());
			}
			catch(Exception uex) {
				log.error("Impossibile aggiornare lo stato della operazione di import AIC.");
			}

			throw new StiQueryServiceException("Errore durante l'esecuzione della richiesta.", ex);
		}
	}

	private boolean isValidToken(final String token) {
		// TODO: sfruttare changeSetUri per la sicurezza
		return true;
	}
	
}
