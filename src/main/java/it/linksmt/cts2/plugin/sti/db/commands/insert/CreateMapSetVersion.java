package it.linksmt.cts2.plugin.sti.db.commands.insert;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemVersion;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiConstants;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class CreateMapSetVersion extends HibernateCommand {

	private CodeSystemVersion csVers1;
	private CodeSystemVersion csVers2;
	private Date releaseDate;
	private String description;
	private String organization;

	public CreateMapSetVersion(final CodeSystemVersion csVers1, final CodeSystemVersion csVers2, final Date releaseDate) {
		this.csVers1 = csVers1;
		this.csVers2 = csVers2;
		this.releaseDate = releaseDate;
	}
	
	public CreateMapSetVersion(final CodeSystemVersion csVers1, final CodeSystemVersion csVers2, final Date releaseDate, String description, String organization) {
		this.csVers1 = csVers1;
		this.csVers2 = csVers2;
		this.releaseDate = releaseDate;
		this.description = description;
		this.organization = organization;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
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

		if ( (chkList!=null) && (chkList.size() > 0) ) {
			throw new StiHibernateException("La risorsa di mapping risulta presente del sistema.");
		}

		MapSetVersion newMap = new MapSetVersion();
		newMap.setFullname(fullname);
		newMap.setReleaseDate(releaseDate);
		newMap.setStatus(StiConstants.STATUS_CODES.ACTIVE.getCode());
		newMap.setStatusDate(new Date());
		newMap.setDescription(description);
		newMap.setOrganization(organization);
		
		session.save(newMap);
		session.flush();
		session.refresh(newMap);

		return newMap;
	}

}
