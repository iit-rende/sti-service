package it.linksmt.cts2.plugin.sti.importer.mapset;

import it.linksmt.cts2.plugin.sti.db.commands.insert.CreateMapSetVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetCodeSystemLastVersion;
import it.linksmt.cts2.plugin.sti.db.commands.search.GetMapSetVersion;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateUtil;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemConcept;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersion;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.StiServiceProvider;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class ImportAutomaticMapSet {
	
	private static Logger log = Logger.getLogger(ImportAutomaticMapSet.class);

	
	public static MapSetVersion addMapping(String srcCsName,String trgCsName,String srcValElem,String trgValElem){
		try{
			HibernateUtil hibernateUtil = StiServiceProvider.getHibernateUtil();
			Session session = null;
			session = hibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();

			CodeSystemVersion srcVers = new GetCodeSystemLastVersion(srcCsName).execute(session);
			CodeSystemVersion trgVers = new GetCodeSystemLastVersion(trgCsName).execute(session);
			
			/*salvataggio di MapSetVersion*/
			MapSetVersion mapSet = new GetMapSetVersion(srcVers, trgVers).execute(session);
			if(mapSet==null){
				mapSet = new CreateMapSetVersion(srcVers, trgVers, new Date()).execute(session);
			}
			
			log.debug("Importazione Risorsa Mapping Generico: " + mapSet.getFullname());
			
			
			CodeSystemConcept srcConcept = new GetCodeSystemConcept(srcValElem, srcVers.getVersionId().longValue()).execute(session);
			CodeSystemEntityVersion srcCodeSystemEntityVersion = srcConcept.getCodeSystemEntityVersion();
			
			CodeSystemConcept trgConcept = new GetCodeSystemConcept(trgValElem, trgVers.getVersionId().longValue()).execute(session);
			CodeSystemEntityVersion trgCodeSystemEntityVersion = trgConcept.getCodeSystemEntityVersion();
			
			/*generazione di CodeSystemEntityVersionAssociation*/
			CodeSystemEntityVersionAssociation newAssoc = new CodeSystemEntityVersionAssociation();

			newAssoc.setLeftId(srcCodeSystemEntityVersion.getVersionId().longValue());
			newAssoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId1(srcCodeSystemEntityVersion);
			newAssoc.setCodeSystemEntityVersionByCodeSystemEntityVersionId2(trgCodeSystemEntityVersion);

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
			
			session.clear();
			session.getTransaction().commit();
			session.close();
			
			return mapSet;
		}catch(Exception e){
			log.error("Errore durante la scrittura dei dati.", e);
			return null;
		}
		
	}
	
	
	
	
	
	
}
