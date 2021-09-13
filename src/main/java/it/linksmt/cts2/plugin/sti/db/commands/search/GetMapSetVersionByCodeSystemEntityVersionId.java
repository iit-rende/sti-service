package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetMapSetVersionByCodeSystemEntityVersionId extends HibernateCommand {

	private Long codeSystemEntityVersionId;

	public GetMapSetVersionByCodeSystemEntityVersionId(final Long codeSystemEntityVersionId) {
		this.codeSystemEntityVersionId = codeSystemEntityVersionId;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public MapSetVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		//check in src target
		List<CodeSystemEntityVersionAssociation> chkList = session.createCriteria(CodeSystemEntityVersionAssociation.class).add(Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId1.versionId", codeSystemEntityVersionId)).list();
		if(chkList==null || chkList.size()==0){
			//check in destination target
			chkList = session.createCriteria(CodeSystemEntityVersionAssociation.class).add(Restrictions.eq("codeSystemEntityVersionByCodeSystemEntityVersionId2.versionId", codeSystemEntityVersionId)).list();
		}

		
		MapSetVersion mapSetVersion = null;
		if(chkList!=null && chkList.size()>0){
			CodeSystemEntityVersionAssociation codeSystemEntityVersionAssociation = chkList.get(0);
			mapSetVersion = codeSystemEntityVersionAssociation.getMapSetVersion();
		}
		
//		// Check versione esistente
//		List<MapSetVersion> chkList = session.createCriteria(MapSetVersion.class).add(Restrictions.ilike("versionId", versionId)).list();
//		
//		MapSetVersion mapSetVersion = null;
//		if ( (chkList!=null) && (chkList.size() == 1) ) {
//			mapSetVersion = chkList.get(0);
//		}

		return mapSetVersion;
	}
}
