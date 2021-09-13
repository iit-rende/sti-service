package it.linksmt.cts2.plugin.sti.db.commands.search;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import edu.mayo.cts2.framework.model.association.Association;
import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.search.util.DbTransformUtil;
import it.linksmt.cts2.plugin.sti.service.AbstractStiService;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;

public class GetAssociationsInMapSet extends HibernateCommand {

	private AbstractStiService service = null;
	private String mapSetVersion = null;

	public GetAssociationsInMapSet(final String mapSetVersion, final AbstractStiService service) {
		this.mapSetVersion = mapSetVersion;
		this.service = service;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public List<Association> execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		MapSetVersion mapVers = (MapSetVersion) session.createCriteria(MapSetVersion.class)
				.add(Restrictions.ilike("fullname", mapSetVersion))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode())).uniqueResult();

		if (mapVers == null) {
			throw new StiHibernateException("Errore durante la lettura della risorsa di mapping: " + mapSetVersion);
		}

		List<Association> retVal = new ArrayList<Association>();

		Criteria assCrit = session.createCriteria(CodeSystemEntityVersionAssociation.class)
				.add(Restrictions.eq("mapSetVersion.versionId", mapVers.getVersionId().longValue()))
				.add(Restrictions.or(
						Restrictions.eq("associationKind", StiConstants.ASSOCIATION_KIND.CROSS_MAPPING.getCode()),
						Restrictions.eq("associationKind", StiConstants.ASSOCIATION_KIND.LINK.getCode())))
				.add(Restrictions.eq("status", StiConstants.STATUS_CODES.ACTIVE.getCode()));


		List<CodeSystemEntityVersionAssociation> resList = assCrit.list();
		if (resList != null) {
			for (int i = 0; i < resList.size(); i++) {
				retVal.add(DbTransformUtil.entityVersionAssociationToAssociation(
						session, resList.get(i), service));
			}
		}

		return retVal;
	}

}
