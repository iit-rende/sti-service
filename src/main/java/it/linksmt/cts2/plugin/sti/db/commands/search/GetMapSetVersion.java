package it.linksmt.cts2.plugin.sti.db.commands.search;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class GetMapSetVersion extends HibernateCommand {

	private CodeSystemVersion csVers1;
	private CodeSystemVersion csVers2;

	public GetMapSetVersion(final CodeSystemVersion csVers1,final CodeSystemVersion csVers2) {
		this.csVers1 = csVers1;
		this.csVers2 = csVers2;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if (userInfo == null) {
			throw new StiAuthorizationException("Occorre effettuare il login per utilizzare il servizio.");
		}
	}

	@Override
	public MapSetVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {


		String fullname =
				StiServiceUtil.trimStr(csVers1.getCodeSystem().getName()) + " (" +
				StiServiceUtil.trimStr(csVers1.getName()) + ") - " +
				StiServiceUtil.trimStr(csVers2.getCodeSystem().getName()) + " (" +
				StiServiceUtil.trimStr(csVers2.getName()) + ")";

		// Check versione esistente
		List<MapSetVersion> chkList = session.createCriteria(MapSetVersion.class)
				.add(Restrictions.ilike("fullname", fullname)).list();
		
		MapSetVersion mapSetVersion = null;
		if ( (chkList!=null) && (chkList.size() == 1) ) {
			mapSetVersion = chkList.get(0);
		}

		return mapSetVersion;
	}
}
