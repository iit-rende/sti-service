package it.linksmt.cts2.plugin.sti.db.commands.delete;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemEntityVersionByEntityId;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByIdAlt;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersions;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetMapSetVersionByCodeSystemEntityVersionId;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.enums.CodeSystemType;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

@SuppressWarnings("unchecked")
public class DeleteCodeSystemVersionById extends HibernateCommand {

	private static Logger log = Logger.getLogger(DeleteCodeSystemVersionById.class);

	private Long versionId = null;

	private static int numInsert = 0;
	private static Session session = null;

	private String SQL_QUERY_META = "select entVers.* from code_system_version_entity_membership as memb"
			+ " inner join code_system_entity as ent on memb.codesystementityid = ent.id"
			+ " inner join code_system_entity_version as entVers on ent.currentversionid = entVers.versionId"
			+ " inner join code_system_metadata_value as metaval on entVers.versionId = metaval.codesystementityversionid"
			+ " inner join metadata_parameter as param on metaval.metadataparameterid = param.id "
			+ " where memb.codesystemversionid = " ;
	
	
	private static final String CALL_FUNCTION_REMOVE_CODE_SYSTEM = " SELECT * FROM remove_code_system(:id) ";
	
	
	public DeleteCodeSystemVersionById(final Long versionId) {
		this.versionId = versionId;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	
	@Override
	public CodeSystemVersion execute(final Session originalSession) throws StiAuthorizationException, StiHibernateException {

		CodeSystemVersion csVersion = new GetCodeSystemVersionByIdAlt(versionId).execute(originalSession);
		
		if ( (csVersion == null) ) {
			throw new StiHibernateException("Impossibile leggere i dati della versione del Code System con id: " + versionId);
		}
		
		try{
			/*Rimuovo gli eventuali MapSetVersion mappati con la versione del codesystem attuale (versionId)*/
			List<CodeSystemEntityVersion> entList = originalSession.createSQLQuery(SQL_QUERY_META + String.valueOf(versionId)).addEntity(CodeSystemEntityVersion.class)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			
			if(entList!=null && entList.size()>0 && entList.get(0)!=null && entList.get(0).getVersionId()!=null){
				MapSetVersion mapSetVersion = (MapSetVersion) new GetMapSetVersionByCodeSystemEntityVersionId(entList.get(0).getVersionId()).execute(originalSession);
				if(mapSetVersion!=null && mapSetVersion.getFullname()!=null){
					new DeleteMapSetVersion(mapSetVersion.getFullname()).execute(originalSession);
				}
			}
		}
		catch(Exception e){
			log.error("ERROR::"+e.getLocalizedMessage(),e);
		}

		// Individuo la versione precedente
		CodeSystemVersion prevVers = null;
		Date releaseDate = csVersion.getReleaseDate();

		List<CodeSystemVersion> versList = new GetCodeSystemVersions(
				csVersion.getCodeSystem().getName(),
				StiConstants.STATUS_CODES.ACTIVE).execute(originalSession);

		for (int i = 0; i < versList.size(); i++) {
			CodeSystemVersion curVerPrev = versList.get(i);

			if ( curVerPrev.getReleaseDate().before(releaseDate) &&
				((prevVers == null) || (curVerPrev.getReleaseDate().after(
						prevVers.getReleaseDate())) ) ) {
				prevVers = curVerPrev;
			}
		}

		// Individuo la versione successiva
		CodeSystemVersion nextVers = null;
		for (int i = 0; i < versList.size(); i++) {
			CodeSystemVersion curVerNext = versList.get(i);

			if ( curVerNext.getReleaseDate().after(releaseDate) &&
				((nextVers == null) || (curVerNext.getReleaseDate().before(
						nextVers.getReleaseDate())) ) ) {
				nextVers = curVerNext;
			}
		}

		Long previousVersionId = null;
		if (prevVers != null) {
			previousVersionId = prevVers.getVersionId();
		}
		
		numInsert = 0;
		session = originalSession;

		CodeSystem curCs = csVersion.getCodeSystem();
		if (nextVers == null) {
			
			// Mettere current version al Code System
			curCs.setCurrentVersionId(previousVersionId);

			session.save(curCs);
			session.flush();
			session.refresh(curCs);
			
			if(previousVersionId==null && curCs.getCurrentVersionId()==null){
				// se sto rimuovendo anche l'ultima versione del cs quindi lo elimino definitivamete
				removeCodeSystemByFunction(curCs);
				
				csVersion = null;
				// Chiudo l'ultima transazione
				session.clear();
				session.getTransaction().commit();
				session.close();
				return csVersion;
			}
		
		}
		else {

			// Salvare
			nextVers.setPreviousVersionId(previousVersionId);

			session.save(nextVers);
			session.flush();
			session.refresh(nextVers);

			// Aggiornare tutte le Entity
			updateAllCsEntities(nextVers, previousVersionId);

			log.info("Eliminazione di " + curCs.getName() + ":" + versionId +" terminata. Totale: " + String.valueOf(numInsert));
		}

		csVersion.setStatus(StiConstants.STATUS_CODES.DELETED.getCode());
		csVersion.setStatusDate(new Date());

		session.save(csVersion);
		session.flush();
		session.refresh(csVersion);

		// Chiudo l'ultima transazione
		session.clear();
		session.getTransaction().commit();
		session.close();
		return csVersion;
	}

	private void removeCodeSystemByFunction(CodeSystem curCs) throws StiAuthorizationException, StiHibernateException {
//		List<CodeSystemVersion> versions = new GetCodeSystemVersionsByCSIdAndStatus(curCs.getId(), StiConstants.STATUS_CODES.ACTIVE.getCode()).execute(session);
//		if(versions!=null && versions.size()>0){
//			for (CodeSystemVersion codeSystemVersion : versions) {
//				log.info("Il codesystem ["+curCs.getName()+"] ha ancora disponibile la versione ["+codeSystemVersion.getName()+"]");
//			}
//		}
//		else{
			log.info("CALL FUNCTION remove_code_system(:id) [id="+curCs.getId()+"]");
			
			if(curCs.getCodeSystemType().equals(CodeSystemType.VALUE_SET.getKey())){
				new DeleteValueSet(curCs.getName()).execute(session);
			}
			
			SQLQuery querySelect = session.createSQLQuery(CALL_FUNCTION_REMOVE_CODE_SYSTEM);
			
			querySelect.setLong("id",curCs.getId());
			querySelect.uniqueResult();
//		}
	}


	private void updateAllCsEntities(
			final CodeSystemVersion csVersion,
			final Long previousCsVersionId)
			throws StiHibernateException, StiAuthorizationException {

		Set<CodeSystemVersionEntityMembership> entIt = csVersion.getCodeSystemVersionEntityMemberships();
		if (entIt == null) {
			return;
		}

		for (CodeSystemVersionEntityMembership memb : entIt) {
//			Set<CodeSystemEntityVersion> vSet = memb.getCodeSystemEntity().getCodeSystemEntityVersions();
//			if (vSet.size() != 1) {
//				throw new StiHibernateException("Errore durante la lettura delle relazioni tra il CS e le Enties.");
//			}
//			
//			CodeSystemEntityVersion csEnt = vSet.iterator().next();
			
			
			CodeSystemEntityVersion csEnt = (CodeSystemEntityVersion) new GetCodeSystemEntityVersionByEntityId(memb.getCodeSystemEntity().getId()).execute(session);
			Long previousVersionId = null;


			if (previousCsVersionId != null) {
				CodeSystemConcept prevConc = new GetCodeSystemConcept(
						csEnt.getCodeSystemConcepts().iterator().next().getCode(),
						previousCsVersionId.longValue()).execute(session);
				if(prevConc!=null && prevConc.getCodeSystemEntityVersion()!=null && prevConc.getCodeSystemEntityVersion().getVersionId()!=null){
					previousVersionId = prevConc.getCodeSystemEntityVersion().getVersionId();
				}
				
			}

			csEnt.setPreviousVersionId(previousVersionId);

			numInsert++;
			if ((numInsert % ImportCsUtil.CHUNK_SIZE_IMPORT) == 0) {
				// Chiudo la transazione per il chunk
				SessionFactory sessFactory = session.getSessionFactory();

				session.clear();
				session.getTransaction().commit();
				session.close();

				// Apro una nuova transazione per il nuovo chunk
				session = sessFactory.openSession();
				session.beginTransaction();
			}

			// Log avanzamento
			if ((numInsert % 100) == 0) {
				log.info("Numero elementi elaborati: " + numInsert);
			}
		}
	}
}
