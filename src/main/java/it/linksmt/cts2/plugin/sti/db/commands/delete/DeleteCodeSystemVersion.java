package it.linksmt.cts2.plugin.sti.db.commands.delete;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByName;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersions;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystem;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersionEntityMembership;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class DeleteCodeSystemVersion extends HibernateCommand {

	private static Logger log = Logger.getLogger(DeleteCodeSystemVersion.class);

	private String csVersionName = null;

	private static int numInsert = 0;
	private static Session session = null;

	public DeleteCodeSystemVersion(final String csVersionName) {
		this.csVersionName = csVersionName;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public CodeSystemVersion execute(final Session originalSession) throws StiAuthorizationException, StiHibernateException {

		List<CodeSystemVersion> csList = new GetCodeSystemVersionByName(csVersionName).execute(originalSession);
		if ( (csList == null) || (csList.size() != 1) ) {
			throw new StiHibernateException("Impossibile leggere i dati "
					+ "della versione del Code System: " + csVersionName);
		}

		CodeSystemVersion csVersion = csList.get(0);

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
		}
		else {

			// Salvare
			nextVers.setPreviousVersionId(previousVersionId);

			session.save(nextVers);
			session.flush();
			session.refresh(nextVers);

			// Aggiornare tutte le Entity
			updateAllCsEntities(nextVers, previousVersionId);

			log.info("Eliminazione di " + curCs.getName() + ":" + csVersionName + " terminata. Totale: " + String.valueOf(numInsert));
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


	private void updateAllCsEntities(
			final CodeSystemVersion csVersion,
			final Long previousCsVersionId)
			throws StiHibernateException, StiAuthorizationException {

		Set<CodeSystemVersionEntityMembership> entIt = csVersion.getCodeSystemVersionEntityMemberships();
		if (entIt == null) {
			return;
		}

		for (CodeSystemVersionEntityMembership memb : entIt) {
			Set<CodeSystemEntityVersion> vSet = memb.getCodeSystemEntity().getCodeSystemEntityVersions();
			if (vSet.size() != 1) {
				throw new StiHibernateException("Errore durante la lettura delle relazioni tra il CS e le Enties.");
			}

			Long previousVersionId = null;
			CodeSystemEntityVersion csEnt = vSet.iterator().next();

			if (previousCsVersionId != null) {
				CodeSystemConcept prevConc = new GetCodeSystemConcept(
						csEnt.getCodeSystemConcepts().iterator().next().getCode(),
						previousCsVersionId.longValue()).execute(session);

				previousVersionId = prevConc.getCodeSystemEntityVersion().getVersionId();
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
