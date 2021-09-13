package it.linksmt.cts2.plugin.sti.db.commands.delete;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.linksmt.cts2.plugin.sti.db.hibernate.HibernateCommand;
import it.linksmt.cts2.plugin.sti.db.model.CodeSystemEntityVersionAssociation;
import it.linksmt.cts2.plugin.sti.db.model.MapSetVersion;
import it.linksmt.cts2.plugin.sti.service.exception.StiAuthorizationException;
import it.linksmt.cts2.plugin.sti.service.exception.StiHibernateException;
import it.linksmt.cts2.plugin.sti.service.util.StiServiceUtil;

public class DeleteMapSetVersion extends HibernateCommand {

	private static Logger log = Logger.getLogger(DeleteMapSetVersion.class);

	private String fullname;

	public DeleteMapSetVersion(final String fullname) {
		this.fullname = fullname;
	}

	@Override
	public void checkPermission(final Session session) throws StiAuthorizationException, StiHibernateException {
		if ((userInfo == null) || (!userInfo.isAdministrator())) {
			throw new StiAuthorizationException("Operazione consentita solo a livello amministrativo.");
		}
	}

	@Override
	public MapSetVersion execute(final Session session) throws StiAuthorizationException, StiHibernateException {

		if (StiServiceUtil.isNull(fullname)) {
			throw new StiHibernateException("Occorre specificare il nome del mapping generico.");
		}

		// Check versione esistente
		List<MapSetVersion> chkList = session.createCriteria(MapSetVersion.class)
				.add(Restrictions.ilike("fullname", StiServiceUtil.trimStr(fullname))).list();

		if ( (chkList!=null) && (chkList.size() != 1) ) {
			throw new StiHibernateException("Errore di lettura della risorsa di Mapping.");
		}

		MapSetVersion retVal = chkList.get(0);

		if (retVal.getCodeSystemEntityVersionAssociations() != null) {
			CodeSystemEntityVersionAssociation[] assocList = retVal.getCodeSystemEntityVersionAssociations().toArray(
					new CodeSystemEntityVersionAssociation[retVal.getCodeSystemEntityVersionAssociations().size()]);

			for (int j = 0; j < assocList.length; j++) {
				CodeSystemEntityVersionAssociation valCur = assocList[j];
				retVal.getCodeSystemEntityVersionAssociations().remove(valCur);

				session.delete(valCur);
				session.flush();
			}
		}

		session.delete(retVal);
		session.flush();

		return retVal;
	}
}
