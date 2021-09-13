package it.linksmt.cts2.plugin.sti.importer.mapset;

import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateMapSetVersion;
import it.linksmt.cts2.plugin.sti.db.commands.insert.ImportCsUtil;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemVersionByNameAndCodeSystemName;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.dtos.ChangelogDto;
import it.linksmt.cts2.plugin.sti.importer.ChangeLogUtil;
import it.linksmt.cts2.plugin.sti.importer.ImportException;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.opencsv.CSVReader;

public final class ImportMapSetCsv {

	private static Logger log = Logger.getLogger(ImportMapSetCsv.class);

	private ImportMapSetCsv() {
	}

	public static MapSetVersion importNewVersion(final String fromVersionName, final String toVersionName, 
			final Date releaseDate, final File csvData, String description, String organization) throws Exception {

		CSVReader readerMap = null;

		Session session = null;
		HibernateUtil hibUtil = StiServiceProvider.getHibernateUtil();

		try {
			session = hibUtil.getSessionFactory().openSession();
			session.beginTransaction();

//			readerMap = new CSVReader(new FileReader(csvData));
			
			// Salto l'Header
			readerMap = new CSVReader(new FileReader(csvData), ',', '\'', 1);
			
			String[] line1 = readerMap.readNext();
			if(line1==null || line1.length != 4){
				throw new StiHibernateException("Impossibile leggere i dati dal file");
			}
			
			String fromCodeSystemName = line1[0];
			String toCodeSystemName = line1[2];
			
			CodeSystemVersion sis1Vers = new GetCodeSystemVersionByNameAndCodeSystemName(fromVersionName,fromCodeSystemName).execute(session);
			CodeSystemVersion sis2Vers = new GetCodeSystemVersionByNameAndCodeSystemName(toVersionName,toCodeSystemName).execute(session);
			
			if (sis1Vers == null) {
				throw new StiHibernateException("Impossibile leggere i dati " + "della versione del Code System: " + fromVersionName);
			}
	
			if (sis2Vers == null) {
				throw new StiHibernateException("Impossibile leggere i dati " + "della versione del Code System: " + toVersionName);
			}


			MapSetVersion mapSet = new CreateMapSetVersion(sis1Vers, sis2Vers, releaseDate, description, organization).execute(session);
			log.info("Importazione Risorsa Mapping Generico: " + mapSet.getFullname());

			
			readerMap = new CSVReader(new FileReader(csvData), ',', '\'', 1);
			int numInsert = 0;
			String[] lineVal = null;
			
			while ((lineVal = readerMap.readNext()) != null) {
				if(lineVal.length == 4){
					String codSis1 = StiServiceUtil.trimStr(lineVal[1]);
					String codSis2 = StiServiceUtil.trimStr(lineVal[3]);

					CodeSystemConcept srcConcept = new GetCodeSystemConcept(codSis1, sis1Vers.getVersionId().longValue()).execute(session);

					CodeSystemConcept trgConcept = new GetCodeSystemConcept(codSis2, sis2Vers.getVersionId().longValue()).execute(session);

					if (srcConcept == null) {
						throw new StiHibernateException("Impossibile leggere i dati " + "della entità sorgente: " + codSis1);
					}

					if (trgConcept == null) {
						throw new StiHibernateException("Impossibile leggere i dati " + "della entità destinazione: " + codSis2);
					}

					// Association
					CodeSystemEntityVersionAssociation newAssoc = new CodeSystemEntityVersionAssociation();

					newAssoc.setLeftId(srcConcept.getCodeSystemEntityVersion().getVersionId().longValue());
					newAssoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(srcConcept.getCodeSystemEntityVersion());
					newAssoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(trgConcept.getCodeSystemEntityVersion());

					newAssoc.setForwardName(StiServiceUtil.trimStr(StiConstants.GENERIC_FORWARD_NAME));
					newAssoc.setReverseName(StiServiceUtil.trimStr(StiConstants.GENERIC_REVERSE_NAME));
					newAssoc.setAssociationKind(StiConstants.ASSOCIATION_KIND.CROSS_MAPPING.getCode());

					newAssoc.setMapSetVersion(mapSet);

					newAssoc.setInsertTimestamp(new Date());
					newAssoc.setStatus(StiConstants.STATUS_CODES.ACTIVE.getCode());
					newAssoc.setStatusDate(new Date());

					session.save(newAssoc);
					session.flush();
					session.refresh(newAssoc);

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
			
			
			ChangelogDto changelogDto = new ChangelogDto();
			changelogDto.setTitle(sis1Vers.getCodeSystem().getName()+" ("+sis1Vers.getName()+") - "+sis2Vers.getCodeSystem().getName()+" ("+sis2Vers.getName()+")");
			changelogDto.setType(ChangeLogUtil.TYPE_MAPPING);
			changelogDto.setImportedRow(numInsert);
			changelogDto.setCodeSystem(sis1Vers.getCodeSystem().getName());
			changelogDto.setVersion(sis1Vers.getName());
			changelogDto.setCodeSystemTo(sis2Vers.getCodeSystem().getName());
			changelogDto.setVersionTo(sis2Vers.getName());
			changelogDto.setDateCreate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
			
			ChangeLogUtil.addChangeLogMapping(hibUtil, changelogDto, true);

			// Chiudo l'ultima transazione
			session.clear();
			session.getTransaction().commit();
			session.close();

			log.info("Importazione Mapping terminata. Totale: " + String.valueOf(numInsert));

			return mapSet;
		} catch (Exception rex) {

			try {
				if (session != null) {
					session.clear();
				}
			} catch (Exception ex) {
			}

			if (rex instanceof ImportException) {
				throw (ImportException) rex;
			}
			if (rex instanceof StiHibernateException) {
				throw (StiHibernateException) rex;
			}
			if (rex instanceof StiAuthorizationException) {
				throw (StiAuthorizationException) rex;
			}

			throw new ImportException("Errore durante l'importazione del file per il Mapping generico.", rex);
		} finally {

			try {
				readerMap.close();
			} catch (Exception ee) {
				log.error("Errore durante la chiusura del file.", ee);
			}

			if ((session != null) && (session.isOpen())) {
				session.close();
			}
		}
	}
}
